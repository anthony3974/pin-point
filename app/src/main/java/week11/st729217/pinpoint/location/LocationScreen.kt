package week11.st729217.pinpoint.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import week11.st729217.pinpoint.favorites.viewmodel.FavoritesViewModel
import week11.st729217.pinpoint.nazMapfeature.mapViewModel.MapViewModel
import week11.st729217.pinpoint.pushNotification.service.NotificationHelper

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun LocationScreen(modifier: Modifier = Modifier,
                   favoritesViewModel: FavoritesViewModel,
                   viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current

    // naz
    val uiState by viewModel.uiState.collectAsState()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

//    var hasLocationPermission by remember {
//        mutableStateOf(areLocationPermissionsGranted(context))
//    }

//    val locationPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            hasLocationPermission = permissions.values.all { it }
//        }
//    )

    var currentLocation by remember { mutableStateOf<Location?>(null) }

    // naz
    val defaultLocation = LatLng(43.6532, -79.3832)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }

    // naz
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // naz
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    var showAddFavoriteDialog by remember { mutableStateOf<LatLng?>(null) }

    val favoriteLocations = favoritesViewModel.favoriteLocations

    // naz
    LaunchedEffect(Unit) {
        viewModel.initializeLocationClient(context)

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.startTracking()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MapViewModel.Event.ShowNotification -> {
                    // Trigger the actual notification
                    NotificationHelper.showLocalNotification(context, "Nearby Alert", event.message)
                }
            }
        }
    }

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
                },
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
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
                // DRAW MARKERS FOR FRIENDS
                uiState.friendLocations.forEach { (uid, latLng) ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Friend", // You can lookup name if you want
                        snippet = "Live",
                        // Optional: Change color to Azure so it looks different from standard red pins
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
                }
            }
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp), // Padding for status bar
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(end = 12.dp)) {
                        Text(
                            text = if (uiState.isSharing) "You are LIVE" else "You are hidden",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (uiState.isSharing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.locationLog,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Switch(
                        checked = uiState.isSharing,
                        onCheckedChange = { viewModel.toggleLocationSharing(it) }
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
                onClick = { permissionLauncher.launch(locationPermissions) },
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
