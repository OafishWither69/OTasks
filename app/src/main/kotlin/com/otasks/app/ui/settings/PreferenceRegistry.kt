// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.ui.settings
import com.otasks.app.R
import com.otasks.domain.Settings
import com.otasks.domain.PreferenceRegistry as Codec

object PreferenceRegistry {
    data class Entry(
        val key: String,
        val page: String,
        val title: Int,
        val section: String = "",
        val valueLabel: Int = R.string.preference_value,
    ) {
        val type: String get() = Codec.types.getValue(key)
        val defaultValue: String get() = Codec.encode(Settings()).getValue(key)
        val searchKeywords: String get() = key

        fun accepts(value: String): Boolean = Codec.accepts(key, value)
    }

    val entries: List<Entry> =
        listOf(
            Entry("goalType", "Goals", R.string.pref_goal_type),
            Entry("kcalTarget", "Goals", R.string.pref_kcal_target),
            Entry("proteinTargetG", "Goals", R.string.pref_protein_target_g),
            Entry("goalWeightKg", "Goals", R.string.pref_goal_weight_kg),
            Entry("weeklyChangeKg", "Goals", R.string.pref_weekly_change_kg),
            Entry("weekdayGoalsEnabled", "Goals", R.string.pref_weekday_goals_enabled),
            Entry("weightUnit", "Goals", R.string.pref_weight_unit),
            Entry("dayStartHour", "Day and timing", R.string.pref_day_start_hour),
            Entry("wakeMold", "Day and timing", R.string.pref_wake_mold),
            Entry("wakeBeforeFirstClassMin", "Day and timing", R.string.pref_wake_before_first_class_min),
            Entry("minGapBigMin", "Day and timing", R.string.pref_min_gap_big_min),
            Entry("minGapSnackMin", "Day and timing", R.string.pref_min_gap_snack_min),
            Entry("maxFixedShiftMin", "Day and timing", R.string.pref_max_fixed_shift_min),
            Entry("latestMealMold", "Day and timing", R.string.pref_latest_meal_mold),
            Entry("workoutEnabled", "Workout", R.string.pref_workout_enabled),
            Entry("workoutDaysMask", "Workout", R.string.pref_workout_days_mask),
            Entry("workoutTiming", "Workout", R.string.pref_workout_timing),
            Entry("workoutAnchorSectionId", "Workout", R.string.pref_workout_anchor_section_id),
            Entry("workoutOffsetMin", "Workout", R.string.pref_workout_offset_min),
            Entry("workoutStartMold", "Workout", R.string.pref_workout_start_mold),
            Entry("workoutDurationMin", "Workout", R.string.pref_workout_duration_min),
            Entry("mealAlarmsEnabled", "Notifications & reminders", R.string.pref_meal_alarms_enabled),
            Entry("followUpMin", "Notifications & reminders", R.string.pref_follow_up_min),
            Entry("missedNotification", "Notifications & reminders", R.string.pref_missed_notification),
            Entry("weighInEnabled", "Notifications & reminders", R.string.pref_weigh_in_enabled),
            Entry("weighInWeekday", "Notifications & reminders", R.string.pref_weigh_in_weekday),
            Entry("weighInMold", "Notifications & reminders", R.string.pref_weigh_in_mold),
            Entry("weighInStyle", "Notifications & reminders", R.string.pref_weigh_in_style),
            Entry("defaultTaskReminderStyle", "Notifications & reminders", R.string.pref_default_task_reminder_style),
            Entry("starredNudgeMold", "Notifications & reminders", R.string.pref_starred_nudge_mold),
            Entry("stopAlarmsAfterTarget", "Notifications & reminders", R.string.pref_stop_alarms_after_target),
            Entry("dailySummaryEnabled", "Notifications & reminders", R.string.pref_daily_summary_enabled),
            Entry("dailySummaryMold", "Notifications & reminders", R.string.pref_daily_summary_mold),
            Entry("upcomingNoticeMin", "Notifications & reminders", R.string.pref_upcoming_notice_min),
            Entry("dayOverviewEnabled", "Notifications & reminders", R.string.pref_day_overview_enabled),
            Entry("dayOverviewMold", "Notifications & reminders", R.string.pref_day_overview_mold),
            Entry("alarmDetailsOnLockScreen", "Alarms & sound", R.string.pref_alarm_details_on_lock_screen),
            Entry("ringBeforeUnlock", "Alarms & sound", R.string.pref_ring_before_unlock),
            Entry("themeMode", "Display & motion", R.string.pref_theme_mode),
            Entry("dynamicColor", "Display & motion", R.string.pref_dynamic_color),
            Entry("accentKey", "Display & motion", R.string.pref_accent_key),
            Entry("refreshRateMode", "Display & motion", R.string.pref_refresh_rate_mode),
            Entry("refreshHighestInBatterySaver", "Display & motion", R.string.pref_refresh_highest_in_battery_saver),
            Entry("animations", "Display & motion", R.string.pref_animations),
            Entry("haptics", "Display & motion", R.string.pref_haptics),
            Entry("appTextScale", "Display & motion", R.string.pref_app_text_scale),
            Entry("density", "Display & motion", R.string.pref_density),
            Entry("todayView", "Display & motion", R.string.pref_today_view),
            Entry("showWeightTrend", "Display & motion", R.string.pref_show_weight_trend),
            Entry("trendSmoothingPct", "Display & motion", R.string.pref_trend_smoothing_pct),
            Entry("showPlanLine", "Display & motion", R.string.pref_show_plan_line),
            Entry("timeFormat", "Calendar & time", R.string.pref_time_format),
            Entry("firstDayOfWeek", "Calendar & time", R.string.pref_first_day_of_week),
            Entry("dateFormat", "Calendar & time", R.string.pref_date_format),
            Entry("startDestination", "Calendar & time", R.string.pref_start_destination),
            Entry("showTasksInCalendar", "Calendar & time", R.string.pref_show_tasks_in_calendar),
            Entry("markerHighPct", "Calendar & time", R.string.pref_marker_high_pct),
            Entry("markerMidPct", "Calendar & time", R.string.pref_marker_mid_pct),
            Entry("confirmSingleDay", "Behaviour", R.string.pref_confirm_single_day),
            Entry("autoCollapseDoneMeals", "Behaviour", R.string.pref_auto_collapse_done_meals),
            Entry("showCalories", "Behaviour", R.string.pref_show_calories),
            Entry("showProtein", "Behaviour", R.string.pref_show_protein),
            Entry("showNextMealCard", "Behaviour", R.string.pref_show_next_meal_card),
            Entry("undoSeconds", "Behaviour", R.string.pref_undo_seconds),
            Entry("trashDays", "Behaviour", R.string.pref_trash_days),
            Entry("voiceControlEnabled", "Behaviour", R.string.pref_voice_control_enabled),
            Entry("voiceAllowChanges", "Behaviour", R.string.pref_voice_allow_changes),
            Entry("defaultRepeatFrom", "Behaviour", R.string.pref_default_repeat_from),
            Entry("showWontDo", "Behaviour", R.string.pref_show_wont_do),
            Entry("quickAddSmartRecognition", "Behaviour", R.string.pref_quick_add_smart_recognition),
            Entry("quickAddSuggestions", "Behaviour", R.string.pref_quick_add_suggestions),
            Entry("autoBackupEnabled", "Backup", R.string.pref_auto_backup_enabled),
        )

