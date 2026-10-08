package app.selvard.core.domain.store

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalDataDeletionTest {
    @Test
    fun recordsBothSuccessfulDeletions() = runTest {
        val result = LocalDataDeletion.run({ 4 }, {})
        assertTrue(result.complete)
        assertEquals("Deleted 4 recorded event(s). Deleted all declared identities.", result.receipt())
    }

    @Test
    fun stillDeletesIdentitiesWhenEventStoreFails() = runTest {
        var identitiesAttempted = false
        val result = LocalDataDeletion.run({ error("store unavailable") }, { identitiesAttempted = true })
        assertTrue(identitiesAttempted)
        assertFalse(result.complete)
        assertTrue(result.receipt().contains("Event deletion was not confirmed"))
        assertTrue(result.identitiesDeleted)
    }

    @Test
    fun preservesEventDeletionWhenVaultFails() = runTest {
        val result = LocalDataDeletion.run({ 3 }, { error("vault unavailable") })
        assertEquals(3, result.deletedEvents)
        assertFalse(result.identitiesDeleted)
        assertTrue(result.receipt().contains("Deleted 3 recorded event(s)"))
        assertFalse(result.receipt().contains("vault unavailable"))
    }

    @Test
    fun neitherFailureClaimsSuccess() = runTest {
        val result = LocalDataDeletion.run({ error("events") }, { error("vault") })
        assertFalse(result.complete)
        assertEquals(null, result.deletedEvents)
        assertFalse(result.identitiesDeleted)
    }

    @Test
    fun cancellationStopsFurtherDeletion() = runTest {
        var identitiesAttempted = false
        assertFailsWith<CancellationException> {
            LocalDataDeletion.run({ throw CancellationException() }, { identitiesAttempted = true })
        }
        assertFalse(identitiesAttempted)
    }
}
