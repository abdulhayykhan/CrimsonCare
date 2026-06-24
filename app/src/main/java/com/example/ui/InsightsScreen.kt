package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyLog
import com.example.ui.theme.*
import com.example.util.CycleHistoryItem
import com.example.util.PeriodCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: CrimsonViewModel,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allDailyLogs.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    val averageCycleLength = userSettings?.averageCycleLength ?: 28

    // Calculate past cycles & symptoms using our PeriodCalculator helpers
    val pastCycles = remember(allLogs) {
        PeriodCalculator.calculatePastCycleLengths(allLogs)
    }

    val topSymptoms = remember(allLogs) {
        PeriodCalculator.calculateTopSymptomsPast3Months(allLogs)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "CYCLE TRENDS & ANALYTICS",
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
            // Header Description Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("insights_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                border = BorderStroke(1.dp, SleekOutline)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(WarmRose, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Insights Logo",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Health Trends",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Understand your bodies natural rhythms by tracking changes in cycle lengths and symptoms over time.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = SleekTextSecondary
                        )
                    }
                }
            }

            // Requirement 1: Cycle History Section (Bar Chart & Visual List)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CYCLE HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextSecondary
                )

                if (pastCycles.isEmpty()) {
                    // Help state explaining how cycles are populated
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_cycle_history_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekBackgroundVariant),
                        border = BorderStroke(1.dp, SleekBorderVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "More Data Needed",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "A completed cycle is calculated between the start dates of two consecutive period episodes. Record period flows across multiple months to populate this visual chart.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Beautiful Side-by-Side Bar Chart representation of past cycle lengths
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cycle_bar_chart_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekBackgroundVariant),
                        border = BorderStroke(1.dp, SleekBorderVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Past Cycle Durations",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekTextPrimary
                            )

                            // Chart container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                // Draw horizontal reference lines (e.g., standard 28-day reference)
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf("35d", "28d", "21d", "14d").forEach { days ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = days,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = SleekTextSecondary,
                                                modifier = Modifier.width(28.dp)
                                            )
                                            Spacer(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(1.dp)
                                                    .background(SleekBorderVariant)
                                            )
                                        }
                                    }
                                }

                                // Side-by-Side Bar Grid
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 32.dp, end = 8.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    pastCycles.takeLast(5).forEachIndexed { index, cycle ->
                                        // Proportional height factor based on max 40 days
                                        val heightRatio = (cycle.lengthDays.toFloat() / 40f).coerceIn(0.1f, 1.0f)
                                        val animatedHeightFraction by animateFloatAsState(
                                            targetValue = heightRatio,
                                            animationSpec = tween(1000, delayMillis = index * 100),
                                            label = "barHeight"
                                        )

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = "${cycle.lengthDays}d",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                color = CrimsonPrimary
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Box(
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .fillMaxHeight(animatedHeightFraction)
                                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                    .background(
                                                        if (cycle.lengthDays in 21..35) CrimsonPrimary else ColorLuteal
                                                    )
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            val formatter = DateTimeFormatter.ofPattern("MM/dd")
                                            Text(
                                                text = cycle.startDate.format(formatter),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = SleekTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Divider(color = SleekBorderVariant)

                            // Average Cycle stat pill description
                            val averageCalculated = pastCycles.map { it.lengthDays }.average().toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Average Cycle Length",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SleekTextSecondary
                                    )
                                    Text(
                                        text = "$averageCalculated Days",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = CrimsonPrimary
                                    )
                                }

                                val isRegular = averageCalculated in (averageCycleLength - 2)..(averageCycleLength + 2)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isRegular) WarmRose else SleekBorderVariant,
                                            RoundedCornerShape(100)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isRegular) "Highly Regular" else "Varies Slightly",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = if (isRegular) CrimsonPrimary else SleekTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Visual List of Past Cycles
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pastCycles.reversed().forEachIndexed { idx, cycle ->
                            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, SleekBorderVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Cycle #${pastCycles.size - idx}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = SleekTextPrimary
                                        )
                                        Text(
                                            text = "${cycle.startDate.format(formatter)} - ${cycle.endDate.format(formatter)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SleekTextSecondary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${cycle.lengthDays}",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            ),
                                            color = CrimsonPrimary
                                        )
                                        Text(
                                            text = "days",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SleekTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Requirement 2: Symptom Summary Section (Frequent Symptoms Past 3 Months)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "MOST FREQUENT SYMPTOMS (3M)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextSecondary
                )

                if (topSymptoms.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("empty_symptoms_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekBackgroundVariant),
                        border = BorderStroke(1.dp, SleekBorderVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No Symptom Logs Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Logged daily symptoms from the main page or calendar to view a breakdown of your most frequent physical and emotional changes here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SleekTextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("symptoms_breakdown_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SleekBorderVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val maxCount = topSymptoms.first().second.toFloat()

                            topSymptoms.forEach { (symptom, count) ->
                                val progressRatio = count.toFloat() / maxCount
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progressRatio,
                                    animationSpec = tween(800),
                                    label = "symptomProgress"
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = symptom,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = SleekTextPrimary
                                        )

                                        Box(
                                            modifier = Modifier
                                                .background(WarmRose, RoundedCornerShape(100))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (count == 1) "1 time" else "$count times",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = CrimsonPrimary
                                            )
                                        }
                                    }

                                    // Visual progress meter
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = CrimsonPrimary,
                                        trackColor = SleekBorderVariant
                                    )
                                }
                            }
                        }
                    }

                    // Personalized Health Tip Card based on top logged symptom
                    val primarySymptom = topSymptoms.first().first
                    val recommendationText = when (primarySymptom.lowercase()) {
                        "cramps" -> "Cramps are often caused by uterine contraction. Apply a heating pad, increase magnesium intake, and stay hydrated with lukewarm water."
                        "bloating" -> "To reduce period bloating, limit sodium and processed foods. Incorporate ginger tea, cucumbers, and light walks into your routine."
                        "headache" -> "Hormonal headaches can trigger during drops in estrogen. Focus on sleep, reduce screen time, and maintain stable blood sugar."
                        "fatigue" -> "Low energy is natural during menstruation. Prioritize restorative rest, focus on iron-rich foods, and avoid intense workouts."
                        "mood swings" -> "Emotional fluctuations are driven by hormonal shifts. Practice deep breathing, step into nature, and treat yourself with kindness."
                        else -> "Keep staying hydrated, practicing light movement, and prioritizing restful sleep to support your body's hormone cycle."
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmRose),
                        border = BorderStroke(1.dp, SleekOutline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(CrimsonPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Care Tip",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Care Tip: Managing $primarySymptom",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = CrimsonSecondary
                                )
                            }
                            Text(
                                text = recommendationText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                color = SleekTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
