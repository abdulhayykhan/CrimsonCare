package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.util.CsvExporter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CrimsonViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val allLogs by viewModel.allDailyLogs.collectAsStateWithLifecycle()

    var exportStatus by remember { mutableStateOf<String?>(null) }
    var exportSuccess by remember { mutableStateOf(false) }

    // Notification Permission Handling for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Automatically granted on older versions
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            // Re-trigger alarm setup
            userSettings?.let { settings ->
                com.example.util.CycleNotificationScheduler.schedulePredictionNotification(context, settings)
            }
        } else {
            Toast.makeText(context, "Prediction reminders require notification permission.", Toast.LENGTH_LONG).show()
        }
    }

    val sharedPrefs = remember(context) { context.getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE) }
    var isAppLockEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("app_lock_enabled", false))
    }
    var selectedTheme by remember {
        mutableStateOf(sharedPrefs.getString("theme_mode", "system") ?: "system")
    }

    // Helper to calculate total logged symptoms
    val totalSymptomsCount = remember(allLogs) {
        allLogs.sumOf { log ->
            if (log.symptoms.isNotBlank()) {
                log.symptoms.split(",").filter { it.trim().isNotEmpty() }.size
            } else 0
        }
    }

    // Export logic triggered after permission check/approval
    val triggerExport = {
        try {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val filename = "crimson_care_data_$timestamp.csv"
            val csvContent = CsvExporter.generateCsvContent(userSettings, allLogs)
            val fileUri = CsvExporter.exportToDownloads(context, filename, csvContent)
            
            if (fileUri != null) {
                exportStatus = "Data exported successfully to Downloads folder as:\n$filename"
                exportSuccess = true
                Toast.makeText(context, "Export Successful!", Toast.LENGTH_SHORT).show()
            } else {
                exportStatus = "Failed to export data. Please try again."
                exportSuccess = false
            }
        } catch (e: Exception) {
            exportStatus = "Error: ${e.localizedMessage}"
            exportSuccess = false
        }
    }

    // Permission request launcher for older Android versions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            triggerExport()
        } else {
            exportStatus = "Permission denied. Cannot write file to Downloads directory on older Android versions without storage permission."
            exportSuccess = false
        }
    }

    val requestPermissionOrExport = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // MediaStore API on Q+ does not require any permission to write to Downloads
            triggerExport()
        } else {
            val checkPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (checkPermission == PackageManager.PERMISSION_GRANTED) {
                triggerExport()
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "APP PREFERENCES & EXPORTS",
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
            // 1. Data Export Preferences Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_data_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                border = BorderStroke(1.dp, SleekOutline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(WarmRose, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "CSV Export Icon",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export Cycle Records",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "Secure, Offline, Private",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "Export your local cycle parameters, flow intensities, and symptom logs to a standardized, plain-text CSV file. This file is saved directly to your phone's public Downloads folder. We use local file operations only, ensuring complete cloud privacy.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = SleekTextSecondary
                    )

                    Button(
                        onClick = { requestPermissionOrExport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_csv_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Export Data to CSV",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    // Display Export Status Results
                    exportStatus?.let { status ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("export_status_alert"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (exportSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (exportSuccess) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (exportSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (exportSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 16.sp
                                    ),
                                    color = if (exportSuccess) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
            }

            // 1.5 Local Prediction Notifications Preferences Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notifications_pref_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                border = BorderStroke(1.dp, SleekOutline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(WarmRose, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notification Icon",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prediction Alerts",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (hasNotificationPermission) "Enabled" else "Permission Required",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasNotificationPermission) Color(0xFF2E7D32) else CrimsonPrimary,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "Receive a friendly local reminder exactly 2 days before your predicted cycle is expected to start. This reminder operates 100% on-device, preserving your complete data privacy.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = SleekTextSecondary
                    )

                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("request_notifications_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Enable Prediction Reminders",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Your predictive local reminder is active and scheduled.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                }
            }

            // 1.6 Offline Security and App Lock Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_lock_security_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                border = BorderStroke(1.dp, SleekOutline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(WarmRose, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security App Lock",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric / PIN App Lock",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (isAppLockEnabled) "Enforced" else "Disabled",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAppLockEnabled) CrimsonPrimary else SleekTextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { checked ->
                                isAppLockEnabled = checked
                                sharedPrefs.edit().putBoolean("app_lock_enabled", checked).apply()
                                // Dynamically update the FLAG_SECURE flag on the activity window
                                (context as? android.app.Activity)?.let { activity ->
                                    if (checked) {
                                        activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                                    } else {
                                        activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                                    }
                                }
                                val statusMsg = if (checked) "App Lock enabled successfully!" else "App Lock disabled."
                                Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CrimsonPrimary,
                                uncheckedThumbColor = SleekTextSecondary,
                                uncheckedTrackColor = SleekBorderVariant
                            ),
                            modifier = Modifier.testTag("app_lock_switch")
                        )
                    }

                    Text(
                        text = "Require fingerprint, face unlock, or device PIN every time CrimsonCare is launched or returned from background. Prevents unwanted access to your personal cycle statistics.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = SleekTextSecondary
                    )
                }
            }

            // 1.7 Visual Theme Selection Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("visual_theme_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                border = BorderStroke(1.dp, SleekOutline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(WarmRose, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Visual Theme Settings",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Application Theme",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = SleekTextPrimary
                            )
                            Text(
                                text = when (selectedTheme) {
                                    "light" -> "Force Light Mode"
                                    "dark" -> "Force Dark Mode"
                                    else -> "Match System Default"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimary,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = "Choose whether CrimsonCare matches your device appearance or forces a custom light or dark aesthetic.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = SleekTextSecondary
                    )

                    // Triple segmented buttons/selectors for Light, Dark, System
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(
                            Triple("light", "Light", "theme_btn_light"),
                            Triple("dark", "Dark", "theme_btn_dark"),
                            Triple("system", "System", "theme_btn_system")
                        )
                        options.forEach { (mode, label, tag) ->
                            val isSelected = selectedTheme == mode
                            Button(
                                onClick = {
                                    selectedTheme = mode
                                    sharedPrefs.edit().putString("theme_mode", mode).apply()
                                    Toast.makeText(context, "$label Theme applied successfully!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(tag),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) CrimsonPrimary else SleekBackgroundVariant,
                                    contentColor = if (isSelected) Color.White else SleekTextPrimary
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                border = if (isSelected) null else BorderStroke(1.dp, SleekBorderVariant)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. Data Statistics Panel
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "DATABASE METRICS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextSecondary
                )

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("database_stats_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekBackgroundVariant),
                    border = BorderStroke(1.dp, SleekBorderVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        StatRow(label = "Total Days Tracked", value = "${allLogs.size}")
                        Divider(color = SleekBorderVariant)
                        StatRow(label = "Total Symptoms Logged", value = "$totalSymptomsCount")
                        Divider(color = SleekBorderVariant)
                        StatRow(label = "Configured Cycle Duration", value = "${userSettings?.averageCycleLength ?: 28} days")
                        Divider(color = SleekBorderVariant)
                        StatRow(label = "Configured Flow Duration", value = "${userSettings?.averagePeriodLength ?: 5} days")
                    }
                }
            }

            // 3. Re-Onboard / Reset App Panel
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ADVANCED CONTROLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = SleekTextSecondary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekBorderVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Reconfigure Onboarding Settings",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekTextPrimary
                            )
                        }

                        Text(
                            text = "Clears current tracking settings (your average cycle duration and last period date) to let you restart the setup process. Your past symptom and flow logs will NOT be deleted.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 15.sp),
                            color = SleekTextSecondary
                        )

                        var showResetConfirm by remember { mutableStateOf(false) }

                        if (!showResetConfirm) {
                            Button(
                                onClick = { showResetConfirm = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("reset_settings_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekBackgroundVariant,
                                    contentColor = SleekTextPrimary
                                ),
                                shape = RoundedCornerShape(22.dp),
                                border = BorderStroke(1.dp, SleekBorderVariant)
                            ) {
                                Text(
                                    text = "Reset Onboarding Setup",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showResetConfirm = false },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SleekBackgroundVariant,
                                        contentColor = SleekTextPrimary
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        viewModel.resetSettings()
                                        val sharedPrefs = context.getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE)
                                        sharedPrefs.edit().putBoolean("onboarding_completed", false).apply()
                                        viewModel.navigateTo(CrimsonScreen.ONBOARDING)
                                        Toast.makeText(context, "Settings Reset Successfully", Toast.LENGTH_SHORT).show()
                                        showResetConfirm = false
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("confirm_reset_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CrimsonPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Text("Confirm Reset", fontWeight = FontWeight.Bold)
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
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SleekTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SleekTextPrimary
        )
    }
}
