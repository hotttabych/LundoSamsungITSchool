package io.whyscape.lundo.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import dev.jeziellago.compose.markdowntext.MarkdownText
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import io.whyscape.lundo.R
import io.whyscape.lundo.common.BuildVariables
import io.whyscape.lundo.data.db.BookEntity
import io.whyscape.lundo.data.db.BookStatus
import io.whyscape.lundo.domain.DataStoreManager.Companion.AI_TASK_ACCESS_PREFERENCE_KEY
import io.whyscape.lundo.domain.model.AiMode
import io.whyscape.lundo.domain.model.ChatMessage
import io.whyscape.lundo.domain.model.Question
import io.whyscape.lundo.domain.model.TestData
import io.whyscape.lundo.ui.viewModel.BookViewModel
import io.whyscape.lundo.ui.viewModel.ChatViewModel
import io.whyscape.lundo.ui.viewModel.FlashcardViewModel
import io.whyscape.lundo.ui.viewModel.SettingsViewModel
import io.whyscape.lundo.ui.viewModel.TodoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Random
import java.util.UUID
import kotlin.math.absoluteValue

@Composable
fun AiChatScreen(
    viewModel: ChatViewModel,
    todoViewModel: TodoViewModel,
    flashcardViewModel: FlashcardViewModel,
    bookViewModel: BookViewModel,
    settingsViewModel: SettingsViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userInput = remember { mutableStateOf("") }
    val selectedMode by viewModel.selectedMode.collectAsState()

    val context = LocalContext.current

    val todos by todoViewModel.todos.collectAsState(emptyList())

    var showTeddyBearRain by remember { mutableStateOf(false) }

    // инициируем диалог, если сообщений нет
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            val initialGreeting = when (selectedMode) {
                AiMode.LUNO -> context.getString(R.string.luno_initial_greeting)
                AiMode.SEVA -> context.getString(R.string.seva_initial_greeting)
                AiMode.NOCTON -> context.getString(R.string.nocton_initial_greeting)
                AiMode.AZOR -> context.getString(R.string.azor_initial_greeting)
                AiMode.RAVEL -> context.getString(R.string.ravel_initial_greeting)
            }
            viewModel.sendMessageStreamed(
                context,
                initialGreeting,
                todos,
                todoViewModel,
                flashcardViewModel,
                bookViewModel,
                asUser = false
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            var sheetOpen by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(selectedMode.picture)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(selectedMode.label) + " avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(selectedMode.label), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.online), style = MaterialTheme.typography.labelSmall)
                }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { sheetOpen = true }
                )
            }

            if (sheetOpen) {
                AiModeSelectorSheet(
                    viewModel = settingsViewModel,
                    currentMode = selectedMode,
                    onSelect = { viewModel.setMode(it) },
                    onDismiss = { sheetOpen = false }
                )
            }

            val listState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                state = listState
            ) {
                items(items = messages) { msg ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)) {
                        ChatBubble(
                            context = context,
                            message = msg,
                            viewModel = viewModel,
                            todoViewModel = todoViewModel,
                            flashcardViewModel = flashcardViewModel,
                            bookViewModel = bookViewModel,
                            onTriggerRain = {
                                showTeddyBearRain = true
                            }
                        )
                    }
                }
                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            LaunchedEffect(messages, isLoading) {
                if (messages.isNotEmpty() || isLoading) {
                    listState.animateScrollToItem(messages.size)
                }
            }

            val snackbarHostState = remember { SnackbarHostState() }

            val context = LocalContext.current
            
            val attachedFileBytes = remember { mutableStateOf<ByteArray?>(null) }
            val attachedFileName = remember { mutableStateOf<String?>(null) }
            val attachedMimeType = remember { mutableStateOf<String?>(null) }

            val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream ->
                        attachedFileBytes.value = stream.readBytes()
                        attachedFileName.value = it.lastPathSegment ?: "attached_file"
                        attachedMimeType.value = context.contentResolver.getType(it) ?: "application/octet-stream"
                    }
                }
            }

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    TextField(
                        value = userInput.value,
                        onValueChange = { userInput.value = it },
                        label = {
                            Text(
                                attachedFileName.value?.let { "Attached: $it" } ?: stringResource(R.string.your_message)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(onClick = { fileLauncher.launch("*/*") }) {
                    Icon(
                        painter = painterResource(R.drawable.clipboard_check_bold_duotone),
                        contentDescription = "Attach File",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = {
                    if (userInput.value.isNotBlank() || attachedFileBytes.value != null) {
                        viewModel.sendMessageStreamed(
                            context,
                            userInput.value.ifBlank { context.getString(R.string.file) },
                            todos,
                            todoViewModel,
                            flashcardViewModel,
                            bookViewModel,
                            fileBytes = attachedFileBytes.value,
                            mimeType = attachedMimeType.value
                        )

                        if (attachedFileBytes.value != null) {
                            viewModel.pushMessage(
                                ChatMessage("user", userInput.value, attachedFileName.value, true)
                            )
                        }

                        userInput.value = ""
                        attachedFileBytes.value = null
                        attachedFileName.value = null
                        attachedMimeType.value = null
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.plane_bold_duotone),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(R.string.send)
                    )
                }
            }

            SnackbarHost(hostState = snackbarHostState)
        }

        if (showTeddyBearRain) {
            EmojiRainOverlayText(
                modifier = Modifier.fillMaxSize(),
                emojis = listOf("🧸")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBubble(context: Context,
               message: ChatMessage,
               viewModel: ChatViewModel,
               todoViewModel: TodoViewModel,
               flashcardViewModel: FlashcardViewModel,
               bookViewModel: BookViewModel,
               onTriggerRain: () -> Unit
) {
    val fullText = message.text
    val isUser = message.role == "user"

    val flashcards = extractFlashcards(fullText)
    val books = extractBooks(context, fullText)
    val tests = extractTests(fullText)
    val teddyBearRains = ArrayList(extractTeddyBearRains(fullText))
    val gifs = extractGifs(fullText)

    val textWithoutFlashcardsAndBooks = removeCommandsFromText(fullText)

    val bubbleColor =
        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val alignment = if (isUser) Arrangement.End else Arrangement.Start
    val cornerShape = if (isUser)
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    else
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Column(
            modifier = Modifier
                .background(bubbleColor, cornerShape)
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            MarkdownText(
                markdown = textWithoutFlashcardsAndBooks,
                style = MaterialTheme.typography.bodyLarge.copy(color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary)
            )

            message.fileName?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📎 $it",
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            val coroutineScope = rememberCoroutineScope()

            flashcards.forEach { flashcard ->
                Spacer(modifier = Modifier.height(12.dp))
                FlashcardItem(flashcard)
            }

            books.forEach { book ->
                Spacer(modifier = Modifier.height(12.dp))
                BookItem(book)
            }

            var selectedTest by remember { mutableStateOf<TestData?>(null) }
            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            tests.forEach { test ->
                Spacer(modifier = Modifier.height(12.dp))
                TestItem(test) {
                    selectedTest = test
                    coroutineScope.launch {
                        bottomSheetState.show()
                    }
                }
            }

            selectedTest?.let { test ->
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                            selectedTest = null
                        }
                    },
                    sheetState = bottomSheetState
                ) {
                    TestBottomSheet(test) {}
                }
            }

            if (teddyBearRains.isNotEmpty()) {
                onTriggerRain()
            }

            if (gifs.isNotEmpty()) {
                GifGallery(gifs)
            }

            if (!message.isSuccessful) {
                val todos by todoViewModel.todos.collectAsState(emptyList())
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = {
                            viewModel.resendPreviousUserMessage(
                                context = context,
                                todoList = todos,
                                todoViewModel = todoViewModel,
                                flashcardViewModel = flashcardViewModel,
                                bookViewModel = bookViewModel
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.resend_message_button))
                    }
                }
            }
        }
    }
}

