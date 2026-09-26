package app.selvard.core.domain.appguard

/**
 * Static classification of well-known Android permissions (developer.android.com
 * permission groups + special app access). A permission absent from this catalog
 * is reported as UNCLASSIFIED, never silently ignored - honest by construction.
 * This catalog classifies *what a permission can access*, nothing else.
 */
enum class PermissionSensitivity { NORMAL, SENSITIVE, HIGH_SENSITIVITY, UNCLASSIFIED }

object PermissionCatalog {

    data class Entry(val sensitivity: PermissionSensitivity, val group: String)

    val entries: Map<String, Entry> = buildMap {
        // Ubiquitous normal permissions (INTERNET & co.) - listed so they
        // classify as NORMAL instead of generating UNCLASSIFIED noise.
        put("android.permission.INTERNET", Entry(PermissionSensitivity.NORMAL, "network"))
        put("android.permission.ACCESS_NETWORK_STATE", Entry(PermissionSensitivity.NORMAL, "network"))
        put("android.permission.ACCESS_WIFI_STATE", Entry(PermissionSensitivity.NORMAL, "network"))
        put("android.permission.CHANGE_NETWORK_STATE", Entry(PermissionSensitivity.NORMAL, "network"))
        put("android.permission.WAKE_LOCK", Entry(PermissionSensitivity.NORMAL, "power"))
        put("android.permission.VIBRATE", Entry(PermissionSensitivity.NORMAL, "hardware"))
        put("android.permission.FOREGROUND_SERVICE", Entry(PermissionSensitivity.NORMAL, "lifecycle"))
        put("android.permission.RECEIVE_BOOT_COMPLETED", Entry(PermissionSensitivity.NORMAL, "lifecycle"))
        put("android.permission.BLUETOOTH_CONNECT", Entry(PermissionSensitivity.NORMAL, "nearby_devices"))
        put("android.permission.BLUETOOTH_SCAN", Entry(PermissionSensitivity.NORMAL, "nearby_devices"))
        // Location - the highest-impact group when combined with background access.
        put("android.permission.ACCESS_FINE_LOCATION", Entry(PermissionSensitivity.SENSITIVE, "location"))
        put("android.permission.ACCESS_COARSE_LOCATION", Entry(PermissionSensitivity.SENSITIVE, "location"))
        put("android.permission.ACCESS_BACKGROUND_LOCATION", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "location"))
        // Contacts / calendar
        put("android.permission.READ_CONTACTS", Entry(PermissionSensitivity.SENSITIVE, "contacts"))
        put("android.permission.WRITE_CONTACTS", Entry(PermissionSensitivity.SENSITIVE, "contacts"))
        put("android.permission.GET_ACCOUNTS", Entry(PermissionSensitivity.SENSITIVE, "contacts"))
        put("android.permission.READ_CALENDAR", Entry(PermissionSensitivity.NORMAL, "calendar"))
        put("android.permission.WRITE_CALENDAR", Entry(PermissionSensitivity.NORMAL, "calendar"))
        // Camera / microphone
        put("android.permission.CAMERA", Entry(PermissionSensitivity.SENSITIVE, "camera"))
        put("android.permission.RECORD_AUDIO", Entry(PermissionSensitivity.SENSITIVE, "microphone"))
        // SMS / call log / phone
        put("android.permission.SEND_SMS", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "sms"))
        put("android.permission.RECEIVE_SMS", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "sms"))
        put("android.permission.READ_SMS", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "sms"))
        put("android.permission.RECEIVE_WAP_PUSH", Entry(PermissionSensitivity.SENSITIVE, "sms"))
        put("android.permission.RECEIVE_MMS", Entry(PermissionSensitivity.SENSITIVE, "sms"))
        put("android.permission.READ_CALL_LOG", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "call_log"))
        put("android.permission.WRITE_CALL_LOG", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "call_log"))
        put("android.permission.CALL_PHONE", Entry(PermissionSensitivity.SENSITIVE, "phone"))
        put("android.permission.ANSWER_PHONE_CALLS", Entry(PermissionSensitivity.SENSITIVE, "phone"))
        put("android.permission.ADD_VOICEMAIL", Entry(PermissionSensitivity.SENSITIVE, "phone"))
        put("android.permission.USE_SIP", Entry(PermissionSensitivity.SENSITIVE, "phone"))
        put("android.permission.PROCESS_OUTGOING_CALLS", Entry(PermissionSensitivity.SENSITIVE, "phone"))
        put("android.permission.READ_PHONE_STATE", Entry(PermissionSensitivity.SENSITIVE, "phone"))
        put("android.permission.READ_PHONE_NUMBERS", Entry(PermissionSensitivity.SENSITIVE, "phone"))
        // Storage
        put("android.permission.READ_EXTERNAL_STORAGE", Entry(PermissionSensitivity.NORMAL, "storage"))
        put("android.permission.WRITE_EXTERNAL_STORAGE", Entry(PermissionSensitivity.NORMAL, "storage"))
        put("android.permission.MANAGE_EXTERNAL_STORAGE", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "storage"))
        put("android.permission.READ_MEDIA_IMAGES", Entry(PermissionSensitivity.NORMAL, "storage"))
        put("android.permission.READ_MEDIA_VIDEO", Entry(PermissionSensitivity.NORMAL, "storage"))
        put("android.permission.READ_MEDIA_AUDIO", Entry(PermissionSensitivity.NORMAL, "storage"))
        // Sensors / body
        put("android.permission.BODY_SENSORS", Entry(PermissionSensitivity.SENSITIVE, "sensors"))
        put("android.permission.BODY_SENSORS_BACKGROUND", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "sensors"))
        put("android.permission.ACTIVITY_RECOGNITION", Entry(PermissionSensitivity.SENSITIVE, "sensors"))
        // Special app access - the classic stalkerware/banking-trojan toolkit.
        put("android.permission.SYSTEM_ALERT_WINDOW", Entry(PermissionSensitivity.SENSITIVE, "overlay"))
        put("android.permission.PACKAGE_USAGE_STATS", Entry(PermissionSensitivity.SENSITIVE, "app_usage"))
        put("android.permission.REQUEST_INSTALL_PACKAGES", Entry(PermissionSensitivity.SENSITIVE, "installs"))
        put("android.permission.BIND_ACCESSIBILITY_SERVICE", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "accessibility"))
        put("android.permission.BIND_DEVICE_ADMIN", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "device_admin"))
        put("android.permission.BIND_VPN_SERVICE", Entry(PermissionSensitivity.SENSITIVE, "vpn"))
        put("android.permission.WRITE_SETTINGS", Entry(PermissionSensitivity.SENSITIVE, "settings"))
        put("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", Entry(PermissionSensitivity.NORMAL, "power"))
        // Notification access - reads notification content (often OTPs).
        put("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE", Entry(PermissionSensitivity.HIGH_SENSITIVITY, "notifications"))
        put("android.permission.POST_NOTIFICATIONS", Entry(PermissionSensitivity.NORMAL, "notifications"))
    }

    /** Known launcher/installer package names for honest installer reporting. */
    val knownInstallers: Map<String, String> = mapOf(
        "com.android.vending" to "Google Play Store",
        "com.amazon.android.venezia" to "Amazon Appstore",
        "com.sec.android.app.samsungapps" to "Samsung Galaxy Store",
        "com.huawei.appmarket" to "HUAWEI AppGallery",
        "com.xiaomi.mishop" to "Xiaomi GetApps",
        "com.heytap.market" to "OPPO App Market",
        "com.bbk.appstore" to "vivo App Store",
        "org.fdroid.fdroid" to "F-Droid",
        "com.aurora.store" to "Aurora Store",
    )

    fun classify(permission: String): Entry =
        entries[permission] ?: Entry(PermissionSensitivity.UNCLASSIFIED, "unknown")
}
