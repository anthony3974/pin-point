package week11.st729217.pinpoint.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import week11.st729217.pinpoint.favorites.viewmodel.FavoritesViewModel

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun LocationScreen(modifier: Modifier = Modifier, favoritesViewModel: FavoritesViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember {
        mutableStateOf(areLocationPermissionsGranted(context))
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

    var showAddFavoriteDialog by remember { mutableStateOf<LatLng?>(null) }

    val favoriteLocations = favoritesViewModel.favoriteLocations

    fun getCurrentLocation(onLocation: (Location?) -> Unit) {
        if (areLocationPermissionsGranted(context)) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location: Location? ->
                    onLocation(location)
                }
        }
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

    showAddFavoriteDialog?.let { latLng ->
        AddFavoriteDialog(
            latLng = latLng,
            onDismiss = { showAddFavoriteDialog = null },
            onConfirm = { name ->
                favoritesViewModel.addFavorite(latLng, name)
                showAddFavoriteDialog = null
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    showAddFavoriteDialog = latLng
                }
            ) {
                currentLocation?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.latitude, it.longitude)),
                        title = "Current Location",
                        snippet = "You are here"
                    )
                }
                favoriteLocations.forEach { favorite ->
                    Marker(
                        state = MarkerState(position = favorite.location),
                        title = favorite.name,
                        snippet = "Lat: ${favorite.location.latitude}, Lng: ${favorite.location.longitude}"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFavoriteDialog(
    latLng: LatLng,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("New Favorite") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Favorite") },
        text = {
            Column {
                Text("Enter a name for this location:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lat: %.4f, Lng: %.4f".format(latLng.latitude, latLng.longitude),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun areLocationPermissionsGranted(context: Context): Boolean = locationPermissions.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}
