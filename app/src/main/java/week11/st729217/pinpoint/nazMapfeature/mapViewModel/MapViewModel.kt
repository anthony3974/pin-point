package week11.st729217.pinpoint.nazMapfeature.mapViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update // <--- Import this for thread-safe updates
import kotlinx.coroutines.launch
import week11.st729217.pinpoint.addFriendFeature.repository.FriendRepository
import week11.st729217.pinpoint.nazMapfeature.repository.MqttRepository

data class MapUiState(
    val isSharing: Boolean = false,
    val locationLog: String = "Location Sharing Off",
    val friendLocations: Map<String, LatLng> = emptyMap()
)

class MapViewModel(
    private val mqttRepository: MqttRepository = MqttRepository(),
    private val friendRepository: FriendRepository = FriendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 1. New Variables for Logic
    private var myLastLocation: Location? = null
    private val alertHistory = mutableMapOf<String, Long>() // Tracks cooldowns

    // Settings
    private val ALERT_DISTANCE = 1000f // 1 km
    private val COOLDOWN_TIME = 5 * 60 * 1000L // 5 Minutes

    // 2. Event Channel (To talk to the UI)
    sealed class Event {
        data class ShowNotification(val message: String) : Event()
    }
    private val _eventChannel = Channel<Event>()
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        // 1. Update the Callback Implementation
        mqttRepository.setLocationCallback(object : MqttRepository.LocationCallback {
            override fun onLocationReceived(friendUid: String, lat: Double, lng: Double) {
                _uiState.update { currentState ->
                    val updatedMap = currentState.friendLocations.toMutableMap()
                    updatedMap[friendUid] = LatLng(lat, lng)
                    currentState.copy(friendLocations = updatedMap)
                }

                calculateDistance(friendUid, lat, lng)
            }

            // HANDLE REMOVAL
            override fun onUserDisconnect(friendUid: String) {
                _uiState.update { currentState ->
                    val updatedMap = currentState.friendLocations.toMutableMap()

                    // Remove the friend from the map
                    updatedMap.remove(friendUid)

                    currentState.copy(friendLocations = updatedMap)
                }
                Log.d("MapViewModel", "Removed marker for $friendUid")
            }
        })

        trackFriends()
    }

    // --- LOCATION SETUP ---
    fun initializeLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->

                    //  Always save my location, even if not sharing!
                    // This allows Ghost Mode users to still get alerts.
                    myLastLocation = location

                    if (_uiState.value.isSharing) {
                        publishToMqtt(location.latitude, location.longitude)
                    }
                }
            }
        }
    }

    // --- TOGGLE LOGIC ---
    fun toggleLocationSharing(shouldShare: Boolean) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // A. Update Firestore
            firestore.collection("users").document(uid)
                .update("shareStatus", shouldShare)
                .addOnFailureListener { e -> Log.e("Firestore", "Error updating status", e) }

            // B. Update UI
            _uiState.update { it.copy(isSharing = shouldShare) }

            // C. Handle Logic
            if (shouldShare) {
                // Ensure connected before publishing
                val isConnected = mqttRepository.connect()
                if (isConnected) {
                    startLocationUpdates()
                    _uiState.update { it.copy(locationLog = "Connected. Sharing Live.") }
                } else {
                    _uiState.update { it.copy(locationLog = "Connection Failed", isSharing = false) }
                }
            } else {
                // 2. SEND GOODBYE PACKET BEFORE STOPPING
                mqttRepository.publishOffline(uid)

                stopLocationUpdates()

                // Do not disconnect entirely, so we can still see other friends
                _uiState.update { it.copy(locationLog = "You are hidden.") }
            }
        }
    }

    // --- PUBLISH ---
    private fun publishToMqtt(lat: Double, lng: Double) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            mqttRepository.publishLocation(uid, lat, lng)
        }
    }

    // --- SUBSCRIBE LOGIC ---
    private fun trackFriends() {
        val currentUser = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            friendRepository.getAcceptedFriends(currentUser).collect { friends ->

                // FIX: Simply call connect. The Repo handles the check if already connected.
                mqttRepository.connect()

                friends.forEach { friend ->
                    mqttRepository.subscribeToFriend(friend.uid)
                    Log.d("MapViewModel", "Subscribed to ${friend.uid}")
                }
            }
        }
    }

    // --- HELPERS ---
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    private fun calculateDistance(friendUid: String, friendLat: Double, friendLng: Double) {
        val myLoc = myLastLocation ?: return // Need my location first

        // 1. Math: Calculate meters between points
        val results = FloatArray(1)
        Location.distanceBetween(
            myLoc.latitude, myLoc.longitude,
            friendLat, friendLng,
            results
        )
        val distance = results[0] // Distance in meters

        // 2. Logic: Is he close?
        if (distance <= ALERT_DISTANCE) {

            // 3. Logic: Is the Cooldown over?
            val lastAlertTime = alertHistory[friendUid] ?: 0L
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastAlertTime > COOLDOWN_TIME) {

                // ACTION: Trigger Notification
                viewModelScope.launch {
                    _eventChannel.send(Event.ShowNotification("A friend is within 1km!"))
                }

                // RESET TIMER
                alertHistory[friendUid] = currentTime
            }
        }
    }

    // 6. Safe Start Function
    // Call this ONLY when permission is granted
    @SuppressLint("MissingPermission")
    fun startTracking() {
        // Only start if we have the client ready
        if (::fusedLocationClient.isInitialized) {
            val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
            fusedLocationClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        mqttRepository.disconnect() // Disconnect only when screen is closed
    }
}