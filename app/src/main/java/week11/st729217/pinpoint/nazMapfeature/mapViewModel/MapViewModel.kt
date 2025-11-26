package week11.st729217.pinpoint.nazMapfeature.mapViewModel


import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import week11.st729217.pinpoint.nazMapfeature.repository.MqttRepository

data class MapUiState(
    val isSharing: Boolean = false,
    val locationLog: String = "Location Sharing Off"
)

class MapViewModel(
    private val mqttRepository: MqttRepository = MqttRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Location Client
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 1. Initialize Location Logic (Call this from UI once)
    fun initializeLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Publish to MQTT every time GPS updates
                    if (_uiState.value.isSharing) {
                        publishToMqtt(location.latitude, location.longitude)
                    }
                }
            }
        }
    }

    // 2. TOGGLE SWITCH LOGIC
    fun toggleLocationSharing(shouldShare: Boolean) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // A. Update Firestore (so friends know IF they should look for you)
            firestore.collection("users").document(uid)
                .update("shareStatus", shouldShare)
                .addOnFailureListener { e -> Log.e("Firestore", "Error updating status", e) }

            // B. Update UI
            _uiState.value = _uiState.value.copy(isSharing = shouldShare)

            // C. Handle MQTT & GPS
            if (shouldShare) {
                val isConnected = mqttRepository.connect()
                if (isConnected) {
                    startLocationUpdates()
                    _uiState.value = _uiState.value.copy(locationLog = "Connected. Sending location...")
                } else {
                    _uiState.value = _uiState.value.copy(locationLog = "Connection Failed", isSharing = false)
                }
            } else {
                stopLocationUpdates()
                mqttRepository.disconnect()
                _uiState.value = _uiState.value.copy(locationLog = "Location Sharing Stopped")
            }
        }
    }

    // 3. Helper to Publish
    private fun publishToMqtt(lat: Double, lng: Double) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            mqttRepository.publishLocation(uid, lat, lng)
            // Optional: Update log for debug
            // _uiState.value = _uiState.value.copy(locationLog = "Sent: $lat, $lng")
        }
    }

    // 4. GPS Start
    @SuppressLint("MissingPermission") // We assume permissions are checked in UI
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 5 seconds
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // 5. GPS Stop
    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // Cleanup when ViewModel dies
    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        mqttRepository.disconnect()
    }
}