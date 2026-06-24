package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyLog
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLoggerScreen(
    viewModel: CrimsonViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val existingLog by viewModel.selectedDateLog.collectAsStateWithLifecycle()

    // Parse the current selected date
    val parsedDate = remember(selectedDate) {
        try {
            LocalDate.parse(selectedDate)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    val displayDateStr = remember(parsedDate) {
        parsedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
    }

    // Input States
    var selectedFlowIntensity by remember { mutableStateOf(0) }
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    // Prepopulate states when existingLog loads or changes
    LaunchedEffect(existingLog, selectedDate) {
        if (existingLog != null) {
            selectedFlowIntensity = existingLog!!.flowIntensity
            selectedSymptoms.clear()
            if (existingLog!!.symptoms.isNotBlank()) {
                selectedSymptoms.addAll(existingLog!!.symptoms.split(","))
            }
        } else {
            selectedFlowIntensity = 0
            selectedSymptoms.clear()
        }
    }

    // Common symptoms list with associated icons and labels
    val symptomItems = remember {
        listOf(
            SymptomItem("Cramps", Icons.Default.Warning, "cramps_toggle"),
            SymptomItem("Bloating", Icons.Default.Info, "bloating_toggle"),
            SymptomItem("Headache", Icons.Default.Face, "headache_toggle"),
            SymptomItem("Fatigue", Icons.Default.Home, "fatigue_toggle"),
            SymptomItem("Mood Swings", Icons.Default.FavoriteBorder, "mood_swings_toggle")
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Logger",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "LOG SYMPTOMS & FLOW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp
                            ),
                            color = SleekTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(CrimsonScreen.DASHBOARD) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextPrimary
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
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Requirement 1: Date Selection Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "DATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextSecondary
                )

                // Date selection card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            calendar.set(parsedDate.year, parsedDate.monthValue - 1, parsedDate.dayOfMonth)
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    viewModel.selectDate(newDate.toString())
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .testTag("date_picker_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                    border = BorderStroke(1.dp, SleekOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(WarmRose, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = CrimsonSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayDateStr,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (parsedDate == LocalDate.now()) "Today" else "Click to change date",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Requirement 2: Flow Intensity Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FLOW INTENSITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextSecondary
                )

                // Selectable flow intensity cards in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intensities = listOf(
                        FlowOption(0, "None", "flow_none", Color(0xFF90A4AE)),
                        FlowOption(1, "Light", "flow_light", Color(0xFFF06292)),
                        FlowOption(2, "Medium", "flow_medium", Color(0xFFE91E63)),
                        FlowOption(3, "Heavy", "flow_heavy", Color(0xFFC2185B))
                    )

                    intensities.forEach { option ->
                        val isSelected = selectedFlowIntensity == option.value
                        val cardBg by animateColorAsState(
                            targetValue = if (isSelected) WarmRose else SleekBackgroundVariant,
                            animationSpec = tween(durationMillis = 200),
                            label = "cardBg"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) CrimsonPrimary else SleekBorderVariant,
                            animationSpec = tween(durationMillis = 200),
                            label = "borderColor"
                        )

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(84.dp)
                                .clickable { selectedFlowIntensity = option.value }
                                .testTag(option.tag),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.5.dp, borderColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Draw droplet indicator or None indicator
                                if (option.value == 0) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "None",
                                        tint = if (isSelected) CrimsonPrimary else SleekTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    // Custom visual indicators for Light, Medium, Heavy
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                color = option.color.copy(alpha = if (isSelected) 1f else 0.4f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (option.value >= 2) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color.White, CircleShape)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) CrimsonPrimary else SleekTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Requirement 3: Symptoms Grid Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SYMPTOMS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextSecondary
                )

                // Grid layout for symptom items (using clean custom grids since nested scroll can be tricky,
                // let's use standard Column + Row pairs for 100% robust layout inside ScrollState)
                val chunkedSymptoms = symptomItems.chunked(2)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chunkedSymptoms.forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { symptom ->
                                val isSelected = selectedSymptoms.contains(symptom.name)
                                val cardBg by animateColorAsState(
                                    targetValue = if (isSelected) WarmRose else SleekBackgroundVariant,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "symptomBg"
                                )
                                val borderColor by animateColorAsState(
                                    targetValue = if (isSelected) CrimsonPrimary else SleekBorderVariant,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "symptomBorder"
                                )

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clickable {
                                            if (isSelected) {
                                                selectedSymptoms.remove(symptom.name)
                                            } else {
                                                selectedSymptoms.add(symptom.name)
                                            }
                                        }
                                        .testTag(symptom.tag),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.5.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    color = if (isSelected) CrimsonPrimary.copy(alpha = 0.15f) else Color.White,
                                                    shape = RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = symptom.icon,
                                                contentDescription = symptom.name,
                                                tint = if (isSelected) CrimsonPrimary else SleekTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Text(
                                            text = symptom.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                fontSize = 14.sp
                                            ),
                                            color = SleekTextPrimary
                                        )
                                    }
                                }
                            }

                            // If row is incomplete, add empty placeholder spacer
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Requirement 4: Save Action
            Button(
                onClick = {
                    viewModel.saveDailyLog(
                        date = selectedDate,
                        flowIntensity = selectedFlowIntensity,
                        symptoms = selectedSymptoms.toList()
                    )
                    viewModel.navigateTo(CrimsonScreen.DASHBOARD)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_log_button"),
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
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Save Daily Log",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}

data class SymptomItem(
    val name: String,
    val icon: ImageVector,
    val tag: String
)

data class FlowOption(
    val value: Int,
    val label: String,
    val tag: String,
    val color: Color
)
