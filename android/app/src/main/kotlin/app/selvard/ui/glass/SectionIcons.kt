package app.selvard.ui.glass

import androidx.compose.ui.graphics.vector.ImageVector
import app.selvard.ui.design.SelvardIcons

/** Consistent line icons; labels and semantics carry the destination's meaning. */
object SectionIcons {
    fun forKey(key: String): ImageVector = when (key) {
        "shield" -> SelvardIcons.Shield
        "wifi" -> SelvardIcons.Wifi
        "apps" -> SelvardIcons.Apps
        "person" -> SelvardIcons.Person
        "history", "timeline" -> SelvardIcons.Timeline
        "settings" -> SelvardIcons.Settings
        else -> SelvardIcons.Home
    }
}
