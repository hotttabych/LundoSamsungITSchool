package io.whyscape.lundo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.whyscape.lundo.R
import io.whyscape.lundo.ui.components.QuoteCard.QuoteCard
import io.whyscape.lundo.ui.viewModel.MoodTrackerViewModel
import io.whyscape.lundo.ui.viewModel.QuoteViewModel
import io.whyscape.lundo.ui.viewModel.TodoViewModel
import io.whyscape.lundo.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    quoteViewModel: QuoteViewModel,
    todoViewModel: TodoViewModel,
    userViewModel: UserViewModel,
    moodTrackerViewModel: MoodTrackerViewModel,
    navController: NavController
) {
    val quote by quoteViewModel.quote.collectAsState()

    val context = LocalContext.current

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var newTask by remember { mutableStateOf("") }
    val todos by todoViewModel.todos.collectAsState(initial = emptyList())
    val completeTodos = todos.filter { it.isCompleted }
    val todoCounter by todoViewModel.completionCounter.collectAsState(initial = 0)
    val user by userViewModel.user.collectAsState(initial = null)

    val language = getCurrentAppLanguage()

    if (sharedPreferences.getBoolean("firstLaunch", true)) {
        if (todos.isEmpty()) {
            val defaultTasks = when (language) {
                "ru" -> listOf(
                    "Утренняя зарядка — 10 мин",
                    "Рефлексия — 3 предложения",
                    "Прочитать любую статью в Википедии",
                    "Попробовать что-то новое",
                    "Остановиться и побыть в моменте",
                    "Выучить 5 новых слов на иностранном языке",
                    "Прогуляться хотя бы 15 минут",
                    "Поставить 3 вещи на свои места",
                    "Выпить стакан воды",
                    "Написать 1 идею, как сделать мир лучше"
                )
                else -> listOf( // fallback to English
                    "Morning exercise — 10 minutes",
                    "Reflection — write 3 sentences",
                    "Read any Wikipedia article",
                    "Try something new",
                    "Pause and be in the moment",
                    "Learn 5 new foreign words",
                    "Go for a 15-minute walk",
                    "Put 3 items back to their place",
                    "Drink a glass of water",
                    "Write down 1 idea to improve the world"
                )
            }

            defaultTasks.forEach { task ->
                todoViewModel.addTodo(task)
            }
        }
        sharedPreferences.edit { putBoolean("firstLaunch", false) }
    }

    LaunchedEffect(Unit) {
        quoteViewModel.loadQuote(language)
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxWidth(),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    user?.photoUri?.let { photoUri ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.profile_photo),
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = stringResource(R.string.hi, user?.name.toString()),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.your_todays_progress),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val progress = if (todos.isNotEmpty()) {
                            completeTodos.count().toFloat() / todos.count()
                        } else {
                            0f
                        }

                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = tween(durationMillis = 500),
                            label = "Animated Progress"
                        )

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(18.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            if (progress > 0f) {
                                stringResource(
                                    R.string.your_todays_progress_content,
                                    completeTodos.count(),
                                    todos.count()
                                )
                            } else {
                                stringResource(R.string.your_todays_progress_content_empty)
                            },
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                MoodSummaryCard(
                    modifier = Modifier.fillMaxWidth(),
                    moodTrackerViewModel = moodTrackerViewModel,
                    onClick = { navController.navigate("mood") }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val focus = getTodayFocus(language)
                        Text(
                            stringResource(R.string.focus_of_the_day),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            modifier = Modifier.align(Alignment.Start),
                            text = focus.title,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(focus.description, fontSize = 14.sp)
                    }
                }
            }

            item {
                QuoteCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    quoteText = quote?.quoteText,
                    quoteAuthor = quote?.quoteAuthor,
                    quoteLink = quote?.quoteLink,
                    buttonCallback = { quoteViewModel.loadQuote(language) }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.insight_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (todoCounter <= 0) stringResource(
                                R.string.task_insight_empty
                            ) else stringResource(
                                R.string.task_insight, todoCounter
                            ), fontSize = 14.sp
                        )
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.todays_journey),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTask,
                        onValueChange = { newTask = it },
                        label = { Text("New Task") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTask.isNotBlank()) {
                                todoViewModel.addTodo(newTask)
                                newTask = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }

            items(todos) { todo ->
                TodoItem(
                    todo = todo,
                    onToggle = {
                        todoViewModel.toggleTodoCompletion(todo)
                        user?.let { user ->
                            userViewModel.updateUser(user.copy(lastUsageTimestamp = System.currentTimeMillis()))
                        }
                    },
                    onDelete = { todoViewModel.deleteTodo(todo) }
                )
            }

            item {
                InstantGrowthButton(language) { challenge ->
                    todoViewModel.addTodo(challenge.task)
                }
            }
        }
    }
}

