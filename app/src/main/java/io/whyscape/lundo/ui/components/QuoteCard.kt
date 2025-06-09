package io.whyscape.lundo.ui.components

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import io.whyscape.lundo.R

object QuoteCard {
    @Composable
    fun QuoteCard(
        modifier: Modifier,
        quoteText: String?,
        quoteAuthor: String?,
        quoteLink: String? = null,
        buttonCallback: (() -> Unit)? = null
    ) {
        val quoteLinkNoPrefix = quoteLink?.replace("http://", "")
        val context = LocalContext.current
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                IconText(
                    iconRes = R.drawable.quote_closing_24,
                    text = stringResource(R.string.random_quote),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    modifier = Modifier.align(Alignment.Start),
                    text = quoteText.toString(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!quoteAuthor.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.End),
                        text = "- $quoteAuthor",
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!quoteLinkNoPrefix.isNullOrEmpty()) {
                    Text(
                        text = stringResource(R.string.quote_source, quoteLinkNoPrefix),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .clickable {
                                openUrl(context, quoteLink)
                            }
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { buttonCallback?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.another_quote), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    fun openUrl(context: Context, url: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, url.toUri())
    }
}