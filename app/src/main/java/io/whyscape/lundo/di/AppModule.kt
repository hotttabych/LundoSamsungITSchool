package io.whyscape.lundo.di

import io.whyscape.lundo.data.db.DiaryDatabase
import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.whyscape.lundo.common.PreferencesManager
import io.whyscape.lundo.data.db.AppDatabase
import io.whyscape.lundo.data.db.BookDao
import io.whyscape.lundo.data.db.CounterDao
import io.whyscape.lundo.data.db.FlashcardDao
import io.whyscape.lundo.data.db.MoodDao
import io.whyscape.lundo.data.db.TodoDao
import io.whyscape.lundo.data.db.UserDao
import io.whyscape.lundo.data.remote.QuoteApi
import io.whyscape.lundo.data.repository.BookRepositoryImpl
import io.whyscape.lundo.data.repository.FlashcardRepositoryImpl
import io.whyscape.lundo.data.repository.GetMoodHistoryUseCase
import io.whyscape.lundo.data.repository.MoodRepositoryImpl
import io.whyscape.lundo.data.repository.NoteRepositoryImpl
import io.whyscape.lundo.data.repository.ProxyNoteRepository
import io.whyscape.lundo.data.repository.QuoteRepositoryImpl
import io.whyscape.lundo.data.repository.SaveMoodUseCase
import io.whyscape.lundo.data.repository.TodoRepositoryImpl
import io.whyscape.lundo.data.repository.UserRepositoryImpl
import io.whyscape.lundo.domain.DataStoreManager
import io.whyscape.lundo.domain.repository.BookRepository
import io.whyscape.lundo.domain.repository.FlashcardRepository
import io.whyscape.lundo.domain.repository.MoodRepository
import io.whyscape.lundo.domain.repository.NoteRepository
import io.whyscape.lundo.domain.repository.QuoteRepository
import io.whyscape.lundo.domain.repository.TodoRepository
import io.whyscape.lundo.domain.repository.UserRepository
import io.whyscape.lundo.domain.usecase.AddTodoUseCase
import io.whyscape.lundo.domain.usecase.DeleteNote
import io.whyscape.lundo.domain.usecase.DeleteTodoUseCase
import io.whyscape.lundo.domain.usecase.DiaryUseCases
import io.whyscape.lundo.domain.usecase.GetAllNotes
import io.whyscape.lundo.domain.usecase.GetCounterUseCase
import io.whyscape.lundo.domain.usecase.GetQuoteUseCase
import io.whyscape.lundo.domain.usecase.GetTodosUseCase
import io.whyscape.lundo.domain.usecase.GetUserUseCase
import io.whyscape.lundo.domain.usecase.IncrementCounterUseCase
import io.whyscape.lundo.domain.usecase.InsertNote
import io.whyscape.lundo.domain.usecase.LogoutUseCase
import io.whyscape.lundo.domain.usecase.ToggleTodoCompletionUseCase
import io.whyscape.lundo.domain.usecase.UpdateUserUseCase
import io.whyscape.lundo.ui.viewModel.TodoViewModelFactory
import io.whyscape.lundo.ui.viewModel.UserViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideQuoteApi(client: HttpClient): QuoteApi {
        return QuoteApi(client)
    }

    @Provides
    @Singleton
    fun provideQuoteRepository(api: QuoteApi): QuoteRepository {
        return QuoteRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideGetQuoteUseCase(repository: QuoteRepository): GetQuoteUseCase {
        return GetQuoteUseCase(repository)
    }

    fun provideTodoDao(application: Application): TodoDao {
        return AppDatabase.getDatabase(application).todoDao()
    }

    fun provideCounterDao(application: Application): CounterDao {
        return AppDatabase.getDatabase(application).counterDao()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(application: Application): TodoRepository {
        return TodoRepositoryImpl(
            todoDao = provideTodoDao(application),
            counterDao = provideCounterDao(application)
        )
    }
    @Provides
    @Singleton
    fun provideGetTodosUseCase(application: Application): GetTodosUseCase {
        return GetTodosUseCase(provideTodoRepository(application))
    }
    @Provides
    @Singleton
    fun provideAddTodoUseCase(application: Application): AddTodoUseCase {
        return AddTodoUseCase(provideTodoRepository(application))
    }
    @Provides
    @Singleton
    fun provideToggleTodoCompletionUseCase(application: Application): ToggleTodoCompletionUseCase {
        return ToggleTodoCompletionUseCase(provideTodoRepository(application))
    }
    @Provides
    @Singleton
    fun provideDeleteTodoUseCase(application: Application): DeleteTodoUseCase {
        return DeleteTodoUseCase(provideTodoRepository(application))
    }
    @Provides
    @Singleton
    fun provideGetCounterUseCase(application: Application): GetCounterUseCase {
        return GetCounterUseCase(provideTodoRepository(application))
    }
    @Provides
    @Singleton
    fun provideIncrementCounterUseCase(application: Application): IncrementCounterUseCase {
        return IncrementCounterUseCase(provideTodoRepository(application))
    }

    fun provideTodoViewModelFactory(application: Application): TodoViewModelFactory {
        return TodoViewModelFactory(application)
    }

    fun provideUserDao(application: Application): UserDao {
        return AppDatabase.getDatabase(application).userDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(application: Application): UserRepository {
        return UserRepositoryImpl(provideUserDao(application))
    }
    @Provides
    @Singleton
    fun provideGetUserUseCase(application: Application): GetUserUseCase {
        return GetUserUseCase(provideUserRepository(application))
    }
    @Provides
    @Singleton
    fun provideLogoutUseCase(application: Application): LogoutUseCase {
        return LogoutUseCase(provideUserRepository(application))
    }
    @Provides
    @Singleton
    fun provideUpdateUserUseCase(application: Application): UpdateUserUseCase {
        return UpdateUserUseCase(provideUserRepository(application))
    }

    fun provideUserViewModelFactory(application: Application): UserViewModelFactory {
        return UserViewModelFactory(application)
    }

    fun provideFlashcardDao(application: Application): FlashcardDao {
        return AppDatabase.getDatabase(application).flashcardDao()
    }

    @Provides
    @Singleton
    fun provideFlashcardRepository(application: Application): FlashcardRepository {
        return FlashcardRepositoryImpl(provideFlashcardDao(application))
    }

    fun provideBookDao(application: Application): BookDao {
        return AppDatabase.getDatabase(application).bookDao()
    }

    @Provides
    @Singleton
    fun provideBookRepository(application: Application): BookRepository {
        return BookRepositoryImpl(provideBookDao(application))
    }

    @Provides
    @Singleton
    fun provideMoodRepository(application: Application): MoodRepository {
        return MoodRepositoryImpl(provideMoodDao(application))
    }

    fun provideMoodDao(application: Application): MoodDao {
        return AppDatabase.getDatabase(application).moodDao()
    }

    @Provides
    @Singleton
    fun provideSaveMoodUseCase(application: Application): SaveMoodUseCase {
        return SaveMoodUseCase(provideMoodRepository(application))
    }

    @Provides
    @Singleton
    fun provideGetMoodHistoryUseCase(application: Application): GetMoodHistoryUseCase {
        return GetMoodHistoryUseCase(provideMoodRepository(application))
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Named("aiTaskAccess")
    fun provideAiTaskAccess(@ApplicationContext context: Context): Boolean {
        return runBlocking {
            provideDataStoreManager(context).aiTaskAccessToggleFlow.first()
        }
    }

    private var diaryDbInstance: DiaryDatabase? = null
    private var noteRepoInstance: NoteRepositoryImpl? = null

    @Provides
    @Singleton
    fun provideNoteRepository(): NoteRepository? {
        return noteRepoInstance
    }

    @Provides
    @Singleton
    fun provideProxyNoteRepository(): ProxyNoteRepository = ProxyNoteRepository()

    @Provides
    @Singleton
    fun provideNoteUseCases(proxyRepo: ProxyNoteRepository): DiaryUseCases {
        return DiaryUseCases(
            getAllNotes = GetAllNotes(proxyRepo),
            insertNote = InsertNote(proxyRepo),
            deleteNote = DeleteNote(proxyRepo)
        )
    }

    fun initSecureDb(context: Context, passphrase: ByteArray) {
        if (diaryDbInstance == null) {
            diaryDbInstance = DiaryDatabase.create(context, passphrase)
            noteRepoInstance = NoteRepositoryImpl(diaryDbInstance!!.noteDao())
        }
    }

    fun clearSecureDb() {
        diaryDbInstance?.close()
        diaryDbInstance = null
        noteRepoInstance = NoteRepositoryImpl(null)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}