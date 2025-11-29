package week11.st729217.pinpoint.addFriendFeature.model

// This data class matches your Firestore document structure
data class Users(
    val uid: String? = "",        // Document ID (and field)
    val name: String? = "",       // Display Name
    val email: String? = "",      // Searchable Email
    val shareStatus: Boolean? = false // Default: Location sharing is OFF
)