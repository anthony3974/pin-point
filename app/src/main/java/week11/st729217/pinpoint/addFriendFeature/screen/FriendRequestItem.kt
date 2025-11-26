package week11.st729217.pinpoint.addFriendFeature.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import week11.st729217.pinpoint.addFriendFeature.model.Friend


//this is single friend which will be use in lazy column or list of friends

@Composable
fun FriendRequestItem(
    friend: Friend,
    onAcceptClick: () -> Unit
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
            // Left Side: Name and Email
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!friend.email.isNullOrEmpty()) {
                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right Side: Accept Button
            Button(onClick = onAcceptClick) {
                Text("Accept")
            }
        }
    }
}