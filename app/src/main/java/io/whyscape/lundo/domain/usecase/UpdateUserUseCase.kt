package io.whyscape.lundo.domain.usecase

import io.whyscape.lundo.data.db.UserEntity
import io.whyscape.lundo.domain.repository.UserRepository

class UpdateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: UserEntity?) {
        repository.updateUser(user)
    }
}