package app.selvard.link

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import app.selvard.SelvardApplication
import app.selvard.ui.link.LinkCheckScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Share-target entry (explicit user action; no silent interception by design).
 * Also reachable from the in-app manual check field.
 */
class CheckLinkActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText: String? = if (intent?.action == Intent.ACTION_SEND) {
            @Suppress("DEPRECATION")
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
        val app = application as SelvardApplication
        setContent {
            LinkCheckScreen(
                initialUrl = sharedText?.extractFirstUrl(),
                guardian = app.linkGuardian,
                onAnalyzed = { url, verdict ->
                    val event = LinkEventRecorder.toEvent(url, verdict, System.currentTimeMillis())
                    lifecycleScope.launch(Dispatchers.IO) {
                        app.eventBus.publish(event)
                        app.eventStore.append(event)
                    }
                },
            )
        }
    }

    private fun String.extractFirstUrl(): String? {
        val token = trim().split(Regex("\\s+")).firstOrNull { it.isNotBlank() } ?: return null
        return if (token.contains("://")) token else null
    }
}
