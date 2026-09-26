package app.selvard.core.domain.release

sealed interface ReleaseDownloadState {
    data object Idle : ReleaseDownloadState

    data class Downloading(
        val downloadedBytes: Long,
        val expectedBytes: Long?,
        val startedAtMillis: Long,
        val lastProgressAtMillis: Long,
    ) : ReleaseDownloadState {
        val progressFraction: Double = when {
            expectedBytes == null || expectedBytes <= 0L -> 0.0
            else -> (downloadedBytes.toDouble() / expectedBytes.toDouble()).coerceIn(0.0, 1.0)
        }
    }

    data class Finalizing(
        val downloadedBytes: Long,
        val expectedBytes: Long?,
        val reason: String,
        val atMillis: Long,
    ) : ReleaseDownloadState

    data class Validating(
        val fileSizeBytes: Long,
        val expectedBytes: Long?,
        val atMillis: Long,
    ) : ReleaseDownloadState

    data class Completed(
        val fileSizeBytes: Long,
        val atMillis: Long,
    ) : ReleaseDownloadState

    data class Stalled(
        val downloadedBytes: Long,
        val expectedBytes: Long?,
        val stalledForMillis: Long,
        val atMillis: Long,
    ) : ReleaseDownloadState

    data class Failed(
        val reason: String,
        val atMillis: Long,
    ) : ReleaseDownloadState
}

class ReleaseDownloadTracker(
    private val stallTimeoutMillis: Long = 30_000L,
    private val finalizationTimeoutMillis: Long = 30_000L,
) {
    init {
        require(stallTimeoutMillis > 0) { "stallTimeoutMillis must be > 0" }
        require(finalizationTimeoutMillis > 0) { "finalizationTimeoutMillis must be > 0" }
    }

    private var state: ReleaseDownloadState = ReleaseDownloadState.Idle

    fun currentState(): ReleaseDownloadState = state

    fun start(expectedBytes: Long?, nowMillis: Long): ReleaseDownloadState {
        require(nowMillis >= 0L) { "nowMillis must be >= 0" }
        require(expectedBytes == null || expectedBytes >= 0L) { "expectedBytes must be null or >= 0" }
        state = ReleaseDownloadState.Downloading(
            downloadedBytes = 0L,
            expectedBytes = expectedBytes,
            startedAtMillis = nowMillis,
            lastProgressAtMillis = nowMillis,
        )
        return state
    }

    fun onBytesCopied(bytesCopied: Long, nowMillis: Long): ReleaseDownloadState {
        require(bytesCopied >= 0L) { "bytesCopied must be >= 0" }
        require(nowMillis >= 0L) { "nowMillis must be >= 0" }

        val downloading = state as? ReleaseDownloadState.Downloading
            ?: return state

        val updated = downloading.copy(
            downloadedBytes = downloading.downloadedBytes + bytesCopied,
            lastProgressAtMillis = if (bytesCopied > 0L) nowMillis else downloading.lastProgressAtMillis,
        )

        state = when {
            hasReachedExpectedSize(updated) -> {
                ReleaseDownloadState.Finalizing(
                    downloadedBytes = updated.downloadedBytes,
                    expectedBytes = updated.expectedBytes,
                    reason = "progress_reached_expected_size",
                    atMillis = nowMillis,
                )
            }

            else -> updated
        }
        return state
    }

    fun onTick(nowMillis: Long): ReleaseDownloadState {
        require(nowMillis >= 0L) { "nowMillis must be >= 0" }

        state = when (val current = state) {
            is ReleaseDownloadState.Downloading -> {
                val stalledFor = nowMillis - current.lastProgressAtMillis
                when {
                    hasReachedExpectedSize(current) -> {
                        ReleaseDownloadState.Finalizing(
                            downloadedBytes = current.downloadedBytes,
                            expectedBytes = current.expectedBytes,
                            reason = "stalled_after_100_percent",
                            atMillis = nowMillis,
                        )
                    }

                    stalledFor >= stallTimeoutMillis -> {
                        ReleaseDownloadState.Stalled(
                            downloadedBytes = current.downloadedBytes,
                            expectedBytes = current.expectedBytes,
                            stalledForMillis = stalledFor,
                            atMillis = nowMillis,
                        )
                    }

                    else -> current
                }
            }

            is ReleaseDownloadState.Finalizing -> {
                val stalledFor = nowMillis - current.atMillis
                if (stalledFor >= finalizationTimeoutMillis) {
                    ReleaseDownloadState.Failed(
                        reason = "stream_finalization_timeout",
                        atMillis = nowMillis,
                    )
                } else {
                    current
                }
            }

            else -> current
        }
        return state
    }

    fun onStreamFinalized(fileSizeBytes: Long, nowMillis: Long): ReleaseDownloadState {
        require(fileSizeBytes >= 0L) { "fileSizeBytes must be >= 0" }
        require(nowMillis >= 0L) { "nowMillis must be >= 0" }

        val expectedBytes = expectedBytesOrNull()

        state = when {
            fileSizeBytes <= 0L -> {
                ReleaseDownloadState.Failed(reason = "empty_file", atMillis = nowMillis)
            }

            expectedBytes != null && fileSizeBytes < expectedBytes -> {
                ReleaseDownloadState.Failed(
                    reason = "incomplete_file_size",
                    atMillis = nowMillis,
                )
            }

            else -> {
                ReleaseDownloadState.Validating(
                    fileSizeBytes = fileSizeBytes,
                    expectedBytes = expectedBytes,
                    atMillis = nowMillis,
                )
            }
        }
        return state
    }

    fun onValidationFinished(
        valid: Boolean,
        nowMillis: Long,
        failureReason: String = "file_validation_failed",
    ): ReleaseDownloadState {
        require(nowMillis >= 0L) { "nowMillis must be >= 0" }
        val validating = state as? ReleaseDownloadState.Validating ?: return state

        state = if (valid) {
            ReleaseDownloadState.Completed(fileSizeBytes = validating.fileSizeBytes, atMillis = nowMillis)
        } else {
            ReleaseDownloadState.Failed(reason = failureReason, atMillis = nowMillis)
        }
        return state
    }

    private fun hasReachedExpectedSize(state: ReleaseDownloadState.Downloading): Boolean {
        val expected = state.expectedBytes ?: return false
        if (expected <= 0L) return false
        return state.downloadedBytes >= expected
    }

    private fun expectedBytesOrNull(): Long? = when (val current = state) {
        is ReleaseDownloadState.Downloading -> current.expectedBytes
        is ReleaseDownloadState.Finalizing -> current.expectedBytes
        is ReleaseDownloadState.Validating -> current.expectedBytes
        is ReleaseDownloadState.Stalled -> current.expectedBytes
        else -> null
    }
}
