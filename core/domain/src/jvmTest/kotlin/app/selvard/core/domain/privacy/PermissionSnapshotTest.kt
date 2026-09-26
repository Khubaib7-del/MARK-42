package app.selvard.core.domain.privacy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PermissionSnapshotTest {

    @Test
    fun firstSnapshotListsEverythingAsInstalled() {
        val snap = snapshot(1_000L, "a" to setOf("p1"), "b" to emptySet())
        val diff = diffSnapshots(null, snap)
        assertEquals(listOf("a", "b"), diff.addedPackages)
        assertTrue(diff.removedPackages.isEmpty())
        assertTrue(diff.isEmpty.not())
    }

    @Test
    fun detectsAddedRemovedAndChanged() {
        val old = snapshot(1_000L, "keep" to setOf("p1"), "gone" to setOf("p1"), "morph" to setOf("p1"))
        val new = snapshot(
            2_000L,
            "keep" to setOf("p1"),
            "fresh" to setOf("p2"),
            "morph" to setOf("p1", "p2"),
        )
        val diff = diffSnapshots(old, new)
        assertEquals(listOf("fresh"), diff.addedPackages)
        assertEquals(listOf("gone"), diff.removedPackages)
        assertEquals(setOf("p2"), diff.changed.getValue("morph").added)
        assertTrue(diff.changed.getValue("morph").removed.isEmpty())
        assertEquals(1, diff.unchangedCount)
    }

    @Test
    fun identicalSnapshotsAreEmpty() {
        val snap = snapshot(1_000L, "a" to setOf("p1"))
        assertTrue(diffSnapshots(snap, snap).isEmpty)
    }

    @Test
    fun changedReportingIsCappedAndCounted() {
        val old = snapshot(1_000L, *Array(80) { "pkg$it" to setOf("p1") })
        val new = snapshot(2_000L, *Array(80) { "pkg$it" to setOf("p1", "p2") })
        val diff = diffSnapshots(old, new)
        assertEquals(SnapshotDiff.MAX_CHANGED_REPORTED, diff.changed.size)
        assertEquals(80 - SnapshotDiff.MAX_CHANGED_REPORTED, diff.omittedChangedCount)
        assertTrue(!diff.isEmpty)
        assertTrue(diff.summaryLine().length <= SnapshotDiff.MAX_SUMMARY_LENGTH)
    }

    @Test
    fun guardsRejectBadInput() {
        assertFailsWith<IllegalArgumentException> { snapshot(-1L) }
        assertFailsWith<IllegalArgumentException> {
            PermissionSnapshot(0L, List(PermissionSnapshot.MAX_PACKAGES + 1) { PackageEntry("p$it", emptySet()) })
        }
    }

    private fun snapshot(at: Long, vararg pkgs: Pair<String, Set<String>>) =
        PermissionSnapshot(at, pkgs.map { PackageEntry(it.first, it.second) })
}
