package week11.st729217.pinpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.onesignal.OneSignal
import week11.st729217.pinpoint.ui.navigation.AppNavHost
import week11.st729217.pinpoint.ui.theme.PinpointTheme

class MainActivity : ComponentActivity() {
    private val favoritesViewModel by viewModels<FavoritesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Sync the user with OneSignal immediately on startup
            OneSignal.login(currentUser.uid)
            android.util.Log.d("OneSignal", "Syncing User: ${currentUser.uid}")
        }

        setContent {
            PinpointTheme {
                AppNavHost(favoritesViewModel = favoritesViewModel)
            }
        }
    }
}
