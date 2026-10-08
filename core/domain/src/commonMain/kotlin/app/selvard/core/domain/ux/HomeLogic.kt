package app.selvard.core.domain.ux

import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.SecurityEvent

/** Human-readable phrasing for the activity feed. Only fields that are safe to show are used. */
object EventPresentation {

    data class Line(val title: String, val detail: String)

    fun describe(event: SecurityEvent): Line {
        val asset = event.affectedAsset
        // URL refs are hosts only and identity refs are pre-masked; app packages are not private.
        val subject = asset?.takeIf { it.type == AssetType.URL || it.type == AssetType.APP || it.type == AssetType.IDENTITY }?.ref
        val title = when (event.category) {
            EventCategory.LINK -> "Link checked"
            EventCategory.NETWORK -> "Lookup blocked"
            EventCategory.APPLICATION -> "Apps reviewed"
            EventCategory.PRIVACY -> "Privacy fact recorded"
            EventCategory.IDENTITY -> "Identity check"
            EventCategory.DEVICE -> "Device fact recorded"
            EventCategory.USER, EventCategory.SYSTEM -> "Event recorded"
        }
        return Line(title, subject ?: event.source.replace('_', ' '))
    }
}

/** Short relative timestamps for lists ("3 h ago"). Pure, so it is unit-tested. */
object RelativeTime {
    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    fun format(nowMillis: Long, thenMillis: Long): String {
        val delta = (nowMillis - thenMillis).coerceAtLeast(0L)
        return when {
            delta < MINUTE -> "just now"
            delta < HOUR -> "${delta / MINUTE} min ago"
            delta < DAY -> "${delta / HOUR} h ago"
            delta < 2 * DAY -> "yesterday"
            else -> "${delta / DAY} days ago"
        }
    }
}

enum class AttentionKind { ENABLE_NETWORK_FILTER, SCAN_APPS, RESCAN_APPS }

data class AttentionItem(val kind: AttentionKind, val title: String, val detail: String)

/**
 * "Needs attention" is built only from things Selvard can actually observe:
 * whether the opt-in filter is on and whether apps were ever scanned. It never
 * invents findings to fill the list.
 */
object AttentionPlanner {
    const val STALE_SCAN_DAYS = 7
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    private const val MAX_ITEMS = 3

    fun plan(networkFilterOn: Boolean, lastAppScanMillis: Long?, nowMillis: Long): List<AttentionItem> = buildList {
        if (!networkFilterOn) {
            add(
                AttentionItem(
                    AttentionKind.ENABLE_NETWORK_FILTER,
                    "Turn on the DNS filter",
                    "Off now. Known bad domains are not being refused.",
                ),
            )
        }
        when {
            lastAppScanMillis == null -> add(
                AttentionItem(AttentionKind.SCAN_APPS, "Review installed apps", "Not scanned yet. Takes a few seconds."),
            )
            nowMillis - lastAppScanMillis > STALE_SCAN_DAYS * DAY_MILLIS -> add(
                AttentionItem(AttentionKind.RESCAN_APPS, "Re-scan installed apps", "Last scan was over a week ago."),
            )
        }
    }.take(MAX_ITEMS)
}
