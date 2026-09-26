package app.selvard.appguard

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import app.selvard.core.domain.appguard.PackageFacts

/**
 * Package inventory capture (Phase 5). Reads requested permissions (manifest
 * facts), installer source, target SDK, and the system-app flag. Grant state is
 * deliberately NOT read and NOT claimed (documented OS limitation).
 */
class PackageInventoryScanner(private val packageManager: PackageManager) {

    fun scan(): List<PackageFacts> =
        packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            .map { info ->
                val appInfo: ApplicationInfo? = info.applicationInfo
                PackageFacts(
                    packageName = info.packageName,
                    label = (appInfo?.loadLabel(packageManager)?.toString() ?: info.packageName)
                        .take(PackageFacts.MAX_LABEL_LENGTH),
                    isSystemApp = appInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0,
                    installerPackageName = installerOf(info.packageName),
                    targetSdkVersion = appInfo?.targetSdkVersion ?: 0,
                    // Truncate, never throw: one permission-heavy package must not
                    // kill the whole scan on a real device.
                    requestedPermissions = (info.requestedPermissions?.toList() ?: emptyList())
                        .take(PackageFacts.MAX_PERMISSIONS),
                )
            }
            .sortedBy { it.packageName }

    private fun installerOf(packageName: String): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            }.getOrNull()
        } else {
            @Suppress("DEPRECATION")
            runCatching { packageManager.getInstallerPackageName(packageName) }.getOrNull()
        }

    companion object {
        fun from(context: Context): PackageInventoryScanner =
            PackageInventoryScanner(context.packageManager)
    }
}