@Composable
fun MoodSummaryCard(
    modifier: Modifier = Modifier,
    moodTrackerViewModel: MoodTrackerViewModel,
    onClick: () -> Unit
) {
    val moodHistory by moodTrackerViewModel.moodHistory.collectAsState(initial = emptyList())

    val medianMood = moodHistory.map { it.mood }.sorted().let { moods ->
        when {
            moods.isEmpty() -> null
            moods.size % 2 == 1 -> moods[moods.size / 2]
            else -> (moods[moods.size / 2] + moods[moods.size / 2 - 1]) / 2
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.your_median_mood),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            medianMood?.let {
                val emojiLabel = Mood.getEmojiLabel(it)
                val moodDescription = Mood.getDescription(it)

                Column {
                    Text(
                        text = emojiLabel,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = moodDescription, fontSize = 14.sp)

                }
            } ?: Text(
                text = stringResource(R.string.not_enough_mood_data),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onClick() },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.open_mood_tracker), fontWeight = FontWeight.Bold)
            }
        }
    }
}

enum class Mood(
    val emoji: String,
    val labels: Map<String, String>,
    val description: Map<String, String>
) {
    HAPPY(
        "😄",
        mapOf("en" to "Happy", "ru" to "Счастливое"),
        mapOf(
            "en" to "You're feeling joyful and full of light. The world is smiling back at you.",
            "ru" to "Ты ощущаешь радость и внутренний свет. Мир улыбается тебе в ответ."
        )
    ),
    CONTENT(
        "😊",
        mapOf("en" to "Content", "ru" to "Довольное"),
        mapOf(
            "en" to "You're calm and grounded. Everything feels just right.",
            "ru" to "Ты спокоен и уравновешен. Всё как надо."
        )
    ),
    NEUTRAL(
        "😐",
        mapOf("en" to "Neutral", "ru" to "Нейтральное"),
        mapOf(
            "en" to "You're in balance. Neither high nor low — simply existing peacefully.",
            "ru" to "Ты в балансе. Ни вверх, ни вниз — просто спокойное бытие."
        )
    ),
    SAD(
        "😞",
        mapOf("en" to "Sad", "ru" to "Грустное"),
        mapOf(
            "en" to "Something's weighing on your heart. It’s okay to slow down and feel it.",
            "ru" to "На сердце тяжесть. Позволь себе притормозить и прожить это чувство."
        )
    ),
    CRYING(
        "😭",
        mapOf("en" to "Crying", "ru" to "Очень грустное"),
        mapOf(
            "en" to "You’re overwhelmed. Let it out — tears are the soul's way to breathe.",
            "ru" to "Слишком многое навалилось. Слёзы — это дыхание души, не сдерживай их."
        )
    ),
    UNKNOWN(
        "❓",
        mapOf("en" to "Unknown", "ru" to "Неизвестное"),
        mapOf(
            "en" to "We couldn’t read your mood. But every feeling matters.",
            "ru" to "Не удалось распознать настроение. Но каждое чувство имеет значение."
        )
    );

    companion object {
        fun fromInt(index: Int): Mood = entries.getOrNull(index) ?: UNKNOWN

        fun getEmojiLabel(mood: Int): String {
            val moodEnum = fromInt(mood)
            val lang = Locale.getDefault().language.take(2)
            val label = moodEnum.labels[lang] ?: moodEnum.labels["en"] ?: "Unknown"
            return "${moodEnum.emoji} $label"
        }

        fun getDescription(mood: Int): String {
            val moodEnum = fromInt(mood)
            val lang = Locale.getDefault().language.take(2)
            return moodEnum.description[lang] ?: moodEnum.description["en"] ?: ""
        }
    }
}

