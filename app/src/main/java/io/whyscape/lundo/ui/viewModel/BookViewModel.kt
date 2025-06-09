package io.whyscape.lundo.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.whyscape.lundo.data.db.BookEntity
import io.whyscape.lundo.data.db.BookStatus
import io.whyscape.lundo.domain.repository.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    val books = repository.books.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addBook(
        title: String,
        description: String,
        link: String?,
        status: BookStatus
    ) {
        viewModelScope.launch {
            repository.addBook(
                title = title,
                description = description,
                link = link,
                status = status
            )
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch { repository.updateBook(book) }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch { repository.deleteBook(book) }
    }
}