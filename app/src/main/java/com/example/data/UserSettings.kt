package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val lastPeriodDate: String, // YYYY-MM-DD
    val averageCycleLength: Int = 28, // default 28 days
    val averagePeriodLength: Int = 5 // default 5 days
)
