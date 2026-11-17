package com.pesawise.app.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthRepository(private val userDao: UserDao) {
    suspend fun register(fullName: String, email: String, password: String): Result<String> {
        val normalizedEmail = email.trim().lowercase()
        if (userDao.findByEmail(normalizedEmail) != null) return Result.failure(IllegalArgumentException("An account with this email already exists."))

        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val user = UserAccount(
            fullName = fullName.trim(),
            email = normalizedEmail,
            passwordHash = hashPassword(password, salt),
            passwordSalt = Base64.encodeToString(salt, Base64.NO_WRAP)
        )
        return runCatching { userDao.insert(user) }.map { "Account created. You can now sign in." }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Email or password is incorrect."))
        val salt = Base64.decode(user.passwordSalt, Base64.NO_WRAP)
        if (hashPassword(password, salt) != user.passwordHash) {
            return Result.failure(IllegalArgumentException("Email or password is incorrect."))
        }
        return Result.success("Welcome back, ${user.fullName}!")
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return try {
            Base64.encodeToString(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,
                Base64.NO_WRAP
            )
        } finally {
            spec.clearPassword()
        }
    }
}
