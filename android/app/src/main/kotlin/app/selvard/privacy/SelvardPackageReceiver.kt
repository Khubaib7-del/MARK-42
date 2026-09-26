package app.selvard.privacy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.selvard.SelvardApplication

/**
 * Phase 6 install/update/remove facts. The receiver only forwards system
 * broadcasts; recording is best-effort and events are timestamp facts.
 * Pre-install history is unknowable and never backfilled (stated in UI).
 */
class SelvardPackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? SelvardApplication ?: return
        val packageName = intent.data?.schemeSpecificPart ?: return
        val replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED ->
                if (!replacing) {
                    PrivacyEventRecorder.recordPackageEvent(app, PackageEvents.PACKAGE_ADDED, packageName, false)
                }
            Intent.ACTION_PACKAGE_REPLACED ->
                PrivacyEventRecorder.recordPackageEvent(app, PackageEvents.PACKAGE_REPLACED, packageName, true)
            Intent.ACTION_PACKAGE_REMOVED ->
                if (!replacing) {
                    PrivacyEventRecorder.recordPackageEvent(app, PackageEvents.PACKAGE_REMOVED, packageName, false)
                }
            Intent.ACTION_BOOT_COMPLETED ->
                PrivacyEventRecorder.recordBoot(app, System.currentTimeMillis())
            Intent.ACTION_LOCKED_BOOT_COMPLETED ->
                PrivacyEventRecorder.recordBoot(app, System.currentTimeMillis())
        }
    }
}
