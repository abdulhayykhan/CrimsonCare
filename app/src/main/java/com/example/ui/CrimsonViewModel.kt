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
