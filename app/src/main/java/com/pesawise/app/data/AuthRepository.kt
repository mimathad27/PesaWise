package com.pesawise.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {
    suspend fun register(fullName: String, email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName.trim()).build())?.await()
        auth.signOut()
        "Account created. You can now sign in."
    }

    suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        "Welcome back, ${result.user?.displayName?.takeIf { it.isNotBlank() } ?: email.trim()}!"
    }

    suspend fun signInWithGoogle(idToken: String): Result<String> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        "Welcome${result.user?.displayName?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""}!"
    }

    suspend fun sendPasswordReset(email: String): Result<String> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        "If an account exists for this email, a password reset link has been sent."
    }

    suspend fun updateDisplayName(name: String): Result<String> = runCatching {
        val user = checkNotNull(auth.currentUser) { "You need to sign in again." }
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).await()
        "Profile name updated."
    }

    fun setLanguageCode(languageCode: String) {
        auth.setLanguageCode(languageCode)
    }

    fun signOut() = auth.signOut()

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun currentProfile(): AuthProfile? = auth.currentUser?.let {
        AuthProfile(it.displayName.orEmpty(), it.email.orEmpty())
    }
}

data class AuthProfile(
    val fullName: String,
    val email: String
)