    fun pageKeys(page: String): Set<String> = entries.filter { it.page == page }.map { it.key }.toSet()

    val preferencePages: Set<String> =
        setOf("Alarms & sound", "Notifications & reminders", "Display & motion", "Calendar & time", "Behaviour")
    val allPreferenceKeys: Set<String> get() = entries.filter { it.page in preferencePages }.map { it.key }.toSet()

    data class ProfileEntry(
        val key: String,
        val title: Int,
        val type: String,
        val defaultValue: String,
        val page: String = "Alarms & sound",
    )

    private val defaults = com.otasks.domain.AlarmProfile("DEFAULT")
    val profileEntries: List<ProfileEntry> =
        listOf(
            ProfileEntry("useDefault", R.string.profile_use_default, "Boolean", defaults.useDefault.toString()),
            ProfileEntry("soundUri", R.string.profile_sound_uri, "String?", defaults.soundUri.toString()),
            ProfileEntry("volumeMode", R.string.profile_volume_mode, "String", defaults.volumeMode.toString()),
            ProfileEntry("volumePercent", R.string.profile_volume_percent, "Int", defaults.volumePercent.toString()),
            ProfileEntry("rampSeconds", R.string.profile_ramp_seconds, "Int", defaults.rampSeconds.toString()),
            ProfileEntry("vibrationPattern", R.string.profile_vibration_pattern, "String", defaults.vibrationPattern.toString()),
            ProfileEntry("ringSeconds", R.string.profile_ring_seconds, "Int", defaults.ringSeconds.toString()),
            ProfileEntry("snoozeMin", R.string.profile_snooze_min, "Int", defaults.snoozeMin.toString()),
            ProfileEntry("maxSnoozes", R.string.profile_max_snoozes, "Int", defaults.maxSnoozes.toString()),
            ProfileEntry("flashScreen", R.string.profile_flash_screen, "Boolean", defaults.flashScreen.toString()),
            ProfileEntry("whenNormal", R.string.profile_when_normal, "String", defaults.whenNormal.toString()),
            ProfileEntry("whenVibrate", R.string.profile_when_vibrate, "String", defaults.whenVibrate.toString()),
            ProfileEntry("whenSilent", R.string.profile_when_silent, "String", defaults.whenSilent.toString()),
            ProfileEntry("whenDnd", R.string.profile_when_dnd, "String", defaults.whenDnd.toString()),
            ProfileEntry("whenInCall", R.string.profile_when_in_call, "String", defaults.whenInCall.toString()),
        )
}
