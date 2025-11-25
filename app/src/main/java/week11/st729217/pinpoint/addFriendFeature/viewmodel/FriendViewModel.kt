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

// Simple state to track what's happening on the screen
data class FriendUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val pendingRequests: List<Friend> = emptyList() // <--- NEW FIELD
)

class FriendViewModel(
    private val repository: FriendRepository = FriendRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    fun addFriend(targetEmail: String) {
        // 1. Basic Input Validation
        if (targetEmail.isBlank()) {
            _uiState.value = FriendUiState(errorMessage = "Please enter an email address.")
            return
        }

        // 2. Get Current User Info
        val currentUser = auth.currentUser
        val myUid = currentUser?.uid
        val myName = currentUser?.displayName ?: "Unknown"
        val myEmail = currentUser?.email ?: ""

        if (myUid == null) {
            _uiState.value = FriendUiState(errorMessage = "You are not logged in.")
            return
        }

        // 3. Trigger the Repository logic
        viewModelScope.launch {
            _uiState.value = FriendUiState(isLoading = true) // Show loading spinner

            val result = repository.sendFriendRequest(myUid, myName, myEmail, targetEmail)

            if (result.isSuccess) {
                _uiState.value = FriendUiState(successMessage = "Friend request sent to $targetEmail!")
            } else {
                _uiState.value = FriendUiState(errorMessage = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    // 2. Add a function to start listening
    fun loadPendingRequests() {
        val currentUser = auth.currentUser
        val myUid = currentUser?.uid ?: return

        viewModelScope.launch {
            repository.getPendingRequests(myUid)
                .collect { requests ->
                    // Whenever database changes, update the UI state
                    _uiState.value = _uiState.value.copy(pendingRequests = requests)
                }
        }
    }

    // Call this when the ViewModel starts (init block)
//    init {
//        loadPendingRequests()
//    }

    // Helper to reset state after showing a Toast/Snackbar
    fun clearState() {
        _uiState.value = FriendUiState()
    }


}