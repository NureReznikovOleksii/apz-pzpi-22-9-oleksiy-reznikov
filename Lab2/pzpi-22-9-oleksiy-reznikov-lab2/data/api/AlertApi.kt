package com.electricmonitor.mobile.data.api

import com.electricmonitor.mobile.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AlertApi {

    @GET("api/alerts")
    suspend fun getAlerts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
        @Query("deviceId") deviceId: String? = null
    ): Response<ListResponse<AlertContainer>>

    @GET("api/alerts/{alertId}")
    suspend fun getAlert(@Path("alertId") alertId: String): Response<ApiResponse<AlertResponseData>>

    @PUT("api/alerts/{alertId}/resolve")
    suspend fun resolveAlert(@Path("alertId") alertId: String): Response<ApiResponse<AlertResponseData>>

    @PUT("api/alerts/{alertId}/acknowledge")
    suspend fun acknowledgeAlert(@Path("alertId") alertId: String): Response<ApiResponse<AlertResponseData>>

    @DELETE("api/alerts/{alertId}")
    suspend fun deleteAlert(@Path("alertId") alertId: String): Response<ApiResponse<Nothing>>

    @GET("api/alerts/unread-count")
    suspend fun getUnreadAlertsCount(): Response<ApiResponse<AlertCountData>>
}

data class AlertResponseData(
    val alert: Alert
)

data class AlertCountData(
    val count: Int
)