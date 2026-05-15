package com.example.projectmatrix.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.domain.repository.MessengerRepository
import com.example.projectmatrix.domain.usecase.ConfirmRegistrationUseCase
import com.example.projectmatrix.domain.usecase.LoginUseCase
import com.example.projectmatrix.domain.usecase.RegisterUseCase
import com.example.projectmatrix.domain.usecase.ResetPasswordUseCase
import com.example.projectmatrix.domain.usecase.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val verificationCode: String = "",
    val resetCode: String = "",
    val resetNewPassword: String = "",
    val resetPasswordConfirmation: String = "",
    val displayName: String = "",
    val language: String = "ru",
    val isRegisterMode: Boolean = false,
    val isPasswordResetMode: Boolean = false,
    val isLoading: Boolean = false,
    val isWaitingForCode: Boolean = false,
    val isWaitingForResetCode: Boolean = false,
    val info: String? = null,
    val error: String? = null,
    val isAuthorized: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val login: LoginUseCase,
    private val register: RegisterUseCase,
    private val confirmRegistration: ConfirmRegistrationUseCase,
    private val sendPasswordResetEmail: SendPasswordResetEmailUseCase,
    private val resetPassword: ResetPasswordUseCase,
    appSettingsRepository: AppSettingsRepository,
    repository: MessengerRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        AuthUiState(
            language = appSettingsRepository.settings.value.language,
            isAuthorized = repository.token != null,
        )
    )
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun setUsername(value: String) = _state.update { it.copy(username = value, isWaitingForCode = false, info = null, error = null) }
    fun setPassword(value: String) = _state.update { it.copy(password = value, error = null) }
    fun setPasswordConfirmation(value: String) = _state.update { it.copy(passwordConfirmation = value, error = null) }
    fun setVerificationCode(value: String) = _state.update { it.copy(verificationCode = value.filter(Char::isDigit).take(6), error = null) }
    fun setResetCode(value: String) = _state.update { it.copy(resetCode = value, error = null) }
    fun setResetNewPassword(value: String) = _state.update { it.copy(resetNewPassword = value, error = null) }
    fun setResetPasswordConfirmation(value: String) = _state.update { it.copy(resetPasswordConfirmation = value, error = null) }
    fun setDisplayName(value: String) = _state.update { it.copy(displayName = value, error = null) }
    fun setLanguage(value: String) = _state.update { it.copy(language = value, error = null) }
    fun toggleMode() = _state.update {
        it.copy(
            isRegisterMode = !it.isRegisterMode,
            isPasswordResetMode = false,
            isWaitingForCode = false,
            isWaitingForResetCode = false,
            verificationCode = "",
            info = null,
            error = null,
        )
    }

    fun togglePasswordReset() = _state.update {
        it.copy(
            isPasswordResetMode = !it.isPasswordResetMode,
            isRegisterMode = false,
            isWaitingForCode = false,
            isWaitingForResetCode = false,
            resetCode = "",
            resetNewPassword = "",
            resetPasswordConfirmation = "",
            info = null,
            error = null,
        )
    }

    fun submit() {
        viewModelScope.launch {
            val current = state.value
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                if (current.isPasswordResetMode && current.isWaitingForResetCode) {
                    require(current.resetNewPassword == current.resetPasswordConfirmation) {
                        if (current.language == "en") "Passwords do not match" else "Пароли не совпадают"
                    }
                    require(current.resetNewPassword.length >= 6) {
                        if (current.language == "en") "Password must contain at least 6 characters" else "Пароль должен быть не короче 6 символов"
                    }
                    resetPassword(extractResetCode(current.resetCode), current.resetNewPassword)
                } else if (current.isPasswordResetMode) {
                    require(current.username.isNotBlank()) {
                        if (current.language == "en") "Enter email" else "Введите email"
                    }
                    sendPasswordResetEmail(current.username)
                } else if (current.isRegisterMode && current.isWaitingForCode) {
                    confirmRegistration(current.username, current.verificationCode)
                } else if (current.isRegisterMode) {
                    require(current.password == current.passwordConfirmation) {
                        if (current.language == "en") "Passwords do not match" else "Пароли не совпадают"
                    }
                    register(current.username, current.password, current.displayName, current.language)
                } else {
                    login(current.username, current.password)
                }
            }.onSuccess {
                if (current.isPasswordResetMode && current.isWaitingForResetCode) {
                    _state.update {
                        it.copy(
                            password = "",
                            resetCode = "",
                            resetNewPassword = "",
                            resetPasswordConfirmation = "",
                            isLoading = false,
                            isPasswordResetMode = false,
                            isWaitingForResetCode = false,
                            info = if (current.language == "en") {
                                "Password has been changed. Sign in with the new password."
                            } else {
                                "Пароль изменен. Войдите с новым паролем."
                            },
                        )
                    }
                } else if (current.isPasswordResetMode) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isWaitingForResetCode = true,
                            info = if (current.language == "en") {
                                "Password reset email has been sent"
                            } else {
                                "Письмо для сброса пароля отправлено"
                            },
                        )
                    }
                } else if (current.isRegisterMode && !current.isWaitingForCode) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isWaitingForCode = true,
                            info = if (current.language == "en") {
                                "Verification email has been sent"
                            } else {
                                "Письмо подтверждения отправлено"
                            },
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, isAuthorized = true) }
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, error = throwable.toAuthMessage(state.value.language)) }
            }
        }
    }
}

