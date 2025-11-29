package week11.st729217.pinpoint.addFriendFeature.repository


import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st729217.pinpoint.addFriendFeature.model.Friend
import week11.st729217.pinpoint.addFriendFeature.model.FriendStatus

class FriendRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Logic: Find user by email -> Send Request (Batch Write)
    suspend fun sendFriendRequest(
        myUid: String,
        myName: String,
        myEmail: String,
        friendEmail: String
    ): Result<Unit> {
        return try {
            // 1. Search for the friend by email
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", friendEmail)
                .get()
                .await()

            //returning if no user found.
            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("User with this email not found."))
            }

            // Get the friend's details
            val friendDoc = querySnapshot.documents.first()
            val friendUid = friendDoc.id
            val friendName = friendDoc.getString("name") ?: "noName"

            // Validation: Don't let users add themselves
            if (friendUid == myUid) {
                return Result.failure(Exception("You cannot add yourself as a friend."))
            }

            // 2. Prepare the Batch Write (Updates both users at once)
            val batch = firestore.batch()

            //Their Profile in My Friend List
            // Reference A: My 'friends' list (I sent it)
            val myFriendRef = firestore.collection("users").document(myUid)
                .collection("friends").document(friendUid)

            val myFriendData = Friend(
                uid = friendUid,
                name = friendName,
                email = friendEmail,
                status = FriendStatus.SENT.name,
                timestamp = Timestamp.now()
            )


            //My Profile in their Friend List
            // Reference B: Their 'friends' list (They receive it)
            val theirFriendRef = firestore.collection("users").document(friendUid)
                .collection("friends").document(myUid)

            val theirFriendData = Friend(
                uid = myUid,
                name = myName, // Store my name so they know who sent it
                email = myEmail, // Optional
                status = FriendStatus.PENDING.name,
                timestamp = Timestamp.now()
            )

            // Queue the operations
            batch.set(myFriendRef, myFriendData)
            batch.set(theirFriendRef, theirFriendData)

            // Commit the batch
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Listen for real-time updates of pending requests
    fun getPendingRequests(myUid: String): Flow<List<Friend>> = callbackFlow {
        val collectionRef = firestore.collection("users").document(myUid)
            .collection("friends")
            .whereEqualTo("status", FriendStatus.PENDING.name)

        // This listener fires every time the data changes in Firestore
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Close the stream if there's an error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Convert the documents into a List of Friend objects
                val requests = snapshot.toObjects(Friend::class.java)
                trySend(requests) // Send the new list down the pipe
            }
        }

        // When the ViewModel stops listening (screen closed), remove the Firestore listener
        awaitClose { listenerRegistration.remove() }
    }


    // Accept a friend request
    suspend fun acceptFriendRequest(myUid: String, friendUid: String): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // Update MY document (status: pending -> accepted)
            val myFriendRef = firestore.collection("users").document(myUid)
                .collection("friends").document(friendUid)

            batch.update(myFriendRef, "status", FriendStatus.ACCEPTED.name)

            // Update THEIR document (status: sent -> accepted)
            val theirFriendRef = firestore.collection("users").document(friendUid)
                .collection("friends").document(myUid)

            batch.update(theirFriendRef, "status", FriendStatus.ACCEPTED.name)

            // Commit both changes at once
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Listen for real-time updates of ACCEPTED friends
    fun getAcceptedFriends(myUid: String): Flow<List<Friend>> = callbackFlow {
        val collectionRef = firestore.collection("users").document(myUid)
            .collection("friends")
            .whereEqualTo("status", FriendStatus.ACCEPTED.name) // <--- The specific filter

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val friends = snapshot.toObjects(Friend::class.java)
                trySend(friends)
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    // Decline a friend request (Deletes the documents)
    suspend fun declineFriendRequest(myUid: String, friendUid: String): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // 1. Reference to MY document
            val myFriendRef = firestore.collection("users").document(myUid)
                .collection("friends").document(friendUid)

            // 2. Reference to THEIR document
            val theirFriendRef = firestore.collection("users").document(friendUid)
                .collection("friends").document(myUid)

            // 3. Delete both
            batch.delete(myFriendRef)
            batch.delete(theirFriendRef)

            // 4. Commit
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove an existing friend (Unfriend)
    suspend fun removeFriend(myUid: String, friendUid: String): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // 1. Delete them from MY list
            val myFriendRef = firestore.collection("users").document(myUid)
                .collection("friends").document(friendUid)

            // 2. Delete me from THEIR list
            val theirFriendRef = firestore.collection("users").document(friendUid)
                .collection("friends").document(myUid)

            batch.delete(myFriendRef)
            batch.delete(theirFriendRef)

            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}//end of the Class