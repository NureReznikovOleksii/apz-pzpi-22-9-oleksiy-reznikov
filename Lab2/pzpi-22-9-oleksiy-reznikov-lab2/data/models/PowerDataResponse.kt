package com.electricmonitor.mobile.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class PowerDataResponse(
    val success: Boolean,
    val count: Int,
    val totalCount: Int? = null,
    val data: PowerDataContainer
)

data class PowerDataContainer(
    val deviceId: String,
    val interval: String = "raw",
    val powerData: List<PowerDataPoint>,
    val pagination: Pagination? = null
)

@Parcelize
data class PowerDataPoint(
    val timestamp: String,
    val room1Power: Double,
    val room2Power: Double,
    val totalPower: Double,
    val maxPowerLimit: Double,
    val room1Connected: Boolean,
    val room2Connected: Boolean,
    val deviceConnected: Boolean,
    val metadata: PowerMetadata? = null,
    val alerts: List<PowerAlert> = emptyList()
) : Parcelable

@Parcelize
data class PowerMetadata(
    val voltage: Double? = null,
    val current: Double? = null,
    val frequency: Double? = null,
    val powerFactor: Double? = null,
    val temperature: Double? = null,
    val humidity: Double? = null
) : Parcelable

@Parcelize
data class PowerAlert(
    val type: String,
    val room: String? = null,
    val message: String? = null,
    val value: Double? = null,
    val timestamp: String
) : Parcelable

data class PowerStatistics(
    val deviceId: String,
    val period: StatisticsPeriod,
    val statistics: BasicStats,
    val groupedData: List<GroupedPowerData>,
    val trends: PowerTrends
)

data class StatisticsPeriod(
    val days: Int,
    val startDate: String,
    val endDate: String
)

data class BasicStats(
    val totalDataPoints: Int = 0,
    val avgRoom1Power: Double = 0.0,
    val avgRoom2Power: Double = 0.0,
    val avgTotalPower: Double = 0.0,
    val maxTotalPower: Double = 0.0,
    val minTotalPower: Double = 0.0,
    val totalAlertsCount: Int = 0,
    val powerConsumption: Double = 0.0
)

data class GroupedPowerData(
    val _id: TimeGroup,
    val avgRoom1Power: Double,
    val avgRoom2Power: Double,
    val avgTotalPower: Double,
    val maxTotalPower: Double,
    val dataPoints: Int,
    val alertsCount: Int
)

data class TimeGroup(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int? = null,
    val minute: Int? = null
)

data class PowerTrends(
    val consumption: ConsumptionTrend,
    val peakUsage: List<PeakUsage>,
    val efficiency: EfficiencyMetrics
)

data class ConsumptionTrend(
    val trend: String, // increasing, decreasing, stable
    val percentage: String,
    val direction: String // up, down
)

data class PeakUsage(
    val hour: Int,
    val averagePower: Int,
    val maxPower: Double,
    val dataPoints: Int
)

data class EfficiencyMetrics(
    val efficiency: String,
    val utilizationRate: String,
    val wastePercentage: String,
    val optimalRange: OptimalRange
)

data class OptimalRange(
    val min: Double,
    val max: Double
)