package com.electricmonitor.mobile.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Alert(
    val _id: String,
    val device: String, // device id
    val deviceId: String,
    val user: String, // user id
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val data: AlertData? = null,
    val status: AlertStatus,
    val isRead: Boolean = false,
    val acknowledgedBy: String? = null,
    val acknowledgedAt: String? = null,
    val resolvedBy: String? = null,
    val resolvedAt: String? = null,
    val resolution: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
) : Parcelable

enum class AlertType {
    POWER_LIMIT_EXCEEDED,
    ROOM_DISCONNECTED,
    ROOM_RECONNECTED,
    DEVICE_OFFLINE,
    DEVICE_ONLINE,
    CONFIGURATION_CHANGED,
    SYSTEM_ERROR,
    MAINTENANCE_REQUIRED
}

enum class AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    IGNORED
}

@Parcelize
data class AlertData(
    val room: String? = null, // room1, room2, both, device
    val powerValue: Double? = null,
    val powerLimit: Double? = null,
    // ИСПРАВЛЕНО: Добавлен @RawValue для Map типов
    val previousState: @RawValue Map<String, Any>? = null,
    val currentState: @RawValue Map<String, Any>? = null,
    val additionalInfo: @RawValue Map<String, Any>? = null
) : Parcelable

data class AlertResponse(
    val success: Boolean,
    val count: Int,
    val totalCount: Int,
    val data: AlertContainer
)

data class AlertContainer(
    val alerts: List<Alert>,
    val pagination: Pagination
)

data class AlertStatistics(
    val period: StatisticsPeriod,
    val statistics: AlertStats
)

data class AlertStats(
    val totalAlerts: Int = 0,
    val activeAlerts: Int = 0,
    val acknowledgedAlerts: Int = 0,
    val resolvedAlerts: Int = 0,
    val criticalAlerts: Int = 0,
    val highAlerts: Int = 0,
    val unreadAlerts: Int = 0
)

data class AlertActionRequest(
    val resolution: String? = null,
    val acknowledgment: String? = null
)

data class BulkAlertRequest(
    val alertIds: List<String>,
    val resolution: String? = null
)