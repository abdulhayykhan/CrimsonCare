package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyLog
import com.example.data.UserSettings
import com.example.ui.theme.*
import com.example.util.CyclePhase
import com.example.util.CycleState
import com.example.util.PeriodCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrimsonDashboard(
    viewModel: CrimsonViewModel,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val cycleState by viewModel.cycleState.collectAsStateWithLifecycle()
    val allDailyLogs by viewModel.allDailyLogs.collectAsStateWithLifecycle()
    val selectedDateLog by viewModel.selectedDateLog.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    var showSetupDialog by remember { mutableStateOf(false) }
    var hasShownOvulationPopup by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var hasShownLatePeriodAlert by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // Linear gradient background: Light Pink to Soft Peach to White
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFDE7E9), // Light Pink
            Color(0xFFFFF1F2), // Soft Peach
            Color(0xFFFFFFFF)  // White
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "CrimsonCare",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = CrimsonPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "CYCLE OVERVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp
                                ),
                                color = CrimsonSecondary
                            )
                        }
                    },
                    actions = {
                        if (userSettings != null && userSettings?.lastPeriodDate?.isNotBlank() == true) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(44.dp)
                                    .background(WarmRose, CircleShape)
                                    .clickable { showSetupDialog = true }
                                    .testTag("settings_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Edit Settings",
                                    tint = CrimsonSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White.copy(alpha = 0.45f) // Frosted translucent glass
                    ),
                    modifier = Modifier.border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                )
            },
            containerColor = Color.Transparent, // Transparent scaffold container
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val hasSettings = userSettings != null && userSettings?.lastPeriodDate?.isNotBlank() == true

                if (!hasSettings) {
                    // If no settings exist, prompt with onboarding
                    OnboardingSetupView(
                        onSave = { date, cycle, period ->
                            viewModel.saveUserSettings(date, cycle, period)
                        }
                    )
                } else {
                    // Active tracker dashboard
                    DashboardContent(
                    cycleState = cycleState,
                    allLogs = allDailyLogs,
                    onLogClick = {
                        viewModel.selectDate(LocalDate.now().toString())
                        viewModel.navigateTo(CrimsonScreen.DAILY_LOGGER)
                    },
                    onLogEditClick = { date ->
                        viewModel.selectDate(date)
                        viewModel.navigateTo(CrimsonScreen.DAILY_LOGGER)
                    },
                    onDeleteLog = { date ->
                        viewModel.deleteDailyLog(date)
                    },
                    onResetRequest = {
                        viewModel.resetSettings()
                    }
                )
            }

            // Dialog for editing user settings
            if (showSetupDialog) {
                Dialog(onDismissRequest = { showSetupDialog = false }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Update Tracking Settings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OnboardingFormContent(
                                initialDate = userSettings?.lastPeriodDate ?: "",
                                initialCycle = userSettings?.averageCycleLength ?: 28,
                                initialPeriod = userSettings?.averagePeriodLength ?: 5,
                                onSave = { date, cycle, period ->
                                    viewModel.saveUserSettings(date, cycle, period)
                                    showSetupDialog = false
                                },
                                onCancel = { showSetupDialog = false }
                            )
                        }
                    }
                }
            }

            // Ovulation Alert Pop-up Dialog
            val isTodayOvulationDay = cycleState.predictedOvulationDate != null && cycleState.predictedOvulationDate == LocalDate.now()
            if (isTodayOvulationDay && !hasShownOvulationPopup) {
                Dialog(onDismissRequest = { hasShownOvulationPopup = true }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("ovulation_dialog"),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFFDE7E9), CircleShape)
                                    .border(1.dp, Color(0xFFB12E33).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFB12E33),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "You're glowing, beautiful!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB12E33)
                                ),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "It's your predicted ovulation day. Enjoy the energy boost today—I'm always here admiring you!",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFB12E33).copy(alpha = 0.85f),
                                    lineHeight = 20.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { hasShownOvulationPopup = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFB12E33),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(100),
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(48.dp)
                                    .testTag("dismiss_ovulation_button")
                            ) {
                                Text(
                                    text = "Dismiss",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Late Period Anomaly Dialog
            val isPeriodLate = cycleState.nextPeriodDate != null && LocalDate.now().isAfter(cycleState.nextPeriodDate!!)
            val hasFlowOnOrAfterPredicted = allDailyLogs.any { log ->
                val logDate = PeriodCalculator.parseDate(log.date)
                logDate != null && (logDate.isEqual(cycleState.nextPeriodDate) || logDate.isAfter(cycleState.nextPeriodDate!!)) && log.flowIntensity > 0
            }
            if (isPeriodLate && !hasFlowOnOrAfterPredicted && !hasShownLatePeriodAlert) {
                Dialog(onDismissRequest = { hasShownLatePeriodAlert = true }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("late_period_dialog"),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFFDE7E9), CircleShape)
                                    .border(1.dp, Color(0xFFB12E33).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFB12E33),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "Is your cycle a bit late, beautiful?",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB12E33)
                                ),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Your cycle seems a little late. Let's log a delayed start or update your settings. I'm here to support you!",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFB12E33).copy(alpha = 0.85f),
                                    lineHeight = 20.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { hasShownLatePeriodAlert = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFB12E33),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(100),
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(48.dp)
                                    .testTag("dismiss_late_period_button")
                            ) {
                                Text(
                                    text = "Dismiss",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
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

@Composable
fun OnboardingSetupView(
    onSave: (String, Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to CrimsonCare",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your private, offline-first period companion. All data is securely stored on this device and never leaves it.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure Your Cycle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OnboardingFormContent(
                    initialDate = "",
                    initialCycle = 28,
                    initialPeriod = 5,
                    onSave = onSave,
                    onCancel = null
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Shield Icon",
                tint = ColorMenstruation,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Data stored 100% locally",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorMenstruation,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun OnboardingFormContent(
    initialDate: String,
    initialCycle: Int,
    initialPeriod: Int,
    onSave: (String, Int, Int) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var lastPeriodDate by remember { mutableStateOf(initialDate) }
    var cycleLength by remember { mutableStateOf(initialCycle) }
    var periodLength by remember { mutableStateOf(initialPeriod) }

    val context = LocalContext.current
    val parsedDate = remember(lastPeriodDate) {
        PeriodCalculator.parseDate(lastPeriodDate)
    }

    val displayDate = remember(parsedDate) {
        if (parsedDate != null) {
            parsedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } else {
            "Select Date"
        }
    }

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedLocalDate = LocalDate.of(year, month + 1, dayOfMonth)
                lastPeriodDate = PeriodCalculator.formatDate(selectedLocalDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Last period date picker
        Text(
            text = "When did your last period start?",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
                .testTag("date_picker_trigger"),
            cornerRadius = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (lastPeriodDate.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Average Cycle Length Selector
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Average Cycle Length",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$cycleLength Days",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = cycleLength.toFloat(),
                onValueChange = { cycleLength = it.toInt() },
                valueRange = 21f..45f,
                steps = 24,
                modifier = Modifier.testTag("cycle_length_slider")
            )
        }

        // Average Period Length Selector
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Average Period Length",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$periodLength Days",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = periodLength.toFloat(),
                onValueChange = { periodLength = it.toInt() },
                valueRange = 3f..10f,
                steps = 7,
                modifier = Modifier.testTag("period_length_slider")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onCancel != null) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
            Button(
                onClick = { onSave(lastPeriodDate, cycleLength, periodLength) },
                enabled = lastPeriodDate.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_settings_button")
            ) {
                Text("Save Settings")
            }
        }
    }
}

@Composable
fun DashboardContent(
    cycleState: CycleState,
    allLogs: List<DailyLog>,
    onLogClick: () -> Unit,
    onLogEditClick: (String) -> Unit,
    onDeleteLog: (String) -> Unit,
    onResetRequest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Privacy Banner (Capsule style)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .background(SleekBackgroundVariant, RoundedCornerShape(100))
                        .border(1.dp, SleekBorderVariant, RoundedCornerShape(100))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "DATA STORED 100% LOCALLY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp
                        ),
                        color = SleekTextSecondary
                    )
                }
            }
        }

        // Predictive Symptom Warning card on Day 1
        val showPredictiveWarning = cycleState.currentCycleDay == 1 && PeriodCalculator.checkCrampsInLastTwoCycles(allLogs)
        if (showPredictiveWarning) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("predictive_symptom_warning_card"),
                    cornerRadius = 24.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFDE7E9), CircleShape)
                                .border(1.dp, Color(0xFFB12E33).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFB12E33),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "A loving heads-up, gorgeous",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB12E33)
                                )
                            )
                            Text(
                                text = "I noticed you usually get cramps around this time. Please take it extra easy, grab a warm heating pad, and prepare ahead.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFB12E33).copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Floating Hero Progress Ring
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val phase = cycleState.phase ?: CyclePhase.FOLLICULAR
                val activeColor = when (phase) {
                    CyclePhase.MENSTUAL -> ColorMenstruation
                    CyclePhase.FOLLICULAR -> ColorFollicular
                    CyclePhase.OVULATION -> ColorOvulation
                    CyclePhase.LUTEAL -> ColorLuteal
                }
                val circleBaseColor = SleekCircleBase

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp)
                ) {
                    // Outer background circle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        drawCircle(
                            color = circleBaseColor,
                            radius = (size.minDimension - strokeWidth) / 2,
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    // Progress ring
                    val progress = if (cycleState.currentCycleDay != null) {
                        val totalLength = 28f // standard default fallback
                        (cycleState.currentCycleDay.toFloat() / totalLength).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    CycleProgressRing(
                        progress = progress,
                        activeColor = activeColor,
                        modifier = Modifier.size(228.dp)
                    )

                    // Dynamic text details
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "CYCLE DAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp
                            ),
                            color = SleekTextSecondary
                        )

                        Text(
                            text = if (cycleState.currentCycleDay != null) {
                                "${cycleState.currentCycleDay}"
                            } else {
                                "--"
                            },
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = 72.sp
                            ),
                            color = SleekTextPrimary,
                            modifier = Modifier.testTag("cycle_day_display")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Styled Phase Badge
                        Box(
                            modifier = Modifier
                                .background(color = WarmRose, shape = RoundedCornerShape(100))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = phase.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = CrimsonSecondary
                            )
                        }
                    }
                }
            }
        }

        // Info Cards Section (Prediction & Fertility)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Prediction Card
                val countdownText = when {
                    cycleState.daysUntilNextPeriod == null -> "Please configure last period start"
                    cycleState.daysUntilNextPeriod > 0 -> "Next period in ${cycleState.daysUntilNextPeriod} days"
                    cycleState.daysUntilNextPeriod == 0 -> "Expected today!"
                    else -> "Period is ${Math.abs(cycleState.daysUntilNextPeriod)} days overdue"
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon Box
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CrimsonPrimary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendar",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Text Column
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Prediction",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = SleekTextSecondary
                            )
                            Text(
                                text = countdownText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary,
                                modifier = Modifier.testTag("countdown_text")
                            )
                        }
                    }
                }

                // Fertility Card
                val fertilityChances = when (cycleState.phase) {
                    CyclePhase.MENSTUAL -> "Low chance of conception"
                    CyclePhase.FOLLICULAR -> "Increasing chance of conception"
                    CyclePhase.OVULATION -> "High chance of conception"
                    CyclePhase.LUTEAL -> "Low chance of conception"
                    null -> "Track daily logs to estimate fertility"
                }

                val fertilityIcon = when (cycleState.phase) {
                    CyclePhase.OVULATION -> Icons.Default.Favorite
                    else -> Icons.Default.Star
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon Box
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SleekTextSecondary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = fertilityIcon,
                                contentDescription = "Fertility",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Text Column
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Fertility",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = SleekTextSecondary
                            )
                            Text(
                                text = fertilityChances,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Primary Log Action
        item {
            Button(
                onClick = onLogClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("log_today_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Today",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Log Today's Symptoms",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // Historical Timeline Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Log History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (allLogs.isNotEmpty()) {
                    Text(
                        text = "${allLogs.size} logs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Timeline Records List
        if (allLogs.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No logs yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Log your flow intensity and physical symptoms daily to get higher prediction accuracy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(allLogs) { log ->
                LogHistoryItem(
                    log = log,
                    onEdit = { onLogEditClick(log.date) },
                    onDelete = { onDeleteLog(log.date) }
                )
            }
        }

        // System reset tool (for developer/tester convenience)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onResetRequest,
                    modifier = Modifier.testTag("reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Reset Settings",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset Onboarding Settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun CycleProgressRing(
    progress: Float,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeftOffset = androidx.compose.ui.geometry.Offset(
            x = (size.width - diameter) / 2,
            y = (size.height - diameter) / 2
        )
        val size2 = androidx.compose.ui.geometry.Size(diameter, diameter)

        // Draw background base circle
        drawCircle(
            color = activeColor.copy(alpha = 0.1f),
            radius = diameter / 2,
            style = Stroke(width = strokeWidth)
        )

        // Draw active arc
        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            topLeft = topLeftOffset,
            size = size2,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun LogHistoryItem(
    log: DailyLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val date = remember(log.date) {
        LocalDate.parse(log.date)?.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")) ?: log.date
    }

    val flowText = when (log.flowIntensity) {
        1 -> "Light Flow"
        2 -> "Medium Flow"
        3 -> "Heavy Flow"
        else -> "No Flow"
    }

    val flowColor = when (log.flowIntensity) {
        1 -> ColorFollicular
        2 -> ColorLuteal
        3 -> ColorMenstruation
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(flowColor, CircleShape)
                    )
                    Text(
                        text = flowText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = flowColor
                    )
                }

                if (log.symptoms.isNotBlank()) {
                    val symptomList = log.symptoms.split(",")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        symptomList.take(3).forEach { symptom ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = symptom,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (symptomList.size > 3) {
                            Text(
                                text = "+${symptomList.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Log",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogTodayDialog(
    date: String,
    existingLog: DailyLog?,
    onDismiss: () -> Unit,
    onSave: (Int, List<String>) -> Unit
) {
    val displayDate = remember(date) {
        LocalDate.parse(date)?.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")) ?: date
    }

    var selectedFlow by remember { mutableStateOf(existingLog?.flowIntensity ?: 0) }
    val symptomsSet = remember { mutableStateOf(existingLog?.symptoms?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()) }

    val standardSymptoms = listOf(
        "Cramps", "Headache", "Bloating", "Mood Swings",
        "Fatigue", "Acne", "Backache", "Sore Breasts"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Log Today: $displayDate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Flow intensity selection
            Text(
                text = "Period Flow Intensity",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val intensityLevels = listOf("None", "Light", "Medium", "Heavy")
                intensityLevels.forEachIndexed { index, label ->
                    val isSelected = selectedFlow == index
                    val activeColor = when (index) {
                        1 -> ColorFollicular
                        2 -> ColorLuteal
                        3 -> ColorMenstruation
                        else -> MaterialTheme.colorScheme.outline
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedFlow = index }
                            .padding(vertical = 12.dp)
                            .testTag("flow_intensity_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Symptoms multi-selector
            Text(
                text = "Symptoms Experienced",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                standardSymptoms.forEach { symptom ->
                    val isSelected = symptomsSet.value.contains(symptom)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                symptomsSet.value = symptomsSet.value - symptom
                            } else {
                                symptomsSet.value = symptomsSet.value + symptom
                            }
                        },
                        label = { Text(symptom) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save & Cancel actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(selectedFlow, symptomsSet.value.toList())
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_log_button")
                ) {
                    Text("Save Log")
                }
            }
        }
    }
}
