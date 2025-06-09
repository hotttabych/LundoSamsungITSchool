package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.data.db.UserEntity
import io.whyscape.lundo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUserUseCase(private val repository: UserRepository) {
    operator fun invoke(): Flow<UserEntity?> {
        return repository.getUser()
    }
}