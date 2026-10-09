package com.bulktracker.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bulktracker.app.ui.components.BottomBarItem
import com.bulktracker.app.ui.components.CustomBottomBar
import com.bulktracker.app.ui.history.HistoryScreen
import com.bulktracker.app.ui.history.HistoryViewModel
import com.bulktracker.app.ui.schedule.ScheduleScreen
import com.bulktracker.app.ui.schedule.ScheduleViewModel
import com.bulktracker.app.ui.settings.SettingsScreen
import com.bulktracker.app.ui.settings.SettingsViewModel
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.ui.today.TodayScreen
import com.bulktracker.app.ui.today.TodayViewModel
import com.bulktracker.app.util.DateUtils
import java.time.LocalDate
import java.time.LocalDateTime

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Today : Screen("today", "Today", Icons.Outlined.CheckCircle)
    data object Tasks : Screen("tasks", "Tasks", Icons.Outlined.TaskAlt)
    data object Schedule : Screen("schedule", "Schedule", Icons.Outlined.DateRange)
    data object History : Screen("history", "History", Icons.Outlined.CalendarMonth)
    data object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private var powerSaveReceiver: BroadcastReceiver? = null

    private fun applyBestRefreshRate() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager?.isPowerSaveMode == true) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = 0
                }
                return
            }

            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            } ?: return

            val currentMode = display.mode ?: return
            val currentWidth = currentMode.physicalWidth
            val currentHeight = currentMode.physicalHeight

            val bestMode = display.supportedModes
                .filter { it.physicalWidth == currentWidth && it.physicalHeight == currentHeight }
                .maxByOrNull { it.refreshRate }

            if (bestMode != null) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = bestMode.modeId
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    applyBestRefreshRate()
                }
            }
            powerSaveReceiver = receiver
            ContextCompat.registerReceiver(
                this,
                receiver,
                IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            powerSaveReceiver?.let {
                unregisterReceiver(it)
                powerSaveReceiver = null
            }
        } catch (_: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as BulkTrackerApp
        val container = app.container

        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // S4: Validate untrusted intent extras
        val incomingEpochDay = intent?.getLongExtra("epochDay", Long.MIN_VALUE) ?: Long.MIN_VALUE
        if (incomingEpochDay != Long.MIN_VALUE) {
            val now = LocalDate.now()
            val incomingDate = LocalDate.ofEpochDay(incomingEpochDay)
            if (Math.abs(incomingDate.toEpochDay() - now.toEpochDay()) <= 3650) {
                container.selectedDate.value = incomingDate
            }
        }

        setContent {
            val settings by container.trackerRepository.observeSettings().collectAsStateWithLifecycle(
                initialValue = com.bulktracker.app.data.db.SettingsEntity()
            )

            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (settings.themeMode) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }

            // R3: Update edgeToEdge styling on themeMode change
            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT
                    ) { isDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT,
                        AndroidColor.TRANSPARENT
                    ) { isDarkTheme }
                )
                onDispose { }
            }

            // T4 & R16: Recompute logicalToday on ON_RESUME
            DisposableEffect(this) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        applyBestRefreshRate()
                        val currentToday = DateUtils.logicalToday(LocalDateTime.now(), settings.dayStartHour)
                        if (container.selectedDate.value.isBefore(currentToday)) {
                            container.selectedDate.value = currentToday
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            BulkTrackerTheme(darkTheme = isDarkTheme) {
                MainAppContent(container = container)
            }
        }
        applyBestRefreshRate()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val app = application as BulkTrackerApp
        val container = app.container
        val incomingEpochDay = intent.getLongExtra("epochDay", Long.MIN_VALUE)
        if (incomingEpochDay != Long.MIN_VALUE) {
            val now = LocalDate.now()
            val incomingDate = LocalDate.ofEpochDay(incomingEpochDay)
            if (Math.abs(incomingDate.toEpochDay() - now.toEpochDay()) <= 3650) {
                container.selectedDate.value = incomingDate
            }
        }
    }
}

@Composable
fun MainAppContent(container: AppContainer) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = BulkTrackerTheme.colors

    val items = listOf(
        Screen.Today,
        Screen.Tasks,
        Screen.Schedule,
        Screen.History,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val showBottomNav = currentRoute in items.map { it.route }
            if (showBottomNav) {
                CustomBottomBar(
                    items = items.map { screen ->
                        BottomBarItem(
                            route = screen.route,
                            label = screen.label,
                            icon = screen.icon
                        )
                    },
                    currentRoute = currentRoute,
                    onItemSelected = { targetRoute ->
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                val viewModel: TodayViewModel = viewModel(
                    factory = TodayViewModel.provideFactory(container)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                TodayScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    snackbarHostState = snackbarHostState
                )
            }

            composable(Screen.Tasks.route) {
                val viewModel: com.bulktracker.app.ui.tasks.TasksViewModel = viewModel(
                    factory = com.bulktracker.app.ui.tasks.TasksViewModel.provideFactory(container)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.bulktracker.app.ui.tasks.TasksScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            composable(Screen.Schedule.route) {
                val viewModel: com.bulktracker.app.ui.schedule.ScheduleViewModel = viewModel(
                    factory = com.bulktracker.app.ui.schedule.ScheduleViewModel.provideFactory(container)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.bulktracker.app.ui.schedule.ScheduleScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            composable(Screen.History.route) {
                val viewModel: com.bulktracker.app.ui.history.HistoryViewModel = viewModel(
                    factory = com.bulktracker.app.ui.history.HistoryViewModel.provideFactory(container)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.bulktracker.app.ui.history.HistoryScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            composable(Screen.Settings.route) {
                val viewModel: com.bulktracker.app.ui.settings.SettingsViewModel = viewModel(
                    factory = com.bulktracker.app.ui.settings.SettingsViewModel.provideFactory(container)
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.bulktracker.app.ui.settings.SettingsScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}
