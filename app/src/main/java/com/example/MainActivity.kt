package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import android.view.WindowManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.AppLockScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Settings
import android.content.Context
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.example.data.CrimsonDatabase
import com.example.data.CrimsonRepository
import com.example.util.CycleNotificationScheduler
import com.example.ui.CalendarScreen
import com.example.ui.InsightsScreen
import com.example.ui.SettingsScreen
import com.example.ui.OnboardingScreen
import com.example.ui.CrimsonDashboard
import com.example.ui.CrimsonScreen
import com.example.ui.CrimsonViewModel
import com.example.ui.CrimsonViewModelFactory
import com.example.ui.DailyLoggerScreen
import com.example.ui.theme.*

class MainActivity : FragmentActivity() {
    private val isAppUnlocked = mutableStateOf(false)
    private var shouldLockOnStart = true

    private val themeModeState = mutableStateOf("system")
    private val preferenceChangeListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "theme_mode") {
            themeModeState.value = prefs.getString("theme_mode", "system") ?: "system"
        }
    }

    private fun updateWindowSecureFlag() {
        val sharedPrefs = getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE)
        val appLockEnabled = sharedPrefs.getBoolean("app_lock_enabled", false)
        if (appLockEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun checkAndShowBiometricPrompt() {
        val sharedPrefs = getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE)
        val appLockEnabled = sharedPrefs.getBoolean("app_lock_enabled", false)

        if (!appLockEnabled) {
            isAppUnlocked.value = true
            return
        }

        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d("MainActivity", "Biometrics/PIN not enrolled or not supported. Overpassing.")
            isAppUnlocked.value = true
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isAppUnlocked.value = true
                Log.d("MainActivity", "Biometric authentication succeeded.")
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e("MainActivity", "Authentication error: $errString ($errorCode)")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d("MainActivity", "Authentication failed.")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("CrimsonCare Secure Lock")
            .setSubtitle("Access your secure companion offline")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to launch BiometricPrompt", e)
            isAppUnlocked.value = true
        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPrefs = getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE)
        val appLockEnabled = sharedPrefs.getBoolean("app_lock_enabled", false)
        
        updateWindowSecureFlag()

        if (appLockEnabled) {
            if (shouldLockOnStart) {
                isAppUnlocked.value = false
                checkAndShowBiometricPrompt()
                shouldLockOnStart = false
            }
        } else {
            isAppUnlocked.value = true
        }
    }

    override fun onStop() {
        super.onStop()
        shouldLockOnStart = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedPrefs = getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE)
        themeModeState.value = sharedPrefs.getString("theme_mode", "system") ?: "system"
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        
        // Initialize local Room database and Repository
        val database = CrimsonDatabase.getDatabase(applicationContext)
        val repository = CrimsonRepository(database.crimsonDao())
        
        // Construct the ViewModel via standard ViewModelProvider Factory
        val factory = CrimsonViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[CrimsonViewModel::class.java]
        
        // First Launch Detection: check SharedPreferences
        val onboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)
        if (!onboardingCompleted) {
            viewModel.navigateTo(CrimsonScreen.ONBOARDING)
        }

        // Start collecting userSettings and allDailyLogs to schedule/re-schedule all local notifications
        lifecycleScope.launch {
            combine(viewModel.userSettings, viewModel.allDailyLogs) { settings, logs ->
                Pair(settings, logs)
            }.collectLatest { (settings, logs) ->
                CycleNotificationScheduler.scheduleAllNotifications(applicationContext, settings, logs)
            }
        }
        
        enableEdgeToEdge()
        setContent {
            val currentThemeMode by remember { themeModeState }
            MyApplicationTheme(themeMode = currentThemeMode) {
                val unlocked by isAppUnlocked
                val context = androidx.compose.ui.platform.LocalContext.current
                val sharedPrefs = remember(context) { context.getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE) }
                val appLockEnabled = sharedPrefs.getBoolean("app_lock_enabled", false)

                if (appLockEnabled && !unlocked) {
                    AppLockScreen(
                        onTriggerUnlock = { checkAndShowBiometricPrompt() }
                    )
                } else {
                    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

                    val showBottomBar = currentScreen != CrimsonScreen.DAILY_LOGGER && 
                            currentScreen != CrimsonScreen.ONBOARDING &&
                            userSettings != null && 
                            userSettings?.lastPeriodDate?.isNotBlank() == true
                
                val backgroundGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFDE7E9), // Light Pink
                        Color(0xFFFFF1F2), // Soft Peach
                        Color(0xFFFFFFFF)  // White
                    )
                )

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundGradient)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar(
                                    containerColor = Color.White.copy(alpha = 0.45f),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.60f),
                                                    Color.White.copy(alpha = 0.25f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                        )
                                        .testTag("bottom_nav_bar")
                                ) {
                                NavigationBarItem(
                                    selected = currentScreen == CrimsonScreen.DASHBOARD,
                                    onClick = { viewModel.navigateTo(CrimsonScreen.DASHBOARD) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Home",
                                            tint = if (currentScreen == CrimsonScreen.DASHBOARD) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "HOME",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = if (currentScreen == CrimsonScreen.DASHBOARD) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = WarmRose
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == CrimsonScreen.CALENDAR,
                                    onClick = { viewModel.navigateTo(CrimsonScreen.CALENDAR) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Calendar",
                                            tint = if (currentScreen == CrimsonScreen.CALENDAR) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "CALENDAR",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = if (currentScreen == CrimsonScreen.CALENDAR) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = WarmRose
                                    ),
                                    modifier = Modifier.testTag("nav_calendar")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == CrimsonScreen.INSIGHTS,
                                    onClick = { viewModel.navigateTo(CrimsonScreen.INSIGHTS) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Insights",
                                            tint = if (currentScreen == CrimsonScreen.INSIGHTS) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "INSIGHTS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = if (currentScreen == CrimsonScreen.INSIGHTS) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = WarmRose
                                    ),
                                    modifier = Modifier.testTag("nav_insights")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == CrimsonScreen.SETTINGS,
                                    onClick = { viewModel.navigateTo(CrimsonScreen.SETTINGS) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = if (currentScreen == CrimsonScreen.SETTINGS) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "SETTINGS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            color = if (currentScreen == CrimsonScreen.SETTINGS) CrimsonPrimary else SleekTextSecondary
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = WarmRose
                                    ),
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        modifier = Modifier.padding(innerPadding),
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            CrimsonScreen.ONBOARDING -> {
                                OnboardingScreen(
                                    viewModel = viewModel
                                )
                            }
                            CrimsonScreen.DASHBOARD -> {
                                CrimsonDashboard(
                                    viewModel = viewModel
                                )
                            }
                            CrimsonScreen.DAILY_LOGGER -> {
                                DailyLoggerScreen(
                                    viewModel = viewModel
                                )
                            }
                            CrimsonScreen.CALENDAR -> {
                                CalendarScreen(
                                    viewModel = viewModel
                                )
                            }
                            CrimsonScreen.INSIGHTS -> {
                                InsightsScreen(
                                    viewModel = viewModel
                                )
                            }
                            CrimsonScreen.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
                } // Closing the Box
                } // Closing the else { block
            }
        }
    }

    override fun onDestroy() {
        val sharedPrefs = getSharedPreferences("crimson_care_prefs", Context.MODE_PRIVATE)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onDestroy()
    }
}
