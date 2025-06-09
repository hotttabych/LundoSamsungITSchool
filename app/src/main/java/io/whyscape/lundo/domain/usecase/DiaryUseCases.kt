package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.data.db.NoteEntity
import io.whyscape.lundo.domain.repository.NoteRepository
import javax.inject.Inject

data class DiaryUseCases(
    val getAllNotes: GetAllNotes,
    val insertNote: InsertNote,
    val deleteNote: DeleteNote
)

class GetAllNotes @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke() = repo.getNotesNow()
}

class InsertNote @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) = repo.addNote(note)
}

class DeleteNote @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(note: NoteEntity) = repo.deleteNote(note)
}