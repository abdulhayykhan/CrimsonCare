package com.example.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun OnboardingScreen(
    viewModel: CrimsonViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) }

    // Onboarding Data States
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var averageCycleLength by remember { mutableStateOf(28) }
    var averagePeriodLength by remember { mutableStateOf(5) }

    val formattedDate = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Indicator (Step Progress)
            if (currentStep > 0) {
                StepProgressBar(currentStep = currentStep, totalSteps = 3)
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Content Area with Animated Transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    0 -> WelcomeStep()
                    1 -> DateSelectionStep(
                        selectedDate = selectedDate,
                        formattedDate = formattedDate,
                        onDateSelected = { selectedDate = it }
                    )
                    2 -> CycleLengthStep(
                        cycleLength = averageCycleLength,
                        onCycleLengthChanged = { averageCycleLength = it }
                    )
                    3 -> PeriodDurationStep(
                        periodLength = averagePeriodLength,
                        onPeriodLengthChanged = { averagePeriodLength = it }
                    )
                }
            }

            // Bottom Navigation Buttons
            OnboardingNavigationButtons(
                currentStep = currentStep,
                onBack = { currentStep = (currentStep - 1).coerceAtLeast(0) },
                onNext = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        // Save inputs to database
                        viewModel.saveUserSettings(
                            lastPeriodDate = selectedDate.toString(),
                            averageCycle = averageCycleLength,
                            averagePeriod = averagePeriodLength
                        )
                        // Save first-time flag to SharedPreferences
                        val sharedPrefs = context.getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE)
                        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
                        
                        // Route to dashboard
                        viewModel.navigateTo(CrimsonScreen.DASHBOARD)
                    }
                }
            )
        }
    }
}

@Composable
fun StepProgressBar(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isCompletedOrCurrent = i <= currentStep
            val color = if (isCompletedOrCurrent) CrimsonPrimary else SleekBorderVariant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_welcome_step"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Large brand icon circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(WarmRose, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_app_logo),
                contentDescription = "CrimsonCare Logo",
                tint = CrimsonPrimary,
                modifier = Modifier.size(64.dp)
            )
        }

        Text(
            text = "Welcome to CrimsonCare",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = (-0.5).sp
            ),
            color = SleekTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your private, intimate companion for cycle mapping, ovulation insights, and symptom tracking.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            color = SleekTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Privacy Promise Card (Polished & Highlighted)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("onboarding_privacy_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
            border = BorderStroke(1.dp, SleekOutline)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Lock Icon",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "PRIVACY GUARANTEE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                Text(
                    text = "Zero Cloud Storage. 100% Local.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = SleekTextPrimary
                )

                Text(
                    text = "All of your cycle parameters, period history, and daily logs are stored exclusively in your device's local database sandbox. There are no accounts, cloud syncs, or trackers. Your personal health journey remains entirely private to you.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = SleekTextSecondary
                )
            }
        }
    }
}

@Composable
fun DateSelectionStep(
    selectedDate: LocalDate,
    formattedDate: String,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_date_step"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(WarmRose, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar Icon",
                tint = CrimsonPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "When did your last period start?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SleekTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Knowing your last cycle's start date allows us to calibrate predictions for your menstruation, ovulation, and fertile cycles accurately.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
            color = SleekTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Date Picker Action Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val calendar = Calendar.getInstance()
                    calendar.set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .testTag("onboarding_date_button"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCream),
            border = BorderStroke(1.5.dp, CrimsonPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LAST PERIOD START DATE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SleekTextPrimary
                    )
                }

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = CrimsonPrimary
                )
            }
        }
    }
}

@Composable
fun CycleLengthStep(
    cycleLength: Int,
    onCycleLengthChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_cycle_step"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(WarmRose, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Cycle Loop Icon",
                tint = CrimsonPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "How long is your typical cycle?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SleekTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "A menstrual cycle is measured from the first day of one period up to the first day of your next period. Standard cycles usually run between 25 and 35 days.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
            color = SleekTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Days Counter Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekBackgroundVariant),
            border = BorderStroke(1.dp, SleekBorderVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    FilledIconButton(
                        onClick = { if (cycleLength > 21) onCycleLengthChanged(cycleLength - 1) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, SleekBorderVariant, CircleShape)
                            .testTag("cycle_decrement")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Days", tint = SleekTextPrimary)
                    }

                    Text(
                        text = "$cycleLength Days",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = CrimsonPrimary
                    )

                    FilledIconButton(
                        onClick = { if (cycleLength < 45) onCycleLengthChanged(cycleLength + 1) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, SleekBorderVariant, CircleShape)
                            .testTag("cycle_increment")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Increase Days", tint = SleekTextPrimary)
                    }
                }

                // Smooth Material 3 Slider for tactile feedback
                Slider(
                    value = cycleLength.toFloat(),
                    onValueChange = { onCycleLengthChanged(it.toInt()) },
                    valueRange = 21f..45f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPrimary,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = SleekBorderVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("cycle_slider")
                )
            }
        }
    }
}

@Composable
fun PeriodDurationStep(
    periodLength: Int,
    onPeriodLengthChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_period_step"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(WarmRose, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Period Star Icon",
                tint = CrimsonPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "How long does your flow last?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SleekTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Count the consecutive number of days you experience active flow. A typical cycle flow duration ranges from 3 to 10 days.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
            color = SleekTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Days Counter Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekBackgroundVariant),
            border = BorderStroke(1.dp, SleekBorderVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    FilledIconButton(
                        onClick = { if (periodLength > 3) onPeriodLengthChanged(periodLength - 1) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, SleekBorderVariant, CircleShape)
                            .testTag("period_decrement")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Days", tint = SleekTextPrimary)
                    }

                    Text(
                        text = "$periodLength Days",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = CrimsonPrimary
                    )

                    FilledIconButton(
                        onClick = { if (periodLength < 10) onPeriodLengthChanged(periodLength + 1) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, SleekBorderVariant, CircleShape)
                            .testTag("period_increment")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Increase Days", tint = SleekTextPrimary)
                    }
                }

                // Smooth Material 3 Slider for tactile feedback
                Slider(
                    value = periodLength.toFloat(),
                    onValueChange = { onPeriodLengthChanged(it.toInt()) },
                    valueRange = 3f..10f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonPrimary,
                        activeTrackColor = CrimsonPrimary,
                        inactiveTrackColor = SleekBorderVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("period_slider")
                )
            }
        }
    }
}

@Composable
fun OnboardingNavigationButtons(
    currentStep: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("onboarding_back_button"),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.5.dp, SleekBorderVariant),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextPrimary)
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(2f)
                .height(52.dp)
                .testTag(if (currentStep == 3) "onboarding_complete_button" else "onboarding_next_button"),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrimsonPrimary,
                contentColor = Color.White
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentStep == 0) {
                        "Let's Get Started"
                    } else if (currentStep == 3) {
                        "Complete Setup"
                    } else {
                        "Next"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Icon(
                    imageVector = if (currentStep == 3) Icons.Default.Check else Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
