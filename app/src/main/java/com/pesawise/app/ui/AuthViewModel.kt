package com.pesawise.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesawise.app.data.AuthRepository
import kotlinx.coroutines.launch

data class AuthUiState(
    val isSignUp: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val message: String? = null,
    val isError: Boolean = false,
    val busy: Boolean = false,
    val loggedIn: Boolean = false
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var state = androidx.compose.runtime.mutableStateOf(AuthUiState())
        private set

    fun update(transform: (AuthUiState) -> AuthUiState) { state.value = transform(state.value) }

    fun submit() {
        val current = state.value
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(current.email.trim()).matches()) {
            update { it.copy(message = "Enter a valid email address.", isError = true) }; return
        }
        if (current.password.length < 8) {
            update { it.copy(message = "Use a password with at least 8 characters.", isError = true) }; return
        }
        if (current.isSignUp && current.fullName.isBlank()) {
            update { it.copy(message = "Enter your full name.", isError = true) }; return
        }
        if (current.isSignUp && current.password != current.confirmPassword) {
            update { it.copy(message = "Passwords do not match.", isError = true) }; return
        }
        update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val result = if (current.isSignUp) repository.register(current.fullName, current.email, current.password)
            else repository.signIn(current.email, current.password)

            update {
                it.copy(
                    busy = false,
                    message = result.fold({ msg -> msg }, { e -> e.message ?: "Something went wrong." }),
                    isError = result.isFailure,
                    // Only a successful sign-in opens the app, not a sign-up
                    loggedIn = result.isSuccess && !current.isSignUp,
                    // After sign-up, switch to the Log in tab and clear the passwords
                    isSignUp = if (result.isSuccess && current.isSignUp) false else it.isSignUp,
                    password = if (result.isSuccess) "" else it.password,
                    confirmPassword = if (result.isSuccess) "" else it.confirmPassword
                )
            }
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
}
