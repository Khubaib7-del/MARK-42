package app.selvard

import android.app.Application
import androidx.room.Room
import app.selvard.core.domain.crypto.EventPayloadCrypto
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.eventbus.InMemoryEventBus
import app.selvard.core.domain.link.DevelopmentSampleFeed
import app.selvard.core.domain.link.LinkGuardian
import app.selvard.core.domain.net.DnsFilterEngine
import app.selvard.core.domain.appguard.AppGuardian
import app.selvard.core.domain.privacy.BootRecord
import app.selvard.core.domain.privacy.PackageEntry
import app.selvard.core.domain.store.EventStore
import app.selvard.network.NetworkGuardianState
import app.selvard.privacy.PackageEvents
import app.selvard.privacy.SelvardSeverity
import app.selvard.data.KeystoreKeyring
import app.selvard.data.RoomEventStore
import app.selvard.data.SelvardDatabase

class SelvardApplication : Application() {

    val database: SelvardDatabase by lazy {
        Room.databaseBuilder(this, SelvardDatabase::class.java, DATABASE_NAME).build()
    }

    val eventStore: EventStore by lazy {
        RoomEventStore(database.eventDao(), EventPayloadCrypto(KeystoreKeyring().masterKey()))
    }

    val linkGuardian: LinkGuardian by lazy {
        LinkGuardian(listOf(DevelopmentSampleFeed()))
    }

    val eventBus by lazy { InMemoryEventBus() }

    val sampleFeed by lazy { DevelopmentSampleFeed() }

    val dnsFilterEngine by lazy { DnsFilterEngine(listOf(sampleFeed)) }

    val appGuardian by lazy { AppGuardian() }

    /** Tunnel state observed by the UI; OFF is the honest default until the tunnel is live. */
    val networkGuardianState by lazy { NetworkGuardianState() }

    val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO,
    )

    /**
     * Phase 6 helpers. Severity mapping is explicit (never "safe");
     * dedupe is in-memory (restarts may redeliver one stale fact, stated in UI).
     */
    fun selvardSeverity(level: SelvardSeverity): Severity =
        if (level == SelvardSeverity.LOW) Severity.INFO else Severity.INFO

    private val recentPackageFacts = ArrayDeque<Pair<String, String>>()

    /** True if this exact (package, kind) was already recorded since process start. */
    @Synchronized
    fun lastPackageEvent(packageName: String, kind: String): Boolean {
        val key = packageName to kind
        if (recentPackageFacts.contains(key)) return true
        recentPackageFacts.addLast(key)
        while (recentPackageFacts.size > DEDUPE_WINDOW) recentPackageFacts.removeFirst()
        return false
    }

    /** Boot fact passthrough: the record is the timestamp, nothing inferred. */
    fun cachedBoot(bootAtMillis: Long): BootRecord = BootRecord(bootAtMillis)

    /** Visible-profile package entries for permission snapshots (best-effort, never throws). */
    fun selvardPackageEntries(): List<PackageEntry> = runCatching {
        val pm = packageManager
        val infos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }
        infos.map {
            val perms = runCatching {
                val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(
                        it.packageName,
                        android.content.pm.PackageManager.PackageInfoFlags.of(
                            android.content.pm.PackageManager.GET_PERMISSIONS.toLong(),
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(it.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
                }
                info.requestedPermissions?.toSet() ?: emptySet()
            }.getOrDefault(emptySet())
            PackageEntry(it.packageName, perms)
        }
    }.getOrDefault(emptyList())

    companion object {
        const val DATABASE_NAME = "selvard-events"

        /** Boot-redelivered broadcasts dedupe window (process-lifetime, in-memory). */
        const val DEDUPE_WINDOW = 64

        // PackageEvents kinds are referenced for discoverability; recording uses the string kinds.
        @Suppress("unused")
        private val kinds = listOf(
            PackageEvents.PACKAGE_ADDED,
            PackageEvents.PACKAGE_REMOVED,
            PackageEvents.PACKAGE_REPLACED,
        )
    }
}
