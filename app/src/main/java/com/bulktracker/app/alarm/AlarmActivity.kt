package com.bulktracker.app.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bulktracker.app.BulkTrackerApp
import com.bulktracker.app.data.db.FoodItemEntity
import com.bulktracker.app.data.db.SectionEntity
import com.bulktracker.app.data.db.SettingsEntity
import com.bulktracker.app.domain.VisibilityResolver
import com.bulktracker.app.ui.components.SectionDot
import com.bulktracker.app.ui.theme.Tokens
import com.bulktracker.app.util.DateUtils
import com.bulktracker.app.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val epochDay = intent.getLongExtra("epochDay", 0L)
        val sectionId = intent.getLongExtra("sectionId", 0L)
        val snoozeCount = intent.getIntExtra("snoozeCount", 0)

        setContent {
            AlarmScreenContent(
                epochDay = epochDay,
                sectionId = sectionId,
                snoozeCount = snoozeCount,
                onMarkAllEaten = {
                    sendAction(AlarmReceiver.ACTION_MARK_ALL, epochDay, sectionId)
                },
                onSnooze = {
                    sendAction(AlarmReceiver.ACTION_SNOOZE, epochDay, sectionId, snoozeCount)
                },
                onSkip = {
                    sendAction(AlarmReceiver.ACTION_SKIP, epochDay, sectionId)
                },
                onOpenApp = {
                    sendAction(AlarmReceiver.ACTION_OPEN, epochDay, sectionId)
                }
            )
        }
    }

    private fun sendAction(action: String, epochDay: Long, sectionId: Long, snoozeCount: Int = 0) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra("epochDay", epochDay)
            putExtra("sectionId", sectionId)
            putExtra("snoozeCount", snoozeCount)
        }
        sendBroadcast(intent)
        finish()
    }
}

@Composable
fun AlarmScreenContent(
    epochDay: Long,
    sectionId: Long,
    snoozeCount: Int,
    onMarkAllEaten: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    onOpenApp: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as? BulkTrackerApp
    val container = app?.container

    var section by remember { mutableStateOf<SectionEntity?>(null) }
    var uncheckedItems by remember { mutableStateOf<List<FoodItemEntity>>(emptyList()) }
    var settings by remember { mutableStateOf(SettingsEntity()) }
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        val nowTimer = kotlinx.coroutines.flow.flow {
            while (true) {
                emit(LocalDateTime.now())
                kotlinx.coroutines.delay(1000)
            }
        }
        nowTimer.collect { currentTime = it }
    }

    LaunchedEffect(epochDay, sectionId) {
        if (container != null) {
            withContext(Dispatchers.IO) {
                val trackerDao = container.database.trackerDao()
                val sec = trackerDao.getSectionById(sectionId)
                val set = trackerDao.getSettings() ?: SettingsEntity()
                val allItems = trackerDao.getItemsForSection(sectionId)
                val overrides = trackerDao.getOverridesForDay(epochDay)
                val itemOverrides = overrides.filter { it.targetType == "ITEM" }.associateBy { it.targetId }
                val visibleNonOptional = allItems.filter { item ->
                    !item.isOptional &&
                    VisibilityResolver.isItemVisible(item, epochDay) &&
                    itemOverrides[item.id]?.action != "REMOVE"
                }
                val logEntries = trackerDao.getLogEntriesForDay(epochDay)
                val checkedIds = logEntries.mapNotNull { it.foodItemId }.toSet()
                val unchecked = visibleNonOptional.filter { !checkedIds.contains(it.id) }

                withContext(Dispatchers.Main) {
                    section = sec
                    settings = set
                    uncheckedItems = unchecked
                }
            }
        }
    }

    val totalKcalLeft = uncheckedItems.sumOf { it.kcal ?: 0 }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(Tokens.SpaceXl)
        ) {
            // Top: current time (64sp Light, tnum)
            Text(
                text = currentTime.format(timeFormatter),
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    fontFeatureSettings = "tnum"
                )
            )

            Spacer(modifier = Modifier.height(Tokens.SpaceLg))

            // SectionDot + section name (28sp SemiBold)
            val sec = section
            val secColor = try {
                if (sec != null) Color(android.graphics.Color.parseColor(sec.colorHex)) else Color.White
            } catch (e: Exception) {
                Color.White
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionDot(color = secColor)
                Spacer(modifier = Modifier.width(Tokens.SpaceMd))
                Text(
                    text = sec?.name ?: "Meal",
                    fontFamily = com.bulktracker.app.ui.theme.SerifFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceLg))

            // List of unchecked items (16sp, portion in 13sp #A3A3A3, kcal right-aligned)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Tokens.SpaceMd)
            ) {
                items(uncheckedItems, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = item.portionLabel,
                                fontSize = 13.sp,
                                color = Color(0xFFA3A3A3)
                            )
                        }
                        Text(
                            text = if (item.kcal != null) "${Format.formatKcal(item.kcal)} kcal" else "—",
                            fontSize = 16.sp,
                            color = Color.White,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Tokens.SpaceSm))
                    Text(
                        text = "$totalKcalLeft kcal left in this meal",
                        fontSize = 15.sp,
                        color = Color(0xFFA3A3A3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Tokens.SpaceLg))

            // Bottom, full-width 56dp buttons stacked with 12dp gaps
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // "Mark all eaten" (white fill, black text)
                Button(
                    onClick = onMarkAllEaten,
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Mark all eaten",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // "Snooze 10 min" (1dp white outline, white text; hidden once the snooze limit is reached)
                if (snoozeCount < settings.maxSnoozes) {
                    OutlinedButton(
                        onClick = onSnooze,
                        shape = RoundedCornerShape(Tokens.RadiusMd),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Snooze ${settings.snoozeMin} min",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // "Skip this meal today" (text button)
                TextButton(
                    onClick = onSkip,
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFA3A3A3)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Skip this meal today",
                        fontSize = 15.sp
                    )
                }

                // "Open app" (text button)
                TextButton(
                    onClick = onOpenApp,
                    shape = RoundedCornerShape(Tokens.RadiusMd),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFA3A3A3)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Open app",
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
