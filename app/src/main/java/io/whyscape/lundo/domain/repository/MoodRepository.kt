package io.whyscape.lundo.domain.repository

import io.whyscape.lundo.data.db.MoodEntry
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    suspend fun insertMood(mood: MoodEntry)
    fun getMoodHistory(): Flow<List<MoodEntry>>
}