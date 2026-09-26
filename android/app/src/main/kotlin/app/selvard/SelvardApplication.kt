package app.selvard

import android.app.Application
import androidx.room.Room
import app.selvard.core.domain.crypto.EventPayloadCrypto
import app.selvard.core.domain.eventbus.InMemoryEventBus
import app.selvard.core.domain.link.DevelopmentSampleFeed
import app.selvard.core.domain.link.LinkGuardian
import app.selvard.core.domain.net.DnsFilterEngine
import app.selvard.core.domain.store.EventStore
import app.selvard.network.NetworkGuardianState
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

    /** Tunnel state observed by the UI; OFF is the honest default until the tunnel is live. */
    val networkGuardianState by lazy { NetworkGuardianState() }

    val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO,
    )

    companion object { const val DATABASE_NAME = "selvard-events" }
}
