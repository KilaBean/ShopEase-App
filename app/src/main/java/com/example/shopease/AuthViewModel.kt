package com.example.shopease

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
    // Using mutableStateOf to make it observable in Compose
    private val _isAuthenticated = mutableStateOf(false)

    // Expose as a State object that can be observed
    val isAuthenticated = _isAuthenticated

    fun updateAuthState(isAuthenticated: Boolean) {
        _isAuthenticated.value = isAuthenticated
    }
}