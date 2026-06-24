package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.CrimsonDatabase
import com.example.data.CrimsonRepository
import com.example.util.CycleNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CycleNotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CycleNotificationRecv"
        private const val CHANNEL_ID = "crimson_predictions_channel"
        private const val NOTIFICATION_ID = 404
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule notification on device boot
            Log.d(TAG, "Device booted. Rescheduling prediction alarm.")
            rescheduleAlarmOnBoot(context)
        } else {
            // Standard alarm trigger
            Log.d(TAG, "Alarm received! Building and posting prediction notification.")
            postNotification(context)
        }
    }

    private fun postNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 1. Create Notification Channel on Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cycle Predictions"
            val descriptionText = "Friendly notifications 2 days prior to your predicted period starting."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Build content Intent to launch CrimsonCare when clicked
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build Notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Built-in recognizable system icon
            .setContentTitle("CrimsonCare")
            .setContentText("Your cycle is expected to start in about 2 days.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 4. Post Notification
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Successfully posted local prediction notification.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification", e)
        }
    }

    private fun rescheduleAlarmOnBoot(context: Context) {
        val database = CrimsonDatabase.getDatabase(context.applicationContext)
        val repository = CrimsonRepository(database.crimsonDao())
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repository.userSettings.firstOrNull()
                CycleNotificationScheduler.schedulePredictionNotification(context.applicationContext, settings)
                Log.d(TAG, "Successfully rescheduled alarm on boot: $settings")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarm on boot", e)
            }
        }
    }
}
