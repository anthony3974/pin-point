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
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit // <--- New Parameter
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
            // Left Side: Name
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

            // Right Side: Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // DECLINE BUTTON
                OutlinedButton(
                    onClick = onDeclineClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error // Red color
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Decline")
                }

                // ACCEPT BUTTON
                Button(onClick = onAcceptClick) {
                    Text("Accept")
                }
            }
        }
    }
}