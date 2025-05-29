package com.electricmonitor.mobile.data.api

import com.electricmonitor.mobile.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface DeviceApi {

    @GET("api/devices")
    suspend fun getDevices(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ListResponse<DeviceListContainer>>

    @GET("api/devices/{deviceId}")
    suspend fun getDevice(@Path("deviceId") deviceId: String): Response<ApiResponse<DeviceDetailResponseData>>

    @POST("api/devices")
    suspend fun createDevice(@Body request: CreateDeviceRequest): Response<ApiResponse<DeviceResponseData>>

    @POST("api/devices/add-existing")
    suspend fun addExistingDevice(@Body request: AddDeviceRequest): Response<ApiResponse<DeviceResponseData>>

    @POST("api/devices/add-shared")
    suspend fun addSharedDevice(@Body request: AddSharedDeviceRequest): Response<ApiResponse<DeviceResponseData>>

    @PUT("api/devices/{deviceId}")
    suspend fun updateDevice(
        @Path("deviceId") deviceId: String,
        @Body request: UpdateDeviceRequest
    ): Response<ApiResponse<DeviceResponseData>>

    @DELETE("api/devices/{deviceId}")
    suspend fun deleteDevice(@Path("deviceId") deviceId: String): Response<ApiResponse<Nothing>>

    @POST("api/devices/{deviceId}/control")
    suspend fun controlDevice(
        @Path("deviceId") deviceId: String,
        @Body request: DeviceControlRequest
    ): Response<ControlResponse>

    @PUT("api/devices/{deviceId}/power-limit")
    suspend fun updatePowerLimit(
        @Path("deviceId") deviceId: String,
        @Body request: PowerLimitRequest
    ): Response<ControlResponse>

    @POST("api/devices/{deviceId}/share")
    suspend fun shareDevice(
        @Path("deviceId") deviceId: String,
        @Body request: ShareDeviceRequest
    ): Response<ApiResponse<ShareData>>

    @DELETE("api/devices/{deviceId}/share/{userId}")
    suspend fun removeDeviceSharing(
        @Path("deviceId") deviceId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Nothing>>

    @GET("api/devices/{deviceId}/power-data")
    suspend fun getDevicePowerData(
        @Path("deviceId") deviceId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("interval") interval: String = "hour",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<ListResponse<PowerDataContainer>>

    @GET("api/devices/{deviceId}/alerts")
    suspend fun getDeviceAlerts(
        @Path("deviceId") deviceId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null
    ): Response<ListResponse<AlertContainer>>

    @GET("api/devices/{deviceId}/statistics")
    suspend fun getDeviceStatistics(
        @Path("deviceId") deviceId: String,
        @Query("days") days: Int = 30
    ): Response<ApiResponse<DeviceStatsContainer>>
}