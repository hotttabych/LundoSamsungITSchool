package io.whyscape.lundo.data.repository

import io.whyscape.lundo.data.db.BookDao
import io.whyscape.lundo.data.db.BookEntity
import io.whyscape.lundo.data.db.BookStatus
import io.whyscape.lundo.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val dao: BookDao
) : BookRepository {
    override val books: Flow<List<BookEntity>> = dao.getAllBooks()

    override suspend fun addBook(
        title: String,
        description: String,
        link: String?,
        status: BookStatus
    ) {
        dao.insert(BookEntity(title = title, description = description, link = link, status = status))
    }

    override suspend fun addBook(
        book: BookEntity
    ) {
        dao.insert(book)
    }

    override suspend fun updateBook(
        book: BookEntity
    ) {
        dao.update(book)
    }

    override suspend fun deleteBook(book: BookEntity) {
        dao.delete(book)
    }
}
