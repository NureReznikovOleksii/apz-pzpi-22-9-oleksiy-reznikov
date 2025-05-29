package com.electricmonitor.mobile.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.electricmonitor.mobile.R
import com.electricmonitor.mobile.data.models.Device
import com.electricmonitor.mobile.databinding.ItemDeviceBinding
import java.text.SimpleDateFormat
import java.util.*

class DevicesAdapter(
    private val onDeviceClick: (Device) -> Unit
) : ListAdapter<Device, DevicesAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding, onDeviceClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onDeviceClick: (Device) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device) {
            binding.apply {
                // Device basic info
                textViewDeviceName.text = device.name
                textViewDeviceId.text = device.deviceId
                textViewLocation.text = device.location?.city ?: "No location"

                // Power consumption
                val totalPower = device.currentData.totalPower
                textViewPowerConsumption.text = "${totalPower.toInt()}W"

                // Connection status
                val isOnline = device.status.isOnline
                val isConnected = device.status.isConnected

                when {
                    isOnline && isConnected -> {
                        imageViewStatus.setImageResource(R.drawable.ic_status_online)
                        textViewStatus.text = "Online"
                        textViewStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_online))
                    }
                    isOnline && !isConnected -> {
                        imageViewStatus.setImageResource(R.drawable.ic_status_warning)
                        textViewStatus.text = "Disconnected"
                        textViewStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_warning))
                    }
                    else -> {
                        imageViewStatus.setImageResource(R.drawable.ic_status_offline)
                        textViewStatus.text = "Offline"
                        textViewStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_offline))
                    }
                }

                // Last seen
                device.status.lastSeen?.let { lastSeen ->
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

                // Power limit indicator
                val maxPower = device.configuration.maxPower
                progressBarPower.max = maxPower.toInt()
                progressBarPower.progress = totalPower.toInt()

                // Power limit warning
                if (totalPower > maxPower * 0.8) {
                    progressBarPower.progressTintList = ContextCompat.getColorStateList(
                        itemView.context, R.color.status_warning
                    )
                } else if (totalPower > maxPower * 0.9) {
                    progressBarPower.progressTintList = ContextCompat.getColorStateList(
                        itemView.context, R.color.status_offline
                    )
                } else {
                    progressBarPower.progressTintList = ContextCompat.getColorStateList(
                        itemView.context, R.color.primary_color
                    )
                }

                textViewPowerLimit.text = "/${maxPower.toInt()}W"

                // Room status
                val room1Status = if (device.currentData.room1Connected) "✓" else "✗"
                val room2Status = if (device.currentData.room2Connected) "✓" else "✗"
                textViewRoomStatus.text = "Room 1: $room1Status  Room 2: $room2Status"

                // Owner/Shared indicator
                if (device.access.isOwner) {
                    imageViewOwnership.setImageResource(R.drawable.ic_owner)
                    textViewOwnership.text = "Owner"
                } else {
                    imageViewOwnership.setImageResource(R.drawable.ic_shared)
                    textViewOwnership.text = "Shared"
                }

                // Click listener
                root.setOnClickListener {
                    onDeviceClick(device)
                }
            }
        }
    }

    private class DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem == newItem
        }
    }
}