data class Focus(val title: String, val description: String)

fun getTodayFocus(language: String = "ru"): Focus {
    val focusListRu = listOf(
        "Медитация",
        "Физическая активность",
        "Чтение",
        "Планирование",
        "Самоанализ",
        "Обучение",
        "Благодарность",
        "Отключение от цифрового",
        "Общение",
        "Здоровье",
        "Сон",
        "Уборка",
        "Творчество",
        "Альтруизм"
    )

    val focusListEn = listOf(
        "Meditation",
        "Physical activity",
        "Reading",
        "Planning",
        "Self-reflection",
        "Learning",
        "Gratitude",
        "Digital detox",
        "Social connection",
        "Health",
        "Sleep",
        "Cleaning",
        "Creativity",
        "Altruism"
    )

    val descriptionsEn = listOf(
        "Practice 10 minutes of mindfulness or breathing exercises.",
        "Do at least 20 minutes of physical activity.",
        "Read at least 10 pages of a meaningful book.",
        "Spend time planning your day or week.",
        "Reflect on your actions and feelings today.",
        "Learn something new—read an article or watch a tutorial.",
        "Write down 3 things you're grateful for.",
        "Spend at least 1 hour without a screen.",
        "Have a meaningful conversation with someone.",
        "Focus on eating clean and drinking enough water.",
        "Try to get 8 hours of quality sleep.",
        "Clean up your workspace or home.",
        "Draw, write, or engage in something artistic.",
        "Help someone without expecting anything in return."
    )

    val descriptionsRu = listOf(
        "Позанимайся 10 минут медитацией или дыхательными практиками.",
        "Позанимайся физической активностью не менее 20 минут.",
        "Прочитай минимум 10 страниц полезной книги.",
        "Запланируй свой день или неделю.",
        "Проанализируй свои действия и чувства.",
        "Узнай что-то новое — прочитайте статью или посмотрите обучающее видео.",
        "Запиши 3 вещи, за которые вы благодарны.",
        "Проведи хотя бы час без экрана.",
        "Поговори с кем-то по душам.",
        "Сосредоточься на здоровом питании и питьевом режиме.",
        "Постарайся выспаться — не менее 8 часов.",
        "Убери рабочее место или дом.",
        "Позанимайся творчеством: нарисуйте, напишите, придумайте.",
        "Помоги кому-то без ожидания награды."
    )

    val list = if (language == "en") focusListEn else focusListRu
    val descriptions = if (language == "en") descriptionsEn else descriptionsRu

    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_YEAR)
    val year = calendar.get(Calendar.YEAR)
    val hash = year * 1000 + day // уникальный int для каждого дня

    val index = (hash % list.size).let { if (it < 0) it + list.size else it }

    return Focus(
        title = list[index],
        description = descriptions[index]
    )
}

data class Challenge(val title: String, val description: String, val task: String)

