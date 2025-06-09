package io.whyscape.lundo.domain.repository

import io.whyscape.lundo.data.db.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<UserEntity?>
    suspend fun updateUser(user: UserEntity?)
    suspend fun logout(user: UserEntity)
}