package com.example.projectmatrix.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthClient @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val hasVerifiedUser: Boolean
        get() = auth.currentUser?.isEmailVerified == true

    suspend fun register(email: String, password: String, displayName: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        auth.currentUser?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.trim())
                .build()
        )?.await()
        auth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun login(email: String, password: String): String {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = auth.currentUser ?: error("Firebase user is missing")
        user.reload().await()
        check(user.isEmailVerified) { "Подтвердите email перед входом" }
        return idToken()
    }

    suspend fun verifiedIdToken(): String {
        val user = auth.currentUser ?: error("Firebase user is missing")
        user.reload().await()
        check(user.isEmailVerified) { "Подтвердите email по ссылке из письма" }
        return idToken()
    }

    suspend fun idToken(): String =
        auth.currentUser?.getIdToken(true)?.await()?.token ?: error("Firebase token is missing")

    suspend fun updateDisplayName(displayName: String) {
        val user = auth.currentUser ?: error("Firebase user is missing")
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.trim())
                .build()
        ).await()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: error("Firebase user is missing")
        val email = user.email ?: error("Firebase email is missing")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    suspend fun resetPassword(resetCode: String, newPassword: String) {
        auth.verifyPasswordResetCode(resetCode.trim()).await()
        auth.confirmPasswordReset(resetCode.trim(), newPassword).await()
    }

    fun logout() {
        auth.signOut()
    }
}
