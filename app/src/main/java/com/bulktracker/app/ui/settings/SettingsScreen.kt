package com.bulktracker.app.ui.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bulktracker.app.alarm.AlarmDiagnostics
import com.bulktracker.app.ui.components.PermissionsCard
import com.bulktracker.app.ui.components.SectionDot
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.TitleLg
import com.bulktracker.app.ui.theme.TitleMd
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    uiState: SettingsUiState
) {
    val context = LocalContext.current
    val colors = BulkTrackerTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setAlarmsEnabled(true)
        } else {
            viewModel.setAlarmsEnabled(false)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications are needed for alarms")
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Tokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(Tokens.SpaceLg)
        ) {
            item {
                Spacer(modifier = Modifier.height(Tokens.SpaceSm))
                Text(
                    text = "Settings",
                    style = TitleLg,
                    color = colors.textPrimary
                )
            }

            // Alarms section
            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(Tokens.RadiusLg),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = androidx.compose.foundation.BorderStroke(Tokens.BorderThin, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Tokens.SpaceMd),
                        verticalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alarms",
                                    style = TitleMd,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Bulk Tracker rings when it's time to eat.",
                                    fontSize = Tokens.BodySmSize,
                                    color = colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                            Switch(
                                checked = uiState.settings.alarmsEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (!hasPermission) {
                                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                return@Switch
                                            }
                                        }
                                        viewModel.setAlarmsEnabled(true)
                                    } else {
                                        viewModel.setAlarmsEnabled(false)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.onInk,
                                    checkedTrackColor = colors.ink,
                                    uncheckedThumbColor = colors.textTertiary,
                                    uncheckedTrackColor = colors.surface
                                )
                            )
                        }
                    }
                }
            }

            // Permissions card (from block D3)
            item {
                PermissionsCard()
            }

            // Per-meal switches
            item {
                Text(
                    text = "Meal alarms",
                    style = TitleMd,
                    color = colors.textPrimary
                )
            }

            items(uiState.sections, key = { it.id }) { section ->
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(Tokens.RadiusMd),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = androidx.compose.foundation.BorderStroke(Tokens.BorderThin, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Tokens.SpaceMd, vertical = Tokens.SpaceSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val secColor = try {
                            Color(android.graphics.Color.parseColor(section.colorHex))
                        } catch (e: Exception) {
                            colors.ink
                        }

                        SectionDot(color = secColor)
                        Spacer(modifier = Modifier.width(Tokens.SpaceSm))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = section.name,
                                fontSize = Tokens.BodySize,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = DateUtils.formatMold(section.idealMold),
                                fontSize = Tokens.CaptionSize,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = section.alarmEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.setSectionAlarmEnabled(section.id, enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.onInk,
                                checkedTrackColor = colors.ink,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surface
                            )
                        )
                    }
                }
            }

            // Diagnostics Card
            item {
                val nextAlarmStr = remember {
                    val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    val nextAlarm = am?.nextAlarmClock
                    if (nextAlarm != null) {
                        val dt = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(nextAlarm.triggerTime),
                            ZoneId.systemDefault()
                        )
                        dt.format(DateTimeFormatter.ofPattern("EEE, d MMM h:mm a"))
                    } else {
                        "none"
                    }
                }

                val lastScheduled by AlarmDiagnostics.lastScheduledAlarms.collectAsStateWithLifecycle()
                val lastRun by AlarmDiagnostics.lastReceiverRun.collectAsStateWithLifecycle()

                Card(
                    shape = RoundedCornerShape(Tokens.RadiusLg),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    border = BorderStroke(Tokens.BorderThin, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Tokens.SpaceMd),
                        verticalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
                    ) {
                        Text(
                            text = "Alarm diagnostics",
                            fontSize = Tokens.BodySmSize,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )

                        HorizontalDivider(color = colors.border, thickness = Tokens.BorderThin)

                        // 1. Next AlarmClock
                        Text(
                            text = "Next alarm clock:",
                            fontSize = Tokens.CaptionSize,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Text(
                            text = nextAlarmStr,
                            fontSize = Tokens.BodySmSize,
                            color = colors.textPrimary
                        )

                        HorizontalDivider(color = colors.border, thickness = Tokens.BorderThin)

                        // 2. Last scheduled MAIN alarms
                        Text(
                            text = "Last scheduled MAIN alarms:",
                            fontSize = Tokens.CaptionSize,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        if (lastScheduled.isEmpty()) {
                            Text(
                                text = "none",
                                fontSize = Tokens.BodySmSize,
                                color = colors.textTertiary
                            )
                        } else {
                            lastScheduled.forEach { item ->
                                Text(
                                    text = "• ${item.sectionName} — ${item.triggerTimeFormatted}",
                                    fontSize = Tokens.CaptionSize,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        HorizontalDivider(color = colors.border, thickness = Tokens.BorderThin)

                        // 3. Last AlarmReceiver run
                        Text(
                            text = "Last receiver run:",
                            fontSize = Tokens.CaptionSize,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        if (lastRun == null) {
                            Text(
                                text = "none",
                                fontSize = Tokens.BodySmSize,
                                color = colors.textTertiary
                            )
                        } else {
                            Text(
                                text = "[${lastRun!!.timeFormatted}] ${lastRun!!.type} (${lastRun!!.sectionName})",
                                fontSize = Tokens.CaptionSize,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Result: ${lastRun!!.result}",
                                fontSize = Tokens.CaptionSize,
                                color = if (lastRun!!.result == "rang") colors.success else colors.textSecondary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Tokens.SpaceLg))
            }
        }
    }
}
