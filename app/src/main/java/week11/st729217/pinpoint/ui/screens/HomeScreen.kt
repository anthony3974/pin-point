package week11.st729217.pinpoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onOpenProfile: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Home (Map placeholder)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Add map integration (Google Maps) here.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenProfile) {
            Text("Open Profile")
        }

    }
}
