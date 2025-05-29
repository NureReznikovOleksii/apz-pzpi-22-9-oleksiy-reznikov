package com.electricmonitor.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.electricmonitor.mobile.data.network.NetworkModule

class ElectricMonitorApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ALERTS = "alerts_channel"
        const val NOTIFICATION_CHANNEL_UPDATES = "updates_channel"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize NetworkModule
        NetworkModule.initialize(this)

        // Create notification channels
        createNotificationChannels()

        // Initialize other components if needed
        initializeComponents()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Alerts Channel
            val alertsChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ALERTS,
                getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_alerts_desc)
                enableLights(true)
                lightColor = getColor(R.color.primary_color)
                enableVibration(true)
                setShowBadge(true)
            }

            // Updates Channel
            val updatesChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_UPDATES,
                "App Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for app updates and general information"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(updatesChannel)
        }
    }

    private fun initializeComponents() {
        // Initialize any other required components here
        // For example: crash reporting, analytics, etc.
    }
}