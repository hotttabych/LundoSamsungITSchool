package io.whyscape.lundo.domain.repository

import io.whyscape.lundo.data.db.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<NoteEntity>>
    suspend fun getNotesNow(): List<NoteEntity>
    suspend fun addNote(note: NoteEntity)
    suspend fun deleteNote(note: NoteEntity)
}