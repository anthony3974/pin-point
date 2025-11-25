package week11.st729217.pinpoint.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import week11.st729217.pinpoint.data.AuthRepository

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, displayName: String?) {
        if (!isEmailValid(email)) {
            _uiState.value = AuthUiState(error = "Invalid email format")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)
            val r = repo.register(email, password, displayName)
            if (r.isSuccess) {
                _uiState.value = AuthUiState(success = true)
            } else {
                _uiState.value = AuthUiState(error = mapFirebaseError(r.exceptionOrNull()))
            }
        }
    }

    fun login(email: String, password: String) {
        if (!isEmailValid(email)) {
            _uiState.value = AuthUiState(error = "Invalid email format")
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState(error = "Password required")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)
            val r = repo.login(email, password)
            if (r.isSuccess) {
                _uiState.value = AuthUiState(success = true)
            } else {
                _uiState.value = AuthUiState(error = mapFirebaseError(r.exceptionOrNull()))
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mapFirebaseError(e: Throwable?): String {
        if (e == null) return "Unknown error"
        val msg = e.message ?: return "Unknown error"
        return when {
            msg.contains("invalid-email", true) -> "Invalid email address"
            msg.contains("email-already-in-use", true) -> "Email already in use"
            msg.contains("user-not-found", true) -> "No account found with this email"
            msg.contains("wrong-password", true) -> "Incorrect password"
            msg.contains("weak-password", true) -> "Password too weak"
            else -> e.localizedMessage ?: "Authentication error"
        }
    }
}