enum class ChallengeEnum(
    val titleRu: String,
    val descriptionRu: String,
    val infinitiveRu: String,
    val titleEn: String,
    val descriptionEn: String
) {
    WAKE_EARLIER(
        "Проснись на час раньше",
        "Начни день раньше, чтобы получить больше времени для себя.",
        "Проснуться на час раньше",
        "Wake up 1 hour earlier",
        "Start your day earlier and gain more time for yourself."
    ),
    NO_SOCIAL_MEDIA(
        "Не используй соцсети сегодня",
        "Полный цифровой детокс — проверь, насколько это освобождает.",
        "Не использовать соцсети сегодня",
        "No social media today",
        "A full digital detox — feel the freedom."
    ),
    DO_PUSHUPS(
        "Сделай 30 отжиманий",
        "Физическая нагрузка улучшит тонус и настроение.",
        "Сделать 30 отжиманий",
        "Do 30 push-ups",
        "Physical exercise will boost your energy and mood."
    ),
    WRITE_THANK_YOU(
        "Напиши благодарственное письмо",
        "Вырази благодарность кому-то — это приятно и полезно.",
        "Написать благодарственное письмо",
        "Write a thank-you letter",
        "Express gratitude — it helps both you and others."
    ),
    WALK_STEPS(
        "Пройди 10,000 шагов",
        "Ходьба — это просто, но мощно для тела и мозга.",
        "Пройти 10,000 шагов",
        "Walk 10,000 steps",
        "Walking is simple but powerful for body and brain."
    ),
    READ_BOOK(
        "Прочти 1 главу книги",
        "Выбери вдохновляющую книгу и прочти хотя бы одну главу.",
        "Прочитать 1 главу книги",
        "Read 1 book chapter",
        "Pick an inspiring book and read one chapter."
    ),
    PLAN_TOMORROW(
        "Запланируй завтрашний день",
        "Планирование снижает тревожность и повышает эффективность.",
        "Запланировать завтрашний день",
        "Plan your tomorrow",
        "Planning reduces stress and boosts clarity."
    ),
    TRY_SOMETHING_NEW(
        "Сделай что-то новое",
        "Выйди за рамки — сделай что-то необычное.",
        "Сделать что-то новое",
        "Do something new",
        "Break your routine — try something different."
    ),
    JOURNAL(
        "Веди дневник 10 минут",
        "Опиши, что ты чувствуешь, думаешь, хочешь.",
        "Вести дневник 10 минут",
        "Journal for 10 minutes",
        "Describe how you feel, what you think, and what you want."
    ),
    CLEAN_SPOT(
        "Устрой себе мини-уборку",
        "Уберись в одной зоне — это моментально очистит и разум.",
        "Устроить себе мини-уборку",
        "Clean a small space",
        "Clean just one area — your mind will thank you too."
    );

    fun getChallenge(language: String): Challenge {
        return if (language == "en") {
            Challenge(titleEn, descriptionEn, titleEn)
        } else {
            Challenge(titleRu, descriptionRu, infinitiveRu)
        }
    }
}

fun getRandomChallenge(language: String = "ru"): Challenge {
    val randomChallenge = ChallengeEnum.entries.toTypedArray().random()
    return randomChallenge.getChallenge(language)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantGrowthButton(
    language: String = "ru",
    onAddToTasks: (Challenge) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var currentChallenge by remember { mutableStateOf(getRandomChallenge(language)) }
    var isSheetOpen by remember { mutableStateOf(false) }


    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    isSheetOpen = false
                }
            },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = currentChallenge.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = currentChallenge.description,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = {
                        onAddToTasks(currentChallenge)
                        coroutineScope.launch {
                            sheetState.hide()
                            isSheetOpen = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.add_to_the_task_list),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Button(
        onClick = {
            currentChallenge = getRandomChallenge(language)
            isSheetOpen = true
            coroutineScope.launch { sheetState.show() }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(stringResource(R.string.instant_growth), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun getCurrentAppLanguage(): String {
    val configuration = LocalConfiguration.current
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        configuration.locales[0].language
    } else {
        @Suppress("DEPRECATION")
        configuration.locale.language
    }
}