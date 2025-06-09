package io.whyscape.lundo.data.repository

import io.whyscape.lundo.data.remote.QuoteApi
import io.whyscape.lundo.data.remote.dto.QuoteDto
import io.whyscape.lundo.domain.repository.QuoteRepository

class QuoteRepositoryImpl(private val api: QuoteApi) : QuoteRepository {
    override suspend fun getQuote(language: String): QuoteDto = api.getQuote(language)
}
