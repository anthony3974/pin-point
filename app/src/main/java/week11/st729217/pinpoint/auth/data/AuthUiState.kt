package week11.st729217.pinpoint.auth.data

import com.google.firebase.auth.FirebaseUser

data class AuthUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val user: FirebaseUser? = null
)
