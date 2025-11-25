package week11.st729217.pinpoint.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st729217.pinpoint.FavoritesViewModel

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenProfile) {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            val favoritesViewModel: FavoritesViewModel = viewModel()
            LocationScreen(favoritesViewModel = favoritesViewModel)
        }
    }
}
