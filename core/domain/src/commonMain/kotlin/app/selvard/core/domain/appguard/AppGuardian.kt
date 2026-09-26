package app.selvard.core.domain.appguard

/**
 * Facts about an installed package, captured deliberately: requested permissions
 * (not grant state - grant state is NOT read and NOT claimed, a documented OS
 * limitation), installer package name, target SDK, and system-app flag.
 */
data class PackageFacts(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val installerPackageName: String?,
    val targetSdkVersion: Int,
    val requestedPermissions: List<String>,
) {
    init {
        require(packageName.isNotBlank()) { "package name must not be blank" }
        require(label.length <= MAX_LABEL_LENGTH) { "label exceeds limit" }
        require(requestedPermissions.size <= MAX_PERMISSIONS) { "implausible permission count" }
    }

    companion object {
        const val MAX_LABEL_LENGTH = 128
        const val MAX_PERMISSIONS = 256
    }
}

enum class AppRiskBand { LOW, ELEVATED, HIGH, CRITICAL }

data class AppFinding(
    val kind: String,
    val detail: String,
    val weight: Int,
    val provenance: String,
)

data class AppAnalysis(
    val packageName: String,
    val findings: List<AppFinding>,
    val score: Int,
    val band: AppRiskBand,
    val reasons: List<String>,
) {
    init {
        require(reasons.isNotEmpty()) { "an analysis must always state its reasons" }
        require(findings.size <= MAX_FINDINGS) { "too many findings" }
    }

    companion object {
        const val MAX_FINDINGS = 24

        /**
         * An analysis describes what a package can access and how it got here.
         * It is NOT a malware verdict and must never be presented as one.
         */
        const val NO_MALWARE_CLAIM =
            "This analysis describes requested capabilities and install provenance. " +
                "It is not a malware verdict, and permission absence is not evidence of safety."
    }
}

/**
 * Deterministic App Guardian analysis (Phase 5): pure function of package facts.
 * Facts are facts (a requested permission is a manifest-declared fact), so
 * findings carry factual weights; scores never claim *behavior*, only capability.
 */
class AppGuardian {

    fun analyze(facts: PackageFacts): AppAnalysis {
        val findings = mutableListOf<AppFinding>()

        val classified = facts.requestedPermissions.map { it to PermissionCatalog.classify(it) }
        val highSensitivity = classified.filter {
            it.second.sensitivity == PermissionSensitivity.HIGH_SENSITIVITY
        }
        val sensitive = classified.filter { it.second.sensitivity == PermissionSensitivity.SENSITIVE }
        val unclassified = classified.filter {
            it.second.sensitivity == PermissionSensitivity.UNCLASSIFIED
        }

        highSensitivity.forEach { (permission, entry) ->
            findings += AppFinding(
                "high_sensitivity_permission",
                "$permission (${entry.group} access)",
                WEIGHT_HIGH,
                PROVENANCE_CATALOG,
            )
        }
        if (sensitive.isNotEmpty()) {
            findings += AppFinding(
                "sensitive_permissions",
                "${sensitive.size} sensitive permission(s): " +
                    sensitive.joinToString(limit = 4) {
                        "${it.first.substringAfterLast('.')} (${it.second.group})"
                    },
                WEIGHT_SENSITIVE,
                PROVENANCE_CATALOG,
            )
        }
        if (unclassified.isNotEmpty()) {
            findings += AppFinding(
                "unclassified_permissions",
                "${unclassified.size} permission(s) not in Selvard's catalog (reviewed, not ignored)",
                WEIGHT_UNCLASSIFIED,
                PROVENANCE_CATALOG,
            )
        }

        // Combinations that define classic stalkerware/banking-trojan capability sets.
        val groups = (highSensitivity + sensitive).map { it.second.group }.toSet()
        for ((required, name) in DANGEROUS_COMBOS) {
            if (required.all { it in groups }) {
                findings += AppFinding(
                    "capability_combination",
                    "combined access: $name (${required.joinToString()})",
                    WEIGHT_COMBO,
                    PROVENANCE_CATALOG,
                )
            }
        }

        // Install provenance.
        val installer = facts.installerPackageName
        when {
            installer == null -> findings += AppFinding(
                "unknown_installer",
                "no installer recorded (sideloaded, preinstalled, or wiped)",
                WEIGHT_UNKNOWN_INSTALLER,
                PROVENANCE_INSTALLER,
            )
            installer !in PermissionCatalog.knownInstallers -> findings += AppFinding(
                "unrecognized_installer",
                "installed by '$installer', not a recognized store",
                WEIGHT_UNRECOGNIZED_INSTALLER,
                PROVENANCE_INSTALLER,
            )
            else -> Unit
        }

        // Old target SDK = dangerous permissions auto-granted at install (pre-23 semantics).
        // System apps legitimately target old SDKs; weigh lower.
        if (facts.targetSdkVersion < TARGET_SDK_RUNTIME_PERMS && !facts.isSystemApp) {
            findings += AppFinding(
                "legacy_target_sdk",
                "targets SDK ${facts.targetSdkVersion}: permission grants at install are not user-approved",
                WEIGHT_LEGACY_SDK,
                PROVENANCE_SDK_POLICY,
            )
        }

        val score = findings.sumOf { it.weight } + baseWeight(facts)
        return AppAnalysis(
            packageName = facts.packageName,
            findings = findings,
            score = score,
            band = bandOf(score),
            reasons = buildReasons(facts, findings, score),
        )
    }

