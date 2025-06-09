package io.whyscape.lundo.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(card: List<FlashcardEntity>)

    @Query("SELECT * FROM flashcards ORDER BY timestamp DESC")
    fun getAll(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards ORDER BY timestamp DESC")
    suspend fun getAllNow(): List<FlashcardEntity>

    @Delete
    suspend fun delete(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteById(id: Int)
}