package com.electricmonitor.mobile.data.repositories

import com.electricmonitor.mobile.data.api.DeviceApi
import com.electricmonitor.mobile.data.api.PowerDataApi
import com.electricmonitor.mobile.data.models.*
import com.electricmonitor.mobile.data.network.NetworkModule
import com.electricmonitor.mobile.data.network.handleResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceRepository {

    private val deviceApi: DeviceApi = NetworkModule.getDeviceApi()
    private val powerDataApi: PowerDataApi = NetworkModule.getPowerDataApi()

    suspend fun getDevices(page: Int = 1, limit: Int = 20): Result<List<Device>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deviceApi.getDevices(page, limit)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.devices?.let { devices ->
                        Result.success(devices)
                    } ?: Result.failure(Exception("Devices not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDevice(deviceId: String): Result<DeviceDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deviceApi.getDevice(deviceId)
                android.util.Log.d("DeviceRepository", "API Response: ${response.code()}")

                val result = response.handleResponse()

                if (result.isSuccess) {
                    val deviceDetail = result.getOrNull()?.data?.device
                    android.util.Log.d("DeviceRepository", "Device loaded: ${deviceDetail?.name}")

                    deviceDetail?.let { device ->
                        Result.success(device)
                    } ?: Result.failure(Exception("Device not found"))
                } else {
                    android.util.Log.e("DeviceRepository", "API Error: ${result.exceptionOrNull()}")
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                android.util.Log.e("DeviceRepository", "Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }

    suspend fun createDevice(
        name: String,
        description: String? = null,
        maxPower: Double = 500.0,
        location: DeviceLocation? = null
    ): Result<Device> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateDeviceRequest(name, description, maxPower, location)
                val response = deviceApi.createDevice(request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.device?.let { device ->
                        Result.success(device)
                    } ?: Result.failure(Exception("Device creation failed"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addExistingDevice(
        deviceId: String,
        name: String? = null,
        description: String? = null
    ): Result<Device> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AddDeviceRequest(deviceId, name, description)
                val response = deviceApi.addExistingDevice(request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.device?.let { device ->
                        Result.success(device)
                    } ?: Result.failure(Exception("Device addition failed"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateDevice(
        deviceId: String,
        name: String? = null,
        description: String? = null,
        location: DeviceLocation? = null,
        configuration: DeviceConfiguration? = null
    ): Result<Device> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateDeviceRequest(name, description, location, configuration)
                val response = deviceApi.updateDevice(deviceId, request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.device?.let { device ->
                        Result.success(device)
                    } ?: Result.failure(Exception("Device update failed"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteDevice(deviceId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deviceApi.deleteDevice(deviceId)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun controlDevice(deviceId: String, command: String, reason: String? = null): Result<ControlData> {
        return withContext(Dispatchers.IO) {
            try {
                val request = DeviceControlRequest(command, reason)
                val response = deviceApi.controlDevice(deviceId, request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.let { controlData ->
                        Result.success(controlData)
                    } ?: Result.failure(Exception("Control command failed"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updatePowerLimit(deviceId: String, maxPower: Double): Result<ControlData> {
        return withContext(Dispatchers.IO) {
            try {
                val request = PowerLimitRequest(maxPower)
                val response = deviceApi.updatePowerLimit(deviceId, request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.let { controlData ->
                        Result.success(controlData)
                    } ?: Result.failure(Exception("Power limit update failed"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun shareDevice(
        deviceId: String,
        email: String,
        permissions: DevicePermissions
    ): Result<ShareData> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ShareDeviceRequest(email, permissions)
                val response = deviceApi.shareDevice(deviceId, request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.let { shareData ->
                        Result.success(shareData)
                    } ?: Result.failure(Exception("Device sharing failed"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun removeDeviceSharing(deviceId: String, userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deviceApi.removeDeviceSharing(deviceId, userId)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDevicePowerData(
        deviceId: String,
        startDate: String? = null,
        endDate: String? = null,
        interval: String = "hour",
        page: Int = 1,
        limit: Int = 100
    ): Result<PowerDataContainer> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deviceApi.getDevicePowerData(deviceId, startDate, endDate, interval, page, limit)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.let { powerDataContainer ->
                        Result.success(powerDataContainer)
                    } ?: Result.failure(Exception("Power data not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDeviceAlerts(
        deviceId: String,
        page: Int = 1,
        limit: Int = 20,
        status: String? = null,
        severity: String? = null
    ): Result<AlertContainer> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deviceApi.getDeviceAlerts(deviceId, page, limit, status, severity)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.let { alertContainer ->
                        Result.success(alertContainer)
                    } ?: Result.failure(Exception("Alerts not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDeviceStatistics(deviceId: String, days: Int = 30): Result<DeviceStatsContainer> {
        return withContext(Dispatchers.IO) {
            try {
                val response = deviceApi.getDeviceStatistics(deviceId, days)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.let { statsContainer ->
                        Result.success(statsContainer)
                    } ?: Result.failure(Exception("Statistics not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getRealTimeData(deviceId: String, limit: Int = 100): Result<RealTimeData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = powerDataApi.getRealTimeData(deviceId, limit)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.let { realTimeData ->
                        Result.success(realTimeData)
                    } ?: Result.failure(Exception("Real-time data not found"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addSharedDevice(
        deviceId: String,
        name: String? = null,
        description: String? = null,
        ownerPassword: String
    ): Result<Device> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AddSharedDeviceRequest(deviceId, name, description, ownerPassword)
                val response = deviceApi.addSharedDevice(request)
                val result = response.handleResponse()

                if (result.isSuccess) {
                    result.getOrNull()?.data?.device?.let { device ->
                        Result.success(device)
                    } ?: Result.failure(Exception("Shared device addition failed"))
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}