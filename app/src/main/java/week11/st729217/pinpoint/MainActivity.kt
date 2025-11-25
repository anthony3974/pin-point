package week11.st729217.pinpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import week11.st729217.pinpoint.ui.navigation.AppNavHost
import week11.st729217.pinpoint.ui.theme.PinpointTheme

class MainActivity : ComponentActivity() {
    private val favoritesViewModel by viewModels<FavoritesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinpointTheme {
                AppNavHost(favoritesViewModel = favoritesViewModel)
            }
        }
    }
}
