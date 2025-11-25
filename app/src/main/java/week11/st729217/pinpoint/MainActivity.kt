package week11.st729217.pinpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import week11.st729217.pinpoint.ui.theme.PinPointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinPointTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}
