package com.example.projectmatrix

import com.example.projectmatrix.domain.model.AuthResponse
import com.example.projectmatrix.domain.model.Chat
import com.example.projectmatrix.domain.model.CodeResponse
import com.example.projectmatrix.domain.model.DirectChatRequest
import com.example.projectmatrix.domain.model.ErrorResponse
import com.example.projectmatrix.domain.model.Message
import com.example.projectmatrix.domain.model.RegisterRequest
import com.example.projectmatrix.domain.model.SendMessageRequest
import com.example.projectmatrix.domain.model.UpdateProfileRequest
import com.example.projectmatrix.domain.model.User
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun userDecodeUsesEmptyProfileStatusByDefault() {
        val user = json.decodeFromString<User>(
            """{"id":"u1","username":"tigran","displayName":"Tigran","matrixUserId":"@tigran:min.local"}"""
        )

        assertEquals("", user.profileStatus)
    }

    @Test
    fun registerRequestUsesRussianLanguageByDefault() {
        val request = RegisterRequest(
            username = "user@example.com",
            password = "11122233",
            displayName = "Tigran",
        )

        assertEquals("ru", request.language)
    }

    @Test
    fun messageDecodeUsesNullStatusFieldsByDefault() {
        val message = json.decodeFromString<Message>(
            """
            {
              "id":"m1",
              "chatId":"c1",
              "senderId":"u1",
              "body":"hello",
              "matrixEventJson":"{}",
              "createdAt":"2026-05-30T12:00:00Z"
            }
            """.trimIndent()
        )

        assertNull(message.deliveredAt)
        assertNull(message.readAt)
        assertNull(message.deletedAt)
    }

    @Test
    fun chatDecodeUsesDefaultEmptyUnreadCount() {
        val chat = json.decodeFromString<Chat>(
            """
            {
              "id":"c1",
              "companion":{"id":"u2","username":"test","displayName":"Test","matrixUserId":"@test:min.local"},
              "createdAt":"2026-05-30T12:00:00Z"
            }
            """.trimIndent()
        )

        assertEquals(0, chat.unreadCount)
        assertNull(chat.lastMessage)
    }

    @Test
    fun chatDecodePreservesUnreadCount() {
        val chat = json.decodeFromString<Chat>(
            """
            {
              "id":"c1",
              "companion":{"id":"u2","username":"test","displayName":"Test","matrixUserId":"@test:min.local"},
              "createdAt":"2026-05-30T12:00:00Z",
              "unreadCount":5
            }
            """.trimIndent()
        )

        assertEquals(5, chat.unreadCount)
    }

    @Test
    fun authResponseRoundTripKeepsTokenAndUser() {
        val response = AuthResponse(
            token = "firebase-token",
            user = User("u1", "tigran", "Tigran", "online", "@tigran:min.local"),
        )

        val decoded = json.decodeFromString<AuthResponse>(json.encodeToString(AuthResponse.serializer(), response))

        assertEquals("firebase-token", decoded.token)
        assertEquals("Tigran", decoded.user.displayName)
    }

    @Test
    fun updateProfileRequestRoundTripKeepsNicknameAndStatus() {
        val request = UpdateProfileRequest("tigran_01", "Tigran", "Busy")

        val decoded = json.decodeFromString<UpdateProfileRequest>(
            json.encodeToString(UpdateProfileRequest.serializer(), request)
        )

        assertEquals("tigran_01", decoded.username)
        assertEquals("Busy", decoded.profileStatus)
    }

    @Test
    fun directChatRequestRoundTripKeepsUserId() {
        val decoded = json.decodeFromString<DirectChatRequest>(
            json.encodeToString(DirectChatRequest.serializer(), DirectChatRequest("user-2"))
        )

        assertEquals("user-2", decoded.userId)
    }

    @Test
    fun sendMessageRequestRoundTripKeepsBody() {
        val decoded = json.decodeFromString<SendMessageRequest>(
            json.encodeToString(SendMessageRequest.serializer(), SendMessageRequest("Привет"))
        )

        assertEquals("Привет", decoded.body)
    }

    @Test
    fun errorResponseRoundTripKeepsMessage() {
        val decoded = json.decodeFromString<ErrorResponse>(
            json.encodeToString(ErrorResponse.serializer(), ErrorResponse("Ошибка"))
        )

        assertEquals("Ошибка", decoded.message)
    }

    @Test
    fun codeResponseRoundTripKeepsMessage() {
        val decoded = json.decodeFromString<CodeResponse>(
            json.encodeToString(CodeResponse.serializer(), CodeResponse("Письмо отправлено"))
        )

        assertEquals("Письмо отправлено", decoded.message)
    }

    @Test
    fun messageStatusFieldsDescribeSentDeliveredAndReadStates() {
        val sent = message(deliveredAt = null, readAt = null)
        val delivered = message(deliveredAt = "2026-05-30T12:01:00Z", readAt = null)
        val read = message(deliveredAt = "2026-05-30T12:01:00Z", readAt = "2026-05-30T12:02:00Z")

        assertFalse(sent.deliveredAt != null)
        assertTrue(delivered.deliveredAt != null && delivered.readAt == null)
        assertTrue(read.readAt != null)
    }

    private fun message(deliveredAt: String?, readAt: String?) = Message(
        id = "m1",
        chatId = "c1",
        senderId = "u1",
        body = "text",
        matrixEventJson = "{}",
        createdAt = "2026-05-30T12:00:00Z",
        deliveredAt = deliveredAt,
        readAt = readAt,
        deletedAt = null,
    )
}
