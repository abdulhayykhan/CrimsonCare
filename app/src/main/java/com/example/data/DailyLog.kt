package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val flowIntensity: Int = 0,   // 0 = None, 1 = Light, 2 = Medium, 3 = Heavy
    val symptoms: String = ""      // Comma-separated symptoms e.g. "Cramps,Headache"
)
