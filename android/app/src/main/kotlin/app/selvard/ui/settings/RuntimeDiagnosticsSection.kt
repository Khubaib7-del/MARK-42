package app.selvard.ui.settings

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import app.selvard.RuntimeDiagnostics
import app.selvard.ui.design.SelvardIcons
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Explicit local read of OS exit categories. No analytics, trace upload, or identifiers. */
@Composable
internal fun RuntimeDiagnosticsSection() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var reading by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }
    GlassCard(title = "App reliability", icon = SelvardIcons.Activity,
        actionLabel = if (reading) "Reading…" else "Read recent app exits",
        onAction = {
            if (!reading) {
                reading = true
                failed = false
                scope.launch {
                    try {
                        report = withContext(Dispatchers.IO) { RuntimeDiagnostics.read(context).shareText() }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        report = null
                        failed = true
                    } finally {
                        reading = false
                    }
                }
            }
        },
    ) {
        GlassHelperText("If Selvard closes unexpectedly, inspect Android's recorded exit reasons. " +
            "This stays on your device and contains no browsing activity or identities.")
        if (failed) GlassHelperText("Android's exit records could not be read. Try again.", error = true)
        report?.let { text -> SelectionContainer { Text(text, style = MaterialTheme.typography.bodySmall) } }
    }
}
