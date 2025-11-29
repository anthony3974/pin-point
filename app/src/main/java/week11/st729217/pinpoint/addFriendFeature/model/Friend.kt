package week11.st729217.pinpoint.addFriendFeature.model

import com.google.firebase.Timestamp

data class Friend(
    val uid: String = "",           // The UID of the friend
    val name: String? = "",         // Optional: Store name for easier display later
    val email: String? = "",        // Optional: Store email
    val status: String? = FriendStatus.PENDING.name, // "pending", "sent", "accepted"
    val timestamp: Timestamp = Timestamp.now()
)