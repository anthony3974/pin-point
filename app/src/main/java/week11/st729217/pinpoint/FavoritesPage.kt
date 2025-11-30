package week11.st729217.pinpoint

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesPage(
    modifier: Modifier = Modifier,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val favoriteLocations = favoritesViewModel.favoriteLocations

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Locations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        if (favoriteLocations.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You have no favorite locations yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = favoriteLocations,
                    key = { it.id }
                ) { favorite ->
                    FavoriteListItem(
                        favorite = favorite,
                        onNameChange = { newName ->
                            favoritesViewModel.updateName(favorite.id, newName)
                        },
                        onDelete = {
                            favoritesViewModel.removeFavorite(favorite.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteListItem(
    favorite: FavoriteLocation,
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = favorite.name,
                    onValueChange = onNameChange,
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lat: %.4f, Lng: %.4f".format(
                        favorite.location.latitude,
                        favorite.location.longitude
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Favorite",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}