package io.whyscape.lundo.ui.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.whyscape.lundo.data.db.AppDatabase
import io.whyscape.lundo.data.db.UserDao
import io.whyscape.lundo.data.db.UserEntity
import io.whyscape.lundo.di.AppModule
import io.whyscape.lundo.domain.usecase.GetUserUseCase
import io.whyscape.lundo.domain.usecase.LogoutUseCase
import io.whyscape.lundo.domain.usecase.UpdateUserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class UserState {
    object Loading : UserState()
    object Empty : UserState()
    data class Success(val user: UserEntity) : UserState()
}

class UserViewModel(
    getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userDao: UserDao
) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    val user: Flow<UserEntity?> = getUserUseCase()

    init {
        viewModelScope.launch {
            val existingUser = userDao.getUser().first()
            _userState.value = if (existingUser != null) {
                UserState.Success(existingUser)
            } else {
                UserState.Empty
            }
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            updateUserUseCase(user)
            _userState.value = UserState.Success(user)
        }
    }

    fun logout(user: UserEntity) {
        viewModelScope.launch {
            logoutUseCase(user)
            _userState.value = UserState.Empty
        }
    }
}

class UserViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val userDao: UserDao = AppDatabase.getDatabase(application).userDao()
            val module = AppModule
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(
                module.provideGetUserUseCase(application),
                module.provideUpdateUserUseCase(application),
                module.provideLogoutUseCase(application),
                userDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}