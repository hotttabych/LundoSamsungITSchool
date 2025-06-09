package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.domain.model.Todo
import io.whyscape.lundo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class GetTodosUseCase(private val repository: TodoRepository) {
    operator fun invoke(): Flow<List<Todo>> {
        return repository.getAllTodos()
    }
}