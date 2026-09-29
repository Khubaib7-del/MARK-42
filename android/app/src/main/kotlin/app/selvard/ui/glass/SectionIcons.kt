package app.selvard.ui.glass

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Resolves an [AppSection.iconKey] to a rounded Material icon. Rounded is
 * the documented curvy variant for rounded-brand surfaces (Material Symbols
 * Rounded). Icons are navigation affordances only; meaning stays in words.
 */
object SectionIcons {
    @Composable
    fun forKey(key: String): ImageVector = when (key) {
        "home" -> Icons.Rounded.Home
        "shield" -> Icons.Rounded.Shield
        "wifi" -> Icons.Rounded.Wifi
        "apps" -> Icons.Rounded.Apps
        "person" -> Icons.Rounded.Person
        "history" -> Icons.Rounded.History
        "settings" -> Icons.Rounded.Settings
        else -> Icons.Rounded.Home
    }
}
