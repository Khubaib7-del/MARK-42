package app.selvard

import android.app.Application
import androidx.room.Room
import app.selvard.core.domain.crypto.EventPayloadCrypto
import app.selvard.core.domain.store.EventStore
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

    companion object { const val DATABASE_NAME = "selvard-events" }
}
