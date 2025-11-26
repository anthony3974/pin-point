package week11.st729217.pinpoint.nazMapfeature.mapViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // --- INIT BLOCK: Setup Listeners ---
    init {
        // 1. Setup Callback
        mqttRepository.setLocationCallback(object : MqttRepository.LocationCallback {
            override fun onLocationReceived(userUid: String, lat: Double, lng: Double) {
                // LOGGING: Verify ViewModel gets the data
                Log.d("MapViewModel", "Updating Map for $userUid: $lat, $lng")

                // FIX: Use .update {} for thread safety
                _uiState.update { currentState ->
                    val updatedMap = currentState.friendLocations.toMutableMap()
                    updatedMap[userUid] = LatLng(lat, lng)
                    currentState.copy(friendLocations = updatedMap)
                }
            }
        })

        // 2. Start tracking friends immediately
        trackFriends()
    }

    // --- LOCATION SETUP ---
    fun initializeLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
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
                // FIX: Do NOT disconnect MQTT here.
                // We only stop GPS updates. We stay connected to see friends.
                stopLocationUpdates()
                _uiState.update { it.copy(locationLog = "You are hidden (Viewing Friends)") }
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

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        mqttRepository.disconnect() // Disconnect only when screen is closed
    }
}