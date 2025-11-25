package week11.st729217.pinpoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(onSignOut: () -> Unit, onBack: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Name: ${user?.displayName ?: "—"}")
        Text("Email: ${user?.email ?: "—"}")
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
