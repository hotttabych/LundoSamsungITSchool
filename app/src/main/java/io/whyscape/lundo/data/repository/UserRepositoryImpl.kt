package io.whyscape.lundo.data.repository

import io.whyscape.lundo.data.db.UserDao
import io.whyscape.lundo.data.db.UserEntity
import io.whyscape.lundo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    override fun getUser(): Flow<UserEntity?> {
        return userDao.getUser()
    }

    override suspend fun updateUser(user: UserEntity?) {
        user?.let {
            val existingUser = userDao.getUser().first()
            if (existingUser == null) {
                userDao.insert(user)
            } else {
                userDao.update(user)
            }
        }
    }

    override suspend fun logout(user: UserEntity) {
        userDao.delete(user)
    }
}