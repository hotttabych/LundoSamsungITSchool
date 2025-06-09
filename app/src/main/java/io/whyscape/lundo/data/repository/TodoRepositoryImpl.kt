package io.whyscape.lundo.data.repository

import androidx.room.Transaction
import io.whyscape.lundo.data.db.CounterDao
import io.whyscape.lundo.data.db.CounterEntity
import io.whyscape.lundo.data.db.TodoDao
import io.whyscape.lundo.data.db.TodoEntity
import io.whyscape.lundo.domain.model.Todo
import io.whyscape.lundo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TodoRepositoryImpl(
    private val todoDao: TodoDao,
    private val counterDao: CounterDao
) : TodoRepository {
    override fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTodo(todo: Todo) {
        todoDao.insert(todo.toEntity())
    }

    override suspend fun updateTodo(todo: Todo) {
        todoDao.update(todo.toEntity())
    }

    override suspend fun deleteTodo(todo: Todo) {
        todoDao.delete(todo.toEntity())
    }

    override fun getCounter(): Flow<Int> {
        return counterDao.getCounter().map {
            val count = it?.count ?: 0
            count
        }
    }

    @Transaction
    override suspend fun incrementCounter() {
        val currentCounter = counterDao.getCounter().first()
        if (currentCounter == null) {
            val newCounter = CounterEntity(id = 1, count = 1)
            counterDao.insert(newCounter)
        } else {
            val updatedCounter = currentCounter.copy(count = currentCounter.count + 1)
            counterDao.update(updatedCounter)
        }
    }

    private fun TodoEntity.toDomain() = Todo(
        id,
        task,
        isCompleted
    )

    private fun Todo.toEntity() = TodoEntity(
        id = id,
        task = task,
        isCompleted = isCompleted
    )
}