package com.electricmonitor.mobile.data.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

data class ListResponse<T>(
    val success: Boolean,
    val count: Int,
    val totalCount: Int? = null,
    val data: T,
    val pagination: Pagination? = null
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class DeviceListContainer(
    val devices: List<Device>
)

data class ErrorResponse(
    val error: String,
    val message: String? = null,
    val statusCode: Int? = null
)

// Health check response
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val uptime: Double,
    val mongodb: String,
    val mqtt: String
)

// Real-time data response
data class RealTimeResponse(
    val success: Boolean,
    val data: RealTimeData
)

data class RealTimeData(
    val deviceId: String,
    val currentStatus: DeviceStatus,
    val latestReadings: List<PowerDataPoint>,
    val timestamp: String
)

// Control command response
data class ControlResponse(
    val success: Boolean,
    val message: String,
    val data: ControlData
)

data class ControlData(
    val command: String,
    val deviceId: String,
    val timestamp: String
)