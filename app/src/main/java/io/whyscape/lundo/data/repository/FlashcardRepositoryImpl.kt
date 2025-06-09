package io.whyscape.lundo.data.repository

import io.whyscape.lundo.data.db.FlashcardDao
import io.whyscape.lundo.data.db.FlashcardEntity
import io.whyscape.lundo.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FlashcardRepositoryImpl @Inject constructor(
    private val dao: FlashcardDao
) : FlashcardRepository  {
    override val flashcards: Flow<List<FlashcardEntity>> = dao.getAll()

    override suspend fun addFlashcard(question: String, answer: String) {
        dao.insert(FlashcardEntity(question = question, answer = answer))
    }

    override suspend fun deleteFlashcard(card: FlashcardEntity) {
        dao.delete(card)
    }
}