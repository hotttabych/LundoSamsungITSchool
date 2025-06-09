package io.whyscape.lundo.data.remote

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.whyscape.lundo.R
import io.whyscape.lundo.common.BuildVariables
import io.whyscape.lundo.domain.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.ConnectException

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    @SerialName("system_instruction") val systemInstruction: Content? = null,
    val tools: List<Tool>? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    @SerialName("inline_data") val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
data class Tool(
    @SerialName("google_search") val googleSearch: GoogleSearchTool? = null
)

@Serializable
class GoogleSearchTool

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@Serializable
data class Candidate(
    val content: Content
)

fun createClient(context: Context): HttpClient {
    val baseClientBuilder = OkHttpClient.Builder()
        .cache(Cache(File(context.cacheDir, "http"), 10L * 1024 * 1024))

    val baseClient = baseClientBuilder.build()

    val finalClient = OkHttpClient.Builder()
        .cache(baseClient.cache)
        .build()

    return HttpClient(OkHttp) {
        engine {
            preconfigured = finalClient
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}

fun streamGeminiResponse(
    context: Context,
    messages: List<ChatMessage>,
    systemPrompt: String,
    fileBytes: ByteArray? = null,
    mimeType: String? = null,
    maxRetries: Int = 3
): Flow<Pair<String, Boolean>> = flow {
    var attempt = 0
    var successful = false

    val filePart = if (fileBytes != null && mimeType != null) {
        Part(
            inlineData = InlineData(
                mimeType = mimeType,
                data = android.util.Base64.encodeToString(fileBytes, android.util.Base64.NO_WRAP)
            )
        )
    } else null

    while (attempt < maxRetries && !successful) {
        delay(1000L * attempt)
        try {
            attempt++
            val request = GeminiRequest(
                contents = messages.map {
                    Content(
                        role = it.role,
                        parts = mutableListOf<Part>().apply {
                            filePart?.let { add(it) }
                            add(Part(text = it.text))
                        }
                    )
                },
                systemInstruction = Content(parts = listOf(Part(systemPrompt))),
                tools = listOf(
                    Tool(googleSearch = GoogleSearchTool())
                )
            )

            val client = createClient(context)

            val response: GeminiResponse =
                client.post("https://geminiproxy.sizoffcon.workers.dev/v1beta/models/gemini-2.0-flash-exp:generateContent?key=${BuildVariables.GEMINI_API_KEY}") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()

            when {
                response.candidates != null -> {
                    response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.let {
                        emit(Pair(it, true))
                        successful = true
                    } ?: emit(Pair(context.getString(R.string.gemini_api_no_valid_response_content_found), false))
                }

                response.error != null -> {
                }

                else -> {
                    emit(Pair(context.getString(R.string.gemini_api_unexpected_response_format), false))
                    successful = true
                }
            }
        } catch (e: SerializationException) {
            emit(Pair(context.getString(R.string.gemini_api_error), false))
            Log.e(this::class.simpleName, e.message.toString())
            successful = true
        } catch (e: ConnectException) {
            emit(Pair(context.getString(R.string.gemini_api_error), false))
            Log.e(this::class.simpleName, e.message.toString())
        } catch (e: Exception) {
            emit(Pair(context.getString(R.string.gemini_api_error), false))
            Log.e(this::class.simpleName, e.message.toString())
        }
    }

    if (!successful) {
        emit(Pair(context.getString(R.string.gemini_api_error), false))
    }
}
