package com.electricmonitor.mobile.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val role: String = "user",
    val isActive: Boolean = true,
    val isEmailVerified: Boolean = false,
    val profile: UserProfile? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val lastLogin: String? = null
) : Parcelable

@Parcelize
data class UserProfile(
    val avatar: String? = null,
    val timezone: String = "UTC",
    val language: String = "en",
    val notifications: NotificationSettings = NotificationSettings()
) : Parcelable

@Parcelize
data class NotificationSettings(
    val email: Boolean = true,
    val sms: Boolean = false,
    val push: Boolean = true
) : Parcelable

data class LoginRequest(
    val identifier: String, // email or username
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData
)

data class AuthData(
    val user: User,
    val token: String
)