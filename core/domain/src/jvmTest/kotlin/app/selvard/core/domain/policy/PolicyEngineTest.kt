package app.selvard.core.domain.policy

import app.selvard.core.domain.event.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PolicyEngineTest {

    @Test
    fun safeDefaultIsTheNotificationFloor() {
        val policy = PolicyEngine().policyFor()
        assertEquals(Severity.MEDIUM, policy.notificationSeverityFloor)
    }

    @Test
    fun tighteningIsAlwaysAllowed() {
        val tightened = PolicyEngine().policyFor(notificationSeverityFloor = Severity.INFO)
        assertEquals(Severity.INFO, tightened.notificationSeverityFloor)
    }

    @Test
    fun looseningRequiresExplicitConsent() {
        assertFailsWith<IllegalArgumentException> {
            PolicyEngine().policyFor(notificationSeverityFloor = Severity.HIGH)
        }
        val consented = PolicyEngine().policyFor(
            notificationSeverityFloor = Severity.HIGH,
            notificationLooseningConsented = true,
        )
        assertEquals(Severity.HIGH, consented.notificationSeverityFloor)
    }
}
