package com.example.shopease.utils

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuthException

object AuthUtils {
    fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun checkPasswordRequirements(password: String): Map<String, Boolean> {
        return mapOf(
            "length" to (password.length >= 8),
            "uppercase" to password.matches(Regex(".*[A-Z].*")),
            "lowercase" to password.matches(Regex(".*[a-z].*")),
            "numeric" to password.matches(Regex(".*\\d.*")),
            "special" to password.matches(Regex(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
        )
    }

    fun getAuthErrorMessage(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthException -> when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email format"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_USER_NOT_FOUND" -> "Account not found"
                "ERROR_USER_DISABLED" -> "Account disabled"
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try later."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already registered"
                "ERROR_WEAK_PASSWORD" -> "Password too weak"
                else -> "Authentication failed"
            }
            else -> "An error occurred. Please try again."
        }
    }
}