package app.selvard

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Explicit local inspection only. Never reads exit traces, logcat, or personal security events. */
object RuntimeDiagnostics {
    private const val MAX_EXITS = 5

    data class Exit(val timestampMillis: Long, val reason: String, val status: Int)
    data class Report(val version: String, val api: Int, val supported: Boolean, val exits: List<Exit>) {
        fun shareText(): String = buildString {
            appendLine("Selvard · local runtime diagnostics")
            appendLine("App version: $version")
            appendLine("Android API: $api")
            appendLine("OS-recorded recent process exits (not necessarily crashes):")
            if (!supported) appendLine("Unavailable before Android 11.")
            if (supported && exits.isEmpty()) appendLine("No retained exit records. This does not rule out a crash.")
            exits.forEach {
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date(it.timestampMillis))
                appendLine("$time · ${it.reason} · status ${it.status}")
            }
            appendLine("No messages, URLs, identities, device identifiers, or stack traces included.")
        }
    }

    @Suppress("DEPRECATION")
    fun read(context: Context): Report {
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        val exits = if (supported) {
            val manager = context.getSystemService(ActivityManager::class.java)
            manager.getHistoricalProcessExitReasons(context.packageName, 0, MAX_EXITS).map {
                Exit(it.timestamp, reasonLabel(it.reason), it.status)
            }
        } else {
            emptyList()
        }
        return Report(version, Build.VERSION.SDK_INT, supported, exits)
    }

    private fun reasonLabel(reason: Int): String = when (reason) {
        ApplicationExitInfo.REASON_CRASH -> "App crash"
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "Native crash"
        ApplicationExitInfo.REASON_ANR -> "App not responding (ANR)"
        ApplicationExitInfo.REASON_LOW_MEMORY -> "OS low-memory kill"
        ApplicationExitInfo.REASON_SIGNALED -> "Process terminated by signal"
        ApplicationExitInfo.REASON_USER_REQUESTED -> "User/system requested stop"
        ApplicationExitInfo.REASON_USER_STOPPED -> "User profile stopped"
        ApplicationExitInfo.REASON_EXIT_SELF -> "Process exited itself"
        ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "OS resource-limit termination"
        ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "Initialization failure"
        ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "Permission change"
        ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "Required dependency stopped"
        else -> "Unknown/other OS reason ($reason)"
    }
}
