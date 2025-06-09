package io.whyscape.lundo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteDto(
    @SerialName("quoteText") val quoteText: String,
    @SerialName("quoteAuthor") val quoteAuthor: String,
    @SerialName("senderName") val senderName: String,
    @SerialName("senderLink") val senderLink: String,
    @SerialName("quoteLink") val quoteLink: String
)
