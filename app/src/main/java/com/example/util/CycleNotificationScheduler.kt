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
        
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = "ACTION_PREDICTION_ALARM"
        }
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

    fun scheduleOvulationNotification(context: Context, userSettings: UserSettings?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = "ACTION_OVULATION_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1003, // Unique request code for ovulation alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (userSettings == null || userSettings.lastPeriodDate.isBlank()) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Cancelled ovulation alarm: UserSettings is empty or cleared.")
            return
        }

        val lastPeriodDate = PeriodCalculator.parseDate(userSettings.lastPeriodDate)
        if (lastPeriodDate == null) {
            alarmManager.cancel(pendingIntent)
            Log.e(TAG, "Cancelled ovulation alarm: lastPeriodDate is invalid: ${userSettings.lastPeriodDate}")
            return
        }

        // 1. Calculate next predicted period date
        val nextPeriodDate = lastPeriodDate.plusDays(userSettings.averageCycleLength.toLong())

        // 2. Schedule exactly 14 days before the next predicted period date (Predicted Ovulation Day)
        val triggerDate = nextPeriodDate.minusDays(14)

        // Set trigger time to 8:00 AM on the predicted ovulation day
        val triggerDateTime = triggerDate.atTime(8, 0)
        
        // Convert to epoch milliseconds
        val zoneId = ZoneId.systemDefault()
        val triggerMillis = triggerDateTime.atZone(zoneId).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()

        if (triggerMillis <= currentMillis) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Trigger time has already passed for this cycle ($triggerDateTime). Ovulation alarm canceled/not set.")
            return
        }

        // 3. Schedule the alarm
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
            Log.d(TAG, "Successfully scheduled local ovulation alarm for $triggerDateTime.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule ovulation alarm", e)
        }
    }

    fun scheduleOneDayBeforePeriodNotification(context: Context, userSettings: UserSettings?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = "ACTION_ONE_DAY_BEFORE_PERIOD"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1004,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (userSettings == null || userSettings.lastPeriodDate.isBlank()) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val lastPeriodDate = PeriodCalculator.parseDate(userSettings.lastPeriodDate) ?: return
        val nextPeriodDate = lastPeriodDate.plusDays(userSettings.averageCycleLength.toLong())
        val triggerDate = nextPeriodDate.minusDays(1)
        val triggerDateTime = triggerDate.atTime(9, 0)
        
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()

        if (triggerMillis <= currentMillis) {
            alarmManager.cancel(pendingIntent)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Successfully scheduled 1 Day Before Period alarm for $triggerDateTime.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule 1 Day Before Period alarm", e)
        }
    }

    fun schedulePmsWarningNotification(context: Context, userSettings: UserSettings?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = "ACTION_PMS_WARNING"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1005,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (userSettings == null || userSettings.lastPeriodDate.isBlank()) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val lastPeriodDate = PeriodCalculator.parseDate(userSettings.lastPeriodDate) ?: return
        val nextPeriodDate = lastPeriodDate.plusDays(userSettings.averageCycleLength.toLong())
        val triggerDate = nextPeriodDate.minusDays(4)
        val triggerDateTime = triggerDate.atTime(9, 0)
        
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()

        if (triggerMillis <= currentMillis) {
            alarmManager.cancel(pendingIntent)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Successfully scheduled PMS warning alarm for $triggerDateTime.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule PMS warning alarm", e)
        }
    }

    fun scheduleFertileWindowStartNotification(context: Context, userSettings: UserSettings?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = "ACTION_FERTILE_WINDOW_START"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1006,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (userSettings == null || userSettings.lastPeriodDate.isBlank()) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val lastPeriodDate = PeriodCalculator.parseDate(userSettings.lastPeriodDate) ?: return
        val nextPeriodDate = lastPeriodDate.plusDays(userSettings.averageCycleLength.toLong())
        val ovulationDate = nextPeriodDate.minusDays(14)
        val triggerDate = ovulationDate.minusDays(5)
        val triggerDateTime = triggerDate.atTime(9, 0)
        
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()

        if (triggerMillis <= currentMillis) {
            alarmManager.cancel(pendingIntent)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Successfully scheduled Fertile Window Start alarm for $triggerDateTime.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule Fertile Window Start alarm", e)
        }
    }

    fun scheduleFertileWindowEndNotification(context: Context, userSettings: UserSettings?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = "ACTION_FERTILE_WINDOW_END"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1007,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (userSettings == null || userSettings.lastPeriodDate.isBlank()) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val lastPeriodDate = PeriodCalculator.parseDate(userSettings.lastPeriodDate) ?: return
        val nextPeriodDate = lastPeriodDate.plusDays(userSettings.averageCycleLength.toLong())
        val ovulationDate = nextPeriodDate.minusDays(14)
        val triggerDate = ovulationDate.plusDays(1)
        val triggerDateTime = triggerDate.atTime(9, 0)
        
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()

        if (triggerMillis <= currentMillis) {
            alarmManager.cancel(pendingIntent)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Successfully scheduled Fertile Window End alarm for $triggerDateTime.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule Fertile Window End alarm", e)
        }
    }

    fun schedulePostCycleHealthCheckNotification(context: Context, allLogs: List<com.example.data.DailyLog>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CycleNotificationReceiver::class.java).apply {
            action = "ACTION_POST_CYCLE_HEALTH_CHECK"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1008,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lastPeriodEndDate = getLastLoggedPeriodEndDate(allLogs)
        if (lastPeriodEndDate == null) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val triggerDate = lastPeriodEndDate.plusDays(3)
        val triggerDateTime = triggerDate.atTime(9, 0)
        
        val triggerMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val currentMillis = System.currentTimeMillis()

        if (triggerMillis <= currentMillis) {
            alarmManager.cancel(pendingIntent)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Successfully scheduled Post-Cycle Health Check alarm for $triggerDateTime.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule Post-Cycle Health Check alarm", e)
        }
    }

    fun getLastLoggedPeriodEndDate(allLogs: List<com.example.data.DailyLog>): java.time.LocalDate? {
        val periodDays = allLogs
            .filter { it.flowIntensity > 0 }
            .mapNotNull { PeriodCalculator.parseDate(it.date) }
            .sorted()
        if (periodDays.isEmpty()) return null

        val episodes = mutableListOf<List<java.time.LocalDate>>()
        var currentEpisode = mutableListOf<java.time.LocalDate>()

        for (date in periodDays) {
            if (currentEpisode.isEmpty()) {
                currentEpisode.add(date)
            } else {
                val lastDate = currentEpisode.last()
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastDate, date)
                if (daysBetween <= 4) {
                    currentEpisode.add(date)
                } else {
                    episodes.add(currentEpisode)
                    currentEpisode = mutableListOf(date)
                }
            }
        }
        if (currentEpisode.isNotEmpty()) {
            episodes.add(currentEpisode)
        }

        val lastEpisode = episodes.lastOrNull() ?: return null
        return lastEpisode.maxOrNull()
    }

    fun scheduleAllNotifications(context: Context, userSettings: UserSettings?, allLogs: List<com.example.data.DailyLog>) {
        schedulePredictionNotification(context, userSettings)
        scheduleOvulationNotification(context, userSettings)
        scheduleOneDayBeforePeriodNotification(context, userSettings)
        schedulePmsWarningNotification(context, userSettings)
        scheduleFertileWindowStartNotification(context, userSettings)
        scheduleFertileWindowEndNotification(context, userSettings)
        schedulePostCycleHealthCheckNotification(context, allLogs)
    }
}
