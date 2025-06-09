package io.whyscape.lundo.domain.repository

import io.whyscape.lundo.data.db.FlashcardEntity
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    val flashcards: Flow<List<FlashcardEntity>>

    suspend fun addFlashcard(question: String, answer: String)

    suspend fun deleteFlashcard(card: FlashcardEntity)
}
