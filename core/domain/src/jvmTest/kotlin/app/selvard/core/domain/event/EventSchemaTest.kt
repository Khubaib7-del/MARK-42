package app.selvard.core.domain.event

import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EventSchemaTest {

    @Test
    fun roundTripPreservesEveryField() {
        val event = testEvent(
            affectedAsset = AffectedAsset(AssetType.APP, "com.example.bank"),
            relatedEvents = listOf("evt-other"),
        )
        val decoded = EventJson.decode(EventJson.encode(event))
        assertEquals(event, decoded)
    }

    @Test
    fun schemaVersionIsPinned() {
        // Bumping this value requires an explicit migration (DRD §3); it is tested to be deliberate.
        assertEquals(1, EVENT_SCHEMA_VERSION)
    }

    @Test
    fun dataMinimizationLimitsAreEnforcedAtConstruction() {
        assertFailsWith<IllegalArgumentException> {
            testEvent(evidence = listOf(Evidence("kind", "x".repeat(Evidence.MAX_VALUE_LENGTH + 1), "prov")))
        }
        assertFailsWith<IllegalArgumentException> {
            testEvent(affectedAsset = AffectedAsset(AssetType.URL, "x".repeat(AffectedAsset.MAX_REF_LENGTH + 1)))
        }
        assertFailsWith<IllegalArgumentException> {
            testEvent(eventId = "")
        }
    }

    @Test
    fun unknownFieldsAreRejectedOnDecode() {
        val raw = EventJson.encode(testEvent())
        val extended = raw.dropLast(1) + ",\"unexpectedField\":true}"
        assertFailsWith<SerializationException> { EventJson.decode(extended) }
    }
}
