package io.whyscape.lundo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters WHERE id = 1")
    fun getCounter(): Flow<CounterEntity?>

    @Query("SELECT * FROM counters WHERE id = 1")
    suspend fun getCounterNow(): CounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counter: CounterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(counter: List<CounterEntity>)

    @Update
    suspend fun update(counter: CounterEntity)
}