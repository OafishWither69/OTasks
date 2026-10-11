// SPDX-License-Identifier: GPL-3.0-or-later
package com.otasks.app.data.prefs
import android.util.Log
import com.otasks.app.data.db.OTasksDatabase
import com.otasks.app.data.db.PreferenceEntity
import com.otasks.app.data.db.toDomain
import com.otasks.app.data.db.toEntity
import com.otasks.app.data.repository.HistoryRepository
import com.otasks.app.data.repository.Reason
import com.otasks.app.data.repository.WriteResult
import com.otasks.app.data.repository.writeTransaction
import com.otasks.domain.AlarmProfile
import com.otasks.domain.PreferenceRegistry
import com.otasks.domain.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class PreferenceStore(
    private val db: OTasksDatabase,
    scope: CoroutineScope,
    private val history: HistoryRepository,
) {
    val settings: StateFlow<Settings> =
        db
            .preferenceDao()
            .observe()
            .map { rows ->
                PreferenceRegistry.decode(
                    rows.associate {
                        it.key to
                            it.value
                    },
                ) { key -> Log.w("Preferences", key) }
            }.flowOn(Dispatchers.Default)
            .stateIn(scope, SharingStarted.Eagerly, Settings())

    suspend fun set(
        key: String,
        value: String,
    ): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (key in PreferenceRegistry.protectedKeys ||
                !PreferenceRegistry.accepts(key, value)
            ) {
                return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            }
            db.writeTransaction {
                history.materialize()
                val current = history.settings()
                val rows = PreferenceRegistry.encode(current) + (key to value)
                var invalid = false
                PreferenceRegistry.decode(rows) { invalid = true }
                if (invalid) return@writeTransaction WriteResult.Invalid(Reason.INVALID_VALUE)
                db.preferenceDao().save(PreferenceEntity(key, value))
                if (key in setOf("goalType", "weeklyChangeKg", "goalWeightKg") &&
                    PreferenceRegistry.encode(current)[key] != value
                ) {
                    db.preferenceDao().save(PreferenceEntity("planStartDay", history.today().toEpochDay().toString()))
                }
                if (key == "onboardingDone" && value == "true" &&
                    current.historyStartDay == null
                ) {
                    db.preferenceDao().save(PreferenceEntity("historyStartDay", history.today().toEpochDay().toString()))
                }
                WriteResult.Success(Unit)
            }
        }

    suspend fun setWeekdayGoal(goal: com.otasks.domain.meals.WeekdayGoal): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (goal.weekday !in 1..7 || goal.kcalTarget?.let { it !in 500..10000 } == true ||
                goal.proteinTargetG?.let { !it.isFinite() || it !in 10.0..500.0 } == true
            ) {
                return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            }
            db.writeTransaction {
                history.materialize()
                db.weekdayGoalDao().save(goal.toEntity())
                WriteResult.Success(Unit)
            }
        }

    suspend fun reset(keys: Set<String>): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (!PreferenceRegistry.keys.containsAll(keys)) return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            db.writeTransaction {
                history.materialize()
                db.preferenceDao().all().filter { it.key in keys && it.key !in PreferenceRegistry.protectedKeys }.forEach {
                    db
                        .preferenceDao()
                        .delete(
                            it,
                        )
                }
                WriteResult.Success(Unit)
            }
        }

    val alarmProfiles: Flow<List<AlarmProfile>> = db.alarmProfileDao().observe().map { rows -> rows.map { it.toDomain() } }

    suspend fun setAlarmProfile(profile: AlarmProfile): WriteResult<Unit> =
        withContext(Dispatchers.IO) {
            if (profile.kind !in setOf("DEFAULT", "MEAL", "TASK", "WEIGH_IN") ||
                profile.volumeMode !in setOf("SYSTEM", "FIXED") || profile.volumePercent !in 10..100 || profile.volumePercent % 10 != 0 ||
                profile.rampSeconds !in setOf(0, 15, 30, 60) || profile.ringSeconds !in setOf(30, 60, 120, 300) ||
                profile.snoozeMin !in setOf(5, 10, 15, 20, 30) || profile.maxSnoozes !in setOf(0, 1, 3, 5, 10) ||
                profile.vibrationPattern !in setOf("STANDARD", "PULSE", "HEARTBEAT", "LONG", "NONE") ||
                listOf(profile.whenNormal, profile.whenVibrate, profile.whenSilent, profile.whenDnd).any {
                    it !in
                        setOf("SOUND_VIBRATE", "SOUND", "VIBRATE", "SCREEN")
                } ||
                profile.whenInCall !in setOf("VIBRATE", "SCREEN") ||
                (profile.soundUri?.let { it != "SILENT" && !it.startsWith("content://") } == true)
            ) {
                return@withContext WriteResult.Invalid(Reason.INVALID_VALUE)
            }
            db.writeTransaction {
                db.alarmProfileDao().save(profile.toEntity())
                WriteResult.Success(Unit)
            }
        }
}
