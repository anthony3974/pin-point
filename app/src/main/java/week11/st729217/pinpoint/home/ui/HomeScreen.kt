package week11.st729217.pinpoint.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import week11.st729217.pinpoint.favorites.viewmodel.FavoritesViewModel

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit,
    favoritesViewModel: FavoritesViewModel
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Home", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenProfile) {
            Text("Open Profile")
        }
    }
}
