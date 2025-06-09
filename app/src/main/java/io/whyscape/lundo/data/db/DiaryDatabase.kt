package io.whyscape.lundo.data.db

import android.content.Context
import androidx.room.*
import io.whyscape.lundo.common.Converters
import net.sqlcipher.database.SupportFactory

@Database(entities = [NoteEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        fun create(context: Context, passphrase: ByteArray): DiaryDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                DiaryDatabase::class.java,
                "diary.db"
            ).openHelperFactory(factory).build()
        }
    }
}