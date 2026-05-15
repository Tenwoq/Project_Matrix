package com.example.projectmatrix.domain.usecase

import com.example.projectmatrix.domain.repository.MessengerRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(username: String, password: String) = repository.login(username, password)
}

class RegisterUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(username: String, password: String, displayName: String, language: String) =
        repository.requestRegistrationCode(username, password, displayName, language)
}

class ConfirmRegistrationUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(email: String, code: String) = repository.confirmRegistration(email, code)
}

class ChangePasswordUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String) =
        repository.changePassword(currentPassword, newPassword)
}

class SendPasswordResetEmailUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(email: String) = repository.sendPasswordResetEmail(email)
}

class ResetPasswordUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(resetCode: String, newPassword: String) =
        repository.resetPassword(resetCode, newPassword)
}
