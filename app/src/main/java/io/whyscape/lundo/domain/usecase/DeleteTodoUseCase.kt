package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.domain.model.Todo
import io.whyscape.lundo.domain.repository.TodoRepository

class DeleteTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(todo: Todo) {
        repository.deleteTodo(todo)
    }
}