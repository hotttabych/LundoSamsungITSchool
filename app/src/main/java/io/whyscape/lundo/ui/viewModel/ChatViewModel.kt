package io.whyscape.lundo.ui.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.whyscape.lundo.common.PreferencesManager
import io.whyscape.lundo.data.db.BookStatus
import io.whyscape.lundo.data.remote.streamGeminiResponse
import io.whyscape.lundo.domain.model.AiMode
import io.whyscape.lundo.domain.model.ChatMessage
import io.whyscape.lundo.domain.model.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatViewModel @Inject constructor(@Named("aiTaskAccess") private val isTaskAccessAllowed: Boolean, private val prefs: PreferencesManager) :
    ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _selectedMode = MutableStateFlow(prefs.getAiMode())
    val selectedMode: StateFlow<AiMode> = _selectedMode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setMode(mode: AiMode) {
        _selectedMode.value = mode
        prefs.saveAiMode(mode)
    }

    fun pushMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    fun sendMessageStreamed(
        context: Context,
        userText: String,
        todoList: List<Todo>,
        todoViewModel: TodoViewModel,
        flashcardViewModel: FlashcardViewModel,
        bookViewModel: BookViewModel,
        fileBytes: ByteArray? = null,
        mimeType: String? = null,
        asUser: Boolean = true
    ) {
        val systemPrompt = buildFullSystemPrompt(todoList, _selectedMode.value)

        val updatedMessages = _messages.value + ChatMessage("user", userText)

        if (asUser) {
            _messages.value = updatedMessages
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                streamGeminiResponse(
                    context = context,
                    messages = updatedMessages,
                    systemPrompt = systemPrompt,
                    fileBytes = fileBytes,
                    mimeType = mimeType
                ).collect { aiText ->
                    val commands = extractTodoCommands(aiText.first)

                    commands.forEach { command ->
                        when (command.action) {
                            "add_task" -> todoViewModel.addTodo(command.arguments[0])
                            "delete_task" -> todoList.find {
                                it.task.contains(
                                    command.arguments[0],
                                    true
                                )
                            }
                                ?.let { todoViewModel.deleteTodo(it) }

                            "complete_task" -> todoList.find {
                                it.task.contains(
                                    command.arguments[0],
                                    true
                                )
                            }
                                ?.let { todoViewModel.toggleTodoCompletion(it) }

                            "flashcard" -> {
                                val (q, a) = command.arguments
                                if (q.isNotBlank() && a.isNotBlank()) {
                                    flashcardViewModel.addFlashcard(q, a)
                                    Log.i("ChatViewModel", "Flashcard added: $q -> $a")
                                }
                            }

                            "book" -> {
                                val (title, desc, link, status) = command.arguments
                                bookViewModel.addBook(
                                    title,
                                    desc,
                                    link,
                                    BookStatus.fromDisplayName(status)
                                )
                                Log.i("ChatViewModel", "Book added: $title — $status")
                            }
                        }
                    }

                    val cleanedText = removeCommandsFromText(aiText.first)

                    if (cleanedText.isNotBlank()) {
                        _messages.value = _messages.value + ChatMessage(
                            "model",
                            cleanedText,
                            null,
                            aiText.second
                        )
                    }
                }
            } catch (e: ConnectException) {
                throw e
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resendPreviousUserMessage(
        context: Context,
        todoList: List<Todo>,
        todoViewModel: TodoViewModel,
        flashcardViewModel: FlashcardViewModel,
        bookViewModel: BookViewModel
    ) {
        val messagesList = _messages.value
        // ищем последнее неудачное сообщение от модели
        val failedIndex = messagesList.indexOfLast { it.role == "model" && !it.isSuccessful }
        if (failedIndex <= 0) return

        // ищем последнее сообщение пользователя перед ним
        val previousUserMessage = messagesList.subList(0, failedIndex)
            .lastOrNull { it.role == "user" } ?: return

        sendMessageStreamed(
            context = context,
            userText = previousUserMessage.text,
            todoList = todoList,
            todoViewModel = todoViewModel,
            flashcardViewModel = flashcardViewModel,
            bookViewModel = bookViewModel,
            asUser = false
        )
    }

    fun buildFullSystemPrompt(tasks: List<Todo>, aiMode: AiMode): String {
        val taskList = tasks.joinToString("\n") {
            "- ${it.task} [${if (it.isCompleted) "✓ Выполнено" else "⏳ В процессе"}]"
        }

        return """
        ${aiMode.systemPrompt}

        ${
            if (isTaskAccessAllowed)
                """вот список текущих задач пользователя:
            $taskList

            в диалоге ты можешь управлять приложением с помощью команд:
            - добавить задачу: add_task("текст задачи")
            - завершить задачу: complete_task("текст задачи")
            - удалить задачу: delete_task("текст задачи")
            - создать флеш-карточку (если в разговоре появляется тема, которую стоит запомнить): flashcard("вопрос", "ответ")
            - добавить книгу в список чтения: book("название", "описание", "ссылка вида https://www.ecosia.org/search?q=название книги", "статус")
            статусы книг: "хочу прочитать", "читаю", "прочитано".
            - создать тест: test(Название теста, Вопрос 1|Ответ 1, Ответ 2*, Ответ 3|Пояснение к вопросу 1; Вопрос 2|...|...; ...)
            - если захочешь, даже запустить дождь из плюшевых мишек на весь экран (но не переусердствуй): startTeddyBearRain()
            - вставить гифку в сообщение: find_gif("твой запрос") (например: find_gif("милые котики"))"""
            else
                """в диалоге ты можешь управлять приложением с помощью команд:
            - создать флеш-карточку (если в разговоре появляется тема, которую стоит запомнить): flashcard("вопрос", "ответ")
            - добавить книгу в список чтения: book("название", "описание", "ссылка вида https://www.ecosia.org/search?q=название книги", "статус")
            статусы книг: "хочу прочитать", "читаю", "прочитано".
            - создать тест: test(Название теста, Вопрос 1|Ответ 1, Ответ 2*, Ответ 3|Пояснение к вопросу 1; Вопрос 2|...|...; ...)
            - если захочешь, даже запустить дождь из плюшевых мишек на весь экран (но не переусердствуй): startTeddyBearRain()
            - вставить гифку в сообщение: find_gif("твой запрос") (например: find_gif("милые котики"))"""
        }

        обязательные правила тестов:
        - вопросы разделяются знаком `;`
        - минимум 3 вопроса или больше
        - у каждого вопроса должно быть:
          - сам текст вопроса
          - список из минимум 2–4 вариантов ответа (один из которых правильный, он помечается `*`)
          - пояснение к ответу, отделяется символом `|`
        - используй `*`, чтобы отметить правильный ответ
        - пояснение помогает пользователю понять, почему ответ верный или неверный

        пример теста:
        test("Основы физики", "Что такое ускорение?"|"Скорость", "Изменение скорости"*, "Давление"|"Ускорение — это изменение скорости во времени")

        ты можешь вставлять такие команды в любой части текста. lundo самостоятельно их обработает и отобразит интерактивный тест.

        книгу ищи с помощью инструмента googleSearchTool. ссылка **обязательно** должна быть рабочей.

        команды не отображаются пользователю. пользователь видит результат: карточки, задачи, книги, красиво оформленные под твоими словами.

        текущее время: ${getCurrentTime()}.
    """.trimIndent()
    }

    data class TodoCommand(val action: String, val arguments: List<String>)

    fun extractTodoCommands(text: String): List<TodoCommand> {
        val commands = mutableListOf<TodoCommand>()

        // Задачи
        val taskPattern = Regex("""(add_task|delete_task|complete_task)\("(.+?)"\)""")
        taskPattern.findAll(text).forEach {
            commands.add(TodoCommand(it.groupValues[1], listOf(it.groupValues[2])))
        }

        // Флеш-карточки
        val flashcardPattern = Regex("""flashcard\("(.+?)",\s*"(.+?)"\)""")
        flashcardPattern.findAll(text).forEach {
            commands.add(TodoCommand("flashcard", listOf(it.groupValues[1], it.groupValues[2])))
        }

        // Книги
        val bookPattern = Regex("""book\("(.+?)",\s*"(.+?)",\s*"(.+?)",\s*"(.+?)"\)""")
        bookPattern.findAll(text).forEach {
            commands.add(
                TodoCommand(
                    "book",
                    listOf(
                        it.groupValues[1],
                        it.groupValues[2],
                        it.groupValues[3],
                        it.groupValues[4]
                    )
                )
            )
        }

        return commands
    }

    fun removeCommandsFromText(text: String): String {
        val pattern = Regex("""\b(?:add_task|delete_task|complete_task)\s*\(.*?\)""")
        return text.replace(pattern, "")
    }

    fun getCurrentTime(): String {
        val calendar =
            Calendar.getInstance()
        val timeFormat =
            SimpleDateFormat(
                "HH;mm",
                Locale.getDefault()
            )
        return timeFormat.format(calendar.time)
    }
}