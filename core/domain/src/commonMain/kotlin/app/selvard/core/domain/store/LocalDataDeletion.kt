package app.selvard.core.domain.store

import kotlinx.coroutines.CancellationException

/** Independent deletion outcomes: a failure in one store must not hide success in another. */
data class LocalDataDeletion(val deletedEvents: Int?, val identitiesDeleted: Boolean) {
    val complete: Boolean get() = deletedEvents != null && identitiesDeleted

    fun receipt(): String = listOf(
        deletedEvents?.let { "Deleted $it recorded event(s)." }
            ?: "Event deletion was not confirmed. Retry to clear recorded events.",
        if (identitiesDeleted) "Deleted all declared identities."
            else "Identity deletion was not confirmed. Retry to clear the vault.",
    ).joinToString(" ")

    companion object {
        suspend fun run(purgeEvents: suspend () -> Int, purgeIdentities: suspend () -> Unit): LocalDataDeletion {
            val events = attempt(purgeEvents)
            val identities = attempt(purgeIdentities)
            return LocalDataDeletion(events.getOrNull(), identities.isSuccess)
        }

        // Operational failures are reportable; cancellation and fatal Errors propagate.
        @Suppress("TooGenericExceptionCaught")
        private suspend fun <T> attempt(operation: suspend () -> T): Result<T> = try {
            Result.success(operation())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }
}
