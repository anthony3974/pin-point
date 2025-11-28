package week11.st729217.pinpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import week11.st729217.pinpoint.auth.viewmodel.AuthViewModel
import week11.st729217.pinpoint.favorites.viewmodel.FavoritesViewModel
import week11.st729217.pinpoint.navigation.AppNavHost
import week11.st729217.pinpoint.theme.PinpointTheme

class MainActivity : ComponentActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    private val favoritesViewModel by viewModels<FavoritesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinpointTheme {
                val authState by authViewModel.uiState.collectAsState()

                if (authState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (authState.isAuthenticated) {
                        MainAppScaffold(favoritesViewModel = favoritesViewModel)
                    } else {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
