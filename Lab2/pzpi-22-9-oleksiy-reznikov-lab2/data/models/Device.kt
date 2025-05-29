package com.electricmonitor.mobile.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName
import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type


class DevicePermissionsDeserializer : JsonDeserializer<DevicePermissions?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DevicePermissions? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // Обработка строкового значения
                when (json.asString) {
                    "all" -> DevicePermissions(
                        canView = true,
                        canControl = true,
                        canModifySettings = true
                    )
                    "read" -> DevicePermissions(
                        canView = true,
                        canControl = false,
                        canModifySettings = false
                    )
                    "control" -> DevicePermissions(
                        canView = true,
                        canControl = true,
                        canModifySettings = false
                    )
                    else -> DevicePermissions()
                }
            }
            json.isJsonObject -> {
                // Обработка объекта
                context?.deserialize(json, DevicePermissions::class.java)
            }
            else -> null
        }
    }
}

data class AddSharedDeviceRequest(
    val deviceId: String,
    val name: String? = null,
    val description: String? = null,
    val ownerPassword: String
)

@Parcelize
data class Device(
    val id: String,
    val deviceId: String,
    val name: String,
    val description: String? = null,
    val location: DeviceLocation? = null,
    val status: DeviceStatus,
    val configuration: DeviceConfiguration,
    val currentData: CurrentPowerData,
    val statistics: DeviceStatistics? = null,
    val owner: Owner,
    val access: DeviceAccess,
    val createdAt: String,
    val updatedAt: String? = null
) : Parcelable

@Parcelize
data class DeviceDetail(
    val id: String? = null,
    val deviceId: String,
    val name: String,
    val description: String? = null,
    val location: DeviceLocation? = null,
    val status: DeviceStatus,
    val configuration: DeviceConfiguration,
    val currentData: CurrentPowerData,
    val statistics: DeviceStatistics? = null,
    val owner: Owner? = null,
    val access: DeviceAccess? = null,
    val sharedWith: List<SharedUser> = emptyList(),
    val recentData: List<PowerDataPoint> = emptyList(),
    val createdAt: String,
    val updatedAt: String? = null
) : Parcelable

@Parcelize
data class Owner(
    @SerializedName("_id")
    val id: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
) : Parcelable

@Parcelize
data class DeviceAccess(
    val hasAccess: Boolean = true,
    val isOwner: Boolean = true,
    @JsonAdapter(DevicePermissionsDeserializer::class)
    val permissions: DevicePermissions? = null
) : Parcelable

@Parcelize
data class DevicePermissions(
    val canView: Boolean = true,
    val canControl: Boolean = true,
    val canModifySettings: Boolean = true
) : Parcelable

@Parcelize
data class DeviceLocation(
    val country: String? = null,
    val city: String? = null,
    val address: String? = null,
    val coordinates: Coordinates? = null
) : Parcelable

@Parcelize
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) : Parcelable

@Parcelize
data class DeviceStatus(
    val isOnline: Boolean = false,
    val isConnected: Boolean = false,
    val lastSeen: String? = null,
    val ipAddress: String? = null
) : Parcelable

@Parcelize
data class DeviceConfiguration(
    val alerts: AlertConfiguration,
    val maxPower: Double = 500.0,
    val rooms: List<RoomConfiguration> = emptyList(),
    val autoReconnect: Boolean = true,
    val reconnectDelay: Int = 30
) : Parcelable

@Parcelize
data class AlertConfiguration(
    val powerLimit: Boolean = true,
    val disconnection: Boolean = true,
    val reconnection: Boolean = true
) : Parcelable

@Parcelize
data class RoomConfiguration(
    @SerializedName("_id")
    val id: String? = null,
    val name: String,
    val maxPower: Double,
    val isActive: Boolean = true
) : Parcelable

@Parcelize
data class CurrentPowerData(
    val totalPower: Double = 0.0,
    val room1Power: Double = 0.0,
    val room2Power: Double = 0.0,
    val room1Connected: Boolean = false,
    val room2Connected: Boolean = false,
    val lastUpdated: String
) : Parcelable

@Parcelize
data class DeviceStatistics(
    val totalUptime: Long = 0L,
    val totalPowerConsumption: Double = 0.0,
    val alertsCount: Int = 0
) : Parcelable

@Parcelize
data class SharedUser(
    val userId: String,
    val username: String,
    val email: String,
    val permissions: DevicePermissions,
    val sharedAt: String
) : Parcelable

// Helper extension functions for backward compatibility
val Device.maxPower: Double get() = configuration.maxPower
val DeviceDetail.maxPower: Double get() = configuration.maxPower

// For CurrentPowerData timestamp compatibility
val CurrentPowerData.timestamp: String get() = lastUpdated

// Statistics Models (keeping existing ones for other features)
data class DeviceStats(
    val deviceId: String,
    val totalEnergyConsumed: Double,
    val averagePower: Double,
    val maxPower: Double,
    val minPower: Double,
    val uptimePercentage: Double,
    val room1Stats: RoomStats,
    val room2Stats: RoomStats,
    val dailyAverages: List<DailyAverage>,
    val period: StatsPeriod
)

data class RoomStats(
    val totalEnergy: Double,
    val averagePower: Double,
    val maxPower: Double,
    val connectionUptime: Double
)

data class DailyAverage(
    val date: String,
    val averagePower: Double,
    val totalEnergy: Double,
    val maxPower: Double
)

data class StatsPeriod(
    val startDate: String,
    val endDate: String,
    val days: Int
)

data class DeviceStatsContainer(
    val stats: DeviceStats
)

// Request Models (keeping existing ones)
data class CreateDeviceRequest(
    val name: String,
    val description: String? = null,
    val maxPower: Double = 500.0,
    val location: DeviceLocation? = null
)

data class AddDeviceRequest(
    val deviceId: String,
    val name: String? = null,
    val description: String? = null
)

data class UpdateDeviceRequest(
    val name: String? = null,
    val description: String? = null,
    val location: DeviceLocation? = null,
    val configuration: DeviceConfiguration? = null
)

data class DeviceControlRequest(
    val command: String, // connect, disconnect, reset, restart
    val reason: String? = null
)

data class PowerLimitRequest(
    val maxPower: Double
)

data class ShareDeviceRequest(
    val email: String,
    val permissions: DevicePermissions
)

// Response Models (keeping existing ones)
data class DeviceResponse(
    val success: Boolean,
    val message: String,
    val data: DeviceResponseData
)

data class DeviceResponseData(
    val device: Device
)

data class DeviceDetailResponse(
    val success: Boolean,
    val message: String,
    val data: DeviceDetailResponseData
)

data class DeviceDetailResponseData(
    val device: DeviceDetail
)

data class ShareData(
    val sharedUserId: String,
    val deviceId: String,
    val permissions: DevicePermissions,
    val sharedAt: String
)