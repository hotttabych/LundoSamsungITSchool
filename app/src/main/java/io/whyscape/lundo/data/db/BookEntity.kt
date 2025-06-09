package io.whyscape.lundo.data.db

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.whyscape.lundo.R

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val link: String?,
    val status: BookStatus,
    val timestamp: Long = System.currentTimeMillis()
)

enum class BookStatus(val displayName: String, val key: String) {
    TO_READ("Хочу прочитать", "want_to_read"),
    READING("Читаю", "reading"),
    FINISHED("Завершено", "done");

    companion object {
        fun fromDisplayName(display: String): BookStatus =
            entries.firstOrNull { it.displayName == display } ?: TO_READ

        fun fromKeyOrDisplay(input: String, context: Context): BookStatus {
            val cleaned = input.trim().lowercase()

            return when (cleaned) {
                context.getString(R.string.book_status_reading_lowercase) -> READING
                context.getString(R.string.book_status_done_lowercase) -> FINISHED
                else -> TO_READ
            }
        }
    }
}