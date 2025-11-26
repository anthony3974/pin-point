package week11.st729217.pinpoint.addFriendFeature.screen


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st729217.pinpoint.addFriendFeature.viewmodel.FriendViewModel

@Composable
fun PendingRequestsScreen(
    viewModel: FriendViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle Toasts for success/error
    LaunchedEffect(uiState) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearState()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearState()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Friend Requests",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // The List Logic
        if (uiState.pendingRequests.isEmpty()) {
            // Empty State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending requests.")
            }
        } else {
            // List of Requests
            LazyColumn {
                items(uiState.pendingRequests) { friend ->
                    FriendRequestItem(
                        friend = friend,
                        onAcceptClick = {
                            // Call the ViewModel function
                            viewModel.acceptRequest(friend.uid)
                        }
                    )
                }
            }
        }
    }
}