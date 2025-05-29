package com.electricmonitor.mobile.ui.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.electricmonitor.mobile.R
import com.electricmonitor.mobile.data.models.DeviceDetail
import com.electricmonitor.mobile.data.models.DeviceStatus
import com.electricmonitor.mobile.data.models.PowerDataPoint
import com.electricmonitor.mobile.databinding.FragmentDeviceDetailBinding
import com.electricmonitor.mobile.ui.viewmodels.DeviceViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DeviceDetailFragment : Fragment() {

    private var _binding: FragmentDeviceDetailBinding? = null
    private val binding get() = _binding!!

    private val args: DeviceDetailFragmentArgs by navArgs()
    private val deviceViewModel: DeviceViewModel by viewModels()

    private var realTimeHandler: Handler? = null
    private var realTimeRunnable: Runnable? = null
    private var currentDevice: DeviceDetail? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupChart()
        observeViewModel()

        // Load device details
        deviceViewModel.loadDevice(args.deviceId)
        deviceViewModel.loadPowerData(args.deviceId)

        // Start real-time updates
        startRealTimeUpdates()
    }

    private fun setupUI() {
        binding.apply {
            // Toolbar
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            toolbar.inflateMenu(R.menu.menu_device_detail)
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        // Navigate to edit device
                        true
                    }
                    R.id.action_settings -> {
                        // Navigate to device settings
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmation()
                        true
                    }
                    else -> false
                }
            }

            // Control buttons
            buttonConnect.setOnClickListener {
                controlDevice("connect")
            }

            buttonDisconnect.setOnClickListener {
                controlDevice("disconnect")
            }

            // Power limit button
            buttonUpdatePowerLimit.setOnClickListener {
                showPowerLimitDialog()
            }

            // Refresh button
            swipeRefreshLayout.setOnRefreshListener {
                deviceViewModel.loadDevice(args.deviceId)
                deviceViewModel.loadPowerData(args.deviceId)
            }

            swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_color,
                R.color.secondary_color
            )
        }
    }

    private fun setupChart() {
        binding.chartPowerConsumption.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            // X axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            }

            // Y axis
            axisLeft.apply {
                setDrawGridLines(true)
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            // Legend
            legend.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            }
        }
    }

    private fun observeViewModel() {
        // Observe device details
        deviceViewModel.selectedDevice.observe(viewLifecycleOwner) { device: DeviceDetail? ->
            device?.let {
                currentDevice = it
                updateDeviceInfo(it)
            }
        }

        // Observe power data
        deviceViewModel.powerData.observe(viewLifecycleOwner) { powerData ->
            if (powerData.isNotEmpty()) {
                updateChart(powerData)
            }
        }

        // Observe real-time data
        deviceViewModel.realTimeData.observe(viewLifecycleOwner) { realTimeData ->
            realTimeData?.let {
                updateRealTimeInfo(it.currentStatus, it.latestReadings.firstOrNull())
            }
        }

        // Observe loading state
        deviceViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                swipeRefreshLayout.isRefreshing = isLoading
            }
        }

        // Observe error messages
        deviceViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                deviceViewModel.clearMessages()
            }
        }

        // Observe success messages
        deviceViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                deviceViewModel.clearMessages()
            }
        }
    }

    private fun updateDeviceInfo(device: DeviceDetail) {
        binding.apply {
            // Basic info
            toolbar.title = device.name
            textViewDeviceId.text = "ID: ${device.deviceId}"
            textViewDescription.text = device.description ?: "No description"
            textViewLocation.text = device.location?.city ?: "No location"

            // Current power
            val totalPower = device.currentData.totalPower
            textViewCurrentPower.text = "${totalPower.toInt()}W"
            textViewPowerLimit.text = "/${device.configuration.maxPower.toInt()}W"

            // Progress bar
            progressBarCurrentPower.max = device.configuration.maxPower.toInt()
            progressBarCurrentPower.progress = totalPower.toInt()

            // Room powers
            textViewRoom1Power.text = "${device.currentData.room1Power.toInt()}W"
            textViewRoom2Power.text = "${device.currentData.room2Power.toInt()}W"

            // Room connections
            imageViewRoom1Status.setImageResource(
                if (device.currentData.room1Connected) R.drawable.ic_connected
                else R.drawable.ic_disconnected
            )
            imageViewRoom2Status.setImageResource(
                if (device.currentData.room2Connected) R.drawable.ic_connected
                else R.drawable.ic_disconnected
            )

            textViewRoom1Status.text = if (device.currentData.room1Connected) "Connected" else "Disconnected"
            textViewRoom2Status.text = if (device.currentData.room2Connected) "Connected" else "Disconnected"

            // Device status
            updateDeviceStatus(device.status)

            // Control buttons visibility - проверяем права доступа
            val canControl = device.access?.permissions?.canControl ?: true
            val hasAccess = device.access?.hasAccess ?: true

            // Показываем кнопки только если есть доступ и права на управление
            val showControlButtons = hasAccess && canControl
            buttonConnect.visibility = if (showControlButtons) View.VISIBLE else View.GONE
            buttonDisconnect.visibility = if (showControlButtons) View.VISIBLE else View.GONE
            buttonUpdatePowerLimit.visibility = if (showControlButtons) View.VISIBLE else View.GONE

            // Добавим логирование для отладки
            android.util.Log.d("DeviceDetail",
                "Device updated: ${device.name}, Power: ${totalPower}W, " +
                        "HasAccess: $hasAccess, CanControl: $canControl, ShowButtons: $showControlButtons")
        }
    }

    private fun updateDeviceStatus(status: DeviceStatus) {
        binding.apply {
            when {
                status.isOnline && status.isConnected -> {
                    imageViewDeviceStatus.setImageResource(R.drawable.ic_status_online)
                    // textViewDeviceStatus.text = "Online & Connected"
                    // textViewDeviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_online))
                }
                status.isOnline && !status.isConnected -> {
                    imageViewDeviceStatus.setImageResource(R.drawable.ic_status_warning)
                    // textViewDeviceStatus.text = "Online but Disconnected"
                    // textViewDeviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_warning))
                }
                else -> {
                    imageViewDeviceStatus.setImageResource(R.drawable.ic_status_offline)
                    // textViewDeviceStatus.text = "Offline"
                    // textViewDeviceStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_offline))
                }
            }

            // Last seen
            status.lastSeen?.let { lastSeen ->
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = dateFormat.parse(lastSeen)
                    val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    textViewLastSeen.text = "Last seen: ${timeFormat.format(date)}"
                } catch (e: Exception) {
                    textViewLastSeen.text = "Last seen: $lastSeen"
                }
            } ?: run {
                textViewLastSeen.text = "Last seen: Never"
            }
        }
    }

    private fun updateRealTimeInfo(
        status: DeviceStatus,
        latestReading: PowerDataPoint?
    ) {
        latestReading?.let { reading ->
            binding.apply {
                textViewCurrentPower.text = "${reading.totalPower.toInt()}W"
                textViewRoom1Power.text = "${reading.room1Power.toInt()}W"
                textViewRoom2Power.text = "${reading.room2Power.toInt()}W"

                progressBarCurrentPower.progress = reading.totalPower.toInt()

                // Update room status
                imageViewRoom1Status.setImageResource(
                    if (reading.room1Connected) R.drawable.ic_connected
                    else R.drawable.ic_disconnected
                )
                imageViewRoom2Status.setImageResource(
                    if (reading.room2Connected) R.drawable.ic_connected
                    else R.drawable.ic_disconnected
                )

                textViewRoom1Status.text = if (reading.room1Connected) "Connected" else "Disconnected"
                textViewRoom2Status.text = if (reading.room2Connected) "Connected" else "Disconnected"
            }
        }

        updateDeviceStatus(status)
    }

    private fun updateChart(powerData: List<PowerDataPoint>) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        powerData.forEachIndexed { index, data ->
            entries.add(Entry(index.toFloat(), data.totalPower.toFloat()))

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(data.timestamp)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                labels.add(timeFormat.format(date))
            } catch (e: Exception) {
                labels.add(index.toString())
            }
        }

        val dataSet = LineDataSet(entries, "Power Consumption (W)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.primary_color)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 0f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.primary_color)
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)

        binding.chartPowerConsumption.apply {
            data = lineData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate()
        }
    }

    private fun controlDevice(command: String) {
        deviceViewModel.controlDevice(args.deviceId, command, "User control from mobile app")
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Device")
            .setMessage("Are you sure you want to reset this device? This will restart the device and may cause temporary disconnection.")
            .setPositiveButton("Reset") { _, _ ->
                controlDevice("reset")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Device")
            .setMessage("Are you sure you want to delete this device? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deviceViewModel.deleteDevice(args.deviceId)
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPowerLimitDialog() {
        val currentLimit = currentDevice?.configuration?.maxPower?.toInt() ?: 500

        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setText(currentLimit.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Update Power Limit")
            .setMessage("Enter new power limit (W):")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newLimit = input.text.toString().toDoubleOrNull()
                if (newLimit != null && newLimit > 0) {
                    deviceViewModel.updatePowerLimit(args.deviceId, newLimit)
                } else {
                    showError("Please enter a valid power limit")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startRealTimeUpdates() {
        realTimeHandler = Handler(Looper.getMainLooper())
        realTimeRunnable = object : Runnable {
            override fun run() {
                deviceViewModel.loadRealTimeData(args.deviceId)
                realTimeHandler?.postDelayed(this, 5000) // Update every 5 seconds
            }
        }
        realTimeHandler?.post(realTimeRunnable!!)
    }

    private fun stopRealTimeUpdates() {
        realTimeRunnable?.let { realTimeHandler?.removeCallbacks(it) }
        realTimeHandler = null
        realTimeRunnable = null
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        stopRealTimeUpdates()
    }

    override fun onResume() {
        super.onResume()
        startRealTimeUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopRealTimeUpdates()
        _binding = null
    }
}