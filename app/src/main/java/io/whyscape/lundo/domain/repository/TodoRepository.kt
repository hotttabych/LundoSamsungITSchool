package io.whyscape.lundo.domain.repository

import io.whyscape.lundo.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<Todo>>
    suspend fun addTodo(todo: Todo)
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todo: Todo)
    fun getCounter(): Flow<Int>
    suspend fun incrementCounter()
}