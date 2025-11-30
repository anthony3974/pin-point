package week11.st729217.pinpoint

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
// Your actual OneSignal App ID
const val ONESIGNAL_APP_ID = ""

class PinpointApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable verbose logging for debugging (remove in production)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
        // Use this method to prompt for push notifications.
        // We recommend removing this method after testing and instead use In-App Messages to prompt for notification permission.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }


    }
}