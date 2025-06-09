package io.whyscape.lundo.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import io.whyscape.lundo.R
import io.whyscape.lundo.data.db.BookEntity
import io.whyscape.lundo.data.db.FlashcardEntity
import io.whyscape.lundo.ui.viewModel.BookViewModel
import io.whyscape.lundo.ui.viewModel.FlashcardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    flashCardViewModel: FlashcardViewModel,
    bookViewModel: BookViewModel
) {
    val flashcards by flashCardViewModel.flashcards.collectAsState(initial = emptyList())
    val books by bookViewModel.books.collectAsState(initial = emptyList())

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sheetContentType by remember { mutableStateOf<BottomSheetType?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.library),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            item {
                Text(stringResource(R.string.cards), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                if (flashcards.isNotEmpty()) {
                    LazyRow {
                        items(flashcards.take(10)) { card ->
                            FlashcardPreview(
                                card, Modifier
                                    .width(250.dp)
                                    .padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            sheetContentType = BottomSheetType.FLASHCARDS
                            coroutineScope.launch { sheetState.show() }
                        }
                    ) {
                        Text(stringResource(R.string.show_all_flashcards))
                    }
                } else {
                    PlaceholderText(stringResource(R.string.it_is_empty_here_yet_flashcards))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(stringResource(R.string.books), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                if (books.isNotEmpty()) {
                    LazyRow {
                        items(books.take(10)) { book ->
                            BookPreview(
                                book, Modifier
                                    .width(250.dp)
                                    .padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            sheetContentType = BottomSheetType.BOOKS
                            coroutineScope.launch { sheetState.show() }
                        }
                    ) {
                        Text(stringResource(R.string.show_all_books))
                    }
                } else {
                    PlaceholderText(stringResource(R.string.it_is_empty_here_yet))
                }
            }
        }

        sheetContentType?.let { type ->
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetState.hide()
                        sheetContentType = null
                    }
                },
                sheetState = sheetState
            ) {
                var bookToEdit by remember { mutableStateOf<BookEntity?>(null) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                bookToEdit?.let { book ->
                    ModalBottomSheet(
                        onDismissRequest = { bookToEdit = null },
                        sheetState = sheetState
                    ) {
                        BookEditBottomSheet(
                            book = book,
                            onUpdate = {
                                bookViewModel.updateBook(it)
                                bookToEdit = null
                            },
                            onDismiss = { bookToEdit = null }
                        )
                    }
                }

                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    val filteredFlashcards = flashcards.filter {
                        it.question.contains(searchQuery, ignoreCase = true) ||
                                it.answer.contains(searchQuery, ignoreCase = true)
                    }

                    val filteredBooks = books.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true) ||
                                it.status.displayName.contains(searchQuery, ignoreCase = true)
                    }

                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            label = {
                                Text(
                                    if (type == BottomSheetType.FLASHCARDS) stringResource(R.string.flashcards_search) else stringResource(
                                        R.string.book_search
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.magnifer_line_duotone),
                                    contentDescription = null
                                )
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    when (type) {
                        BottomSheetType.FLASHCARDS -> {
                            item {
                                Text(
                                    stringResource(R.string.all_flashcards),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (filteredFlashcards.isNotEmpty()) {
                                items(filteredFlashcards) { card ->
                                    FlashcardPreview(
                                        card,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        onDelete = {
                                            coroutineScope.launch {
                                                flashCardViewModel.deleteFlashcard(card)
                                            }
                                        }
                                    )
                                }
                            } else {
                                item {
                                    Text(stringResource(R.string.nothing_found), color = Color.Gray)
                                }
                            }
                        }

                        BottomSheetType.BOOKS -> {
                            item {
                                Text(
                                    stringResource(R.string.all_books),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (filteredBooks.isNotEmpty()) {
                                items(filteredBooks) { book ->
                                    BookPreview(
                                        book = book,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        onDelete = {
                                            coroutineScope.launch {
                                                bookViewModel.deleteBook(book)
                                            }
                                        },
                                        onEdit = {
                                            bookToEdit = book
                                        }
                                    )
                                }
                            } else {
                                item {
                                    Text(stringResource(R.string.nothing_found), color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardPreview(
    card: FlashcardEntity,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    var showAnswer by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
            .clickable { showAnswer = !showAnswer }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                card.question,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (showAnswer) {
                Text(card.answer, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    stringResource(R.string.touch_to_reveal_the_answer),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(painterResource(R.drawable.trash_bin_bold_duotone), contentDescription = stringResource(
                            R.string.delete
                        ), tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun BookPreview(
    book: BookEntity,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(book.description, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(stringResource(R.string.book_status, book.status.displayName), fontStyle = FontStyle.Italic, fontSize = 12.sp)

            book.link?.let {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.find_on_the_internet))
                }
            }

            if (onDelete != null || onEdit != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                painter = painterResource(R.drawable.pen_bold_duotone),
                                contentDescription = stringResource(R.string.edit_button)
                            )
                        }
                    }
                    if (onDelete != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete) {
                            Icon(
                                painter = painterResource(R.drawable.trash_bin_bold_duotone),
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderText(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}

enum class BottomSheetType {
    FLASHCARDS, BOOKS
}
