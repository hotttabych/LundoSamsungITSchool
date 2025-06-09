package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class GetCounterUseCase(private val repository: TodoRepository) {
    operator fun invoke(): Flow<Int> {
        return repository.getCounter()
    }
}