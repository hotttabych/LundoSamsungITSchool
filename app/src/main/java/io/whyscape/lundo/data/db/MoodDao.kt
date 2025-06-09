package io.whyscape.lundo.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: Int,
    val note: String,
    val timestamp: Long
)

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mood: List<MoodEntry>)

    @Query("SELECT * FROM MoodEntry ORDER BY timestamp DESC")
    fun getMoodHistory(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM MoodEntry ORDER BY timestamp DESC")
    suspend fun getMoodHistoryNow(): List<MoodEntry>
}
