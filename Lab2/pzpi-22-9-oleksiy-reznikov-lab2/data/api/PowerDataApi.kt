package com.electricmonitor.mobile.data.api

import com.electricmonitor.mobile.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface PowerDataApi {

    @GET("api/power-data/device/{deviceId}")
    suspend fun getPowerData(
        @Path("deviceId") deviceId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("interval") interval: String = "hour",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("latest") latest: Boolean = false
    ): Response<PowerDataResponse>

    @GET("api/power-data/device/{deviceId}/statistics")
    suspend fun getPowerStatistics(
        @Path("deviceId") deviceId: String,
        @Query("days") days: Int = 30,
        @Query("groupBy") groupBy: String = "day"
    ): Response<ApiResponse<PowerStatistics>>

    @GET("api/power-data/device/{deviceId}/realtime")
    suspend fun getRealTimeData(
        @Path("deviceId") deviceId: String,
        @Query("limit") limit: Int = 100
    ): Response<RealTimeResponse>

    @GET("api/power-data/device/{deviceId}/export")
    suspend fun exportPowerData(
        @Path("deviceId") deviceId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("format") format: String = "json"
    ): Response<ApiResponse<ExportData>>

    @GET("api/power-data/device/{deviceId}/alerts")
    suspend fun getPowerAlerts(
        @Path("deviceId") deviceId: String,
        @Query("days") days: Int = 30,
        @Query("severity") severity: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<PowerAlertsContainer>>

    @GET("api/power-data/comparison")
    suspend fun getPowerComparison(
        @Query("deviceIds") deviceIds: String, // comma-separated
        @Query("days") days: Int = 7,
        @Query("interval") interval: String = "day"
    ): Response<ApiResponse<ComparisonData>>
}

data class ExportData(
    val deviceId: String,
    val exportDate: String,
    val powerData: List<PowerDataPoint>
)

data class PowerAlertsContainer(
    val deviceId: String,
    val period: StatisticsPeriod,
    val alerts: List<Alert>,
    val statistics: AlertStats
)

data class ComparisonData(
    val period: StatisticsPeriod,
    val comparison: List<DeviceComparison>
)

data class DeviceComparison(
    val deviceId: String,
    val deviceName: String,
    val statistics: BasicStats,
    val owner: Boolean
)

data class AlertActionResponse(
    val alertId: String,
    val status: String? = null,
    val isRead: Boolean? = null,
    val resolvedAt: String? = null,
    val acknowledgedAt: String? = null,
    val resolution: String? = null
)

data class BulkActionResponse(
    val resolvedCount: Int? = null,
    val updatedCount: Int? = null,
    val alertIds: List<String>
)