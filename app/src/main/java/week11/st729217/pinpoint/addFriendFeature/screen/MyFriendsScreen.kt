package week11.st729217.pinpoint.addFriendFeature.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st729217.pinpoint.addFriendFeature.model.Friend
import week11.st729217.pinpoint.addFriendFeature.viewmodel.FriendViewModel


@Composable
fun MyFriendsScreen(
    viewModel: FriendViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Ensure we are listening for accepted friends
    LaunchedEffect(Unit) {
        viewModel.loadAcceptedFriends()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "My Friends",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.acceptedFriends.isEmpty()) {
            // Empty State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't added any friends yet.")
            }
        } else {
            // List of Friends
            LazyColumn {
                items(uiState.acceptedFriends) { friend ->
                    AcceptedFriendItem(
                        friend = friend,
                        onRemoveClick = {
                            viewModel.removeFriend(friend.uid)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AcceptedFriendItem(
    friend: Friend,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = friend.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Remove Button (Icon Style)
            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Friend",
                    tint = Color.Gray // Subtle color so it's not too aggressive
                )
            }
        }
    }
}