package week11.st729217.pinpoint.addFriendFeature.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import week11.st729217.pinpoint.addFriendFeature.model.Users

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Function to create (or update) the user document
    suspend fun saveUserToFirestore(user: Users): Result<Unit> {
        return try {

            // 1. Safe check: Ensure uid is not null or empty
            val userId = user.uid
            if (userId.isNullOrEmpty()) {
                return Result.failure(Exception("User ID cannot be null or empty"))
            }

            // Path: users/{uid}
            // We use .set() because we are creating the document from scratch
            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}