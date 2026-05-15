package com.example.projectmatrix.domain.usecase

import com.example.projectmatrix.domain.repository.MessengerRepository
import javax.inject.Inject

class GetChatsUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke() = repository.chats()
}

class GetMessagesUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(chatId: String) = repository.messages(chatId)
}

class SendMessageUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(chatId: String, body: String) = repository.sendMessage(chatId, body)
}

class MarkMessagesReadUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(chatId: String) = repository.markMessagesRead(chatId)
}

class DeleteMessageUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(chatId: String, messageId: String, deleteForAll: Boolean) =
        repository.deleteMessage(chatId, messageId, deleteForAll)
}

class SearchUsersUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(query: String) = repository.searchUsers(query)
}

class GetProfileUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke() = repository.profile()
}

class UpdateProfileUseCase @Inject constructor(private val repository: MessengerRepository) {
    suspend operator fun invoke(username: String, displayName: String, profileStatus: String) =
        repository.updateProfile(username, displayName, profileStatus)
}
