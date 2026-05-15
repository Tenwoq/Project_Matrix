package com.example.projectmatrix.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.domain.model.User
import com.example.projectmatrix.domain.repository.MessengerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompanionProfileUiState(
    val user: User? = null,
    val language: String = "ru",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CompanionProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MessengerRepository,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val chatId: String = checkNotNull(savedStateHandle["chatId"])
    private val _state = MutableStateFlow(CompanionProfileUiState(language = appSettingsRepository.settings.value.language))
    val state: StateFlow<CompanionProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _state.update { it.copy(language = settings.language) }
            }
        }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.chats().firstOrNull { it.id == chatId }?.companion }
                .onSuccess { user -> _state.update { it.copy(user = user, isLoading = false) } }
                .onFailure { throwable -> _state.update { it.copy(isLoading = false, error = throwable.message) } }
        }
    }
}
