package io.whyscape.lundo.domain.repository

import io.whyscape.lundo.data.db.BookEntity
import io.whyscape.lundo.data.db.BookStatus
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    val books: Flow<List<BookEntity>>
    suspend fun addBook(title: String, description: String, link: String?, status: BookStatus)
    suspend fun addBook(book: BookEntity)
    suspend fun deleteBook(book: BookEntity)
    suspend fun updateBook(book: BookEntity)
}
