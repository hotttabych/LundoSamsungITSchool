package io.whyscape.lundo.data.repository

import io.whyscape.lundo.data.db.MoodDao
import io.whyscape.lundo.data.db.MoodEntry
import io.whyscape.lundo.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(private val dao: MoodDao) : MoodRepository {
    override suspend fun insertMood(mood: MoodEntry) = dao.insertMood(mood)
    override fun getMoodHistory(): Flow<List<MoodEntry>> = dao.getMoodHistory()
}

class SaveMoodUseCase @Inject constructor(private val repo: MoodRepository) {
    suspend operator fun invoke(mood: MoodEntry) = repo.insertMood(mood)
}

class GetMoodHistoryUseCase @Inject constructor(private val repo: MoodRepository) {
    operator fun invoke() = repo.getMoodHistory()
}