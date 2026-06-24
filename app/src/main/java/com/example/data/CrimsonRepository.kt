package com.example.data

import kotlinx.coroutines.flow.Flow

class CrimsonRepository(private val crimsonDao: CrimsonDao) {
    val userSettings: Flow<UserSettings?> = crimsonDao.getUserSettings()
    val allDailyLogs: Flow<List<DailyLog>> = crimsonDao.getAllDailyLogs()

    suspend fun upsertUserSettings(settings: UserSettings) {
        crimsonDao.upsertUserSettings(settings)
    }

    fun getDailyLogForDate(date: String): Flow<DailyLog?> {
        return crimsonDao.getDailyLogForDate(date)
    }

    suspend fun upsertDailyLog(log: DailyLog) {
        crimsonDao.upsertDailyLog(log)
    }

    suspend fun deleteDailyLog(date: String) {
        crimsonDao.deleteDailyLog(date)
    }
}
