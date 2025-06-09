package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.domain.model.Todo
import io.whyscape.lundo.domain.repository.TodoRepository

class AddTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(task: String) {
        repository.addTodo(Todo(0, task))
    }
}