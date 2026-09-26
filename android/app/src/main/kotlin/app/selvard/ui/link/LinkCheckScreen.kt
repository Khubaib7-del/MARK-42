package app.selvard.ui.link

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.selvard.core.domain.link.LinkGuardian
import app.selvard.core.domain.link.LinkVerdict
import app.selvard.ui.theme.SelvardTheme

/**
 * Share-target entry chrome (activity fills the screen with the app theme).
 * The check fields below are the single verdict implementation, also
 * embedded by the Security tab.
 */
@Composable
fun LinkCheckScreen(
    initialUrl: String?,
    guardian: LinkGuardian,
    onAnalyzed: (url: String, verdict: LinkVerdict) -> Unit,
) {
    SelvardTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                LinkCheckContent(initialUrl = initialUrl, guardian = guardian, onAnalyzed = onAnalyzed)
            }
        }
    }
}
