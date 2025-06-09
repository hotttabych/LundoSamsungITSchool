package io.whyscape.lundo.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1, // Single user with fixed ID
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "handle") val handle: String?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "last_usage_timestamp") val lastUsageTimestamp: Long?,
    @ColumnInfo(name = "coins") val coins: Int,
    @ColumnInfo(name = "photo_uri") val photoUri: String? = null
)
