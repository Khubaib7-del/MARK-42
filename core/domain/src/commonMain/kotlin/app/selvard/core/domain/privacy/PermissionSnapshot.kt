package app.selvard.core.domain.privacy

import kotlinx.serialization.Serializable

/** One package's requested permissions at snapshot time (manifest facts, not grants). */
@Serializable
data class PackageEntry(
    val packageName: String,
    val permissions: Set<String>,
)

/**
 * A point-in-time capture of requested permissions across visible packages.
 * The baseline for change detection; deltas are computed by [diffSnapshots].
 */
@Serializable
data class PermissionSnapshot(
    val capturedAtMillis: Long,
    val entries: List<PackageEntry>,
) {
    init {
        require(capturedAtMillis >= 0) { "capture time must be non-negative" }
        require(entries.size <= MAX_PACKAGES) { "implausible package count" }
    }

    fun index(): Map<String, Set<String>> = entries.associate { it.packageName to it.permissions }

    companion object {
        const val MAX_PACKAGES = 2048
    }
}

/** Permission-set delta for one package present in both snapshots. */
data class PermissionChange(
    val added: Set<String>,
    val removed: Set<String>,
)

/**
 * Delta between two snapshots. Deterministic: outputs are sorted, so equal
 * inputs always produce equal diffs. Reporting is capped; any omission is
 * counted, never silent.
 */
data class SnapshotDiff(
    val addedPackages: List<String>,
    val removedPackages: List<String>,
    val changed: Map<String, PermissionChange>,
    val omittedChangedCount: Int,
    val unchangedCount: Int,
) {
    val isEmpty: Boolean
        get() = addedPackages.isEmpty() && removedPackages.isEmpty() && changed.isEmpty() &&
            omittedChangedCount == 0

    /** One short line for event evidence; always fits the 256-char limit. */
    fun summaryLine(): String {
        val base = "${addedPackages.size} installed, ${removedPackages.size} removed, " +
            "${changed.size} permission-set change(s), $unchangedCount unchanged"
        return if (omittedChangedCount > 0) "$base (+$omittedChangedCount omitted from display)" else base
    }

    companion object {
        const val MAX_CHANGED_REPORTED = 64
        const val MAX_SUMMARY_LENGTH = 256
    }
}

fun diffSnapshots(old: PermissionSnapshot?, new: PermissionSnapshot): SnapshotDiff {
    if (old == null) {
        return SnapshotDiff(
            addedPackages = new.entries.map { it.packageName }.sorted(),
            removedPackages = emptyList(),
            changed = emptyMap(),
            omittedChangedCount = 0,
            unchangedCount = 0,
        )
    }
    val before = old.index()
    val after = new.index()
    val added = (after.keys - before.keys).sorted()
    val removed = (before.keys - after.keys).sorted()
    val changedAll = (after.keys intersect before.keys)
        .mapNotNull { pkg ->
            val delta = PermissionChange(
                added = (after.getValue(pkg) - before.getValue(pkg)).toSortedSet(),
                removed = (before.getValue(pkg) - after.getValue(pkg)).toSortedSet(),
            )
            if (delta.added.isEmpty() && delta.removed.isEmpty()) null else pkg to delta
        }
        .sortedBy { it.first }
    val omitted = (changedAll.size - SnapshotDiff.MAX_CHANGED_REPORTED).coerceAtLeast(0)
    val common = after.keys intersect before.keys
    return SnapshotDiff(
        addedPackages = added,
        removedPackages = removed,
        changed = changedAll.take(SnapshotDiff.MAX_CHANGED_REPORTED).toMap(),
        omittedChangedCount = omitted,
        unchangedCount = common.size - changedAll.size,
    )
}
