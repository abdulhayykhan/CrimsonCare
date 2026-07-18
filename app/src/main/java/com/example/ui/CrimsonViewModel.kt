package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CrimsonRepository
import com.example.data.DailyLog
import com.example.data.UserSettings
import com.example.util.CycleState
import com.example.util.PeriodCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class CrimsonScreen {
    ONBOARDING,
    DASHBOARD,
    DAILY_LOGGER,
    CALENDAR,
    INSIGHTS,
    SETTINGS
}

class CrimsonViewModel(private val repository: CrimsonRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow(CrimsonScreen.DASHBOARD)
    val currentScreen: StateFlow<CrimsonScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: CrimsonScreen) {
        _currentScreen.value = screen
    }

    val userSettings: StateFlow<UserSettings?> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allDailyLogs: StateFlow<List<DailyLog>> = repository.allDailyLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cycleState: StateFlow<CycleState> = userSettings
        .map { settings ->
            PeriodCalculator.calculateCycleState(
                lastPeriodDateStr = settings?.lastPeriodDate,
                averageCycleLength = settings?.averageCycleLength ?: 28,
                averagePeriodLength = settings?.averagePeriodLength ?: 5
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PeriodCalculator.calculateCycleState(null, 28, 5)
        )

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateLog: StateFlow<DailyLog?> = _selectedDate
        .flatMapLatest { date ->
            repository.getDailyLogForDate(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun saveUserSettings(lastPeriodDate: String, averageCycle: Int, averagePeriod: Int) {
        viewModelScope.launch {
            repository.upsertUserSettings(
                UserSettings(
                    lastPeriodDate = lastPeriodDate,
                    averageCycleLength = averageCycle,
                    averagePeriodLength = averagePeriod
                )
            )
        }
    }

    fun saveDailyLog(date: String, flowIntensity: Int, symptoms: List<String>) {
        viewModelScope.launch {
            repository.upsertDailyLog(
                DailyLog(
                    date = date,
                    flowIntensity = flowIntensity,
                    symptoms = symptoms.joinToString(",")
                )
            )
        }
    }

    fun deleteDailyLog(date: String) {
        viewModelScope.launch {
            repository.deleteDailyLog(date)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            // Simply update with empty or delete. Since key is 1, let's write a default or clear
            // Let's upsert user settings with an empty date string to trigger onboarding
            repository.upsertUserSettings(
                UserSettings(
                    lastPeriodDate = "",
                    averageCycleLength = 28,
                    averagePeriodLength = 5
                )
            )
        }
    }

    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

    fun initNotifications(context: android.content.Context) {
        val sharedPrefs = context.getSharedPreferences("crimson_care_prefs", android.content.Context.MODE_PRIVATE)
        val saved = sharedPrefs.getString("in_app_notifications", null)
        if (saved == null) {
            val initial = listOf(
                InAppNotification(
                    title = "You're absolutely glowing!",
                    message = "You're absolutely glowing! Your fertile window is opening.",
                    timestamp = "Today, 10:00 AM"
                ),
                InAppNotification(
                    title = "A loving heads-up, gorgeous",
                    message = "I noticed you usually get cramps around this time. Please take it extra easy, grab a warm heating pad, and prepare ahead.",
                    timestamp = "Yesterday, 8:30 PM"
                ),
                InAppNotification(
                    title = "Health Check-in",
                    message = "Just a loving reminder to do your monthly health check-in, pretty. I want you safe and healthy.",
                    timestamp = "3 days ago"
                )
            )
            val serialized = serializeNotifications(initial)
            sharedPrefs.edit().putString("in_app_notifications", serialized).apply()
            _notifications.value = initial
        } else {
            _notifications.value = deserializeNotifications(saved)
        }
    }

    fun clearNotification(context: android.content.Context, id: String) {
        val updated = _notifications.value.filter { it.id != id }
        _notifications.value = updated
        val sharedPrefs = context.getSharedPreferences("crimson_care_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("in_app_notifications", serializeNotifications(updated)).apply()
    }

    fun clearAllNotifications(context: android.content.Context) {
        _notifications.value = emptyList()
        val sharedPrefs = context.getSharedPreferences("crimson_care_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("in_app_notifications", "").apply()
    }

    fun addNotification(context: android.content.Context, title: String, message: String) {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, h:mm a")
        val timestamp = java.time.LocalDateTime.now().format(formatter)
        val newNotif = InAppNotification(
            title = title,
            message = message,
            timestamp = timestamp
        )
        val updated = listOf(newNotif) + _notifications.value
        _notifications.value = updated
        val sharedPrefs = context.getSharedPreferences("crimson_care_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("in_app_notifications", serializeNotifications(updated)).apply()
    }
}

data class InAppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)

fun serializeNotifications(list: List<InAppNotification>): String {
    return list.joinToString("||") { "${it.id}##${it.title}##${it.message}##${it.timestamp}##${it.isRead}" }
}

fun deserializeNotifications(str: String?): List<InAppNotification> {
    if (str.isNullOrBlank()) return emptyList()
    return str.split("||").mapNotNull { part ->
        val segments = part.split("##")
        if (segments.size >= 4) {
            val isRead = if (segments.size >= 5) segments[4].toBoolean() else false
            InAppNotification(
                id = segments[0],
                title = segments[1],
                message = segments[2],
                timestamp = segments[3],
                isRead = isRead
            )
        } else {
            null
        }
    }
}

class CrimsonViewModelFactory(private val repository: CrimsonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CrimsonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CrimsonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
