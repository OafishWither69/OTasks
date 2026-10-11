// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.domain

object PreferenceRegistry {
    val keys: Set<String> =
        setOf(
            "onboardingDone",
            "goalType",
            "kcalTarget",
            "proteinTargetG",
            "goalWeightKg",
            "weeklyChangeKg",
            "planStartDay",
            "historyStartDay",
            "weekdayGoalsEnabled",
            "weightUnit",
            "dayStartHour",
            "wakeMold",
            "wakeBeforeFirstClassMin",
            "minGapBigMin",
            "minGapSnackMin",
            "maxFixedShiftMin",
            "latestMealMold",
            "workoutEnabled",
            "workoutDaysMask",
            "workoutTiming",
            "workoutAnchorSectionId",
            "workoutOffsetMin",
            "workoutStartMold",
            "workoutDurationMin",
            "mealAlarmsEnabled",
            "followUpMin",
            "missedNotification",
            "weighInEnabled",
            "weighInWeekday",
            "weighInMold",
            "weighInStyle",
            "defaultTaskReminderStyle",
            "starredNudgeMold",
            "stopAlarmsAfterTarget",
            "dailySummaryEnabled",
            "dailySummaryMold",
            "upcomingNoticeMin",
            "dayOverviewEnabled",
            "dayOverviewMold",
            "alarmDetailsOnLockScreen",
            "ringBeforeUnlock",
            "themeMode",
            "dynamicColor",
            "accentKey",
            "refreshRateMode",
            "refreshHighestInBatterySaver",
            "animations",
            "haptics",
            "appTextScale",
            "density",
            "todayView",
            "showWeightTrend",
            "trendSmoothingPct",
            "showPlanLine",
            "timeFormat",
            "firstDayOfWeek",
            "dateFormat",
            "startDestination",
            "showTasksInCalendar",
            "markerHighPct",
            "markerMidPct",
            "confirmSingleDay",
            "autoCollapseDoneMeals",
            "showCalories",
            "showProtein",
            "showNextMealCard",
            "undoSeconds",
            "trashDays",
            "voiceControlEnabled",
            "voiceAllowChanges",
            "defaultRepeatFrom",
            "showWontDo",
            "quickAddSmartRecognition",
            "quickAddSuggestions",
            "lastExportMillis",
            "autoBackupEnabled",
            "autoBackupTreeUri",
            "lastAutoBackupMillis",
            "batteryHelpDismissed",
            "lastDestination",
        )
    val types: Map<String, String> =
        mapOf(
            "onboardingDone" to "Boolean",
            "goalType" to "String",
            "kcalTarget" to "Int?",
            "proteinTargetG" to "Double?",
            "goalWeightKg" to "Double?",
            "weeklyChangeKg" to "Double?",
            "planStartDay" to "Long?",
            "historyStartDay" to "Long?",
            "weekdayGoalsEnabled" to "Boolean",
            "weightUnit" to "String",
            "dayStartHour" to "Int",
            "wakeMold" to "Int",
            "wakeBeforeFirstClassMin" to "Int?",
            "minGapBigMin" to "Int",
            "minGapSnackMin" to "Int",
            "maxFixedShiftMin" to "Int",
            "latestMealMold" to "Int",
            "workoutEnabled" to "Boolean",
            "workoutDaysMask" to "Int",
            "workoutTiming" to "String",
            "workoutAnchorSectionId" to "Long?",
            "workoutOffsetMin" to "Int",
            "workoutStartMold" to "Int",
            "workoutDurationMin" to "Int",
            "mealAlarmsEnabled" to "Boolean",
            "followUpMin" to "Int",
            "missedNotification" to "Boolean",
            "weighInEnabled" to "Boolean",
            "weighInWeekday" to "Int",
            "weighInMold" to "Int",
            "weighInStyle" to "String",
            "defaultTaskReminderStyle" to "String",
            "starredNudgeMold" to "Int?",
            "stopAlarmsAfterTarget" to "Boolean",
            "dailySummaryEnabled" to "Boolean",
            "dailySummaryMold" to "Int",
            "upcomingNoticeMin" to "Int",
            "dayOverviewEnabled" to "Boolean",
            "dayOverviewMold" to "Int?",
            "alarmDetailsOnLockScreen" to "Boolean",
            "ringBeforeUnlock" to "Boolean",
            "themeMode" to "String",
            "dynamicColor" to "Boolean",
            "accentKey" to "String",
            "refreshRateMode" to "String",
            "refreshHighestInBatterySaver" to "Boolean",
            "animations" to "String",
            "haptics" to "Boolean",
            "appTextScale" to "Int",
            "density" to "String",
            "todayView" to "String",
            "showWeightTrend" to "Boolean",
            "trendSmoothingPct" to "Int",
            "showPlanLine" to "Boolean",
            "timeFormat" to "String",
            "firstDayOfWeek" to "String",
            "dateFormat" to "String",
            "startDestination" to "String",
            "showTasksInCalendar" to "Boolean",
            "markerHighPct" to "Int",
            "markerMidPct" to "Int",
            "confirmSingleDay" to "Boolean",
            "autoCollapseDoneMeals" to "Boolean",
            "showCalories" to "Boolean",
            "showProtein" to "Boolean",
            "showNextMealCard" to "Boolean",
            "undoSeconds" to "Int",
            "trashDays" to "Int",
            "voiceControlEnabled" to "Boolean",
            "voiceAllowChanges" to "Boolean",
            "defaultRepeatFrom" to "String",
            "showWontDo" to "Boolean",
            "quickAddSmartRecognition" to "Boolean",
            "quickAddSuggestions" to "Boolean",
            "lastExportMillis" to "Long?",
            "autoBackupEnabled" to "Boolean",
            "autoBackupTreeUri" to "String?",
            "lastAutoBackupMillis" to "Long?",
            "batteryHelpDismissed" to "Boolean",
            "lastDestination" to "String",
        )
    val deviceLocal: Set<String> = setOf("autoBackupTreeUri", "lastAutoBackupMillis", "batteryHelpDismissed", "lastDestination")
    val protectedKeys: Set<String> = setOf("historyStartDay")

