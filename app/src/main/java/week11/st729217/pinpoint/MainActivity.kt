package week11.st729217.pinpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import week11.st729217.pinpoint.ui.navigation.AppNavHost
import week11.st729217.pinpoint.ui.theme.PinpointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinpointTheme {
                AppNavHost()
            }
        }
    }
}
