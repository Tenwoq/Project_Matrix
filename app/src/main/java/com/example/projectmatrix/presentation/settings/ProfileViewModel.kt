package com.example.projectmatrix.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.domain.usecase.GetProfileUseCase
import com.example.projectmatrix.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val username: String = "",
    val displayName: String = "",
    val profileStatus: String = "",
    val language: String = "ru",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val info: String? = null,
    val error: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState(language = appSettingsRepository.settings.value.language))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _state.update { it.copy(language = settings.language) }
            }
        }
        load()
    }

    fun setDisplayName(value: String) {
        _state.update { it.copy(displayName = value.take(80), info = null, error = null) }
    }

    fun setUsername(value: String) {
        _state.update { it.copy(username = value.removePrefix("@").take(32), info = null, error = null) }
    }

    fun setProfileStatus(value: String) {
        _state.update { it.copy(profileStatus = value.take(140), info = null, error = null) }
    }

    fun edit() {
        _state.update { it.copy(isEditing = true, info = null, error = null) }
    }

    fun save() {
        viewModelScope.launch {
            val current = state.value
            if (!current.isEditing) {
                edit()
                return@launch
            }
            _state.update { it.copy(isSaving = true, info = null, error = null) }
            runCatching {
                require(current.displayName.isNotBlank()) {
                    if (current.language == "en") "Name is required" else "Имя обязательно"
                }
                require(current.username.isNotBlank()) {
                    if (current.language == "en") "Nickname is required" else "Никнейм обязателен"
                }
                updateProfile(current.username.trim(), current.displayName.trim(), current.profileStatus.trim())
            }.onSuccess { user ->
                _state.update {
                    it.copy(
                        username = user.username,
                        displayName = user.displayName,
                        profileStatus = user.profileStatus,
                        isSaving = false,
                        isEditing = false,
                        info = if (it.language == "en") "Profile saved" else "Профиль сохранен",
                    )
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false, error = throwable.message) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { getProfile() }
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            username = user.username,
                            displayName = user.displayName,
                            profileStatus = user.profileStatus,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message) }
                }
        }
    }
}
