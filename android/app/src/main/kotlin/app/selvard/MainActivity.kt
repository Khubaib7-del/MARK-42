package app.selvard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.selvard.ui.SelvardApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Posture, verdicts, and masked identities render here; keep them out
        // of screenshots and the recents thumbnail (SECURITY_ARCHITECTURE §3.1).
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
        )
        setContent {
            SelvardApp()
        }
    }
}
