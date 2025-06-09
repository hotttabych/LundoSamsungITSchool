package io.whyscape.lundo.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "counters")
data class CounterEntity(
    @PrimaryKey val id: Int = 1, // Single row with fixed ID
    val count: Int
)