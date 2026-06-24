package com.example.util

import com.example.data.DailyLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class CyclePhase(val displayName: String, val description: String) {
    MENSTUAL("Menstruation", "Your period phase. Focus on rest and self-care."),
    FOLLICULAR("Follicular Phase", "Energy levels rising. Great time to be active."),
    OVULATION("Fertile Window", "Peak fertility. High energy and mood."),
    LUTEAL("Luteal Phase", "Slowing down. PMS symptoms may appear.")
}

data class CycleState(
    val currentCycleDay: Int?,
    val nextPeriodDate: LocalDate?,
    val daysUntilNextPeriod: Int?,
    val phase: CyclePhase?,
    val isOverdue: Boolean
)

data class CycleHistoryItem(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val lengthDays: Int
)

object PeriodCalculator {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun parseDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr, formatter)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(date: LocalDate): String {
        return date.format(formatter)
    }

    fun calculateCycleState(
        lastPeriodDateStr: String?,
        averageCycleLength: Int,
        averagePeriodLength: Int,
        today: LocalDate = LocalDate.now()
    ): CycleState {
        if (lastPeriodDateStr.isNullOrBlank()) {
            return CycleState(
                currentCycleDay = null,
                nextPeriodDate = null,
                daysUntilNextPeriod = null,
                phase = null,
                isOverdue = false
            )
        }

        val lastPeriodDate = parseDate(lastPeriodDateStr) ?: return CycleState(
            currentCycleDay = null,
            nextPeriodDate = null,
            daysUntilNextPeriod = null,
            phase = null,
            isOverdue = false
        )

        // Calculate days since last period started
        val daysBetween = ChronoUnit.DAYS.between(lastPeriodDate, today)
        val currentCycleDay = (daysBetween + 1).toInt()

        // Predict next period date
        val nextPeriodDate = lastPeriodDate.plusDays(averageCycleLength.toLong())

        // Calculate days until next period
        val daysUntilNextPeriod = ChronoUnit.DAYS.between(today, nextPeriodDate).toInt()
        val isOverdue = daysUntilNextPeriod < 0

        // If today is before the last period date (should not happen in normal flows, but handle gracefully)
        if (currentCycleDay < 1) {
            return CycleState(
                currentCycleDay = 1,
                nextPeriodDate = nextPeriodDate,
                daysUntilNextPeriod = daysUntilNextPeriod,
                phase = CyclePhase.MENSTUAL,
                isOverdue = false
            )
        }

        // Determine cycle phase
        val phase = getCyclePhase(currentCycleDay, averageCycleLength, averagePeriodLength)

        return CycleState(
            currentCycleDay = currentCycleDay,
            nextPeriodDate = nextPeriodDate,
            daysUntilNextPeriod = daysUntilNextPeriod,
            phase = phase,
            isOverdue = isOverdue
        )
    }

    private fun getCyclePhase(day: Int, cycleLength: Int, periodLength: Int): CyclePhase {
        val normalizedDay = if (day > cycleLength) {
            // If they are past their cycle length, treat it as luteal / overdue phase or wrap-around
            ((day - 1) % cycleLength) + 1
        } else {
            day
        }

        val ovulationDay = cycleLength - 14
        val fertileStart = ovulationDay - 4
        val fertileEnd = ovulationDay + 1

        return when {
            normalizedDay <= periodLength -> CyclePhase.MENSTUAL
            normalizedDay in fertileStart..fertileEnd -> CyclePhase.OVULATION
            normalizedDay < fertileStart -> CyclePhase.FOLLICULAR
            else -> CyclePhase.LUTEAL
        }
    }

    fun calculatePastCycleLengths(allLogs: List<DailyLog>): List<CycleHistoryItem> {
        val periodDays = allLogs
            .filter { it.flowIntensity > 0 }
            .mapNotNull { parseDate(it.date) }
            .sorted()

        if (periodDays.isEmpty()) return emptyList()

        // Group consecutive period days into episodes if gap <= 4 days
        val episodes = mutableListOf<List<LocalDate>>()
        var currentEpisode = mutableListOf<LocalDate>()

        for (date in periodDays) {
            if (currentEpisode.isEmpty()) {
                currentEpisode.add(date)
            } else {
                val lastDate = currentEpisode.last()
                val daysBetween = ChronoUnit.DAYS.between(lastDate, date)
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

        val startDates = episodes.map { it.first() }.sorted()
        if (startDates.size < 2) return emptyList()

        val cycleHistory = mutableListOf<CycleHistoryItem>()
        for (i in 0 until startDates.size - 1) {
            val start = startDates[i]
            val nextStart = startDates[i + 1]
            val length = ChronoUnit.DAYS.between(start, nextStart).toInt()
            cycleHistory.add(
                CycleHistoryItem(
                    startDate = start,
                    endDate = nextStart.minusDays(1),
                    lengthDays = length
                )
            )
        }
        return cycleHistory
    }

    fun calculateTopSymptomsPast3Months(allLogs: List<DailyLog>, today: LocalDate = LocalDate.now()): List<Pair<String, Int>> {
        val threeMonthsAgo = today.minusMonths(3)
        val symptomsCount = mutableMapOf<String, Int>()

        for (log in allLogs) {
            val logDate = parseDate(log.date) ?: continue
            if (!logDate.isBefore(threeMonthsAgo)) {
                if (log.symptoms.isNotBlank()) {
                    val symptoms = log.symptoms.split(",")
                    for (symptom in symptoms) {
                        val trimmed = symptom.trim()
                        if (trimmed.isNotEmpty()) {
                            symptomsCount[trimmed] = (symptomsCount[trimmed] ?: 0) + 1
                        }
                    }
                }
            }
        }

        return symptomsCount.toList().sortedByDescending { it.second }
    }
}
