package com.bulktracker.app.alarm

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ScheduledAlarmInfo(
    val sectionName: String,
    val triggerTimeFormatted: String
)

data class LastReceiverRunInfo(
    val timeFormatted: String,
    val type: String,
    val sectionName: String,
    val result: String
)

object AlarmDiagnostics {
    private const val PREFS_NAME = "bulktracker_alarm_diagnostics"
    private const val KEY_TIME = "last_run_time"
    private const val KEY_TYPE = "last_run_type"
    private const val KEY_SECTION = "last_run_section"
    private const val KEY_RESULT = "last_run_result"

    private val formatter = DateTimeFormatter.ofPattern("h:mm:ss a")

    private val _lastScheduledAlarms = MutableStateFlow<List<ScheduledAlarmInfo>>(emptyList())
    val lastScheduledAlarms: StateFlow<List<ScheduledAlarmInfo>> = _lastScheduledAlarms.asStateFlow()

    private val _lastReceiverRun = MutableStateFlow<LastReceiverRunInfo?>(null)
    val lastReceiverRun: StateFlow<LastReceiverRunInfo?> = _lastReceiverRun.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val time = prefs.getString(KEY_TIME, null)
        val type = prefs.getString(KEY_TYPE, null)
        val section = prefs.getString(KEY_SECTION, null)
        val result = prefs.getString(KEY_RESULT, null)

        if (time != null && type != null && section != null && result != null) {
            _lastReceiverRun.value = LastReceiverRunInfo(
                timeFormatted = time,
                type = type,
                sectionName = section,
                result = result
            )
        }
    }

    fun recordScheduledAlarms(alarms: List<ScheduledAlarmInfo>) {
        _lastScheduledAlarms.value = alarms
    }

    fun recordReceiverRun(context: Context, type: String, sectionName: String, result: String) {
        val nowFormatted = LocalDateTime.now().format(formatter)
        val runInfo = LastReceiverRunInfo(
            timeFormatted = nowFormatted,
            type = type,
            sectionName = sectionName,
            result = result
        )
        _lastReceiverRun.value = runInfo

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_TIME, nowFormatted)
                .putString(KEY_TYPE, type)
                .putString(KEY_SECTION, sectionName)
                .putString(KEY_RESULT, result)
                .apply()
        } catch (_: Exception) {}
    }
}
