package com.example.projectmatrix.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val profileStatus: String = "",
    val matrixUserId: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User,
)

@Serializable
data class ErrorResponse(val message: String)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val displayName: String,
    val language: String = "ru",
)

@Serializable
data class ConfirmRegistrationRequest(
    val email: String,
    val code: String,
)

@Serializable
data class CodeResponse(val message: String)

@Serializable
data class FirebaseAuthRequest(val idToken: String)

@Serializable
data class UpdateProfileRequest(
    val username: String,
    val displayName: String,
    val profileStatus: String,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class DirectChatRequest(val userId: String)

@Serializable
data class SendMessageRequest(val body: String)

@Serializable
data class Chat(
    val id: String,
    val companion: User,
    val lastMessage: Message? = null,
    val createdAt: String,
    val unreadCount: Int = 0,
)

@Serializable
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val body: String,
    val matrixEventJson: String,
    val createdAt: String,
    val deliveredAt: String? = null,
    val readAt: String? = null,
    val deletedAt: String? = null,
)