    private fun baseWeight(facts: PackageFacts): Int =
        if (facts.isSystemApp) WEIGHT_SYSTEM_BASE else 0

    private fun bandOf(score: Int): AppRiskBand = when {
        score >= CRITICAL_THRESHOLD -> AppRiskBand.CRITICAL
        score >= HIGH_THRESHOLD -> AppRiskBand.HIGH
        score >= ELEVATED_THRESHOLD -> AppRiskBand.ELEVATED
        else -> AppRiskBand.LOW
    }

    private fun buildReasons(facts: PackageFacts, findings: List<AppFinding>, score: Int): List<String> {
        val reasons = mutableListOf(
            "${facts.requestedPermissions.size} permission(s) requested; " +
                "${findings.size} finding(s); capability score $score",
            "Requested permissions are manifest facts; grant state is not read and not claimed",
        )
        if (facts.isSystemApp) {
            reasons += "Preinstalled/system package (platform-signed capabilities may be hidden from this view)"
        }
        reasons += AppAnalysis.NO_MALWARE_CLAIM
        return reasons
    }

    companion object {
        const val PROVENANCE_CATALOG = "permission catalog"
        const val PROVENANCE_INSTALLER = "installer source"
        const val PROVENANCE_SDK_POLICY = "sdk policy"

        const val WEIGHT_HIGH = 30
        const val WEIGHT_SENSITIVE = 10
        const val WEIGHT_UNCLASSIFIED = 2
        const val WEIGHT_COMBO = 40
        const val WEIGHT_UNKNOWN_INSTALLER = 20
        const val WEIGHT_UNRECOGNIZED_INSTALLER = 10
        const val WEIGHT_LEGACY_SDK = 25
        const val WEIGHT_SYSTEM_BASE = -10

        const val ELEVATED_THRESHOLD = 20
        const val HIGH_THRESHOLD = 60
        const val CRITICAL_THRESHOLD = 100

        const val TARGET_SDK_RUNTIME_PERMS = 23

        /** Capability sets that characterize stalkerware/banking-trojan toolkits. */
        val DANGEROUS_COMBOS: List<Pair<List<String>, String>> = listOf(
            listOf("sms", "notifications") to "can read SMS and notifications (OTP interception capability)",
            listOf("sms", "contacts") to "can read SMS and contacts (exfiltration capability)",
            listOf("call_log", "contacts") to "can read call logs and contacts",
            listOf("accessibility", "sms") to "accessibility service plus SMS access (on-screen content capture)",
            listOf("accessibility", "overlay") to "accessibility service plus overlay windows (credential capture capability)",
            listOf("location", "overlay") to "location tracking plus overlay windows",
            listOf("device_admin", "accessibility") to "device admin plus accessibility (lockout capability)",
        )
    }
}
