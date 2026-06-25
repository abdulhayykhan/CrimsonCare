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

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device booted. Rescheduling all prediction and phase alarms.")
                rescheduleAlarmOnBoot(context)
            }
            "ACTION_OVULATION_ALARM" -> {
                Log.d(TAG, "Ovulation alarm received! Posting ovulation notification.")
                postOvulationNotification(context)
            }
            "ACTION_ONE_DAY_BEFORE_PERIOD" -> {
                Log.d(TAG, "One Day Before Period alarm received.")
                postCustomNotification(context, "Your cycle is predicted to start tomorrow. Stay prepared.", 406)
            }
            "ACTION_PMS_WARNING" -> {
                Log.d(TAG, "PMS Warning alarm received.")
                postCustomNotification(context, "Entering your PMS phase. Be gentle with yourself.", 407)
            }
            "ACTION_FERTILE_WINDOW_START" -> {
                Log.d(TAG, "Fertile Window Start alarm received.")
                postCustomNotification(context, "Your fertile window is opening.", 408)
            }
            "ACTION_FERTILE_WINDOW_END" -> {
                Log.d(TAG, "Fertile Window End alarm received.")
                postCustomNotification(context, "Your fertile window has concluded.", 409)
            }
            "ACTION_POST_CYCLE_HEALTH_CHECK" -> {
                Log.d(TAG, "Post-Cycle Health Check alarm received.")
                postCustomNotification(context, "Time for your routine monthly health check-in.", 410)
            }
            else -> {
                Log.d(TAG, "Alarm received! Building and posting standard prediction notification.")
                postNotification(context)
            }
        }
    }

    private fun postCustomNotification(context: Context, text: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cycle Predictions"
            val descriptionText = "Friendly notifications about your cycle phases."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("CrimsonCare")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Successfully posted local notification with ID $notificationId: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification $notificationId", e)
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

    private fun postOvulationNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 1. Create Notification Channel on Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cycle Predictions"
            val descriptionText = "Friendly notifications about your cycle phases."
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
            1,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build Notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Built-in system icon
            .setContentTitle("CrimsonCare")
            .setContentText("You have reached your predicted ovulation day.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 4. Post Notification
        try {
            notificationManager.notify(405, notification) // Use different notification ID 405
            Log.d(TAG, "Successfully posted local ovulation notification.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post ovulation notification", e)
        }
    }

    private fun rescheduleAlarmOnBoot(context: Context) {
        val database = CrimsonDatabase.getDatabase(context.applicationContext)
        val repository = CrimsonRepository(database.crimsonDao())
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repository.userSettings.firstOrNull()
                val logs = repository.allDailyLogs.firstOrNull() ?: emptyList()
                CycleNotificationScheduler.scheduleAllNotifications(context.applicationContext, settings, logs)
                Log.d(TAG, "Successfully rescheduled all alarms on boot: $settings, logs size: ${logs.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarms on boot", e)
            }
        }
    }
}
