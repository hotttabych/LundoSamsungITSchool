package io.whyscape.lundo.data.repository

import io.whyscape.lundo.data.db.NoteDao
import io.whyscape.lundo.data.db.NoteEntity
import io.whyscape.lundo.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

class NoteRepositoryImpl(private val dao: NoteDao?): NoteRepository {
    override fun getNotes(): Flow<List<NoteEntity>> = flow {
        emitAll(dao?.getAllNotes() ?: emptyFlow())
    }


    override suspend fun addNote(note: NoteEntity) { dao?.insert(note) }
    override suspend fun deleteNote(note: NoteEntity) {
        dao?.delete(note)
    }
    override suspend fun getNotesNow(): List<NoteEntity> = dao?.getAllNotesNow() ?: emptyList<NoteEntity>()
}

@Singleton
class ProxyNoteRepository @Inject constructor() : NoteRepository {
    private var realRepo: NoteRepository? = null

    fun setRealRepository(repo: NoteRepository) {
        realRepo = repo
    }

    fun clearRealRepository() {
        realRepo = null
    }

    override fun getNotes(): Flow<List<NoteEntity>> =
        realRepo?.getNotes() ?: emptyFlow()

    override suspend fun getNotesNow(): List<NoteEntity> =
        realRepo?.getNotesNow() ?: emptyList()

    override suspend fun addNote(note: NoteEntity) =
        realRepo?.addNote(note) ?: Unit

    override suspend fun deleteNote(note: NoteEntity) =
        realRepo?.deleteNote(note) ?: Unit
}