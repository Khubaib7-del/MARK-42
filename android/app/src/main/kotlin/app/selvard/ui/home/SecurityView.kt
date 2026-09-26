package app.selvard.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.link.LinkEventRecorder
import app.selvard.ui.link.LinkCheckContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SECURITY: manual link check + posture summary, one scroll, one screen.
 * Manual checks record into the same encrypted store as the share target.
 */
@Composable
fun SecurityView() {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            "Check a link",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "Paste a URL. Analysis runs on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        SecurityLinkCheck(
            app = app,
            onAnalyzed = { url, verdict ->
                val event = LinkEventRecorder.toEvent(url, verdict, System.currentTimeMillis())
                scope.launch(Dispatchers.IO) {
                    app.eventBus.publish(event)
                    app.eventStore.append(event)
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        PostureView()
    }
}

@Composable
private fun SecurityLinkCheck(
    app: SelvardApplication,
    onAnalyzed: (String, app.selvard.core.domain.link.LinkVerdict) -> Unit,
) {
    // Single verdict implementation shared with the share-target activity.
    LinkCheckContent(
        initialUrl = null,
        guardian = app.linkGuardian,
        onAnalyzed = onAnalyzed,
    )
}
