package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyLog
import com.example.ui.theme.*
import com.example.util.PeriodCalculator
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CrimsonViewModel,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val allLogs by viewModel.allDailyLogs.collectAsStateWithLifecycle()
    val selectedDateStr by viewModel.selectedDate.collectAsStateWithLifecycle()

    val selectedDate = remember(selectedDateStr) {
        try {
            LocalDate.parse(selectedDateStr)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    // Active calendar month navigation state
    var currentMonthYear by remember { mutableStateOf(YearMonth.now()) }

    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }

    // Logs map for rapid lookup (Key: "yyyy-MM-dd", Value: DailyLog)
    val logsMap = remember(allLogs) {
        allLogs.associateBy { it.date }
    }

    // Parse user tracking variables
    val lastPeriodLocalDate = remember(userSettings) {
        userSettings?.lastPeriodDate?.let { PeriodCalculator.parseDate(it) }
    }
    val averageCycleLength = userSettings?.averageCycleLength ?: 28
    val averagePeriodLength = userSettings?.averagePeriodLength ?: 5

    // Check if a specific date is predicted to have a period
    fun isPredictedPeriodDay(date: LocalDate): Boolean {
        if (lastPeriodLocalDate == null) return false
        val daysBetween = ChronoUnit.DAYS.between(lastPeriodLocalDate, date)
        if (daysBetween < 0) return false
        val cycleDay = (daysBetween % averageCycleLength) + 1
        return cycleDay <= averagePeriodLength
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Calendar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "CYCLE HISTORIES & PREDICTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp
                            ),
                            color = SleekTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = SleekTextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Month Year Pagination Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonthYear = currentMonthYear.minusMonths(1) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(SleekBackgroundVariant, CircleShape)
                        .testTag("prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = SleekTextPrimary
                    )
                }

                Text(
                    text = currentMonthYear.format(monthFormatter).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("month_year_header")
                )

                IconButton(
                    onClick = { currentMonthYear = currentMonthYear.plusMonths(1) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(SleekBackgroundVariant, CircleShape)
                        .testTag("next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = SleekTextPrimary
                    )
                }
            }

            // Weekday Headings (S, M, T, W, T, F, S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = SleekTextSecondary
                    )
                }
            }

            // Calendar Grid System
            val daysInMonth = currentMonthYear.lengthOfMonth()
            val firstDayOfMonth = currentMonthYear.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday-first offset

            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_grid")
            ) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (colIndex in 0..6) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = currentMonthYear.atDay(dayNumber)
                                val cellDateStr = cellDate.toString()
                                val isSelected = cellDate == selectedDate
                                val logForDay = logsMap[cellDateStr]

                                // State-based flags
                                val hasPastLoggedPeriod = logForDay != null && logForDay.flowIntensity > 0
                                val isFuturePredictedPeriod = cellDate.isAfter(LocalDate.now()) && isPredictedPeriodDay(cellDate)

                                // Animation transitions
                                val cellBgColor by animateColorAsState(
                                    targetValue = when {
                                        isSelected -> CrimsonPrimary
                                        hasPastLoggedPeriod -> WarmRose
                                        else -> Color.Transparent
                                    },
                                    label = "cellBgColor"
                                )

                                val cellTextColor = when {
                                    isSelected -> Color.White
                                    hasPastLoggedPeriod -> CrimsonPrimary
                                    isFuturePredictedPeriod -> ColorLuteal
                                    else -> SleekTextPrimary
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(cellBgColor)
                                        .clickable { viewModel.selectDate(cellDateStr) }
                                        .testTag("day_cell_$dayNumber"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected || hasPastLoggedPeriod || isFuturePredictedPeriod) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                },
                                                fontSize = 14.sp
                                            ),
                                            color = cellTextColor
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Draw marker dot or outline indicator
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (hasPastLoggedPeriod) {
                                                // Logged Period Marker: Solid small red dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(
                                                            color = if (isSelected) Color.White else CrimsonPrimary,
                                                            shape = CircleShape
                                                        )
                                                )
                                            } else if (isFuturePredictedPeriod) {
                                                // Future Predicted Period: Tiny rose square/dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(
                                                            color = if (isSelected) Color.White else ColorLuteal,
                                                            shape = CircleShape
                                                        )
                                                )
                                            } else if (logForDay != null && logForDay.symptoms.isNotBlank()) {
                                                // Logged symptoms but no flow: Slate grey tiny dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(
                                                            color = if (isSelected) Color.White else SleekTextSecondary,
                                                            shape = CircleShape
                                                        )
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(5.dp))
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Empty cell
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Legend / Color Keys Helper Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SleekBackgroundVariant),
                border = BorderStroke(1.dp, SleekBorderVariant)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(CrimsonPrimary, CircleShape)
                        )
                        Text(
                            text = "Logged Period",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SleekTextSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(ColorLuteal, CircleShape)
                        )
                        Text(
                            text = "Predicted Future",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SleekTextSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(SleekTextSecondary, CircleShape)
                        )
                        Text(
                            text = "Logged Symptoms",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SleekTextSecondary
                        )
                    }
                }
            }

            // Requirement 3: Detailed Summary Panel Below the Calendar Grid
            val selectedLog = logsMap[selectedDateStr]
            val isDateFuture = selectedDate.isAfter(LocalDate.now())
            val hasPredictionForSelected = isPredictedPeriodDay(selectedDate)

            Text(
                text = "DAILY REPORT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = SleekTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_summary_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                border = BorderStroke(1.dp, SleekOutline)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Date & Prediction Phase Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val displayDate = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd"))
                            Text(
                                text = displayDate,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (selectedDate == LocalDate.now()) "Today" else "Selected Day",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextSecondary
                            )
                        }

                        // Predicted Status Pill
                        val badgeText = when {
                            selectedLog != null && selectedLog.flowIntensity > 0 -> "Period"
                            isDateFuture && hasPredictionForSelected -> "Predicted Period"
                            hasPredictionForSelected -> "Period Window"
                            else -> "Normal Day"
                        }

                        val badgeBg = when {
                            selectedLog != null && selectedLog.flowIntensity > 0 -> CrimsonPrimary
                            isDateFuture && hasPredictionForSelected -> ColorLuteal
                            else -> SleekTextSecondary
                        }

                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(100))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = badgeText.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Divider(color = SleekOutline.copy(alpha = 0.5f), thickness = 1.dp)

                    if (selectedLog != null) {
                        // Log details present! Show Flow & Symptoms
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Flow Intensity Section
                            val flowLabel = when (selectedLog.flowIntensity) {
                                1 -> "Light Flow"
                                2 -> "Medium Flow"
                                3 -> "Heavy Flow"
                                else -> "No Flow"
                            }
                            val flowIcon = when (selectedLog.flowIntensity) {
                                0 -> Icons.Default.Close
                                else -> Icons.Default.Favorite
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "FLOW INTENSITY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SleekTextSecondary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, SleekBorderVariant, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = flowIcon,
                                        contentDescription = null,
                                        tint = if (selectedLog.flowIntensity > 0) CrimsonPrimary else SleekTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = flowLabel,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = SleekTextPrimary
                                    )
                                }
                            }
                        }

                        // Symptoms Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LOGGED SYMPTOMS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SleekTextSecondary
                            )

                            if (selectedLog.symptoms.isNotBlank()) {
                                val symptomsList = selectedLog.symptoms.split(",")
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    symptomsList.forEach { symptom ->
                                        Box(
                                            modifier = Modifier
                                                .background(WarmRose, RoundedCornerShape(8.dp))
                                                .border(1.dp, SleekOutline, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = CrimsonPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = symptom,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = CrimsonSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No symptoms logged.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        // Quick Log Action / Edit Log Action Button
                        Button(
                            onClick = {
                                viewModel.navigateTo(CrimsonScreen.DAILY_LOGGER)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("calendar_edit_log_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Log",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Edit Daily Log",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                    } else {
                        // No log found. Let the user easily record symptoms
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No logs recorded for this day.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    viewModel.navigateTo(CrimsonScreen.DAILY_LOGGER)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("calendar_create_log_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonSecondary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Log Today",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (selectedDate == LocalDate.now()) "Log Symptoms & Flow" else "Add Symptoms Log",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
