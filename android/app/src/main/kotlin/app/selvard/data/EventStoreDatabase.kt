package app.selvard.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

/** Append-only row; payload is AES-GCM ciphertext of the canonical event JSON. */
@Entity(tableName = "events", indices = [Index("timestampMillis"), Index("category")])
class EventEntity(
    @PrimaryKey val eventId: String,
    val timestampMillis: Long,
    val category: String,
    val payload: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventEntity) return false
        return eventId == other.eventId &&
            timestampMillis == other.timestampMillis &&
            category == other.category &&
            payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = eventId.hashCode()
        result = 31 * result + timestampMillis.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("SELECT * FROM events ORDER BY timestampMillis DESC, eventId DESC LIMIT :limit")
    suspend fun queryRecent(limit: Int): List<EventEntity>

    @Query("DELETE FROM events WHERE category = :category AND timestampMillis < :cutoffMillis")
    suspend fun purgeOlderThan(category: String, cutoffMillis: Long): Int

    @Query("DELETE FROM events")
    suspend fun purgeAll(): Int
}

@Database(entities = [EventEntity::class], version = 1, exportSchema = true)
abstract class SelvardDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
