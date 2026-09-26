package app.selvard.core.domain.release

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReleaseDownloadTrackerTest {

    @Test
    fun reachingExpectedSizeMovesFromDownloadingToFinalizingAt100Percent() {
        val tracker = ReleaseDownloadTracker()

        tracker.start(expectedBytes = 10L, nowMillis = 1L)
        val state = tracker.onBytesCopied(bytesCopied = 10L, nowMillis = 2L)

        assertTrue(state is ReleaseDownloadState.Finalizing)
        state as ReleaseDownloadState.Finalizing
        assertEquals(10L, state.downloadedBytes)
        assertEquals(10L, state.expectedBytes)
        assertEquals("progress_reached_expected_size", state.reason)
    }

    @Test
    fun finalizedStreamWithMatchingSizeCanCompleteAfterValidation() {
        val tracker = ReleaseDownloadTracker()

        tracker.start(expectedBytes = 10L, nowMillis = 1L)
        tracker.onBytesCopied(bytesCopied = 10L, nowMillis = 2L)
        val validating = tracker.onStreamFinalized(fileSizeBytes = 10L, nowMillis = 3L)

        assertTrue(validating is ReleaseDownloadState.Validating)

        val completed = tracker.onValidationFinished(valid = true, nowMillis = 4L)
        assertTrue(completed is ReleaseDownloadState.Completed)
        completed as ReleaseDownloadState.Completed
        assertEquals(10L, completed.fileSizeBytes)
    }

    @Test
    fun finalizedStreamWithIncompleteSizeFailsValidationFlowEarly() {
        val tracker = ReleaseDownloadTracker()

        tracker.start(expectedBytes = 10L, nowMillis = 1L)
        tracker.onBytesCopied(bytesCopied = 10L, nowMillis = 2L)
        val failed = tracker.onStreamFinalized(fileSizeBytes = 9L, nowMillis = 3L)

        assertTrue(failed is ReleaseDownloadState.Failed)
        failed as ReleaseDownloadState.Failed
        assertEquals("incomplete_file_size", failed.reason)
    }

    @Test
    fun noProgressTriggersStalledInsteadOfIndefiniteDownloading() {
        val tracker = ReleaseDownloadTracker(stallTimeoutMillis = 100L)

        tracker.start(expectedBytes = 10L, nowMillis = 0L)
        tracker.onBytesCopied(bytesCopied = 4L, nowMillis = 10L)
        val stalled = tracker.onTick(nowMillis = 200L)

        assertTrue(stalled is ReleaseDownloadState.Stalled)
        stalled as ReleaseDownloadState.Stalled
        assertEquals(4L, stalled.downloadedBytes)
        assertEquals(10L, stalled.expectedBytes)
    }

    @Test
    fun finalizingWithoutStreamCloseTimesOutInsteadOfInfiniteLoading() {
        val tracker = ReleaseDownloadTracker(finalizationTimeoutMillis = 100L)

        tracker.start(expectedBytes = 10L, nowMillis = 0L)
        tracker.onBytesCopied(bytesCopied = 10L, nowMillis = 10L)
        val failed = tracker.onTick(nowMillis = 200L)

        assertTrue(failed is ReleaseDownloadState.Failed)
        failed as ReleaseDownloadState.Failed
        assertEquals("stream_finalization_timeout", failed.reason)
    }
}
