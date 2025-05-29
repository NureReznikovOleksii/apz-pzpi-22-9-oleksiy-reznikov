package com.electricmonitor.mobile.data.api

import com.electricmonitor.mobile.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserProfileData>>

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserProfileData>>

    @PUT("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Nothing>>

    @GET("api/auth/verify-token")
    suspend fun verifyToken(): Response<ApiResponse<AuthData>>

    @POST("api/auth/refresh-token")
    suspend fun refreshToken(): Response<ApiResponse<TokenData>>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Nothing>>

    @DELETE("api/auth/delete-account")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): Response<ApiResponse<Nothing>>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Nothing>>
}