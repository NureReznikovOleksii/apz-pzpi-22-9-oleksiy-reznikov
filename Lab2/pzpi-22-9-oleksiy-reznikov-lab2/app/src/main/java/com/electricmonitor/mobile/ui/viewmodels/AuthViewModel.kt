package com.electricmonitor.mobile.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.electricmonitor.mobile.data.models.User
import com.electricmonitor.mobile.data.repositories.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    private val _registerState = MutableLiveData<AuthState>()
    val registerState: LiveData<AuthState> = _registerState

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.login(identifier, password)

                if (result.isSuccess) {
                    val authResponse = result.getOrNull()
                    authResponse?.let {
                        _userProfile.value = it.data.user
                        _loginState.value = AuthState.Success(it.data.user)
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Login failed"
                    _errorMessage.value = error
                    _loginState.value = AuthState.Error(error)
                }
            } catch (e: Exception) {
                val error = e.message ?: "Login failed"
                _errorMessage.value = error
                _loginState.value = AuthState.Error(error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        phone: String? = null
    ) {
        // Validation
        when {
            username.isBlank() || email.isBlank() || password.isBlank() ||
                    firstName.isBlank() || lastName.isBlank() -> {
                _errorMessage.value = "Please fill in all required fields"
                return
            }
            password != confirmPassword -> {
                _errorMessage.value = "Passwords do not match"
                return
            }
            password.length < 6 -> {
                _errorMessage.value = "Password must be at least 6 characters"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _errorMessage.value = "Please enter a valid email address"
                return
            }
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.register(username, email, password, firstName, lastName, phone)

                if (result.isSuccess) {
                    val authResponse = result.getOrNull()
                    authResponse?.let {
                        _userProfile.value = it.data.user
                        _registerState.value = AuthState.Success(it.data.user)
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Registration failed"
                    _errorMessage.value = error
                    _registerState.value = AuthState.Error(error)
                }
            } catch (e: Exception) {
                val error = e.message ?: "Registration failed"
                _errorMessage.value = error
                _registerState.value = AuthState.Error(error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserProfile() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = authRepository.getProfile()

                if (result.isSuccess) {
                    _userProfile.value = result.getOrNull()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = authRepository.updateProfile(firstName, lastName, phone)

                if (result.isSuccess) {
                    _userProfile.value = result.getOrNull()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Update failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Update failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        when {
            currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                _errorMessage.value = "Please fill in all fields"
                return
            }
            newPassword != confirmPassword -> {
                _errorMessage.value = "New passwords do not match"
                return
            }
            newPassword.length < 6 -> {
                _errorMessage.value = "New password must be at least 6 characters"
                return
            }
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = authRepository.changePassword(currentPassword, newPassword)

                if (result.isSuccess) {
                    _errorMessage.value = null
                    // You might want to show a success message here
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Password change failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Password change failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _userProfile.value = null
                _loginState.value = AuthState.LoggedOut
            } catch (e: Exception) {
                // Log error but still clear user data
                _userProfile.value = null
                _loginState.value = AuthState.LoggedOut
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email address"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = authRepository.forgotPassword(email)

                if (result.isSuccess) {
                    _errorMessage.value = null
                    // Show success message
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send reset email"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkAuthStatus() {
        if (authRepository.isLoggedIn()) {
            loadUserProfile()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}

sealed class AuthState {
    object LoggedOut : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}