@Composable
fun GifGallery(queries: List<String>) {
    var gifs by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(queries) {
        val results = queries.mapNotNull { query ->
            searchGif(query)
        }
        gifs = results
    }

    Column {
        gifs.forEach { gifUrl ->
            Spacer(Modifier.height(4.dp))
            GifImage(gifUrl = gifUrl)
        }
    }
}

@Composable
fun FlashcardItem(card: Flashcard) {
    var showAnswer by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showAnswer = !showAnswer }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = card.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (showAnswer) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.answer,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.tap_to_reveal_the_answer),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
@Preview
fun BookItemPreview() {
    BookItem(
        BookEntity(
            title = "Luno in the Wonderland",
            description = "A saga about a humble yet brave AI who changed the world",
            link = "https://google.com",
            status = BookStatus.READING
        )
    )
}

@Composable
fun BookItem(book: BookEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        val context = LocalContext.current

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconText(
                    iconRes = R.drawable.book_bold_duotone,
                    text = stringResource(R.string.book),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Text(
                book.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(book.description, maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.book_status, book.status),
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            book.link?.let {
                IconText(
                    iconRes = R.drawable.magnifer_line_duotone,
                    text = stringResource(R.string.find_on_the_web),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                    spacerWidth = 6.dp,
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun TestItemPreview() {
    TestItem(TestData("Hi", listOf(Question("hi", listOf("1", "2"), "1", "idk")))) { }
}

@Composable
fun TestItem(
    testData: TestData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.clipboard_check_bold_duotone),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
            )
            Column {
                Text(
                    text = testData.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = pluralStringResource(R.plurals.question_count, testData.questions.size, testData.questions.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

data class Flashcard(val question: String, val answer: String)

fun extractFlashcards(text: String): List<Flashcard> {
    val regex = Regex("""flashcard\("(.+?)",\s*"(.+?)"\)""")
    return regex.findAll(text).map {
        val (question, answer) = it.destructured
        Flashcard(question.trim(), answer.trim())
    }.toList()
}

fun extractGifs(text: String): List<String> {
    val regex = Regex("""find_gif\("(.+?)"\)""")
    val queries = regex.findAll(text).map { it.groupValues[1] }.toList()
    return queries
}

fun extractTeddyBearRains(text: String): List<Int> {
    val regex = Regex("""\bstartTeddyBearRain\s*\(\s*\)""")
    return regex.findAll(text).map { it.range.first }.toList()
}

fun extractTests(text: String): List<TestData> {
    val regex = Regex("""test\((.*?)\)""")
    return regex.findAll(text).mapNotNull { match ->
        try {
            val fullContent = match.groupValues[1]
            val parts = fullContent.splitByCommaRespectingQuotes()

            val title = parts[0].trim().removeSurrounding("\"")
            val questionsRaw =
                parts.subList(1, parts.size).joinToString(",")
            val rawQuestions = questionsRaw.splitBySemicolonRespectingQuotes()

            val questions = rawQuestions.map { block ->
                val (q, a, e) = block.splitByPipeRespectingQuotes()
                val questionText = q.removeSurrounding("\"").trim()
                val explanation = e.removeSurrounding("\"").trim()

                val rawAnswers = a.splitByCommaRespectingQuotes()
                    .map { it.trim().removeSurrounding("\"") }

                val correct = rawAnswers.first { it.contains("*") }.replace("*", "")
                val cleanedAnswers = rawAnswers.map { it.replace("*", "") }.shuffled()

                Question(
                    questionText,
                    cleanedAnswers,
                    correct,
                    explanation
                )
            }

            TestData(title, questions)
        } catch (_: Exception) {
            null
        }
    }.toList()
}

fun String.splitByCommaRespectingQuotes(): List<String> {
    return splitByDelimiterRespectingQuotes(this, ',')
}

fun String.splitBySemicolonRespectingQuotes(): List<String> {
    return splitByDelimiterRespectingQuotes(this, ';')
}

fun String.splitByPipeRespectingQuotes(): List<String> {
    return splitByDelimiterRespectingQuotes(this, '|')
}

fun splitByDelimiterRespectingQuotes(input: String, delimiter: Char): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false

    for (i in input.indices) {
        val c = input[i]
        if (c == '\"') {
            inQuotes = !inQuotes
            current.append(c)
        } else if (c == delimiter && !inQuotes) {
            result.add(current.toString())
            current.clear()
        } else {
            current.append(c)
        }
    }

    result.add(current.toString())
    return result
}

fun removeCommandsFromText(text: String): String {
    val commands = listOf("book", "flashcard", "test", "startTeddyBearRain", "find_gif")
    val result = StringBuilder()
    var i = 0

    while (i < text.length) {
        var matched = false

        for (cmd in commands) {
            if (text.startsWith("$cmd(", i)) {
                i += cmd.length + 1
                var depth = 1
                while (i < text.length && depth > 0) {
                    when (text[i]) {
                        '(' -> depth++
                        ')' -> depth--
                    }
                    i++
                }
                matched = true
                break
            }
        }

        if (!matched) {
            result.append(text[i])
            i++
        }
    }

    return result.toString().replace(Regex("""\s{2,}"""), " ").trim()
}

fun extractBooks(context: Context, text: String): List<BookEntity> {
    val bookRegex =
        Regex("""book\("((?:\\"|[^"])*)",\s*"((?:\\"|[^"])*)",\s*"((?:\\"|[^"])*)",\s*"((?:\\"|[^"])*)"\)""")

    return bookRegex.findAll(text).map { matchResult ->
        val (title, description, link, status) = matchResult.destructured
        BookEntity(
            title = title.trim(),
            description = description.trim(),
            link = link.trim().takeIf { it.isNotEmpty() },
            status = BookStatus.fromKeyOrDisplay(status, context)
        )
    }.toList()
}

@Composable
fun BookEditBottomSheet(
    book: BookEntity,
    onUpdate: (BookEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var description by remember { mutableStateOf(book.description) }
    var link by remember { mutableStateOf(book.link ?: "") }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.edit_book), fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text(stringResource(R.string.link)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        var selectedStatus by remember { mutableStateOf(BookStatus.TO_READ) }

        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            ) {
                OutlinedTextField(
                    value = selectedStatus.displayName,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.status)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                BookStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        onClick = {
                            selectedStatus = status
                            expanded = false
                        },
                        text = { Text(status.displayName) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                onUpdate(
                    book.copy(
                        title = title,
                        description = description,
                        link = link.ifBlank { null },
                        status = selectedStatus
                    )
                )
                onDismiss()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.update))
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondary,
                    RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = stringResource(R.string.typing_indicator),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiModeSelectorSheet(
    viewModel: SettingsViewModel,
    currentMode: AiMode,
    onSelect: (AiMode) -> Unit,
    onDismiss: () -> Unit
) {
    val aiTaskAccessModeToggle by viewModel.aiTaskAccessModeToggle.collectAsState(initial = true)
    val pagerState = rememberPagerState(
        initialPage = AiMode.entries.indexOf(currentMode),
        pageCount = { AiMode.entries.size }
    )
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Text(
                text = stringResource(R.string.choose_your_friend),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.wrapContentHeight(),
                pageSpacing = 16.dp
            ) { page ->
                val mode = AiMode.entries[page]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp)
                        .graphicsLayer {
                            val pageOffset =
                                ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                            val scale = 1f - (0.1f * pageOffset)
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin.Center
                            alpha = 1f - (0.3f * pageOffset)
                        }
                        .clickable {
                            onSelect(mode)
                            onDismiss()
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mode == currentMode)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mode.picture)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.ai_picture),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = mode.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(mode.label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(mode.description),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.and_their_superpowers),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            ToggleCard(
                title = stringResource(R.string.allow_ai_to_manage_tasks),
                isChecked = aiTaskAccessModeToggle,
                onCheckedChange = {
                    viewModel.updateToggle(AI_TASK_ACCESS_PREFERENCE_KEY, it)
                },
                description = stringResource(R.string.allow_ai_to_manage_tasks_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun ToggleCard(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = null
            )
        }
    }
}

suspend fun searchGif(query: String): String? {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val response = client.get("https://tenor.googleapis.com/v2/search") {
        parameter("q", query)
        parameter("key", BuildVariables.TENOR_API_KEY)
        parameter("limit", 1)
        parameter("media_filter", "gif")
        parameter("contentfilter", "high")
    }

    val json = response.body<JsonObject>()
    val results = json["results"]?.jsonArray
    val gif = results?.getOrNull(0)?.jsonObject
    val media = gif?.get("media_formats")?.jsonObject
    val gifUrl = media?.get("gif")?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull

    return gifUrl
}

@Composable
fun GifImage(
    gifUrl: String,
    modifier: Modifier = Modifier
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(gifUrl)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun EmojiRainOverlayText(
    modifier: Modifier = Modifier,
    emojis: List<String>,
    duration: Int = 8000,
    dropDuration: Int = 2400,
    dropFrequency: Int = 500,
    emojiPerFlow: Int = 6
) {
    val scope = rememberCoroutineScope()
    val screenHeight = LocalWindowInfo.current.containerSize.height.dp
    val screenWidth = LocalWindowInfo.current.containerSize.width.dp

    val activeEmojis = remember { mutableStateListOf<AnimatedTextEmoji>() }

    LaunchedEffect(Unit) {
        val flows = duration / dropFrequency
        repeat(flows) {
            repeat(emojiPerFlow) {
                val emoji = emojis.random()
                val startX = Random().nextFloat() * screenWidth.value
                val sizeFactor = 1f + Random().nextGaussian().toFloat().coerceAtLeast(0f)

                val emojiData = AnimatedTextEmoji(
                    id = UUID.randomUUID().toString(),
                    emoji = emoji,
                    x = startX.dp,
                    fontSize = (20f * sizeFactor).coerceIn(16f, 36f).sp,
                    durationMillis = (dropDuration * (1f + (Random().nextFloat() - 0.5f) * 0.5f)).toInt()
                )
                activeEmojis.add(emojiData)

                scope.launch {
                    delay(emojiData.durationMillis.toLong())
                    activeEmojis.remove(emojiData)
                }
            }
            delay(dropFrequency.toLong())
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        activeEmojis.forEach { emoji ->
            val yOffset = remember { Animatable(0f) }

            LaunchedEffect(emoji.id) {
                yOffset.animateTo(
                    targetValue = screenHeight.value,
                    animationSpec = tween(
                        durationMillis = emoji.durationMillis,
                        easing = LinearEasing
                    )
                )
            }

            Text(
                text = emoji.emoji,
                fontSize = emoji.fontSize,
                modifier = Modifier
                    .offset(x = emoji.x, y = yOffset.value.dp)
                    .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
            )
        }
    }
}

data class AnimatedTextEmoji(
    val id: String,
    val emoji: String,
    val x: Dp,
    val fontSize: TextUnit,
    val durationMillis: Int
)