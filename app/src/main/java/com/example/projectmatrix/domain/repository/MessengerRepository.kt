package com.example.projectmatrix.domain.repository

import com.example.projectmatrix.domain.model.AuthResponse
import com.example.projectmatrix.domain.model.Chat
import com.example.projectmatrix.domain.model.CodeResponse
import com.example.projectmatrix.domain.model.Message
import com.example.projectmatrix.domain.model.User
import kotlinx.coroutines.flow.Flow

interface MessengerRepository {
    val token: String?
    val currentUserId: String?
    suspend fun requestRegistrationCode(username: String, password: String, displayName: String, language: String): CodeResponse
    suspend fun confirmRegistration(email: String, code: String): AuthResponse
    suspend fun login(username: String, password: String): AuthResponse
    suspend fun profile(): User
    suspend fun updateProfile(username: String, displayName: String, profileStatus: String): User
    suspend fun changePassword(currentPassword: String, newPassword: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun resetPassword(resetCode: String, newPassword: String)
    suspend fun searchUsers(query: String): List<User>
    suspend fun chats(): List<Chat>
    suspend fun createDirectChat(userId: String): Chat
    suspend fun messages(chatId: String): List<Message>
    suspend fun sendMessage(chatId: String, body: String): Message
    suspend fun markMessagesRead(chatId: String): List<Message>
    suspend fun deleteMessage(chatId: String, messageId: String, deleteForAll: Boolean): Message?
    fun observeMessages(chatId: String): Flow<Message>
    fun logout()
}
