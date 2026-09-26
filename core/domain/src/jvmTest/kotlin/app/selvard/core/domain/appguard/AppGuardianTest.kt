package app.selvard.core.domain.appguard

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppGuardianTest {

    private val guardian = AppGuardian()

    private fun facts(
        pkg: String,
        label: String = pkg,
        system: Boolean = false,
        installer: String? = "com.android.vending",
        targetSdk: Int = 35,
        permissions: List<String> = emptyList(),
    ) = PackageFacts(pkg, label, system, installer, targetSdk, permissions)

    // ---- labeled corpus: typical benign apps stay LOW ----

    @Test
    fun corpusBenignAppsStayLowBand() {
        val benign = listOf(
            facts(
                "com.example.calculator",
                targetSdk = 34,
                permissions = listOf("android.permission.INTERNET"),
            ),
            facts(
                "com.example.notes",
                targetSdk = 33,
                permissions = listOf(
                    "android.permission.POST_NOTIFICATIONS",
                    "android.permission.READ_MEDIA_IMAGES",
                ),
            ),
            facts(
                "com.example.weather",
                targetSdk = 35,
                permissions = listOf(
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.POST_NOTIFICATIONS",
                ),
            ),
        )
        benign.forEach { f ->
            val analysis = guardian.analyze(f)
            assertEquals(AppRiskBand.LOW, analysis.band, "benign app flagged: ${f.packageName}")
            assertTrue(
                analysis.findings.none {
                    it.kind == "high_sensitivity_permission" || it.kind == "capability_combination"
                },
                "no high-sensitivity findings expected for: ${f.packageName}",
            )
        }
    }

    // ---- labeled corpus: stalkerware patterns score HIGH/CRITICAL ----

    @Test
    fun corpusStalkerwarePatternReachesCriticalBandWithNamedCombos() {
        val analysis = guardian.analyze(
            facts(
                "com.fisher.spytools",
                installer = null, // sideloaded
                targetSdk = 22, // legacy install-time grants
                permissions = listOf(
                    "android.permission.READ_SMS",
                    "android.permission.RECEIVE_SMS",
                    "android.permission.READ_CONTACTS",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_BACKGROUND_LOCATION",
                    "android.permission.SYSTEM_ALERT_WINDOW",
                    "android.permission.BIND_ACCESSIBILITY_SERVICE",
                    "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
                    "android.permission.READ_CALL_LOG",
                ),
            ),
        )
        assertEquals(AppRiskBand.CRITICAL, analysis.band)
        val combos = analysis.findings.filter { it.kind == "capability_combination" }
        assertTrue(combos.size >= 4, "expected named stalkerware combos, got ${combos.size}")
        assertTrue(combos.any { it.detail.contains("OTP interception") })
        assertTrue(combos.any { it.detail.contains("exfiltration") })
        assertTrue(combos.any { it.detail.contains("credential capture") })
        // Sideloaded + legacy SDK findings present.
        assertTrue(analysis.findings.any { it.kind == "unknown_installer" })
        assertTrue(analysis.findings.any { it.kind == "legacy_target_sdk" })
    }

    @Test
    fun corpusOtpInterceptionCapabilityIsFlagged() {
        val analysis = guardian.analyze(
            facts(
                "com.example.smsreader",
                permissions = listOf(
                    "android.permission.RECEIVE_SMS",
                    "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
                ),
            ),
        )
        val combo = analysis.findings.first { it.kind == "capability_combination" }
        assertTrue(combo.detail.contains("OTP interception"))
        assertEquals(AppRiskBand.CRITICAL, analysis.band)
    }

    // ---- honest treatment of system apps and unknown catalogs ----

    @Test
    fun systemAppsGetLowerBaselineButNeverZeroClaims() {
        val system = guardian.analyze(
            facts(
                "com.android.systemui",
                system = true,
                installer = null,
                targetSdk = 35,
                permissions = listOf("android.permission.ACCESS_FINE_LOCATION"),
            ),
        )
        // Sensitive permission still reported even for a system app.
        assertTrue(system.findings.any { it.kind == "sensitive_permissions" })
        // unknown_installer is not reported for preinstalled system packages.
        assertTrue(system.reasons.any { it.contains("Preinstalled") })
    }

    @Test
    fun unclassifiedPermissionsAreReportedNotIgnored() {
        val analysis = guardian.analyze(
            facts(
                "com.example.obscure",
                permissions = listOf("com.custom.vendor.permission.SECRET_THING"),
            ),
        )
        val unclassified = analysis.findings.first { it.kind == "unclassified_permissions" }
        assertEquals(2, unclassified.weight)
        assertEquals(AppRiskBand.LOW, analysis.band)
    }

    @Test
    fun recognizedInstallerStoresAreNotFlagged() {
        val analysis = guardian.analyze(
            facts("org.fdroid.someapp", installer = "org.fdroid.fdroid"),
        )
        assertTrue(analysis.findings.none { it.kind.endsWith("installer") })
    }

    @Test
    fun unrecognizedInstallerIsFlagged() {
        val analysis = guardian.analyze(
            facts("com.example.sideapp", installer = "com.sketchy.installer"),
        )
        assertTrue(analysis.findings.any { it.kind == "unrecognized_installer" })
    }

    // ---- contract: no malware verdict, no grant-state claims ----

    @Test
    fun everyAnalysisCarriesNoMalwareClaimAndNoGrantStateClaim() {
        val corpus = listOf(
            facts("com.example.a"),
            facts("com.example.b", permissions = listOf("android.permission.CAMERA")),
            facts(
                "com.example.c",
                installer = null,
                targetSdk = 22,
                permissions = listOf("android.permission.READ_SMS", "android.permission.READ_CONTACTS"),
            ),
        )
        corpus.forEach { f ->
            val analysis = guardian.analyze(f)
            assertTrue(
                analysis.reasons.any { it.contains("not a malware verdict") },
                "analysis of ${f.packageName} must carry the no-malware-claim reason",
            )
            assertTrue(
                analysis.reasons.any { it.contains("grant state is not read") },
                "analysis of ${f.packageName} must state grant state is not claimed",
            )
            assertTrue(analysis.reasons.isNotEmpty())
        }
    }

    @Test
    fun analysisIsDeterministic() {
        val f = facts(
            "com.example.x",
            installer = null,
            targetSdk = 22,
            permissions = listOf("android.permission.READ_SMS", "android.permission.CAMERA"),
        )
        assertEquals(guardian.analyze(f), guardian.analyze(f))
    }

    @Test
    fun modelRejectsImplausibleInput() {
        assertFailsWith<IllegalArgumentException> {
            facts("com.example.huge", permissions = List(300) { "p$it" })
        }
        assertFailsWith<IllegalArgumentException> {
            facts("com.example.blank", label = "x".repeat(200))
        }
    }

    @Test
    fun heaviestPackagesAreCappedNeverCrash() {
        // A permission-heavy real device package must still analyze: findings
        // are capped at MAX_FINDINGS with the omission stated in reasons.
        val heavy = facts(
            "com.example.heavy",
            installer = null,
            targetSdk = 22,
            permissions = List(30) { "android.permission.READ_SMS" } +
                listOf(
                    "android.permission.READ_CONTACTS",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
                    "android.permission.BIND_ACCESSIBILITY_SERVICE",
                    "android.permission.SYSTEM_ALERT_WINDOW",
                    "android.permission.READ_CALL_LOG",
                ),
        )
        val analysis = guardian.analyze(heavy)
        assertTrue(
            analysis.findings.size <= AppAnalysis.MAX_FINDINGS,
            "findings must be capped, got ${analysis.findings.size}",
        )
        assertTrue(
            analysis.reasons.any { it.contains("omitted from display") },
            "omission must be stated in reasons",
        )
        assertTrue(analysis.reasons.any { it.contains("not a malware verdict") })
    }
}
