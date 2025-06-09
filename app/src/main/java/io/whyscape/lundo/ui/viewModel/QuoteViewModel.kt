package io.whyscape.lundo.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.whyscape.lundo.data.remote.dto.QuoteDto
import io.whyscape.lundo.domain.usecase.GetQuoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val getQuoteUseCase: GetQuoteUseCase
) : ViewModel() {

    private val _quote = MutableStateFlow<QuoteDto?>(null)
    val quote: StateFlow<QuoteDto?> get() = _quote

    fun loadQuote(language: String) {
        viewModelScope.launch {
            _quote.value = getQuoteUseCase(language)
        }
    }
}
