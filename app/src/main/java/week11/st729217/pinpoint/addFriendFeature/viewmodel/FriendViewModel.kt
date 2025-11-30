package week11.st729217.pinpoint.addFriendFeature.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import week11.st729217.pinpoint.addFriendFeature.model.Friend
import week11.st729217.pinpoint.addFriendFeature.repository.FriendRepository

data class FriendUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val pendingRequests: List<Friend> = emptyList(),
    val acceptedFriends: List<Friend> = emptyList()

)

class FriendViewModel(
    private val repository: FriendRepository = FriendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    // It is good practice to uncomment this so data loads immediately when the screen opens
    init {
        loadPendingRequests()

        loadAcceptedFriends()
    }

    // 2. ADD THIS FUNCTION: To fetch the accepted friends list
    fun loadAcceptedFriends() {
        val currentUser = auth.currentUser
        val myUid = currentUser?.uid ?: return

        viewModelScope.launch {
            repository.getAcceptedFriends(myUid)
                .collect { friends ->
                    // Update the state with the new list
                    _uiState.value = _uiState.value.copy(acceptedFriends = friends)
                }
        }
    }

    //sending friend request to the registered user by just using their email
    fun addFriend(targetEmail: String) {
        if (targetEmail.isBlank()) {
            // FIX: Use copy to keep the pending list visible while showing error
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter an email address.")
            return
        }

        val currentUser = auth.currentUser
        val myUid = currentUser?.uid
        val myName = currentUser?.displayName ?: "Unknown"
        val myEmail = currentUser?.email ?: ""

        if (myUid == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "You are not logged in.")
            return
        }

        viewModelScope.launch {
            // Use copy so the list doesn't disappear while loading
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.sendFriendRequest(myUid, myName, myEmail, targetEmail)

            if (result.isSuccess) {
                //  Use copy
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Friend request sent to $targetEmail!"
                )
            } else {
                //  Use copy
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun loadPendingRequests() {
        val currentUser = auth.currentUser
        val myUid = currentUser?.uid ?: return

        viewModelScope.launch {
            repository.getPendingRequests(myUid)
                .collect { requests ->
                    _uiState.value = _uiState.value.copy(pendingRequests = requests)
                }
        }
    }

    // In FriendViewModel.kt

    fun acceptRequest(friendUid: String) {
        val currentUser = auth.currentUser
        val myUid = currentUser?.uid ?: return

        viewModelScope.launch {
            // Show loading...
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.acceptFriendRequest(myUid, friendUid)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Friend request accepted!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to accept request"
                )
            }
        }
    }

    fun declineRequest(friendUid: String) {
        val currentUser = auth.currentUser
        val myUid = currentUser?.uid ?: return

        viewModelScope.launch {
            // Optional: Show loading state if you want,
            // but for deletes it's usually fast enough to just run it.
            val result = repository.declineFriendRequest(myUid, friendUid)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    successMessage = "Friend request declined."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to decline request."
                )
            }
        }
    }

    fun removeFriend(friendUid: String) {
        val currentUser = auth.currentUser
        val myUid = currentUser?.uid ?: return

        viewModelScope.launch {
            // Optional: You could add a loading state here if you want
            val result = repository.removeFriend(myUid, friendUid)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    successMessage = "Friend removed."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to remove friend."
                )
            }
        }
    }

    // Helper to reset state after showing a Toast/Snackbar
    fun clearState() {
        // Only reset the messages, DO NOT reset the list
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null,
            isLoading = false
        )
    }
}