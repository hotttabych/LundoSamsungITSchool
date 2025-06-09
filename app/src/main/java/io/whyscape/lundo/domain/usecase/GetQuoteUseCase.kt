package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.data.remote.dto.QuoteDto
import io.whyscape.lundo.domain.repository.QuoteRepository

class GetQuoteUseCase(private val repository: QuoteRepository) {
    suspend operator fun invoke(language: String): QuoteDto = repository.getQuote(language)
}
