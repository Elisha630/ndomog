package com.ndomog.inventory.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndomog.inventory.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.signIn(email, password)
            result.onSuccess {
                _loginState.value = LoginState.Success
                _authState.value = AuthState.Authenticated
            }.onFailure { exception ->
                val userFriendlyMessage = when {
                    exception.message?.contains("Invalid login credentials", ignoreCase = true) == true -> 
                        "Invalid email or password. Please try again."
                    exception.message?.contains("Email not confirmed", ignoreCase = true) == true -> 
                        "Please verify your email before logging in."
                    exception.message?.contains("User not found", ignoreCase = true) == true -> 
                        "This email is not registered. Please sign up first."
                    exception.message?.contains("unauthorized", ignoreCase = true) == true -> 
                        "Authentication failed. Please check your credentials."
                    else -> "Login failed. Please try again later."
                }
                _loginState.value = LoginState.Error(userFriendlyMessage)
            }
        }
    }

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.signUp(email, password)
            result.onSuccess {
                _loginState.value = LoginState.Success
            }.onFailure { exception ->
                val userFriendlyMessage = when {
                    exception.message?.contains("already registered", ignoreCase = true) == true -> 
                        "This email is already registered. Please log in instead."
                    exception.message?.contains("weak password", ignoreCase = true) == true -> 
                        "Password is too weak. Use at least 8 characters."
                    else -> "Sign up failed. Please try again."
                }
                _loginState.value = LoginState.Error(userFriendlyMessage)
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _loginState.value = LoginState.Success
            }.onFailure {
                _loginState.value = LoginState.Error("Failed to send reset email. Please try again later.")
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
