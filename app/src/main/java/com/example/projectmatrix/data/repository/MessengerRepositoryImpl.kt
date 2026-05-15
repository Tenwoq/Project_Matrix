package com.example.projectmatrix.data.repository

import android.content.SharedPreferences
import com.example.projectmatrix.data.remote.FirebaseAuthClient
import com.example.projectmatrix.data.remote.MinApi
import com.example.projectmatrix.domain.model.AuthResponse
import com.example.projectmatrix.domain.model.Chat
import com.example.projectmatrix.domain.model.CodeResponse
import com.example.projectmatrix.domain.model.ConfirmRegistrationRequest
import com.example.projectmatrix.domain.model.LoginRequest
import com.example.projectmatrix.domain.model.Message
import com.example.projectmatrix.domain.model.RegisterRequest
import com.example.projectmatrix.domain.model.User
import com.example.projectmatrix.domain.model.UpdateProfileRequest
import com.example.projectmatrix.domain.repository.MessengerRepository
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessengerRepositoryImpl @Inject constructor(
    private val api: MinApi,
    private val firebaseAuth: FirebaseAuthClient,
    private val preferences: SharedPreferences,
) : MessengerRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override val token: String?
        get() = if (firebaseAuth.hasVerifiedUser) "firebase" else null

    override val currentUserId: String?
        get() = preferences.getString(KEY_USER_ID, null)

    override suspend fun requestRegistrationCode(username: String, password: String, displayName: String, language: String): CodeResponse {
        firebaseAuth.register(username, password, displayName)
        return CodeResponse(if (language == "en") "Verification email sent" else "Письмо подтверждения отправлено")
    }

    override suspend fun confirmRegistration(email: String, code: String): AuthResponse =
        api.firebaseAuth(firebaseAuth.verifiedIdToken()).also(::saveAuth)

    override suspend fun login(username: String, password: String): AuthResponse =
        api.firebaseAuth(firebaseAuth.login(username, password)).also(::saveAuth)

    override suspend fun profile(): User = api.profile(requireToken())

    override suspend fun updateProfile(username: String, displayName: String, profileStatus: String): User {
        val updated = api.updateProfile(requireToken(), UpdateProfileRequest(username, displayName, profileStatus))
        firebaseAuth.updateDisplayName(updated.displayName)
        return updated
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        firebaseAuth.changePassword(currentPassword, newPassword)
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
    }

    override suspend fun resetPassword(resetCode: String, newPassword: String) {
        firebaseAuth.resetPassword(resetCode, newPassword)
    }

    override suspend fun searchUsers(query: String): List<User> = api.searchUsers(requireToken(), query)

    override suspend fun chats(): List<Chat> = api.chats(requireToken())

    override suspend fun createDirectChat(userId: String): Chat = api.createDirectChat(requireToken(), userId)

    override suspend fun messages(chatId: String): List<Message> = api.messages(requireToken(), chatId)

    override suspend fun sendMessage(chatId: String, body: String): Message = api.sendMessage(requireToken(), chatId, body)

    override suspend fun markMessagesRead(chatId: String): List<Message> = api.markMessagesRead(requireToken(), chatId)

    override suspend fun deleteMessage(chatId: String, messageId: String, deleteForAll: Boolean): Message? =
        api.deleteMessage(requireToken(), chatId, messageId, deleteForAll)

    override fun observeMessages(chatId: String): Flow<Message> = flow {
        api.client.webSocket(api.eventsUrl(chatId, requireToken())) {
            for (frame in incoming) {
                if (frame is Frame.Text) emit(json.decodeFromString<Message>(frame.readText()))
            }
        }
    }

    override fun logout() {
        firebaseAuth.logout()
        preferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }

    private fun saveAuth(response: AuthResponse) {
        preferences.edit()
            .putString(KEY_TOKEN, response.token)
            .putString(KEY_USER_ID, response.user.id)
            .apply()
    }

    private suspend fun requireToken(): String = firebaseAuth.idToken()

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
    }
}
