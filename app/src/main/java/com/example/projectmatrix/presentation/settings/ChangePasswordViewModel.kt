package com.example.projectmatrix.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.domain.usecase.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val repeatedPassword: String = "",
    val language: String = "ru",
    val isLoading: Boolean = false,
    val info: String? = null,
    val error: String? = null,
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePassword: ChangePasswordUseCase,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ChangePasswordUiState(language = appSettingsRepository.settings.value.language))
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _state.update { it.copy(language = settings.language) }
            }
        }
    }

    fun setCurrentPassword(value: String) = _state.update { it.copy(currentPassword = value, info = null, error = null) }
    fun setNewPassword(value: String) = _state.update { it.copy(newPassword = value, info = null, error = null) }
    fun setRepeatedPassword(value: String) = _state.update { it.copy(repeatedPassword = value, info = null, error = null) }

    fun submit() {
        viewModelScope.launch {
            val current = state.value
            _state.update { it.copy(isLoading = true, info = null, error = null) }
            runCatching {
                require(current.newPassword == current.repeatedPassword) {
                    if (current.language == "en") "Passwords do not match" else "Пароли не совпадают"
                }
                require(current.newPassword.length >= 6) {
                    if (current.language == "en") "Password must contain at least 6 characters" else "Пароль должен быть не короче 6 символов"
                }
                changePassword(current.currentPassword, current.newPassword)
            }.onSuccess {
                _state.update {
                    it.copy(
                        currentPassword = "",
                        newPassword = "",
                        repeatedPassword = "",
                        isLoading = false,
                        info = if (it.language == "en") "Password changed" else "Пароль изменен",
                    )
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, error = throwable.toPasswordMessage(it.language)) }
            }
        }
    }
}

private fun Throwable.toPasswordMessage(language: String): String {
    val text = message.orEmpty()
    return when {
        text.contains("password is invalid", ignoreCase = true) ||
            text.contains("credential", ignoreCase = true) -> {
            if (language == "en") "Current password is incorrect" else "Текущий пароль неверный"
        }
        text.contains("network", ignoreCase = true) -> {
            if (language == "en") "Network error" else "Ошибка сети"
        }
        else -> text.ifBlank { if (language == "en") "Password change failed" else "Не удалось изменить пароль" }
    }
}
