package week11.st729217.pinpoint.addFriendFeature.screen


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestBadge(
    requestCount: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (requestCount > 0) {
                    Badge {
                        Text(text = requestCount.toString())
                    }
                }
            }
        ) {
            // You can use Icons.Filled.Person or Icons.Filled.Notifications
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Friend Requests"
            )
        }
    }
}


//How to use It

// Inject the SAME ViewModel we used before
//friendViewModel: FriendViewModel = viewModel()

// 1. Observe the state to get the list
//val uiState by friendViewModel.uiState.collectAsState()

// 2. Calculate the count
//val count = uiState.pendingRequests.size
//
//FriendRequestBadge(
//requestCount = count,
//onClick = {
//    // Logic to open the Pending Requests screen
//}