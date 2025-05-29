package com.electricmonitor.mobile.data.models

data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val profile: UserProfile? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class DeleteAccountRequest(
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class UserProfileData(
    val user: User
)

data class TokenData(
    val token: String
)