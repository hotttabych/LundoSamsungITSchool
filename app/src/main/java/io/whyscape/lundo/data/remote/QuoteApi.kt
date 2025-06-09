package io.whyscape.lundo.data.remote

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.whyscape.lundo.data.remote.dto.QuoteDto
import kotlinx.serialization.json.Json

class QuoteApi(private val client: HttpClient) {
    suspend fun getQuote(language: String): QuoteDto {
        val response: HttpResponse = client.get("https://api.forismatic.com/api/1.0/") {
            parameter("method", "getQuote")
            parameter("key", "457653")
            parameter("format", "json")
            parameter("lang", language)
        }
        val body = response.bodyAsText()
        return Json.decodeFromString(QuoteDto.serializer(), body.sanitizeJson())
    }

    fun String.sanitizeJson(): String {
        val invalidEscapeSequenceRegex = """\\([^"\\\/bfnrtu])""".toRegex()

        var sanitizedJson = replace(invalidEscapeSequenceRegex) { matchResult ->
            matchResult.groupValues[1]
        }

        return sanitizedJson
    }
}