    fun decode(
        rows: Map<String, String>,
        invalid: (String) -> Unit = {},
    ): Settings {
        val defaults = Settings()

        fun <T> read(
            key: String,
            default: T,
            parse: (String) -> T?,
            valid: (T) -> Boolean,
        ): T {
            val raw = rows[key] ?: return default
            val parsed = parse(raw)
            if (parsed != null && valid(parsed)) return parsed
            invalid(key)
            return default
        }

        fun <T : Any> nullable(
            key: String,
            default: T?,
            parse: (String) -> T?,
            valid: (T) -> Boolean,
        ): T? {
            if (rows[key] == "null") return null
            return read(key, default, { parse(it) }, { it != null && valid(it) })
        }
        val result =
            defaults.copy(
                onboardingDone = read("onboardingDone", defaults.onboardingDone, String::toBooleanStrictOrNull) { value -> true },
                goalType = read("goalType", defaults.goalType, { it }) { value -> value in setOf("GAIN", "LOSE", "MAINTAIN") },
                kcalTarget = nullable("kcalTarget", defaults.kcalTarget, String::toIntOrNull) { value -> value in 500..10000 },
                proteinTargetG =
                    nullable(
                        "proteinTargetG",
                        defaults.proteinTargetG,
                        String::toDoubleOrNull,
                    ) { value -> value in 10.0..500.0 },
                goalWeightKg =
                    nullable(
                        "goalWeightKg",
                        defaults.goalWeightKg,
                        String::toDoubleOrNull,
                    ) { value -> value in 20.0..300.0 },
                weeklyChangeKg =
                    nullable(
                        "weeklyChangeKg",
                        defaults.weeklyChangeKg,
                        String::toDoubleOrNull,
                    ) { value -> value in 0.05..1.5 },
                planStartDay = nullable("planStartDay", defaults.planStartDay, String::toLongOrNull) { value -> true },
                historyStartDay = nullable("historyStartDay", defaults.historyStartDay, String::toLongOrNull) { value -> true },
                weekdayGoalsEnabled =
                    read(
                        "weekdayGoalsEnabled",
                        defaults.weekdayGoalsEnabled,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                weightUnit = read("weightUnit", defaults.weightUnit, { it }) { value -> value in setOf("KG", "LB") },
                dayStartHour = read("dayStartHour", defaults.dayStartHour, String::toIntOrNull) { value -> value in 0..6 },
                wakeMold = read("wakeMold", defaults.wakeMold, String::toIntOrNull) { value -> value in 0..1799 },
                wakeBeforeFirstClassMin =
                    nullable(
                        "wakeBeforeFirstClassMin",
                        defaults.wakeBeforeFirstClassMin,
                        String::toIntOrNull,
                    ) { value -> value in 15..240 },
                minGapBigMin = read("minGapBigMin", defaults.minGapBigMin, String::toIntOrNull) { value -> value in 30..360 },
                minGapSnackMin = read("minGapSnackMin", defaults.minGapSnackMin, String::toIntOrNull) { value -> value in 15..240 },
                maxFixedShiftMin = read("maxFixedShiftMin", defaults.maxFixedShiftMin, String::toIntOrNull) { value -> value in 0..240 },
                latestMealMold = read("latestMealMold", defaults.latestMealMold, String::toIntOrNull) { value -> value in 0..1799 },
                workoutEnabled = read("workoutEnabled", defaults.workoutEnabled, String::toBooleanStrictOrNull) { value -> true },
                workoutDaysMask = read("workoutDaysMask", defaults.workoutDaysMask, String::toIntOrNull) { value -> value in 0..127 },
                workoutTiming = read("workoutTiming", defaults.workoutTiming, { it }) { value -> value in setOf("AFTER_MEAL", "FIXED") },
                workoutAnchorSectionId =
                    nullable(
                        "workoutAnchorSectionId",
                        defaults.workoutAnchorSectionId,
                        String::toLongOrNull,
                    ) { value -> true },
                workoutOffsetMin = read("workoutOffsetMin", defaults.workoutOffsetMin, String::toIntOrNull) { value -> value in 0..600 },
                workoutStartMold = read("workoutStartMold", defaults.workoutStartMold, String::toIntOrNull) { value -> value in 0..1799 },
                workoutDurationMin =
                    read("workoutDurationMin", defaults.workoutDurationMin, String::toIntOrNull) { value ->
                        value in
                            15..300
                    },
                mealAlarmsEnabled =
                    read(
                        "mealAlarmsEnabled",
                        defaults.mealAlarmsEnabled,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                followUpMin =
                    read("followUpMin", defaults.followUpMin, String::toIntOrNull) { value ->
                        value in
                            setOf(0, 15, 30, 45, 60, 90)
                    },
                missedNotification =
                    read(
                        "missedNotification",
                        defaults.missedNotification,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                weighInEnabled = read("weighInEnabled", defaults.weighInEnabled, String::toBooleanStrictOrNull) { value -> true },
                weighInWeekday = read("weighInWeekday", defaults.weighInWeekday, String::toIntOrNull) { value -> value in 1..7 },
                weighInMold = read("weighInMold", defaults.weighInMold, String::toIntOrNull) { value -> value in 0..1799 },
                weighInStyle = read("weighInStyle", defaults.weighInStyle, { it }) { value -> value in setOf("NOTIFICATION", "ALARM") },
                defaultTaskReminderStyle =
                    read(
                        "defaultTaskReminderStyle",
                        defaults.defaultTaskReminderStyle,
                        { it },
                    ) { value ->
                        value in
                            setOf(
                                "NOTIFICATION",
                                "ALARM",
                            )
                    },
                starredNudgeMold =
                    nullable(
                        "starredNudgeMold",
                        defaults.starredNudgeMold,
                        String::toIntOrNull,
                    ) { value -> value in 0..1799 },
                stopAlarmsAfterTarget =
                    read(
                        "stopAlarmsAfterTarget",
                        defaults.stopAlarmsAfterTarget,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                dailySummaryEnabled =
                    read(
                        "dailySummaryEnabled",
                        defaults.dailySummaryEnabled,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                dailySummaryMold =
                    read(
                        "dailySummaryMold",
                        defaults.dailySummaryMold,
                        String::toIntOrNull,
                    ) { value -> value in 0..1799 },
                upcomingNoticeMin =
                    read(
                        "upcomingNoticeMin",
                        defaults.upcomingNoticeMin,
                        String::toIntOrNull,
                    ) { value ->
                        value in
                            setOf(
                                0,
                                15,
                                30,
                                60,
                                120,
                            )
                    },
                dayOverviewEnabled =
                    read(
                        "dayOverviewEnabled",
                        defaults.dayOverviewEnabled,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                dayOverviewMold =
                    nullable(
                        "dayOverviewMold",
                        defaults.dayOverviewMold,
                        String::toIntOrNull,
                    ) { value -> value in 0..1799 },
                alarmDetailsOnLockScreen =
                    read(
                        "alarmDetailsOnLockScreen",
                        defaults.alarmDetailsOnLockScreen,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                ringBeforeUnlock = read("ringBeforeUnlock", defaults.ringBeforeUnlock, String::toBooleanStrictOrNull) { value -> true },
                themeMode = read("themeMode", defaults.themeMode, { it }) { value -> value in setOf("SYSTEM", "LIGHT", "DARK") },
                dynamicColor = read("dynamicColor", defaults.dynamicColor, String::toBooleanStrictOrNull) { value -> true },
                accentKey =
                    read(
                        "accentKey",
                        defaults.accentKey,
                        { it },
                    ) { value ->
                        value in
                            setOf(
                                "clay",
                                "ocean",
                                "forest",
                                "plum",
                                "rose",
                                "amber",
                                "graphite",
                            )
                    },
                refreshRateMode =
                    read(
                        "refreshRateMode",
                        defaults.refreshRateMode,
                        { it },
                    ) { value ->
                        value in
                            setOf(
                                "HIGHEST",
                                "STANDARD",
                                "SYSTEM",
                            )
                    },
                refreshHighestInBatterySaver =
                    read(
                        "refreshHighestInBatterySaver",
                        defaults.refreshHighestInBatterySaver,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                animations =
                    read(
                        "animations",
                        defaults.animations,
                        { it },
                    ) { value -> value in setOf("NORMAL", "REDUCED", "OFF") },
                haptics = read("haptics", defaults.haptics, String::toBooleanStrictOrNull) { value -> true },
                appTextScale =
                    read("appTextScale", defaults.appTextScale, String::toIntOrNull) { value ->
                        value in 90..130 &&
                            value % 5 == 0
                    },
                density = read("density", defaults.density, { it }) { value -> value in setOf("COMFORTABLE", "COMPACT") },
                todayView = read("todayView", defaults.todayView, { it }) { value -> value in setOf("CARDS", "TIMELINE") },
                showWeightTrend = read("showWeightTrend", defaults.showWeightTrend, String::toBooleanStrictOrNull) { value -> true },
                trendSmoothingPct =
                    read(
                        "trendSmoothingPct",
                        defaults.trendSmoothingPct,
                        String::toIntOrNull,
                    ) { value -> value in setOf(5, 10, 20) },
                showPlanLine = read("showPlanLine", defaults.showPlanLine, String::toBooleanStrictOrNull) { value -> true },
                timeFormat = read("timeFormat", defaults.timeFormat, { it }) { value -> value in setOf("SYSTEM", "H12", "H24") },
                firstDayOfWeek =
                    read(
                        "firstDayOfWeek",
                        defaults.firstDayOfWeek,
                        { it },
                    ) { value ->
                        value in
                            setOf(
                                "LOCALE",
                                "MONDAY",
                                "SUNDAY",
                                "SATURDAY",
                            )
                    },
                dateFormat = read("dateFormat", defaults.dateFormat, { it }) { value -> value in setOf("LOCALE", "DMY", "MDY", "YMD") },
                startDestination =
                    read(
                        "startDestination",
                        defaults.startDestination,
                        { it },
                    ) { value ->
                        value in
                            setOf(
                                "TODAY",
                                "TASKS",
                                "SCHEDULE",
                                "HISTORY",
                                "LAST",
                            )
                    },
                showTasksInCalendar =
                    read(
                        "showTasksInCalendar",
                        defaults.showTasksInCalendar,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                markerHighPct = read("markerHighPct", defaults.markerHighPct, String::toIntOrNull) { value -> value in 50..100 },
                markerMidPct = read("markerMidPct", defaults.markerMidPct, String::toIntOrNull) { value -> value in 10..95 },
                confirmSingleDay = read("confirmSingleDay", defaults.confirmSingleDay, String::toBooleanStrictOrNull) { value -> true },
                autoCollapseDoneMeals =
                    read(
                        "autoCollapseDoneMeals",
                        defaults.autoCollapseDoneMeals,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                showCalories = read("showCalories", defaults.showCalories, String::toBooleanStrictOrNull) { value -> true },
                showProtein = read("showProtein", defaults.showProtein, String::toBooleanStrictOrNull) { value -> true },
                showNextMealCard = read("showNextMealCard", defaults.showNextMealCard, String::toBooleanStrictOrNull) { value -> true },
                undoSeconds = read("undoSeconds", defaults.undoSeconds, String::toIntOrNull) { value -> value in setOf(4, 10, 20) },
                trashDays = read("trashDays", defaults.trashDays, String::toIntOrNull) { value -> value in setOf(7, 30, 90, 0) },
                voiceControlEnabled =
                    read(
                        "voiceControlEnabled",
                        defaults.voiceControlEnabled,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                voiceAllowChanges =
                    read(
                        "voiceAllowChanges",
                        defaults.voiceAllowChanges,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                defaultRepeatFrom =
                    read(
                        "defaultRepeatFrom",
                        defaults.defaultRepeatFrom,
                        { it },
                    ) { value ->
                        value in
                            setOf(
                                "DUE_DATE",
                                "COMPLETION",
                            )
                    },
                showWontDo = read("showWontDo", defaults.showWontDo, String::toBooleanStrictOrNull) { value -> true },
                quickAddSmartRecognition =
                    read(
                        "quickAddSmartRecognition",
                        defaults.quickAddSmartRecognition,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                quickAddSuggestions =
                    read(
                        "quickAddSuggestions",
                        defaults.quickAddSuggestions,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                lastExportMillis = nullable("lastExportMillis", defaults.lastExportMillis, String::toLongOrNull) { value -> true },
                autoBackupEnabled = read("autoBackupEnabled", defaults.autoBackupEnabled, String::toBooleanStrictOrNull) { value -> true },
                autoBackupTreeUri = nullable("autoBackupTreeUri", defaults.autoBackupTreeUri, { it }) { value -> true },
                lastAutoBackupMillis =
                    nullable(
                        "lastAutoBackupMillis",
                        defaults.lastAutoBackupMillis,
                        String::toLongOrNull,
                    ) { value -> true },
                batteryHelpDismissed =
                    read(
                        "batteryHelpDismissed",
                        defaults.batteryHelpDismissed,
                        String::toBooleanStrictOrNull,
                    ) { value -> true },
                lastDestination =
                    read(
                        "lastDestination",
                        defaults.lastDestination,
                        { it },
                    ) { value ->
                        value in
                            setOf(
                                "TODAY",
                                "TASKS",
                                "SCHEDULE",
                                "HISTORY",
                                "SETTINGS",
                            )
                    },
            )
        if (result.markerMidPct >
            result.markerHighPct - 5
        ) {
            invalid("markerMidPct")
            return result.copy(
                markerMidPct =
                    minOf(
                        defaults.markerMidPct,
                        result.markerHighPct - 5,
                    ),
            )
        }
        return result
    }

    fun encode(settings: Settings): Map<String, String> =
        mapOf(
            "onboardingDone" to settings.onboardingDone.toString(),
            "goalType" to settings.goalType.toString(),
            "kcalTarget" to settings.kcalTarget.toString(),
            "proteinTargetG" to settings.proteinTargetG.toString(),
            "goalWeightKg" to settings.goalWeightKg.toString(),
            "weeklyChangeKg" to settings.weeklyChangeKg.toString(),
            "planStartDay" to settings.planStartDay.toString(),
            "historyStartDay" to settings.historyStartDay.toString(),
            "weekdayGoalsEnabled" to settings.weekdayGoalsEnabled.toString(),
            "weightUnit" to settings.weightUnit.toString(),
            "dayStartHour" to settings.dayStartHour.toString(),
            "wakeMold" to settings.wakeMold.toString(),
            "wakeBeforeFirstClassMin" to settings.wakeBeforeFirstClassMin.toString(),
            "minGapBigMin" to settings.minGapBigMin.toString(),
            "minGapSnackMin" to settings.minGapSnackMin.toString(),
            "maxFixedShiftMin" to settings.maxFixedShiftMin.toString(),
            "latestMealMold" to settings.latestMealMold.toString(),
            "workoutEnabled" to settings.workoutEnabled.toString(),
            "workoutDaysMask" to settings.workoutDaysMask.toString(),
            "workoutTiming" to settings.workoutTiming.toString(),
            "workoutAnchorSectionId" to settings.workoutAnchorSectionId.toString(),
            "workoutOffsetMin" to settings.workoutOffsetMin.toString(),
            "workoutStartMold" to settings.workoutStartMold.toString(),
            "workoutDurationMin" to settings.workoutDurationMin.toString(),
            "mealAlarmsEnabled" to settings.mealAlarmsEnabled.toString(),
            "followUpMin" to settings.followUpMin.toString(),
            "missedNotification" to settings.missedNotification.toString(),
            "weighInEnabled" to settings.weighInEnabled.toString(),
            "weighInWeekday" to settings.weighInWeekday.toString(),
            "weighInMold" to settings.weighInMold.toString(),
            "weighInStyle" to settings.weighInStyle.toString(),
            "defaultTaskReminderStyle" to settings.defaultTaskReminderStyle.toString(),
            "starredNudgeMold" to settings.starredNudgeMold.toString(),
            "stopAlarmsAfterTarget" to settings.stopAlarmsAfterTarget.toString(),
            "dailySummaryEnabled" to settings.dailySummaryEnabled.toString(),
            "dailySummaryMold" to settings.dailySummaryMold.toString(),
            "upcomingNoticeMin" to settings.upcomingNoticeMin.toString(),
            "dayOverviewEnabled" to settings.dayOverviewEnabled.toString(),
            "dayOverviewMold" to settings.dayOverviewMold.toString(),
            "alarmDetailsOnLockScreen" to settings.alarmDetailsOnLockScreen.toString(),
            "ringBeforeUnlock" to settings.ringBeforeUnlock.toString(),
            "themeMode" to settings.themeMode.toString(),
            "dynamicColor" to settings.dynamicColor.toString(),
            "accentKey" to settings.accentKey.toString(),
            "refreshRateMode" to settings.refreshRateMode.toString(),
            "refreshHighestInBatterySaver" to settings.refreshHighestInBatterySaver.toString(),
            "animations" to settings.animations.toString(),
            "haptics" to settings.haptics.toString(),
            "appTextScale" to settings.appTextScale.toString(),
            "density" to settings.density.toString(),
            "todayView" to settings.todayView.toString(),
            "showWeightTrend" to settings.showWeightTrend.toString(),
            "trendSmoothingPct" to settings.trendSmoothingPct.toString(),
            "showPlanLine" to settings.showPlanLine.toString(),
            "timeFormat" to settings.timeFormat.toString(),
            "firstDayOfWeek" to settings.firstDayOfWeek.toString(),
            "dateFormat" to settings.dateFormat.toString(),
            "startDestination" to settings.startDestination.toString(),
            "showTasksInCalendar" to settings.showTasksInCalendar.toString(),
            "markerHighPct" to settings.markerHighPct.toString(),
            "markerMidPct" to settings.markerMidPct.toString(),
            "confirmSingleDay" to settings.confirmSingleDay.toString(),
            "autoCollapseDoneMeals" to settings.autoCollapseDoneMeals.toString(),
            "showCalories" to settings.showCalories.toString(),
            "showProtein" to settings.showProtein.toString(),
            "showNextMealCard" to settings.showNextMealCard.toString(),
            "undoSeconds" to settings.undoSeconds.toString(),
            "trashDays" to settings.trashDays.toString(),
            "voiceControlEnabled" to settings.voiceControlEnabled.toString(),
            "voiceAllowChanges" to settings.voiceAllowChanges.toString(),
            "defaultRepeatFrom" to settings.defaultRepeatFrom.toString(),
            "showWontDo" to settings.showWontDo.toString(),
            "quickAddSmartRecognition" to settings.quickAddSmartRecognition.toString(),
            "quickAddSuggestions" to settings.quickAddSuggestions.toString(),
            "lastExportMillis" to settings.lastExportMillis.toString(),
            "autoBackupEnabled" to settings.autoBackupEnabled.toString(),
            "autoBackupTreeUri" to settings.autoBackupTreeUri.toString(),
            "lastAutoBackupMillis" to settings.lastAutoBackupMillis.toString(),
            "batteryHelpDismissed" to settings.batteryHelpDismissed.toString(),
            "lastDestination" to settings.lastDestination.toString(),
        )

    fun accepts(
        key: String,
        value: String,
    ): Boolean {
        if (key !in keys) return false
        var valid = true
        decode(mapOf(key to value)) { valid = false }
        return valid
    }
}
