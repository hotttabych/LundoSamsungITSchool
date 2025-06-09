package io.whyscape.lundo.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import android.app.Application
import io.whyscape.lundo.di.AppModule
import io.whyscape.lundo.domain.model.Todo
import io.whyscape.lundo.domain.usecase.AddTodoUseCase
import io.whyscape.lundo.domain.usecase.DeleteTodoUseCase
import io.whyscape.lundo.domain.usecase.GetCounterUseCase
import io.whyscape.lundo.domain.usecase.GetTodosUseCase
import io.whyscape.lundo.domain.usecase.IncrementCounterUseCase
import io.whyscape.lundo.domain.usecase.ToggleTodoCompletionUseCase
import kotlinx.coroutines.flow.map

class TodoViewModel(
    getTodosUseCase: GetTodosUseCase,
    getCounterUseCase: GetCounterUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val incrementCounterUseCase: IncrementCounterUseCase
) : ViewModel() {
    val todos: Flow<List<Todo>> = getTodosUseCase().map { todoList ->
        todoList.sortedBy { it.isCompleted }
    }
    val completionCounter: Flow<Int> = getCounterUseCase()

    fun addTodo(task: String) {
        viewModelScope.launch {
            addTodoUseCase(task)
        }
    }

    fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch {
            if (!todo.isCompleted) {
                incrementCounterUseCase()
            }
            toggleTodoCompletionUseCase(todo)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            deleteTodoUseCase(todo)
        }
    }
}

class TodoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            val module = AppModule
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(
                module.provideGetTodosUseCase(application),
                module.provideGetCounterUseCase(application),
                module.provideAddTodoUseCase(application),
                module.provideToggleTodoCompletionUseCase(application),
                module.provideDeleteTodoUseCase(application),
                module.provideIncrementCounterUseCase(application)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}