private fun extractResetCode(value: String): String {
    val trimmed = value.trim()
    val match = Regex("[?&]oobCode=([^&]+)").find(trimmed)
    val rawCode = match?.groupValues?.get(1) ?: trimmed
    return URLDecoder.decode(rawCode, StandardCharsets.UTF_8.name())
}

private fun Throwable.toAuthMessage(language: String): String {
    val message = this.message.orEmpty()
    val type = this::class.qualifiedName.orEmpty()
    if (message.contains("Email is already used", ignoreCase = true) ||
        message.contains("Email is already registered", ignoreCase = true) ||
        message.contains("email address is already in use", ignoreCase = true) ||
        type.endsWith("FirebaseAuthUserCollisionException")
    ) {
        return if (language == "en") {
            "Email is already used for another account"
        } else {
            "Email уже используется для другого аккаунта"
        }
    }
    if (message.contains("network error", ignoreCase = true) ||
        message.contains("timeout", ignoreCase = true) ||
        type.endsWith("FirebaseNetworkException")
    ) {
        return if (language == "en") {
            "Firebase is unavailable. Check internet connection and Firebase Auth settings."
        } else {
            "Firebase недоступен. Проверь интернет и настройки Firebase Authentication."
        }
    }
    if (message.contains("sign-in provider is disabled", ignoreCase = true) ||
        message.contains("operation-not-allowed", ignoreCase = true) ||
        type.endsWith("FirebaseAuthException")
    ) {
        return if (language == "en") {
            "Enable Email/Password sign-in in Firebase Console."
        } else {
            "Включи Email/Password вход в Firebase Console."
        }
    }
    if (message.contains("password is invalid", ignoreCase = true) ||
        message.contains("at least 6 characters", ignoreCase = true) ||
        type.endsWith("FirebaseAuthWeakPasswordException")
    ) {
        return if (language == "en") {
            "Password must contain at least 6 characters."
        } else {
            "Пароль должен быть не короче 6 символов."
        }
    }
    if (message.contains("email address is badly formatted", ignoreCase = true) ||
        type.endsWith("FirebaseAuthInvalidCredentialsException")
    ) {
        return if (language == "en") {
            "Enter a valid email."
        } else {
            "Введите корректный email."
        }
    }
    if (message.contains("expired", ignoreCase = true) ||
        message.contains("invalid action code", ignoreCase = true) ||
        type.endsWith("FirebaseAuthActionCodeException")
    ) {
        return if (language == "en") {
            "Reset code is invalid or expired."
        } else {
            "Код сброса неверный или устарел."
        }
    }
    return message.ifBlank { if (language == "en") "Authentication error" else "Ошибка авторизации" }
}
