package week11.st729217.pinpoint.addFriendFeature.screen


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st729217.pinpoint.addFriendFeature.viewmodel.FriendViewModel

@Composable
fun AddFriendScreen(
    // We inject the ViewModel here (defaulting to a new instance)
    viewModel: FriendViewModel = viewModel()
) {
    // 1. Observe the UI State
    val uiState by viewModel.uiState.collectAsState()

    // Local state for the text field
    var emailInput by remember { mutableStateOf("") }

    // Helper to show Toasts
    val context = LocalContext.current

    // 2. Handle Side Effects (Success/Error Messages)
    LaunchedEffect(uiState) {
        uiState.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            emailInput = "" // Clear input on success
            viewModel.clearState() // Reset VM state
        }
        uiState.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearState() // Reset VM state
        }
    }

    // 3. The Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Find Friends",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Email Input
        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Enter friend's email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Button
        Button(
            onClick = { viewModel.addFriend(emailInput) },
            enabled = !uiState.isLoading, // Disable button while loading
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send Friend Request")
            }
        }
    }
}