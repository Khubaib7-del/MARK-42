package app.selvard.identity

import app.selvard.SelvardApplication
import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.AffectedAsset
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.Evidence
import app.selvard.core.domain.event.PrivacyClass
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.newEventId
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.identity.BreachRecord
import app.selvard.core.domain.identity.ExposureState
import app.selvard.core.domain.identity.HibpResponses
import app.selvard.core.domain.identity.HibpQueryMode
import app.selvard.core.domain.identity.IdentityExposure
import java.net.URLEncoder
import kotlinx.coroutines.launch

/**
 * Phase 7 breach checks. Range mode never transmits the address; full-address
 * mode only runs with explicit per-identity consent. Events carry masked
 * identities and minimized breach facts only; no result claims safety.
 */
object IdentityCheckEngine {

    suspend fun checkRange(
        identity: app.selvard.core.domain.identity.DeclaredIdentity,
        client: HibpClient,
    ): CheckOutcome {
        val hash = IdentityExposure.addressHash(identity.normalized)
        return when (val r = client.rangeQuery(IdentityExposure.rangePrefix(hash))) {
            is HibpResult.Ok -> {
                val names = runCatching { HibpResponses.parseRange(r.body) }
                    .getOrElse { return CheckOutcome(ExposureState.CHECK_FAILED, emptyList(), "unparseable range response") }
                    .let { IdentityExposure.matchRange(hash, it) }
                if (names.isEmpty()) {
                    CheckOutcome(ExposureState.NO_BREACHES_FOUND, emptyList(), null)
                } else {
                    CheckOutcome(
                        ExposureState.BREACHED,
                        names.take(IdentityExposure.MAX_BREACH_NAMES_RECORDED).map {
                            BreachRecord(it, null, emptyList())
                        },
                        null,
                    )
                }
            }
            is HibpResult.NoMatch -> CheckOutcome(ExposureState.NO_BREACHES_FOUND, emptyList(), null)
            is HibpResult.RateLimited ->
                CheckOutcome(ExposureState.CHECK_FAILED, emptyList(), rateLine(r.retryAfterSecs))
            is HibpResult.Failed -> CheckOutcome(ExposureState.CHECK_FAILED, emptyList(), r.reason)
        }
    }

    suspend fun checkFull(
        identity: app.selvard.core.domain.identity.DeclaredIdentity,
        client: HibpClient,
    ): CheckOutcome {
        require(identity.mode == HibpQueryMode.FULL_ADDRESS) { "full check needs full-address consent" }
        val encoded = URLEncoder.encode(identity.normalized, "UTF-8")
        return when (val r = client.fullQuery(encoded)) {
            is HibpResult.Ok -> {
                val breaches = runCatching { HibpResponses.parseBreaches(r.body) }
                    .getOrElse { return CheckOutcome(ExposureState.CHECK_FAILED, emptyList(), "unparseable breach response") }
                if (breaches.isEmpty()) {
                    CheckOutcome(ExposureState.NO_BREACHES_FOUND, emptyList(), null)
                } else {
                    CheckOutcome(
                        ExposureState.BREACHED,
                        breaches.take(IdentityExposure.MAX_BREACH_NAMES_RECORDED),
                        null,
                    )
                }
            }
            is HibpResult.NoMatch -> CheckOutcome(ExposureState.NO_BREACHES_FOUND, emptyList(), null)
            is HibpResult.RateLimited ->
                CheckOutcome(ExposureState.CHECK_FAILED, emptyList(), rateLine(r.retryAfterSecs))
            is HibpResult.Failed -> CheckOutcome(ExposureState.CHECK_FAILED, emptyList(), r.reason)
        }
    }

    fun record(app: SelvardApplication, identity: app.selvard.core.domain.identity.DeclaredIdentity, outcome: CheckOutcome) {
        val severity = when (outcome.state) {
            ExposureState.BREACHED -> Severity.MEDIUM
            else -> Severity.INFO
        }
        // Masked ref + minimized breach names only; the address itself is never persisted.
        val evidences = listOf(
            Evidence("exposure_state", IdentityExposure.summaryLine(outcome.state, outcome.breaches.size), "identity_exposure"),
            Evidence("identity_ref", identity.masked.take(Evidence.MAX_VALUE_LENGTH), "identity_vault"),
            Evidence("query_mode", identity.mode.name.take(Evidence.MAX_VALUE_LENGTH), "identity_vault"),
        ) + outcome.breaches.take(4).map {
            Evidence("breach", breachLine(it).take(Evidence.MAX_VALUE_LENGTH), "haveibeenpwned.com")
        } + (outcome.failureReason?.let {
            listOf(Evidence("check_failure", it.take(Evidence.MAX_VALUE_LENGTH), "haveibeenpwned.com"))
        } ?: emptyList())
        val event = SecurityEvent(
            eventId = newEventId(),
            timestampMillis = System.currentTimeMillis(),
            source = "identity_exposure",
            category = EventCategory.IDENTITY,
            severity = severity,
            confidence = Confidence.HIGH,
            affectedAsset = AffectedAsset(AssetType.IDENTITY, identity.masked.take(AffectedAsset.MAX_REF_LENGTH)),
            evidence = evidences,
            actionTaken = ActionTaken.RECORDED,
            privacyClassification = PrivacyClass.HIGHLY_SENSITIVE,
        )
        app.scope.launch { runCatching { app.eventBus.publish(event); app.eventStore.append(event) } }
    }

    private fun rateLine(retryAfterSecs: Int?): String =
        "rate limited" + (retryAfterSecs?.let { " (retry after ${it}s)" } ?: "")

    private fun breachLine(b: BreachRecord): String =
        b.name + (b.breachDate?.let { " ($it)" } ?: "") +
            (if (b.dataClasses.isNotEmpty()) " [" + b.dataClasses.take(3).joinToString(", ") + "]" else "")
}

data class CheckOutcome(
    val state: ExposureState,
    val breaches: List<BreachRecord>,
    val failureReason: String?,
)
