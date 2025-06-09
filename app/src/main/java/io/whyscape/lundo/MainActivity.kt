package io.whyscape.lundo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.whyscape.lundo.di.AppModule
import io.whyscape.lundo.ui.components.AiChatScreen
import io.whyscape.lundo.ui.components.DiaryTabContent
import io.whyscape.lundo.ui.components.HomeScreen
import io.whyscape.lundo.ui.components.LibraryScreen
import io.whyscape.lundo.ui.components.MoodScreen
import io.whyscape.lundo.ui.components.NotesScreen
import io.whyscape.lundo.ui.components.SignUpScreen
import io.whyscape.lundo.ui.theme.LundoTheme
import io.whyscape.lundo.ui.viewModel.BookViewModel
import io.whyscape.lundo.ui.viewModel.ChatViewModel
import io.whyscape.lundo.ui.viewModel.DiaryViewModel
import io.whyscape.lundo.ui.viewModel.FlashcardViewModel
import io.whyscape.lundo.ui.viewModel.MoodTrackerViewModel
import io.whyscape.lundo.ui.viewModel.QuoteViewModel
import io.whyscape.lundo.ui.viewModel.SettingsViewModel
import io.whyscape.lundo.ui.viewModel.TodoViewModel
import io.whyscape.lundo.ui.viewModel.UserViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val quoteViewModel: QuoteViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()
    private val flashcardViewModel: FlashcardViewModel by viewModels()
    private val moodTrackerViewModel: MoodTrackerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val diaryViewModel: DiaryViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels {
        AppModule.provideUserViewModelFactory(application)
    }
    private val todoViewModel: TodoViewModel by viewModels {
        AppModule.provideTodoViewModelFactory(application)
    }

    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { keepSplashScreen }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            userViewModel.user.collect {
                keepSplashScreen = false
            }
        }

        setContent {
            LundoTheme {
                val user by userViewModel.user.collectAsState(initial = null)

                if (user == null) {
                    SignUpScreen(
                        onRegistrationComplete = {},
                        userViewModel = userViewModel
                    )
                } else {
                    MainScreen(
                        quoteViewModel,
                        todoViewModel,
                        flashcardViewModel,
                        bookViewModel,
                        userViewModel,
                        chatViewModel,
                        moodTrackerViewModel,
                        settingsViewModel,
                        diaryViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    quoteViewModel: QuoteViewModel,
    todoViewModel: TodoViewModel,
    flashcardViewModel: FlashcardViewModel,
    bookViewModel: BookViewModel,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    moodTrackerViewModel: MoodTrackerViewModel,
    settingsViewModel: SettingsViewModel,
    diaryViewModel: DiaryViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars),
        bottomBar = {
            BottomNavigationBar(navController)
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    quoteViewModel,
                    todoViewModel,
                    userViewModel,
                    moodTrackerViewModel,
                    navController
                )
            }
            composable("ai_chat") {
                AiChatScreen(
                    chatViewModel,
                    todoViewModel,
                    flashcardViewModel,
                    bookViewModel,
                    settingsViewModel
                )
            }
            composable("library") { LibraryScreen(flashcardViewModel, bookViewModel) }
            composable("mood") {
                MoodScreen(moodTrackerViewModel = moodTrackerViewModel)
            }
            composable("diary") {
                DiaryTabContent(diaryViewModel)
            }
            composable("diary_notes") {
                NotesScreen(diaryViewModel)
            }
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    val navController = rememberNavController()
    BottomNavigationBar(navController = navController)
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            stringResource(R.string.home),
            "home",
            painterResource(R.drawable.sofa_bold_duotone),
            painterResource(R.drawable.sofa_line_duotone)
        ),
        BottomNavItem(
            stringResource(R.string.mood),
            "mood",
            painterResource(R.drawable.smile_circle_bold_duotone),
            painterResource(R.drawable.smile_circle_line_duotone)
        ),
        BottomNavItem(
            stringResource(R.string.ai_chat),
            "ai_chat",
            painterResource(R.drawable.stars_bold_duotone),
            painterResource(R.drawable.stars_line_duotone)
        ),
        BottomNavItem(
            stringResource(R.string.library),
            "library",
            painterResource(R.drawable.book_bold_duotone),
            painterResource(R.drawable.book_line_duotone)
        ),
        BottomNavItem(
            stringResource(R.string.diary_title),
            "diary",
            painterResource(R.drawable.notebook_bookmark_bold_duotone),
            painterResource(R.drawable.notebook_bookmark_line_duotone)
        )
    )
    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, item ->
            val isSelected = currentRoute == item.route
            val isAiChatItem = item.route == "ai_chat"

            NavigationBarItem(
                icon = {
                    if (isAiChatItem) {
                        Box(
                            modifier = Modifier
                                .size(width = 84.dp, height = 48.dp)
                                .clip(RoundedCornerShape(percent = 64))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = if (isSelected) item.selectedPainter else item.unselectedPainter,
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Crossfade(targetState = isSelected) { selected ->
                            Icon(
                                painter = if (selected) item.selectedPainter else item.unselectedPainter,
                                contentDescription = item.title
                            )
                        }
                    }
                },
                label = if (!isAiChatItem) {
                    {
                        Text(
                            text = item.title,
                            fontSize = 8.sp
                        )
                    }
                } else {
                    null
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = if (isAiChatItem)
                    NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                else
                    NavigationBarItemDefaults.colors()
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedPainter: Painter,
    val unselectedPainter: Painter
)