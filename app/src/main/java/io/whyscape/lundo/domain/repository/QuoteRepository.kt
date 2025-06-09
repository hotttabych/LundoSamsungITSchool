package io.whyscape.lundo.domain.repository

import io.whyscape.lundo.data.remote.dto.QuoteDto

interface QuoteRepository {
    suspend fun getQuote(language: String): QuoteDto
}
