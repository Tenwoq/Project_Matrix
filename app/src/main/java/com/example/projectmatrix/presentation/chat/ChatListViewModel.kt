package com.example.projectmatrix.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.domain.model.Chat
import com.example.projectmatrix.domain.model.User
import com.example.projectmatrix.domain.repository.MessengerRepository
import com.example.projectmatrix.domain.usecase.GetChatsUseCase
import com.example.projectmatrix.domain.usecase.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val users: List<User> = emptyList(),
    val query: String = "",
    val language: String = "ru",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChats: GetChatsUseCase,
    private val searchUsers: SearchUsersUseCase,
    private val repository: MessengerRepository,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatListUiState())
    val state: StateFlow<ChatListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _state.update { it.copy(language = settings.language) }
            }
        }
        refresh()
        viewModelScope.launch {
            while (true) {
                delay(3_000)
                refresh(showLoading = false)
            }
        }
    }

    fun refresh(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) _state.update { it.copy(isLoading = true, error = null) }
            runCatching { getChats() }
                .onSuccess { chats -> _state.update { it.copy(chats = chats, isLoading = false, error = null) } }
                .onFailure { throwable -> _state.update { it.copy(error = throwable.message, isLoading = false) } }
        }
    }

    fun setQuery(value: String) {
        _state.update { it.copy(query = value) }
        viewModelScope.launch {
            if (value.length < 2) {
                _state.update { it.copy(users = emptyList()) }
            } else {
                runCatching { searchUsers(value) }
                    .onSuccess { users -> _state.update { it.copy(users = users) } }
            }
        }
    }

    fun openDirectChat(userId: String, onOpened: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.createDirectChat(userId) }
                .onSuccess { onOpened(it.id) }
                .onFailure { throwable -> _state.update { it.copy(error = throwable.message) } }
        }
    }

    fun logout(onLogout: () -> Unit) {
        repository.logout()
        onLogout()
    }
}
