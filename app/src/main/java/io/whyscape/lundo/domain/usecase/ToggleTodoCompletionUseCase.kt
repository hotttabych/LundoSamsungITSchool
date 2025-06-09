package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.domain.model.Todo
import io.whyscape.lundo.domain.repository.TodoRepository

class ToggleTodoCompletionUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(todo: Todo) {
        val updatedTodo = Todo(
            todo.id,
            todo.task,
            !todo.isCompleted
        )
        repository.updateTodo(updatedTodo)
    }
}