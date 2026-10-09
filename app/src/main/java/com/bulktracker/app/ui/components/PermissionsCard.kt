package com.bulktracker.app.ui.components

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bulktracker.app.ui.theme.BulkTrackerTheme
import com.bulktracker.app.ui.theme.Tokens

data class PermissionsState(
    val notificationsGranted: Boolean,
    val exactAlarmsGranted: Boolean,
    val fullScreenGranted: Boolean
) {
    val hasProblem: Boolean
        get() = !notificationsGranted || !exactAlarmsGranted || !fullScreenGranted
}

fun checkPermissionsState(context: Context): PermissionsState {
    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val exactGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() ?: false
    } else {
        true
    }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        notificationManager?.canUseFullScreenIntent() ?: false
    } else {
        true
    }

    return PermissionsState(
        notificationsGranted = notifGranted,
        exactAlarmsGranted = exactGranted,
        fullScreenGranted = fullScreenGranted
    )
}

@Composable
fun PermissionsCard(
    modifier: Modifier = Modifier,
    onPermissionChanged: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = BulkTrackerTheme.colors
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionsState by remember { mutableStateOf(checkPermissionsState(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsState = checkPermissionsState(context)
                onPermissionChanged?.invoke()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        permissionsState = checkPermissionsState(context)
        onPermissionChanged?.invoke()
    }

    Card(
        shape = RoundedCornerShape(Tokens.RadiusLg),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        border = BorderStroke(Tokens.BorderThin, colors.border),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Tokens.SpaceMd),
            verticalArrangement = Arrangement.spacedBy(Tokens.SpaceSm)
        ) {
            Text(
                text = "Alarm permissions",
                style = com.bulktracker.app.ui.theme.TitleMd,
                color = colors.textPrimary
            )

            // 1. Notifications
            PermissionRow(
                title = "Notifications",
                isGranted = permissionsState.notificationsGranted,
                onFix = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                }
            )

            HorizontalDivider(color = colors.border, thickness = Tokens.BorderThin)

            // 2. Exact alarms
            PermissionRow(
                title = "Exact alarms",
                isGranted = permissionsState.exactAlarmsGranted,
                onFix = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            )

            HorizontalDivider(color = colors.border, thickness = Tokens.BorderThin)

            // 3. Full-screen alarms
            PermissionRow(
                title = "Full-screen alarms",
                isGranted = permissionsState.fullScreenGranted,
                onFix = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            )

            HorizontalDivider(color = colors.border, thickness = Tokens.BorderThin)

            // 4. Battery: always shown as a grey info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Tokens.SpaceXs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(Tokens.IconSm)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                Text(
                    text = "OnePlus can stop alarms. Set battery usage to Unrestricted.",
                    fontSize = Tokens.CaptionSize,
                    lineHeight = Tokens.CaptionLineHeight,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Tokens.SpaceSm))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    border = BorderStroke(Tokens.BorderThin, colors.border)
                ) {
                    Text(
                        text = "Fix",
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onFix: () -> Unit
) {
    val colors = BulkTrackerTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Tokens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGranted) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Granted",
                tint = colors.success,
                modifier = Modifier.size(Tokens.IconSm)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Not granted",
                tint = colors.danger,
                modifier = Modifier.size(Tokens.IconSm)
            )
        }

        Spacer(modifier = Modifier.width(Tokens.SpaceSm))

        Text(
            text = title,
            fontSize = Tokens.BodySmSize,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        if (!isGranted) {
            OutlinedButton(
                onClick = onFix,
                shape = RoundedCornerShape(Tokens.RadiusMd),
                border = BorderStroke(Tokens.BorderThin, colors.border)
            ) {
                Text(
                    text = "Fix",
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            }
        }
    }
}
