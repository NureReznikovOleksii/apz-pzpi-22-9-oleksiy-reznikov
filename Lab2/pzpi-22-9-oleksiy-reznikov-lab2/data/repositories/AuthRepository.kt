package com.electricmonitor.mobile.data.repositories

import android.content.Context
import com.electricmonitor.mobile.data.api.AuthApi
import com.electricmonitor.mobile.data.models.*
import com.electricmonitor.mobile.data.network.NetworkModule
import com.electricmonitor.mobile.data.network.handleResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {

    private val authApi: AuthApi = NetworkModule.getAuthApi()

    suspend fun login(identifier: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(identifier, password)
                val response = authApi.login(request)
                val result = response.handleResponse()

                // Save token if successful
                if (result.isSuccess) {
                    result.getOrNull()?.data?.token?.let { token ->
                        NetworkModule.setAuthToken(token, context)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String? = null
    ): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(username, email, password, firstName, lastName, phone)
                val response = authApi.register(request)
                val result = response.handleResponse()

                // Save token if successful
                if (result.isSuccess) {
                    result.getOrNull()?.data?.token?.let { token ->
                        NetworkModule.setAuthToken(token, context)
                    }
                }

                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getProfile(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.getProfile()
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.user?.let { user ->
                        Result.success(user)
                    } ?: Result.failure(Exception("User data not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null,
        profile: UserProfile? = null
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateProfileRequest(firstName, lastName, phone, profile)
                val response = authApi.updateProfile(request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.user?.let { user ->
                        Result.success(user)
                    } ?: Result.failure(Exception("User data not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ChangePasswordRequest(currentPassword, newPassword)
                val response = authApi.changePassword(request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun verifyToken(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.verifyToken()
                val result = response.handleResponse()
                Result.success(result.isSuccess)
            } catch (e: Exception) {
                Result.success(false) // Token is invalid
            }
        }
    }

    suspend fun refreshToken(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.refreshToken()
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.token?.let { token ->
                        NetworkModule.setAuthToken(token, context)
                        Result.success(token)
                    } ?: Result.failure(Exception("Token not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                authApi.logout()
                NetworkModule.clearAuthToken(context)
                Result.success(Unit)
            } catch (e: Exception) {
                // Clear token anyway
                NetworkModule.clearAuthToken(context)
                Result.success(Unit)
            }
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = DeleteAccountRequest(password)
                val response = authApi.deleteAccount(request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    NetworkModule.clearAuthToken(context)
                    Result.success(Unit)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ForgotPasswordRequest(email)
                val response = authApi.forgotPassword(request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun isLoggedIn(): Boolean {
        val sharedPrefs = context.getSharedPreferences("ElectricMonitorPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("auth_token", null) != null
    }

    fun getCurrentToken(): String? {
        val sharedPrefs = context.getSharedPreferences("ElectricMonitorPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("auth_token", null)
    }
}