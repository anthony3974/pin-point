package week11.st729217.pinpoint

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import week11.st729217.pinpoint.ui.theme.PinPointTheme

class MainActivity : ComponentActivity() {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinPointTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun LocationScreen(modifier: Modifier = Modifier) {
        var hasLocationPermission by remember {
            mutableStateOf(areLocationPermissionsGranted())
        }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                hasLocationPermission = permissions.values.all { it }
            }
        )

        var currentLocation by remember { mutableStateOf<Location?>(null) }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
        }

        LaunchedEffect(hasLocationPermission) {
            if (hasLocationPermission) {
                getCurrentLocation {
                    currentLocation = it
                    it?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                    }
                }
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            if (hasLocationPermission) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    currentLocation?.let {
                        Marker(
                            state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                            title = "Current Location",
                            snippet = "You are here"
                        )
                    }
                }
                Button(
                    onClick = {
                        getCurrentLocation {
                            currentLocation = it
                            it?.let {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Get Current Location")
                }
            } else {
                Button(
                    onClick = { locationPermissionLauncher.launch(locationPermissions) },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text("Request Location Permission")
                }
            }
        }
    }

    private fun areLocationPermissionsGranted(): Boolean = locationPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation(onLocation: (Location?) -> Unit) {
        if (areLocationPermissionsGranted()) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location: Location? ->
                    onLocation(location)
                }
        }
    }

    companion object {
        private val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
