package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.UserSettings
import com.example.receiver.CycleNotificationReceiver
import java.time.ZoneId

object CycleNotificationScheduler {
    private const val TAG = "CycleNotificationSched"

    fun schedulePredictionNotification(context: Context, userSettings: UserSettings?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, CycleNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1002, // Unique request code for this alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (userSettings == null || userSettings.lastPeriodDate.isBlank()) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Cancelled prediction alarm: UserSettings is empty or cleared.")
            return
        }

        val lastPeriodDate = PeriodCalculator.parseDate(userSettings.lastPeriodDate)
        if (lastPeriodDate == null) {
            alarmManager.cancel(pendingIntent)
            Log.e(TAG, "Cancelled prediction alarm: lastPeriodDate is invalid: ${userSettings.lastPeriodDate}")
            return
        }

        // 1. Calculate next predicted period date
        val nextPeriodDate = lastPeriodDate.plusDays(userSettings.averageCycleLength.toLong())

        // 2. Schedule exactly 2 days before the next predicted period date
        val triggerDate = nextPeriodDate.minusDays(2)

        // Set trigger time to 9:00 AM on the trigger date
        val triggerDateTime = triggerDate.atTime(9, 0)
        
        // Convert to epoch milliseconds
        val zoneId = ZoneId.systemDefault()
        val triggerMillis = triggerDateTime.atZone(zoneId).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()

        if (triggerMillis <= currentMillis) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Trigger time has already passed for this cycle ($triggerDateTime). Alarm canceled/not set.")
            return
        }

        // 3. Schedule the alarm using setAndAllowWhileIdle for robust local triggers
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Successfully scheduled local prediction alarm for $triggerDateTime.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule prediction alarm", e)
        }
    }
}
