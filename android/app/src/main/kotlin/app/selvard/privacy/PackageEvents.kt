package app.selvard.privacy

/** Kinds recorded from system package broadcasts; removal is lower severity (loss of signal, not gain). */
enum class SelvardSeverity { INFO, LOW }

object PackageEvents {
    const val PACKAGE_ADDED = "package_installed"
    const val PACKAGE_REMOVED = "package_removed"
    const val PACKAGE_REPLACED = "package_updated"
}
