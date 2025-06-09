package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.domain.repository.TodoRepository

class IncrementCounterUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke() {
        repository.incrementCounter()
    }
}