package com.electricmonitor.mobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.electricmonitor.mobile.data.models.*
import com.electricmonitor.mobile.data.repositories.DeviceRepository
import kotlinx.coroutines.launch

class DeviceViewModel : ViewModel() {

    private val deviceRepository = DeviceRepository()

    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices

    private val _selectedDevice = MutableLiveData<DeviceDetail?>()
    val selectedDevice: LiveData<DeviceDetail?> = _selectedDevice

    private val _powerData = MutableLiveData<List<PowerDataPoint>>()
    val powerData: LiveData<List<PowerDataPoint>> = _powerData

    private val _realTimeData = MutableLiveData<RealTimeData?>()
    val realTimeData: LiveData<RealTimeData?> = _realTimeData

    private val _deviceStats = MutableLiveData<DeviceStatsContainer?>()
    val deviceStats: LiveData<DeviceStatsContainer?> = _deviceStats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _needPasswordVerification = MutableLiveData<Triple<String, String?, String?>?>()
    val needPasswordVerification: LiveData<Triple<String, String?, String?>?> = _needPasswordVerification

    fun loadDevices(refresh: Boolean = false) {
        if (refresh) {
            _isRefreshing.value = true
        } else {
            _isLoading.value = true
        }

        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.getDevices()

                if (result.isSuccess) {
                    _devices.value = result.getOrNull() ?: emptyList()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load devices"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load devices"
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun loadDevice(deviceId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.getDevice(deviceId)

                if (result.isSuccess) {
                    _selectedDevice.value = result.getOrNull()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load device"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load device"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDevice(
        name: String,
        description: String? = null,
        maxPower: Double = 500.0,
        location: DeviceLocation? = null
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Device name is required"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.createDevice(name.trim(), description?.trim(), maxPower, location)

                if (result.isSuccess) {
                    _successMessage.value = "Device created successfully"
                    loadDevices() // Refresh the list
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to create device"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to create device"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDevice(
        deviceId: String,
        name: String? = null,
        description: String? = null,
        location: DeviceLocation? = null,
        configuration: DeviceConfiguration? = null
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.updateDevice(deviceId, name?.trim(), description?.trim(), location, configuration)

                if (result.isSuccess) {
                    _successMessage.value = "Device updated successfully"
                    loadDevice(deviceId) // Refresh the device details
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update device"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update device"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDevice(deviceId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.deleteDevice(deviceId)

                if (result.isSuccess) {
                    _successMessage.value = "Device deleted successfully"
                    loadDevices() // Refresh the list
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete device"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete device"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun controlDevice(deviceId: String, command: String, reason: String? = null) {
        if (command.isBlank()) {
            _errorMessage.value = "Command is required"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.controlDevice(deviceId, command, reason)

                if (result.isSuccess) {
                    _successMessage.value = "Command sent successfully"
                    loadDevice(deviceId) // Refresh device status
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to send command"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send command"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePowerLimit(deviceId: String, maxPower: Double) {
        if (maxPower <= 0) {
            _errorMessage.value = "Power limit must be greater than 0"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.updatePowerLimit(deviceId, maxPower)

                if (result.isSuccess) {
                    _successMessage.value = "Power limit updated successfully"
                    loadDevice(deviceId) // Refresh device details
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update power limit"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update power limit"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPowerData(
        deviceId: String,
        startDate: String? = null,
        endDate: String? = null,
        interval: String = "hour"
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.getDevicePowerData(deviceId, startDate, endDate, interval)

                if (result.isSuccess) {
                    _powerData.value = result.getOrNull()?.powerData ?: emptyList()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load power data"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load power data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRealTimeData(deviceId: String) {
        viewModelScope.launch {
            try {
                val result = deviceRepository.getRealTimeData(deviceId)

                if (result.isSuccess) {
                    _realTimeData.value = result.getOrNull()
                }
            } catch (e: Exception) {
                // Don't show error for real-time data as it's polled frequently
            }
        }
    }

    fun loadDeviceStatistics(deviceId: String, days: Int = 30) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = deviceRepository.getDeviceStatistics(deviceId, days)

                if (result.isSuccess) {
                    _deviceStats.value = result.getOrNull()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load statistics"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load statistics"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSelectedDevice() {
        _selectedDevice.value = null
    }

    fun addExistingDevice(deviceId: String, name: String?, description: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = deviceRepository.addExistingDevice(deviceId, name, description)

                if (result.isSuccess) {
                    _successMessage.value = "Device added successfully"
                    loadDevices() // Refresh device list
                } else {
                    val exception = result.exceptionOrNull()
                    val errorMessage = exception?.message ?: "Unknown error"

                    // Проверяем, нужна ли верификация пароля владельца
                    if (errorMessage.contains("verify owner password") ||
                        errorMessage.contains("belongs to another user")) {
                        _needPasswordVerification.value = Triple(deviceId, name, description)
                    } else {
                        _errorMessage.value = errorMessage
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addSharedDevice(deviceId: String, name: String?, description: String?, ownerPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = deviceRepository.addSharedDevice(deviceId, name, description, ownerPassword)

                if (result.isSuccess) {
                    _successMessage.value = "Shared device added successfully"
                    _needPasswordVerification.value = null // Clear verification state
                    loadDevices() // Refresh device list
                } else {
                    val exception = result.exceptionOrNull()
                    _errorMessage.value = exception?.message ?: "Failed to add shared device"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        _needPasswordVerification.value = null
    }
}