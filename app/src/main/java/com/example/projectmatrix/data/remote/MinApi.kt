package com.example.projectmatrix.data.remote

import com.example.projectmatrix.domain.model.AuthResponse
import com.example.projectmatrix.domain.model.Chat
import com.example.projectmatrix.domain.model.CodeResponse
import com.example.projectmatrix.domain.model.ConfirmRegistrationRequest
import com.example.projectmatrix.domain.model.DirectChatRequest
import com.example.projectmatrix.domain.model.ErrorResponse
import com.example.projectmatrix.domain.model.FirebaseAuthRequest
import com.example.projectmatrix.domain.model.LoginRequest
import com.example.projectmatrix.domain.model.Message
import com.example.projectmatrix.domain.model.RegisterRequest
import com.example.projectmatrix.domain.model.SendMessageRequest
import com.example.projectmatrix.domain.model.UpdateProfileRequest
import com.example.projectmatrix.domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MinApi @Inject constructor() {
    private val baseUrl = "https://min.okak.club"
    private val json = Json { ignoreUnknownKeys = true }

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        install(WebSockets)
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
    }

    suspend fun firebaseAuth(idToken: String): AuthResponse =
        client.post("/api/auth/firebase") { setBody(FirebaseAuthRequest(idToken)) }.parseBody()

    suspend fun login(request: LoginRequest): AuthResponse =
        client.post("/api/auth/login") { setBody(request) }.parseBody()

    suspend fun profile(token: String): User =
        client.get("/api/profile") { bearerAuth(token) }.body()

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): User =
        client.put("/api/profile") {
            bearerAuth(token)
            setBody(request)
        }.parseBody()

    suspend fun searchUsers(token: String, query: String): List<User> =
        client.get("/api/users/search?query=$query") { bearerAuth(token) }.body()

    suspend fun chats(token: String): List<Chat> =
        client.get("/api/chats") { bearerAuth(token) }.body()

    suspend fun createDirectChat(token: String, userId: String): Chat =
        client.post("/api/chats/direct") {
            bearerAuth(token)
            setBody(DirectChatRequest(userId))
        }.parseBody()

    suspend fun messages(token: String, chatId: String): List<Message> =
        client.get("/api/chats/$chatId/messages") { bearerAuth(token) }.body()

    suspend fun sendMessage(token: String, chatId: String, body: String): Message =
        client.post("/api/chats/$chatId/messages") {
            bearerAuth(token)
            setBody(SendMessageRequest(body))
        }.parseBody()

    suspend fun markMessagesRead(token: String, chatId: String): List<Message> =
        client.put("/api/chats/$chatId/messages/read") { bearerAuth(token) }.parseBody()

    suspend fun deleteMessage(token: String, chatId: String, messageId: String, deleteForAll: Boolean): Message? {
        val scope = if (deleteForAll) "all" else "me"
        val response = client.delete("/api/chats/$chatId/messages/$messageId?scope=$scope") { bearerAuth(token) }
        if (response.status == HttpStatusCode.NoContent) return null
        return response.parseBody()
    }

    fun eventsUrl(chatId: String, token: String): String =
        "$baseUrl/api/chats/$chatId/events?token=$token"
            .replace("https://", "wss://")
            .replace("http://", "ws://")

    private suspend inline fun <reified T> HttpResponse.parseBody(): T {
        if (status.isSuccess()) return body()
        val error = runCatching { body<ErrorResponse>().message }.getOrNull()
        throw IllegalStateException(error ?: "Ошибка сервера: ${status.value}")
    }
}
