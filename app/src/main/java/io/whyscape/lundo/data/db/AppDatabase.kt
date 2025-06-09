package io.whyscape.lundo.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.app.Application
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.whyscape.lundo.data.db.AppDatabase.Companion.BookStatusConverter

@Database(entities = [TodoEntity::class, CounterEntity::class, UserEntity::class, FlashcardEntity::class, BookEntity::class, MoodEntry::class], version = 1)
@TypeConverters(BookStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun counterDao(): CounterDao
    abstract fun userDao(): UserDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun bookDao(): BookDao
    abstract fun moodDao(): MoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        class BookStatusConverter {

            @TypeConverter
            fun toStatus(value: Int): BookStatus = BookStatus.entries.getOrNull(value) ?: BookStatus.TO_READ

            @TypeConverter
            fun fromStatus(status: BookStatus): Int = status.ordinal
        }

        fun getDatabase(application: Application): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    AppDatabase::class.java,
                    "todo_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}