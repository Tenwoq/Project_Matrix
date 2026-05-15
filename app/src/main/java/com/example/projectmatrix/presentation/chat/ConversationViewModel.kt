package com.example.projectmatrix.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.domain.model.Message
import com.example.projectmatrix.domain.repository.MessengerRepository
import com.example.projectmatrix.domain.usecase.DeleteMessageUseCase
import com.example.projectmatrix.domain.usecase.GetMessagesUseCase
import com.example.projectmatrix.domain.usecase.MarkMessagesReadUseCase
import com.example.projectmatrix.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUiState(
    val chatId: String = "",
    val companionName: String = "",
    val companionLogin: String = "",
    val companionStatus: String = "",
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val language: String = "ru",
    val currentUserId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMessages: GetMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
    private val markMessagesRead: MarkMessagesReadUseCase,
    private val deleteMessage: DeleteMessageUseCase,
    private val repository: MessengerRepository,
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    private val chatId: String = checkNotNull(savedStateHandle["chatId"])
    private val _state = MutableStateFlow(
        ConversationUiState(
            chatId = chatId,
            currentUserId = repository.currentUserId,
        )
    )
    val state: StateFlow<ConversationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _state.update { it.copy(language = settings.language) }
            }
        }
        load()
        loadCompanion()
        viewModelScope.launch {
            runCatching {
                repository.observeMessages(chatId).collect { incoming ->
                    _state.update { it.copy(messages = mergeMessages(it.messages, listOf(incoming))) }
                    if (incoming.senderId != repository.currentUserId) markRead()
                }
            }.onFailure { throwable ->
                _state.update { it.copy(error = throwable.message) }
            }
        }
    }

    fun setDraft(value: String) = _state.update { it.copy(draft = value) }

    fun send() {
        val text = state.value.draft
        if (text.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(draft = "") }
            runCatching { sendMessage(chatId, text) }
                .onSuccess { message -> _state.update { it.copy(messages = mergeMessages(it.messages, listOf(message))) } }
                .onFailure { throwable -> _state.update { it.copy(error = throwable.message) } }
        }
    }

    fun delete(messageId: String, deleteForAll: Boolean) {
        viewModelScope.launch {
            runCatching { deleteMessage(chatId, messageId, deleteForAll) }
                .onSuccess { updated ->
                    _state.update { current ->
                        current.copy(
                            messages = if (updated == null) {
                                current.messages.filterNot { it.id == messageId }
                            } else {
                                mergeMessages(current.messages, listOf(updated))
                            }
                        )
                    }
                }
                .onFailure { throwable -> _state.update { it.copy(error = throwable.message) } }
        }
    }

    private fun load() {
        viewModelScope.launch {
            runCatching { getMessages(chatId) }
                .onSuccess { messages ->
                    _state.update { it.copy(messages = messages) }
                    markRead()
                }
                .onFailure { throwable -> _state.update { it.copy(error = throwable.message) } }
        }
    }

    private fun loadCompanion() {
        viewModelScope.launch {
            runCatching { repository.chats().firstOrNull { it.id == chatId } }
                .onSuccess { chat ->
                    if (chat != null) {
                        _state.update {
                            it.copy(
                                companionName = chat.companion.displayName,
                                companionLogin = chat.companion.username,
                                companionStatus = chat.companion.profileStatus,
                            )
                        }
                    }
                }
        }
    }

    private suspend fun markRead() {
        runCatching { markMessagesRead(chatId) }
            .onSuccess { updates ->
                if (updates.isNotEmpty()) {
                    _state.update { it.copy(messages = mergeMessages(it.messages, updates)) }
                }
            }
    }
}

private fun mergeMessages(current: List<Message>, updates: List<Message>): List<Message> {
    val merged = current.associateBy { it.id }.toMutableMap()
    updates.forEach { merged[it.id] = it }
    return merged.values.sortedBy { it.createdAt }
}
