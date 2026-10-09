# Bulk Tracker — Master Build Spec for Gemini 3.8 Flash

Oct 5, 2026 · @Aayush

## How to use this spec

Build the app in 11 phases: send the master prompt (blocks A–E) once, then paste one phase prompt at a time and test each phase before moving on. A Flash model does best with a fixed contract and small, checkable steps, not one giant "build everything" request.

AI Studio's Android mode generates a Kotlin + Jetpack Compose + Material 3 project with a single activity and ViewModels, runs it in a cloud emulator in your browser, and installs it on your phone over USB. Apps are client-side only, which suits this app: all data stays on the phone.

**Workflow**

1. Go to aistudio.google.com/apps, click **+ New App**, then click **Build an Android app** under the prompt box so the Android chip is added. If a model picker is shown, choose Gemini 3.8 Flash.
2. Export this doc as Markdown, name the file SPEC.md, attach it with the + button in the prompt box, and send the Starter prompt (top of block A) as the message. If the + menu doesn't let you attach a file, paste blocks A, B, C, D and E into ONE message instead, in that order, and click **Build**.
3. If AI Studio asks you to pick a style, pick the plainest light one. Block C overrides it anyway.
4. Wait for the build (about 5 minutes). Test in the emulator against that phase's checklist. If the build fails, click **Fix errors** first; if that loops, use the fix-it prompts in the last section.
5. Only when a phase passes its checklist, paste the next phase prompt. Never paste two phases at once. Phase 1 copies the spec into the project as docs/SPEC.md (check the line count and last line Gemini reports against your file), and every later prompt starts with "Re-read docs/SPEC.md", so Gemini doesn't lose it in a long session.
6. For small visual tweaks (spacing, a colour, a label), use **annotation mode**: highlight the element in the preview and describe the change. It is cheaper and safer than a chat prompt.

**Install on your OnePlus 13**

1. On the phone: Settings > About device > Version, tap **Build number** 7 times. Then Settings > System > Developer options, turn on **USB debugging**.
2. On a laptop, open AI Studio in **Chrome or Edge** (WebUSB does not work in Safari or Firefox). Use a USB cable that carries data, not a charge-only cable.
3. Click **Install** in the preview panel, then **Install via USB**, pick the phone in the browser's device list, and tap **Allow** on the "Allow USB debugging" prompt on the phone. The app launches by itself.
4. If the install is blocked: in Developer options turn on **Disable permission monitoring**, toggle USB debugging off and on again, reconnect, and retry. OnePlus phones are known to need this for ADB installs.
5. To update the app later, repeat step 3 from the **same AI Studio project**. Updates keep your data. When you've finished installing, turn USB debugging (and Disable permission monitoring, if you used it) back off, and use "Revoke USB debugging authorisations" if you ever connected to a computer that isn't yours.

**Protect your data (read this once)**

- Only ever install updates from the same AI Studio project. A build from a different project, a remix, or a ZIP built in Android Studio has a different signature, so Android forces an uninstall first, which deletes everything you logged.
- Before any reinstall, open Settings in the app and tap **Export backup** (built in Phase 7). Import it if anything goes wrong.
- Do not let the agent "reset" or "recreate" the database. The contract in block A forbids it.

## Your decisions

Every rule below is written into blocks A–D as a fixed value, so Gemini never has to choose. Change a row here first if you change your mind, then ask me to update the blocks.

| Topic | Decision |
| --- | --- |
| Meal slots | 6: Morning Shake, Between Classes, Lunch, 4 PM Snack, Dinner, Night Shake (post-gym) |
| Fixed slots | Morning Shake (wake + 30 min), Lunch 1:30 PM, Dinner 8:30 PM |
| Flexible slots | Between Classes (ideal 12:00 PM), 4 PM Snack (ideal 4:00 PM) |
| Wake-up | 90 min before the first class; 9:00 AM on days with no class |
| Gym | Starts 60 min after dinner, lasts 60 min; Night Shake 45 min after gym ends |
| Gym rest days | Usual rest weekdays in Settings, plus a "Rest day" toggle on Today; on rest days Night Shake is 2.5 h after dinner |
| Night Shake latest | 12:30 AM |
| Minimum gap | 2.5 h between big slots (shakes, lunch, dinner); 1.5 h between a snack and its neighbours |
| Clash with class/event | Move to whichever free time is closer; tie goes earlier |
| Eating during class | Never |
| Fixed slot clashes | Moved by up to 2 h, with a warning; if no free time within 2 h, the app asks |
| Snack doesn't fit | App asks: merge into previous, merge into next, pick a time, or skip today |
| Alarm | Full-screen alarm at 50% of MAXIMUM alarm volume (restored afterwards); sound + vibration on normal ring, vibration only on vibrate, silent or Do Not Disturb |
| Alarm timing | From Phase 3 at normal meal times; smart times once the engine exists (Phase 5) |
| Alarm buttons | Mark all eaten, Snooze, Open app, Skip this meal today |
| Snooze | 10 minutes, at most 3 times, then a "Missed" notification |
| Follow-up | One notification 45 min later, only if items are still unchecked |
| Sunday weigh-in | Alarm at wake-up on Sundays, before the morning shake |
| Timetable entry | Form first; drag-to-create on the week grid in its own late phase |
| Event repeats | One time, weekly on chosen days, every N days or weeks, monthly on the same date; ending never, on a date, or after N times |
| Editing repeating events | "Only this date" or "All" |
| Semesters | One at a time, with start date, end date and holiday ranges |
| Edit scope (foods and whole meals) | Just this day, every [weekday] from now, forever from today, a date range; every change needs a confirmation screen |
| Bare-minimum mode | Per-day toggle; shows only priority items, and the day's target becomes the priority items' total |
| Backup | Manual export/import, plus an automatic weekly backup to a folder you pick |
| Engine check | Hidden screen in Settings that runs the worked examples and shows pass/fail |
| Spec file | Exported as SPEC.md and attached to the first message; Gemini copies it into docs/SPEC.md |
| Voice | Skipped (Gemini can't control personal apps yet) |
| Widget | Yes: next meal and today's calories, built last |
| Look | Monochrome with colour only for meal dots, checkboxes, priority stars and status |
| Theme | Follows the system |
| Motion | Minimal, near-instant (100 ms max, no bounce) |
| Home layout | "Next meal" card on top, then meal cards, with a toggle to a timeline view |
| Checked items | Greyed out with strikethrough, staying in place |
| Haptics | On check only |
| Navigation | 5 tabs: Today, Tasks, Schedule, History (calendar + progress), Settings |
| First-run setup | 4 steps: alarms and permissions, semester dates, gym rest days, current weight |
| Wake-up alarm | None; you use your own alarm |
| Tasks tab | Works like TickTick's core (not a copy of its name or icons): lists + smart lists (Inbox, Today, Tomorrow, Next 7 Days, Calendar, Completed), subtasks, notes, priority flags, tags, repeats, smart quick-add |
| Task reminders | Up to 3 per task; each one is a normal notification or a full-screen alarm, chosen per reminder |
| Tasks and meals | Kept separate: tasks never move meal times |
| Task gestures | No swipes: tap the checkbox to complete, tap the row to open, long-press for actions; completed tasks go to a collapsed "Completed" section |
| Design system and accessibility | Named tokens for every size, all colours WCAG AA in both themes, status markers that differ by shape (colour-blind safe), TalkBack labels, 48dp touch targets, input limits and empty states |

## Block A — Prompt 1: contract and Phase 1

This block names every file, library, rule and default so Gemini has nothing to improvise, and it tells Gemini to stop and ask instead of guessing. It ends by asking for Phase 1 only.

**Starter prompt** (send this as the message, with SPEC.md attached)

```text
Build the Android app specified in the attached file SPEC.md.

Step 1. Create the project file docs/SPEC.md with exactly the attached file's content. Copy it; do not retype, shorten or summarise it. Then tell me how many lines docs/SPEC.md has and quote its last line, so I can check nothing was cut off.

Step 2. Read blocks A, B, C, D and E in it completely. Build Phase 1 only, exactly as the "PHASE 1 — FOUNDATION" part of block A describes. Ignore the "Phase prompts" section of the file; I will send each phase myself.

Follow every rule in block A, especially golden rules G1–G8. When you hit a build error or crash, check block E first. If anything you need is not specified, ask me instead of guessing.
```

```text
BLOCK A — CONTRACT. Read blocks A, B, C, D and E completely before writing any code. They are the full specification. Build ONLY Phase 1 now. Later phases arrive as separate messages. In Phase 1 you also save the spec as the project file docs/SPEC.md (documentation only, not compiled): if I attached SPEC.md, copy that file unchanged; otherwise save blocks A, B, C, D and E word for word. Before every later phase, re-read docs/SPEC.md; if it disagrees with your memory of this chat, docs/SPEC.md wins. If I send a corrected block, update docs/SPEC.md first.

GOLDEN RULES FOR YOU, THE AGENT
G1. Do exactly what the spec says. Where the spec gives a number, name, colour, duration, text or file name, use it exactly.
G2. If something you need is NOT specified, do not invent a behaviour. Stop and ask me one short question, then wait.
G3. Implement only the phase I ask for. Do not start later phases, even partially.
G4. Never rename, move or delete files, classes, functions, database tables or columns that already exist, unless the phase text tells you to.
G5. No TODO comments, no placeholder logic, no demo data except the seed data in block B.
G6. Before you finish: the project must compile and the app must launch in the emulator with no crash.
G7. When you finish, reply with exactly three lists: (1) files created or changed, (2) checklist items done, (3) anything not done and why.
G8. Before inventing a fix for any build error, crash or wrong behaviour, check block E. If an entry matches, apply it exactly and name it in your reply.

APP
Name "Bulk Tracker". Package com.bulktracker.app. A personal, offline, single-user app for someone trying to gain weight. It shows a daily checklist of foods in 6 meal slots, times each slot around the user's classes and events, rings an alarm at each slot, and tracks calories and protein against daily targets.
Not allowed anywhere: internet access, INTERNET permission, accounts, Firebase, analytics, ads, any AI or Gemini API call.

TARGET DEVICE
OnePlus 13, OxygenOS on Android 15 or 16 (ColorOS later), 1440x3168 display, gesture navigation. Portrait only (android:screenOrientation="portrait" on MainActivity). Must work with system font size Large.

STACK (use exactly these; add nothing else unless a phase says so)
- Kotlin, Jetpack Compose, Material 3, single activity, single module.
- Navigation Compose. Lifecycle: viewmodel-compose, runtime-compose.
- Room with KSP. The KSP plugin version must match the project's Kotlin version exactly. Never use kapt.
- java.time for all dates and times. Never use java.util.Date or Calendar.
- Phase 7 adds kotlinx-serialization-json and androidx.documentfile. Phase 9 adds Jetpack Glance.
- Forbidden: Hilt, Dagger, Koin, DataStore, SharedPreferences, WorkManager, any chart library, any image-loading library.

FILES (create exactly these packages; one public class or top-level object per file)
- com.bulktracker.app: BulkTrackerApp.kt (Application), AppContainer.kt, MainActivity.kt
- data/db: Entities.kt (all entities from block B), Daos.kt, AppDatabase.kt
- data/repo: TrackerRepository.kt (foods, logs, habits, weights, settings), ScheduleRepository.kt (semester, holidays, classes, events, exceptions), TaskRepository.kt (task lists, tasks, subtasks, reminders; Phase 10)
- domain: VisibilityResolver.kt, Totals.kt, DayPlanner.kt (interface + DayPlan classes), IdealTimePlanner.kt (Phase 3), RecurrenceExpander.kt (Phase 4), MealScheduler.kt and EngineCheck.kt (Phase 5), TaskRules.kt (Phase 10), QuickAddParser.kt (Phase 11). Pure Kotlin: no android.* imports in this package.
- alarm (Phase 3): AlarmScheduler.kt, AlarmReceiver.kt, AlarmRingService.kt, AlarmActivity.kt, BootReceiver.kt, NotificationHelper.kt
- ui/theme: Color.kt, Theme.kt, Type.kt, Motion.kt, Tokens.kt
- ui/components: shared composables, one per file
- ui/today, ui/tasks, ui/schedule, ui/history, ui/settings: one XScreen.kt + XViewModel.kt each; plus ui/tasks/TaskDetailScreen.kt + TaskDetailViewModel.kt (Phase 10); plus ui/settings/EngineCheckScreen.kt (Phase 5)
- util: DateUtils.kt, Format.kt
- widget (Phase 9)

ARCHITECTURE RULES
A1. Manual DI: AppContainer is created in BulkTrackerApp.onCreate and holds AppDatabase, TrackerRepository and ScheduleRepository. ViewModels receive repositories through a ViewModelProvider.Factory defined in each ViewModel's companion object.
A2. DAOs return Flow for reads and are suspend for writes. Repositories are the only classes that call DAOs. All writes run on Dispatchers.IO.
A3. Each ViewModel exposes exactly one StateFlow<XUiState> built with combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial). Screens read it with collectAsStateWithLifecycle().
A4. Navigation: NavHost with 5 tab routes in this order: "today", "tasks", "schedule", "history", "settings", plus the non-tab route "task/{taskId}" (task detail, Phase 10). A NavigationBar at the bottom shows the 5 tabs with these labels and Material icons: Today (Icons.Outlined.CheckCircle), Tasks (Icons.Outlined.TaskAlt), Schedule (Icons.Outlined.DateRange), History (Icons.Outlined.CalendarMonth), Settings (Icons.Outlined.Settings). The NavigationBar is hidden on "task/{taskId}" and "setup". Add material-icons-extended for these icons.
A5. The Today screen can show any date. A shared object SelectedDate (a MutableStateFlow<LocalDate> inside AppContainer) holds which day Today shows. History and Schedule set it before navigating to "today".
A6. Edge-to-edge: call enableEdgeToEdge() in MainActivity.onCreate before setContent. Every screen is a Scaffold and applies its innerPadding. The FAB and the last list item must sit above the gesture bar.

DATABASE SAFETY (never break these)
D1. Database version is 1. Include every entity from block B in Phase 1, even if it is used later, so the schema does not change.
D2. Never call fallbackToDestructiveMigration() or any destructive variant. Never delete or recreate the database file to fix an error.
D3. If a schema change is truly needed later, bump the version, write a Migration object with explicit SQL, register it, and tell me in your reply.
D4. Seeding: TrackerRepository.seedIfEmpty() runs from BulkTrackerApp.onCreate on Dispatchers.IO. It inserts block B's seed rows only when the sections table has 0 rows.

TIME RULES
T1. A "logical day" is a LocalDate stored as epochDay (Long). The logical day starts at settings.dayStartHour (default 4, i.e. 4:00 AM). util/DateUtils.kt: fun logicalToday(now: LocalDateTime, dayStartHour: Int): LocalDate = if (now.hour < dayStartHour) now.toLocalDate().minusDays(1) else now.toLocalDate().
T2. Times of day inside a logical day are stored as Int "minute of logical day" (mold): minutes after 00:00 of that date. Times after midnight continue past 1440: 12:30 AM of the next calendar morning = 1470. Valid range: dayStartHour*60 to 1440 + dayStartHour*60 - 1 (240..1679 with the default).
T3. util/DateUtils.kt also provides: fun moldToLocalDateTime(day: LocalDate, mold: Int): LocalDateTime, fun formatMold(mold: Int): String giving "9:30 AM", "12:30 AM" (12-hour clock, no leading zero, uppercase AM/PM).
T4. Recompute logicalToday whenever MainActivity resumes (Lifecycle.Event.ON_RESUME). If Today was showing the old today, switch it to the new today.
T5. Use ZoneId.systemDefault(). Never hardcode a time zone.
T6. Material 3 DatePicker works in UTC. Set initialSelectedDateMillis = localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), and read the result with Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(). Never use the system zone for DatePicker values. TimePicker values are plain hour and minute; convert them to mold with T2.

NUMBER RULES
N1. kcal is Int?; protein is Double?. null means "I don't know".
N2. Effective kcal of a log entry = (kcalSnapshot * multiplier).roundToInt() (kotlin.math). Effective protein = proteinSnapshot * multiplier.
N3. Day totals sum the effective values that are not null, and count entries whose kcal is null (unknownCount).
N4. Format.kt: formatKcal(Int) gives "3,200" (Locale("en","IN") grouping); formatProtein(Double) gives "96" when whole, else 1 decimal "12.5". Unknown kcal shows as "—".
N5. Number text fields use KeyboardType.Decimal and accept "." or "," as the decimal separator.

SECURITY AND PRIVACY RULES
S1. No INTERNET permission and no network code anywhere (the app works fully offline, and nothing personal can leave the phone).
S2. Only BootReceiver is exported (its actions are system-only broadcasts). AlarmReceiver and AlarmRingService are exported=false. AlarmActivity is not exported.
S3. All PendingIntents use FLAG_IMMUTABLE (plus FLAG_UPDATE_CURRENT where extras change).
S4. MainActivity is exported (it is the launcher), so treat its intent extras as untrusted: accept epochDay only within 3650 days of today and taskId only if that task exists; otherwise ignore them.
S5. All database access goes through Room with bound parameters; never concatenate user text into SQL.
S6. Never write food, weight, task or schedule data to Logcat.
S7. Backup import is validated in full before anything is written (Phase 7 rules).

CONFIRMATION RULE
C1. Every action that removes something, applies to more than one day, or replaces data shows ConfirmDialog (ui/components/ConfirmDialog.kt) first: a title, one sentence that states exactly what will change and from when (for example "Remove Apple from every Tuesday from 6 Oct 2026. Past days stay as they are."), a "Cancel" text button and a confirm button whose label is the verb ("Remove", "Add", "Cancel class", "Replace data"). Nothing changes until confirm is tapped.

PHASE 1 — FOUNDATION (build this now)
1. Gradle: the STACK above (Room+KSP, Navigation, Lifecycle, material-icons-extended). Nothing else.
2. Every entity, DAO and the database from block B, version 1.
3. Seed data from block B via seedIfEmpty().
4. domain/VisibilityResolver.kt and domain/Totals.kt exactly as block B describes.
5. TrackerRepository: seedIfEmpty(), observeDay(day): Flow<DayData>, toggleItem(day, itemId), observeSettings().
6. Theme from block C (Color.kt, Theme.kt, Type.kt, Motion.kt, Tokens.kt), including the DESIGN SYSTEM section's tokens, component states and accessibility rules.
7. Today screen, CARDS view only, from block C, using each section's idealMold as its time for now (the scheduling engine comes in Phase 5). Week strip, summary card, section cards, check and uncheck rows.
8. Bottom navigation with 5 tabs. Tasks, Schedule, History and Settings show only centred text "Coming in a later phase".
9. Save docs/SPEC.md as described at the top of block A, and report its line count and last line.
10. Today also shows the Next meal card (block C item 5b) using idealMold times.

PHASE 1 CHECKLIST
[ ] App launches; Today shows 6 cards in seed order with all 22 seed items and times 9:30 AM, 12:00 PM, 1:30 PM, 4:00 PM, 8:30 PM, 11:15 PM.
[ ] Tapping a row checks it (grey + strikethrough); totals update immediately; tapping again unchecks it.
[ ] Close and reopen the app: checks are kept.
[ ] Tap yesterday in the week strip: empty checklist; tap today: checks are back.
[ ] Light and dark mode both match block C; nothing hides under the status bar or gesture bar.
[ ] docs/SPEC.md exists in the Code tab and contains blocks A–D.
```

## Block B — Data model and seed library

The seed list is your current plan: 6 sections, 22 items, targets of 3,200 kcal and 115 g protein. Every value can be edited inside the app later (Phase 7), so the numbers here only need to be close. Logs store a snapshot of the name, portion, kcal and protein, so editing an item later never rewrites past days.

```text
BLOCK B — DATA MODEL (Room, database version 1). Store enums as String columns with exactly the values listed. Weekday masks: bit 0 = Monday ... bit 6 = Sunday (bit index = DayOfWeek.value - 1); 127 = every day.

=== FOOD SIDE ===

SectionEntity, table "sections" (a meal slot)
- id: Long PK autoGenerate
- name: String
- role: String         // "MORNING_SHAKE" | "SNACK_1" | "LUNCH" | "SNACK_2" | "DINNER" | "NIGHT_SHAKE" | "CUSTOM"
- size: String         // "BIG" | "SNACK"  (decides the minimum gap, block D)
- timing: String       // "WAKE_RELATIVE" | "FIXED" | "FLEXIBLE" | "AFTER_GYM"
- idealMold: Int       // preferred time, minute of logical day (block A T2)
- durationMin: Int     // how long eating takes; used for clash checks
- colorHex: String
- sortOrder: Int
- isMealSection: Boolean   // true = shows "What I ate" (Lunch, Dinner)
- alarmEnabled: Boolean = true
- activeFromDay: Long      // epochDay
- activeUntilDay: Long?    // epochDay inclusive; null = no end
- weekdaysMask: Int = 127

FoodItemEntity, table "food_items" (index on sectionId)
- id: Long PK autoGenerate
- sectionId: Long
- name: String
- portionLabel: String
- kcal: Int?               // null = unknown
- proteinG: Double?        // null = unknown
- isPriority: Boolean = false
- isOptional: Boolean = false   // excluded from completion %
- sortOrder: Int
- activeFromDay: Long
- activeUntilDay: Long?
- weekdaysMask: Int = 127

DayOverrideEntity, table "day_overrides" (unique index on epochDay+targetType+targetId; insert with OnConflictStrategy.REPLACE — so a section holds only one override per day: pinning a skipped meal un-skips it, and the card menu shows only options that make sense for its current state)
- id: Long PK autoGenerate
- epochDay: Long
- targetType: String       // "ITEM" | "SECTION"
- targetId: Long
- action: String           // "ADD" | "REMOVE" | "SKIP" (sections only) | "MERGE_INTO" (sections only) | "PIN_TIME" (sections only)
- mergeIntoSectionId: Long?   // for MERGE_INTO
- pinnedMold: Int?            // for PIN_TIME

LogEntryEntity, table "log_entries" (index on epochDay)
- id: Long PK autoGenerate
- epochDay: Long
- sectionId: Long
- foodItemId: Long?        // null for custom entries
- nameSnapshot: String
- portionSnapshot: String
- kcalSnapshot: Int?
- proteinSnapshot: Double?
- multiplier: Double = 1.0
- isCustom: Boolean
- createdAtMillis: Long
A library item is CHECKED on a day when a log row exists with that epochDay and foodItemId. toggleItem() runs in @Transaction: delete that row if it exists, else insert one copying the item's name, portion, kcal and protein. A double tap can never create two rows.

DailyHabitEntity, table "daily_habits": epochDay Long PK, waterGlasses Int = 0 (1 glass = 250 ml), sunlight Boolean = false, gym Boolean = false, note String = "", gymPlan String = "DEFAULT" ("DEFAULT" | "REST" | "TRAIN"; REST/TRAIN override the usual rest weekdays for that day), bareMinimum Boolean = false

WeightEntity, table "weights": id Long PK autoGenerate, epochDay Long, kg Double, note String = ""

=== SCHEDULE SIDE ===

SemesterEntity, table "semester" (0 or 1 row, id always 1): id Int PK, startDay Long, endDay Long
HolidayEntity, table "holidays": id Long PK autoGenerate, name String, startDay Long, endDay Long (inclusive)
ClassEntity, table "classes": id Long PK autoGenerate, name String, weekday Int (1 = Monday ... 7 = Sunday), startMold Int, endMold Int, location String = ""
ClassCancellationEntity, table "class_cancellations" (unique index classId+epochDay): id Long PK autoGenerate, classId Long, epochDay Long
EventEntity, table "events"
- id: Long PK autoGenerate
- kind: String            // "PERSONAL" | "EXTRA_CLASS" (a one-off or repeating class outside the timetable)
- title: String
- startDay: Long          // first occurrence
- startMold: Int
- endMold: Int
- repeatType: String      // "NONE" | "DAILY" | "WEEKLY" | "MONTHLY"
- repeatInterval: Int = 1 // every N days (DAILY) or every N weeks (WEEKLY)
- weekdaysMask: Int = 0   // WEEKLY only; must be > 0 for WEEKLY
- endType: String = "NEVER"  // "NEVER" | "UNTIL" | "COUNT"
- untilDay: Long?         // UNTIL, inclusive
- count: Int?             // COUNT, total number of occurrences
- note: String = ""
EventExceptionEntity, table "event_exceptions" (unique index eventId+epochDay): id Long PK autoGenerate, eventId Long, epochDay Long, type String ("CANCELLED" | "MOVED"), newStartMold Int?, newEndMold Int?

=== TASKS SIDE (used from Phase 10, but created in Phase 1 with everything else) ===

TaskListEntity, table "task_lists": id Long PK autoGenerate, name String, colorHex String, sortOrder Int
(There is no Inbox row: a task with listId == null is in the Inbox.)

TaskEntity, table "tasks" (indices on listId, dueDay, completedAtMillis)
- id: Long PK autoGenerate
- listId: Long?            // null = Inbox
- title: String
- notes: String = ""
- priority: Int = 0        // 0 none, 1 low, 2 medium, 3 high
- dueDay: Long?            // epochDay; null = no date
- dueMold: Int?            // null = all-day (only meaningful when dueDay != null)
- tags: String = ""        // lowercase, comma-wrapped, e.g. ",gym,fitness,"; "" = no tags; allowed characters: letters, digits and - only (no _ or %, which are SQL LIKE wildcards); query with a bound parameter: tags LIKE '%,' || :tag || ',%' — never build SQL text by hand
- repeatType: String = "NONE"   // "NONE" | "DAILY" | "WEEKLY" | "MONTHLY"
- repeatInterval: Int = 1
- repeatWeekdaysMask: Int = 0   // WEEKLY only
- repeatStartDay: Long?         // the first due day of the series; set when repeat is turned on
- completedAtMillis: Long?      // null = open
- createdAtMillis: Long

SubtaskEntity, table "subtasks" (index on taskId): id Long PK autoGenerate, taskId Long, title String, done Boolean = false, sortOrder Int

TaskReminderEntity, table "task_reminders" (index on taskId; at most 3 rows per task, enforced in TaskRepository)
- id: Long PK autoGenerate
- taskId: Long
- offsetMin: Int     // minutes BEFORE the due time. Timed tasks: 0, 5, 30, 60, 1440. All-day tasks: 0 = on the day at 9:00 AM, 1440 = the day before at 9:00 AM
- style: String      // "NOTIFICATION" | "ALARM"

Deleting a task deletes its subtasks and reminders in the same transaction. Deleting a list moves its tasks to the Inbox (listId = null) in the same transaction.

=== SETTINGS ===
SettingsEntity, table "settings", exactly one row, id = 1:
- kcalTarget 3200, proteinTarget 115, waterTargetGlasses 12, goalWeightKg 54.0, weeklyGainTargetKg 0.4
- dayStartHour 4, themeMode "system" ("system" | "light" | "dark"), defaultTimeline false
- wakeBeforeFirstClassMin 90, noClassWakeMold 540 (9:00 AM), morningShakeAfterWakeMin 30
- minGapBigMin 150, minGapSnackMin 90
- gymAfterDinnerMin 60, gymDurationMin 60, nightShakeAfterGymMin 45, latestNightShakeMold 1470 (12:30 AM)
- gymRestDaysMask 0 (weekday bits of the usual rest days), restDayNightShakeAfterDinnerMin 150
- alarmsEnabled false, alarmVolumePercent 50 (percent of the alarm stream's MAXIMUM volume), alarmRingSeconds 60, snoozeMin 10, maxSnoozes 3, followUpMin 45, weighInAlarmEnabled true
- lastExportMillis Long? = null, autoBackupEnabled false, autoBackupTreeUri String? = null, lastAutoBackupMillis Long? = null
- maxFixedShiftMin 120, savedAlarmVolume Int? = null (set only while an alarm is ringing), onboardingDone false

=== DOMAIN RULES ===

VisibilityResolver (domain/VisibilityResolver.kt, pure Kotlin)
InRange(x, D) = x.activeFromDay <= D AND (x.activeUntilDay == null OR D <= x.activeUntilDay) AND bit (D.dayOfWeek.value - 1) is set in x.weekdaysMask.
Section S on day D:
  1. Override for (D, SECTION, S.id): REMOVE -> hidden. MERGE_INTO -> hidden, and its visible items are shown under section mergeIntoSectionId that day. ADD -> visible. SKIP -> visible, state "skipped". PIN_TIME -> visible, time = pinnedMold (block D).
  2. No override -> visible if InRange(S, D).
Item I on day D (only when its section is visible or merged):
  1. Override for (D, ITEM, I.id): REMOVE -> hidden. ADD -> visible.
  2. No override -> visible if InRange(I, D).
Log entries for day D are always shown, even if their item is now hidden.

Scope operations (TrackerRepository; each one is called only after ConfirmDialog, block A C1). Day D = the day being viewed. W = bit of D's weekday.
REMOVE an item or section:
  - "Just this day": upsert override (D, REMOVE). Also delete D's log entries for that item (for a section: for all its items).
  - "Every <Weekday> from now": newMask = mask with W cleared. If activeFromDay < D: set old.activeUntilDay = D - 1 and insert a copy (same fields, new id) with activeFromDay = D and weekdaysMask = newMask (for a section, copy ONLY its items that are still active on or after D — activeUntilDay == null or activeUntilDay >= D — pointing to the new section id, with the same masks and activeFromDay = max(item.activeFromDay, D); items that already ended are not copied; in the same transaction, move every day_overrides row with epochDay >= D from the old section/item ids to the new ids). Else just update mask = newMask. If newMask == 0, do "Forever" instead.
  - "Forever from today": set activeUntilDay = D - 1.
  - "Date range" (start..end, at most 366 days, start <= end): upsert REMOVE overrides for every day in the range.
ADD a new item or section:
  - "Just this day": activeFromDay = D, activeUntilDay = D, mask 127.
  - "Every <Weekday> from now": activeFromDay = D, activeUntilDay = null, mask = W only.
  - "Forever from today": activeFromDay = D, activeUntilDay = null, mask 127.
  - "Date range": activeFromDay = start, activeUntilDay = end, mask 127.
Bring back something hidden on D: upsert override (D, ADD).
Never hard-delete a section or item that has log entries.

Completion % for day D = checked visible non-optional items / visible non-optional items, rounded to a whole number. Skipped sections count as unchecked. Custom entries do not count. Show 0 when the denominator is 0.

Bare-minimum day (DailyHabitEntity.bareMinimum == true for day D): VisibilityResolver applies this rule itself (it takes the day's DailyHabitEntity as an input), so every consumer — Today, the DayPlanner, alarms, the widget and totals — gets the same result: only priority items are visible (log rows already saved that day are still shown), and a section with no visible priority item is hidden that day, so it gets no time slot and no alarm. Completion % counts only priority items. The day's calorie and protein targets become the sum of kcal and protein of all visible priority items (3,200 / 115 targets are not used that day). The summary card says "Bare-minimum target".

=== SEED DATA === (insert only when sections is empty; activeFromDay = logicalToday at first launch; sortOrder = list order from 0)

Sections (name | role | size | timing | idealMold | durationMin | colorHex | isMealSection):
0 Morning Shake | MORNING_SHAKE | BIG | WAKE_RELATIVE | 570 (9:30 AM) | 15 | #3B82F6 | false
1 Between Classes | SNACK_1 | SNACK | FLEXIBLE | 720 (12:00 PM) | 15 | #0D9488 | false
2 Lunch | LUNCH | BIG | FIXED | 810 (1:30 PM) | 30 | #EA580C | true
3 4 PM Snack | SNACK_2 | SNACK | FLEXIBLE | 960 (4:00 PM) | 20 | #8B5CF6 | false
4 Dinner | DINNER | BIG | FIXED | 1230 (8:30 PM) | 30 | #DC2626 | true
5 Night Shake (post-gym) | NIGHT_SHAKE | BIG | AFTER_GYM | 1395 (11:15 PM) | 15 | #6366F1 | false

Items (section | name | portionLabel | kcal | proteinG | flags):
Morning Shake | Full cream milk | 500 ml | 400 | 18.0 | priority
Morning Shake | ON Serious Mass | 1/2 scoop (~83 g) | 310 | 12.5 | priority
Between Classes | Apple | 1 medium, cut up | 95 | 0.5 |
Between Classes | Cashews | 6 nuts (~10 g) | 57 | 1.8 |
Between Classes | Pistachios | 15 shelled (~10 g) | 57 | 2.0 |
Between Classes | Dried figs, soaked | 2 figs | 60 | 0.7 |
Between Classes | Roasted chana | small handful (30 g) | 110 | 6.0 | optional
Lunch | Oil drizzled on food | 1 tbsp | 120 | 0.0 | priority
Lunch | Boiled egg | 1 egg | 70 | 6.0 |
Lunch | Lemon squeezed on food | 1/2 lime | 5 | 0.0 |
4 PM Snack | Bananas (big) | 2 bananas | 180 | 2.5 |
4 PM Snack | Yogurt (regular) | 100 g | 100 | 3.0 |
4 PM Snack | Yogurt (high-protein) | 100 g | 100 | 7.0 | optional
4 PM Snack | Peanut butter (100%) | 1 heaped tbsp (~16 g) | 100 | 4.5 |
4 PM Snack | Honey | 1 tsp | 20 | 0.0 |
4 PM Snack | Chia seeds | 2 tbsp | 120 | 4.0 |
4 PM Snack | Pumpkin seeds | 2 tbsp (~20 g) | 110 | 6.0 |
4 PM Snack | Creatine | 3 g in water | 0 | 0.0 |
Dinner | Oil drizzled on food | 1 tbsp | 120 | 0.0 | priority
Night Shake (post-gym) | Full cream milk | 500 ml | 400 | 18.0 | priority
Night Shake (post-gym) | ON Gold Standard whey | 1 scoop | 120 | 24.0 | priority
Night Shake (post-gym) | ON Serious Mass | 1 heaped tbsp (~15 g) | 55 | 2.0 |

Settings row: all defaults above. No semester, classes or events are seeded.

MEAL SIZE PRESETS ("What I ate" chips, Phase 2): Light plate 350 kcal / 10 g; Normal plate 450 kcal / 12 g; Big plate 600 kcal / 16 g; I don't know: null / null.
```

## Block C — Screens and UI

The base is black, white and greys, with Canvas's calm card layout. Colour only appears where TickTick uses it: small meal dots, checkboxes, priority stars and status. Every size, colour and duration is a fixed number.

```text
BLOCK C — UI SPEC

STYLE
Monochrome base (black, white, greys) in the spirit of the Canvas Student app: plain list of cards, lots of white space, no gradients, no shadows (cards use a 1dp border instead). Colour is used ONLY for: section dots and checkboxes (section colorHex), the priority star, status colours, the "now" line, and warnings. Everything else is greyscale.
Cards: corner radius 12dp, 1dp border, padding 16dp. Screen side margins 16dp. Gap between cards 12dp.

DESIGN SYSTEM (applies to every screen in every phase)
Why: one set of named values keeps screens consistent and stops one-off sizes creeping in; the accessibility rules keep the app readable in sunlight, at large font sizes and with TalkBack.

TOKENS (ui/theme/Tokens.kt, object Tokens). Every dp/sp/ms number in this block is one of these tokens. Code outside ui/theme must use Tokens.*, never a literal dp or sp.
  Space: xxs 2dp, xs 4dp, sm 8dp, md 12dp, lg 16dp, xl 24dp, xxl 32dp
  Radius: sm 5dp (checkboxes), md 8dp (buttons, timeline blocks, fields), lg 12dp (cards, dialogs, sheets)
  Border: thin 1dp, strong 1.5dp, focus 2dp
  Size: touchMin 48dp, rowMin 56dp, checkbox 22dp, checkboxSm 20dp, dot 10dp, marker 6dp, iconSm 16dp, iconMd 24dp, fab 56dp
  Type (size / line height / weight): titleLg 22sp/28sp SemiBold, titleMd 17sp/24sp SemiBold, body 16sp/22sp Regular, bodySm 15sp/20sp Regular, caption 13sp/18sp Regular, label 12sp/16sp Medium. (Larger one-off sizes on the alarm screen and Next meal card are listed where they are used.)
  Motion: FAST_MS 100

COMPONENT STATES
  Buttons: height 48dp, corner radius md. Primary = ink fill, onInk text. Secondary = 1dp border outline, textPrimary text. Tertiary = text only. Pressed: ripple in textPrimary at 12% alpha. Disabled: content at 38% alpha, primary container becomes border colour.
  Text fields: OutlinedTextField, label always visible, 1dp border; focused 2dp ink border; error 2dp danger border plus supportingText in danger (caption). Character counter shown when within 10 characters of the limit.
  Chips: unselected = 1dp border, textPrimary; selected = secondaryContainer fill with a 16dp check icon.
  Switch: checked = ink track with onInk thumb; unchecked = track colour with textTertiary thumb and border.
  Dialogs: container = card, corner radius lg, title titleMd, body body, buttons right-aligned (Tertiary for cancel, Primary for confirm).
  Bottom sheets: drag handle, side padding xl, title titleMd at the top left, main action as a Primary button bottom right.
  Snackbar: inverseSurface background, inverseOnSurface text, action label in inverseOnSurface SemiBold; 4 seconds; one at a time.

LOADING: never show spinners. Until a screen's first state arrives, show only the background colour (Room answers in well under 100 ms).

EMPTY STATES (centred, caption in textSecondary, xxl top padding):
  Today with no visible meals: "No meals planned for this day." + Tertiary button "Add meal".
  Schedule Week with no classes: "No classes yet. Tap + to add your timetable."  Events: "No events yet."  Holidays: "No holidays added."
  Progress weight card: "Log your first weigh-in".  Weekly table: "Nothing logged yet."  Tasks list: "No tasks".

INPUT LIMITS AND VALIDATION (error text under the field; Save disabled until valid; typing past a max length is blocked, not cut afterwards):
  Meal, item, class and event names 1–40 characters ("Enter a name"); portion 0–30; task title 1–200; task notes 0–2000; list name 1–30; tag 1–24 of letters, digits or -.
  kcal 0–5000 ("Enter 0 to 5,000"); protein 0–300 g; weight 20.0–200.0 kg.
  End time after start ("End must be after start"); weekly repeat needs at least one day ("Pick at least one day"); "After N times" 1–999; "On date" on or after the start date.

ACCESSIBILITY (TalkBack and large text)
  - Whole rows are the touch target: every checklist, task and subtask row uses Modifier.toggleable(value, role = Role.Checkbox) on the row with merged semantics, read as "<name>, <portion>, <kcal> kcal, checked" (tasks: "<title>, <due text>, <priority>, not completed"). Long-press actions are exposed with semantics { onLongClick(label = "More options") }.
  - Card titles and group headers: semantics { heading() }.
  - ProgressRow: contentDescription "Calories 2,640 of 3,200" and progressBarRangeInfo.
  - Colour is never the only signal: StatusMarker shapes (week strip, calendar); priority words in the description; the clock icon on a moved meal has contentDescription "Moved from <ideal time>", the pin icon "Pinned time".
  - Icons next to a text label are decorative (contentDescription = null). Every standalone IconButton describes its action: "Open lists", "Sort tasks", "Show timeline", "Show cards", "More options", "Add".
  - Timeline blocks are focusable with descriptions such as "Econometrics class, 1:00 to 2:30 PM" and "Lunch at 1:30 PM, 2 of 3 eaten". Calendar cells: "Monday 5 October, 92% complete" or "Monday 5 October, nothing logged".
  - Every tappable element is at least 48dp (Modifier.minimumInteractiveComponentSize() where the visual is smaller).
  - Contrast: use only the colour tokens in this block; they meet WCAG AA in both themes. Do not introduce new colours.
  - Font scaling: no fixed heights on anything containing text; use heightIn(min = ...).
  - If the system animator duration scale is 0 ("Remove animations"), skip animateContentSize.
  - Alarm screen: the section name text has semantics { liveRegion = LiveRegionMode.Assertive } so TalkBack announces "<Section> alarm" when it opens.

COLOURS (ui/theme/Color.kt)
Light: background #FFFFFF, surface #F7F7F7, card #FFFFFF, border #E5E5E5, textPrimary #111111, textSecondary #555555, textTertiary #6E6E6E, track #EBEBEB, ink (buttons, selected states) #111111, onInk #FFFFFF.
Dark: background #0D0D0D, surface #171717, card #141414, border #262626, textPrimary #F5F5F5, textSecondary #A3A3A3, textTertiary #8A8A8A, track #262626, ink #F5F5F5, onInk #0D0D0D.
Accent colours (every value below passes WCAG AA against that theme's background, card and surface: 4.5:1 for text, 3:1 for icons, borders and markers):
  Light: success #16A34A, warning #D97706, warningText #B45309, danger #DC2626, priority star #D97706, now line #DC2626.
  Dark: success #4ADE80, warning #FBBF24, warningText #FBBF24, danger #F87171, priority star #FBBF24, now line #F87171.
  Any TEXT drawn in a warning colour uses warningText, never warning.
Timetable blocks: class fill = border colour, class text = textPrimary; event = no fill, 1dp textSecondary border; gym = 1dp dashed textTertiary border.
Build lightColorScheme(...) and darkColorScheme(...) setting EVERY role below explicitly, so no default purple appears anywhere. dynamicColor = false.
  primary = ink, onPrimary = onInk, primaryContainer = surface, onPrimaryContainer = textPrimary, inversePrimary = onInk,
  secondary = ink, onSecondary = onInk, secondaryContainer = border, onSecondaryContainer = textPrimary,
  tertiary = ink, onTertiary = onInk, tertiaryContainer = surface, onTertiaryContainer = textPrimary,
  background = background, onBackground = textPrimary, surface = card, onSurface = textPrimary, surfaceVariant = surface, onSurfaceVariant = textSecondary, surfaceTint = Color.Transparent,
  inverseSurface = textPrimary, inverseOnSurface = background,
  error = danger, onError = #FFFFFF, errorContainer = danger at 15% alpha, onErrorContainer = danger,
  outline = border, outlineVariant = border, scrim = #000000,
  surfaceBright = card, surfaceDim = surface, surfaceContainerLowest = card, surfaceContainerLow = card, surfaceContainer = surface, surfaceContainerHigh = surface, surfaceContainerHighest = border.
NavigationBar: containerColor = card, a 1dp top divider in border colour, tonalElevation 0, selected indicator = secondaryContainer, selected icon/label = textPrimary, unselected = textTertiary.

TYPE (ui/theme/Type.kt) — system font
Screen title 22sp SemiBold. Card title 17sp SemiBold. Body 16sp Regular. Secondary 13sp Regular textSecondary. Small label 12sp Medium textTertiary. Numbers use fontFeatureSettings "tnum".

MOTION (ui/theme/Motion.kt) — minimal and near-instant
const val FAST_MS = 100. All animations use tween(FAST_MS, easing = LinearOutSlowInEasing). No springs, no bounce, no scale effects, no confetti. Card expand/collapse: animateContentSize(tween(FAST_MS)). Checkbox state changes instantly. Navigation between tabs: no transition animation. Bottom sheets: Material 3 default.

HAPTICS
Only when an item becomes checked: LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove). No haptics anywhere else.

COMPONENTS (ui/components)
- MealCheckbox: 22dp rounded square, corner 5dp. Unchecked: 2dp border in section colour, transparent inside. Checked: filled with section colour and a white 14dp check icon.
- SectionDot: 10dp circle in section colour.
- ProgressRow: label (13sp secondary) and value (15sp tnum) on one line, then an 6dp tall LinearProgressIndicator with rounded ends, track = track colour, fill = ink; fill turns success when value >= target.
- ConfirmDialog: as block A C1.
- ScopeSheet: ModalBottomSheet titled "Remove <name>" or "Add <name>". 4 RadioButton rows: "Just this day (<Mon, 6 Oct>)", "Every <Monday> from now", "Forever from today", "For a date range" (shows start and end date pickers when selected). A full-width ink button "Continue" opens ConfirmDialog with the exact sentence. Used for items AND whole meals.

TODAY SCREEN (route "today")
Shows SelectedDate. Two views, cards (default) and timeline, switched by an icon in the top bar. The choice is remembered in settings.defaultTimeline.
1. Top bar: title "Today" / "Yesterday" / "Tomorrow" / "Mon, 6 Oct" (22sp SemiBold). Actions: view toggle icon (Icons.Outlined.ViewTimeline when in cards view, Icons.Outlined.ViewAgenda when in timeline view); when SelectedDate is not today, a text button "Today"; last, an overflow IconButton (Icons.Outlined.MoreVert) with two checkable menu items: "Bare-minimum day" (toggles DailyHabitEntity.bareMinimum for SelectedDate; while on, a 13sp line "Bare-minimum day — priority items only" shows under the title) and "Rest day (no gym)" (checked when the day is a rest day; tapping sets gymPlan to "REST" or "TRAIN" for that day).
2. Week strip: 7 columns Mon–Sun of the week containing SelectedDate. Each column: weekday letter (12sp tertiary) above the date (16sp). Selected: 32dp ink circle behind the number, onInk text. Today (not selected): number in textPrimary bold with a 4dp ink dot above the letter. Under the number a 6dp StatusMarker that differs by SHAPE as well as colour, so it works for colour-blind users: >= 90% = filled circle in success; 60–89% = 1.5dp ring in warning with a 2dp centre dot; 1–59% = empty 1.5dp ring in danger; nothing if 0% or a future day. Swipe horizontally (HorizontalPager) to change week. A 24dp chevron below toggles the month grid (Phase 6).
3. Schedule chip row (Phase 5): one chip "3 classes · 1 event today" (or "No classes today"). Tap opens the Day Schedule sheet: list of today's classes and events with time; each class row has "Cancel for this day" (or "Restore" if cancelled); each event row has "Cancel this occurrence". Each action uses ConfirmDialog.
4. Warning cards (Phase 5): one per warning from block D, card with a 3dp left border in warning colour, 13sp text, e.g. "Lunch moved to 2:00 PM — clashes with Econometrics (1:00–2:00 PM)".
5. Doesn't-fit cards (Phase 5): border danger, title "<Section> doesn't fit today" (15sp SemiBold), reason line (13sp), and a FlowRow of 4 OutlinedButtons: "Merge into <previous section>", "Merge into <next section>", "Pick a time", "Skip today". Each opens ConfirmDialog, then writes a DayOverride (MERGE_INTO / PIN_TIME / SKIP).
5b. Next meal card (only when SelectedDate is today; first card in the list): the first slot in the DayPlan whose time is later than now minus 15 minutes and that is not complete, skipped or merged. 1.5dp ink border, padding 20dp. Row: SectionDot, name (20sp SemiBold), spacer, time (20sp tnum). Line "3 items left · 710 kcal" (15sp secondary). Full-width ink button "Mark all eaten" (checks all its unchecked non-optional items; Snackbar with Undo) and a text button "Show" that scrolls the list to that card. If no slot qualifies: one line "All meals done today" with Icons.Filled.CheckCircle in success colour.
6. Summary card: ProgressRow "Calories" "2,640 / 3,200 kcal" (append " · 1 unknown" when unknownCount > 0); ProgressRow "Protein" "96 / 115 g"; then one 13sp line "Checklist 78%". After 6:00 PM on today only, if priority items are unchecked, add a second line in warning colour "★ 2 priority items left".
7. Section cards (cards view), in time order from block D (Phase 1: idealMold order):
   Header row: SectionDot, name (card title), spacer, time "1:30 PM" (15sp tnum). If moved from its ideal time show Icons.Outlined.Schedule 16dp before the time; if pinned show Icons.Outlined.PushPin 16dp. Under the name: "2 of 3 · 710 kcal" (13sp secondary); if skipped: "Skipped" instead. Right edge: overflow IconButton (Icons.Outlined.MoreVert) with menu items "Add item", "Pin time for this day", "Skip this meal this day", "Remove meal…" (opens ScopeSheet).
   Tapping the header toggles collapse. A card collapses automatically the first time all its non-optional items become checked; it shows Icons.Filled.CheckCircle (success) next to the time.
   Item rows, min height 56dp: MealCheckbox, 12dp gap, column with name (16sp) and portion (13sp secondary), spacer, kcal (14sp tnum; "—" if unknown). Priority items: a 12dp star (priority star colour) after the name. Optional items: label "optional" 12sp tertiary under the portion. Checked rows: name and kcal in textTertiary with TextDecoration.LineThrough. Rows never change order when checked or unchecked.
   Tap a row: toggle it. Long-press a row: Item sheet (Phase 2).
   Meal sections (isMealSection) list that day's custom entries under a small label "What I ate" plus a text button "+ Add what I ate". Every card ends with a text button "+ Add item".
   Merged-in items appear at the end of the target card under a small label "From <section name>".
8. Timeline view (Phase 5): vertical scroll from 6:00 AM (mold 360) to 1:30 AM (mold 1530). Scale 64dp per hour. Left gutter 52dp with hour labels ("9 AM", 12sp tertiary) and 1dp hour lines in border colour. y position of a mold m = (m - 360) * 64 / 60 dp.
   Draw in this order: class blocks, event blocks, gym block, meal pills, now line.
   Class/event/gym blocks: from start to end, corner 8dp, title 13sp SemiBold and time 12sp. When busy blocks overlap each other, split the width equally between them (max 3 columns).
   Meal pills: height max(duration scaled, 40dp), card colour, 1dp border, 4dp left bar in section colour, text "Lunch · 1:30 PM · 2/3" (14sp). Tap opens a bottom sheet with that section's checklist rows (same rows as the card).
   Now line (today only): 2dp danger-coloured line across the width with an 8dp circle at the left end.
   Tap a class block: sheet with "Cancel this class on <date>" and "Edit class (every week)". Tap an event block: "Cancel this occurrence" and "Edit event…".
9. FAB: 56dp, ink background, onInk + icon. Opens Add sheet. Bottom content padding 96dp so the last card scrolls above the FAB and gesture bar.
Unchecking an item or removing an entry shows a Snackbar "Unchecked <name>" with action "Undo" (restores the exact row).

ADD SHEET (Phase 2) — ModalBottomSheet
Section chips (FilterChip per visible section; preselected = the card that opened it, or from the FAB the section whose scheduled time is nearest to now). Name (required). Portion (optional, placeholder "e.g. 1 bowl"). Calories field + Checkbox "I don't know" (disables field, saves null). Protein (g) field + its own "I don't know". For meal sections, a row of 4 AssistChips above the fields: "Light plate · 350", "Normal plate · 450", "Big plate · 600", "I don't know" (they fill both fields). Checkbox "Also add to this meal's checklist" (default off); when on, Save opens ScopeSheet in Add mode for the new item. Buttons: "Cancel" (text) and "Save" (ink, disabled until Name is filled).

ITEM SHEET (Phase 2, long-press a row)
Title = item name. Multiplier FilterChips "×0.5", "×1", "×1.5", "×2" (checks the item if unchecked). Fields "Calories this day" and "Protein this day", each with "I don't know". Buttons: "Remove from checklist…" (opens ScopeSheet) and, for custom entries, "Delete entry" (ConfirmDialog). Changes only touch that day's LogEntryEntity.

TASKS TAB (route "tasks", Phases 10–11)
A task manager that works like TickTick's core, drawn in this app's monochrome style. Do NOT use TickTick's name, logo, icons or exact artwork anywhere.
PRIORITY COLOURS (used for the task checkbox border and the flag icon): light theme: high #DC2626, medium #D97706, low #2563EB; dark theme: high #F87171, medium #FBBF24, low #60A5FA; none = textTertiary. Every task row with priority >= 1 also gets the contentDescription part "High priority" / "Medium priority" / "Low priority", so priority is never conveyed by colour alone.
1. Navigation drawer: ModalNavigationDrawer opened by a menu IconButton (Icons.Outlined.Menu) at the left of the top bar. Drawer content, top to bottom:
   Smart lists, each a NavigationDrawerItem with an icon and a count of open tasks on the right: "Inbox" (Icons.Outlined.Inbox), "Today" (Icons.Outlined.Today), "Tomorrow" (Icons.Outlined.WbSunny), "Next 7 Days" (Icons.Outlined.DateRange), "Calendar" (Icons.Outlined.CalendarMonth, Phase 11, no count), "Completed" (Icons.Outlined.CheckCircle, no count).
   Header "Lists": one item per TaskListEntity (10dp colour dot + name + count), then "+ New list" (dialog: name + 6 colour circles, same colours as "+ Add meal").
   Header "Tags": one item per distinct tag ("#gym" + count). Long-press a list: menu "Rename", "Delete" (ConfirmDialog "Delete list <name>? Its tasks move to the Inbox.").
   The Tasks tab opens on "Today" the first time; afterwards it remembers the last selected list for the session.
2. Top bar: menu icon, title = current list name (22sp SemiBold), actions: sort IconButton (Icons.Outlined.Sort) with menu "Sort by due date" (default) / "Sort by priority" / "Sort by title" (remembered per session).
3. Contents (LazyColumn with stable keys = task id):
   Group headers (13sp SemiBold textSecondary, 16dp top padding) depend on sort. Due date sort: "Overdue" (header text in danger colour), "Today", "Tomorrow", "Next 7 Days", "Later", "No date". Priority sort: "High", "Medium", "Low", "None". Title sort: no headers. Empty groups are not shown.
   Within a group with due date sort: timed tasks by time, then all-day, then by priority (high first), then by createdAtMillis. Priority sort: by due (no date last), then title. Title sort: alphabetical, case-insensitive.
   Smart list contents: Inbox = open tasks with listId == null. Today = open tasks due today plus all overdue open tasks (in "Overdue"). Tomorrow = open tasks due tomorrow. Next 7 Days = open tasks due today..today+6 plus overdue, with one header per day ("Today", "Tomorrow", then "Wed, 8 Oct" ...) regardless of sort. Tag = open tasks with that tag. Completed = completed tasks, newest first, headers per completion day.
   At the bottom of every list except Completed: a row "Completed (n)" with a chevron, collapsed by default; expanding shows that list's completed tasks (greyed + strikethrough, checkbox filled). Tapping their checkbox reopens them.
   Empty state: centred 15sp textSecondary "No tasks".
4. Task row (min height 56dp, no swipe gestures):
   - Checkbox: 22dp rounded square (corner 5dp), 2dp border in the priority colour; checked = filled with that colour and a white check. Tap toggles completion (block D4 for repeating tasks). The completed task leaves the open list right away and appears in the collapsed Completed section; show Snackbar "Completed <title>" with "Undo".
   - Title 16sp, max 2 lines with ellipsis.
   - Meta line (12sp, shown only when it has content), items separated by 8dp: due text ("Today", "Tomorrow 6:00 PM", "Fri", "15 Oct", "15 Oct 2027" when not this year; in danger colour when overdue), Icons.Outlined.Repeat 14dp if repeating, Icons.Outlined.Notifications 14dp if it has reminders, "2/5" if it has subtasks, tags as "#gym", and in smart lists the list name with its colour dot.
   - Tap the row: open "task/{taskId}". Long-press: DropdownMenu "Date…" (opens Date sheet), "Priority" (submenu High/Medium/Low/None with flag icons), "Move to…" (Inbox + lists), "Delete" (ConfirmDialog, then Snackbar with Undo).
5. Quick add: FAB (same style as Today) opens a ModalBottomSheet that sits above the keyboard (imePadding) with:
   - A single-line TextField, placeholder "Add a task", focused automatically (FocusRequester + keyboardController.show()). ImeAction.Done = save.
   - Phase 11: a row of parse chips under the field showing what the smart quick-add understood (date/time, priority flag, #tags, list). Each chip has an × that cancels that parsed part (the words stay in the title).
   - A row of IconButtons: Date (Icons.Outlined.Event, opens Date sheet), Priority (Icons.Outlined.Flag in the chosen priority colour, menu), List (Icons.Outlined.Folder, menu), Tag (Icons.Outlined.Tag, inserts "#" in the field), and a send IconButton (Icons.AutoMirrored.Filled.Send, ink) enabled when the field is not blank.
   - Defaults from the current list: a user list -> that list; Inbox -> no list; Today -> due today; Tomorrow -> due tomorrow; tag list -> that tag added; Next 7 Days, Calendar, Completed -> Inbox, due today. Explicit choices and parsed text override defaults.
   - After saving, the field clears and stays open for the next task; swipe the sheet down to close.
6. Date sheet (ModalBottomSheet, used by quick add, the long-press menu and task detail):
   - Quick chips: "Today", "Tomorrow", "Next Monday", "No date".
   - DatePicker (block A T6 UTC rule).
   - Row "Time": "None" or the time; tap opens TimePicker in a dialog (block E E7); a "Clear time" text button makes it all-day.
   - Row "Reminders": up to 3 lines, each with an offset dropdown (timed: "On time", "5 min before", "30 min before", "1 hour before", "1 day before"; all-day: "On the day, 9:00 AM", "1 day before, 9:00 AM") and a SegmentedButton "Notification" / "Alarm"; a trailing × removes the line; "+ Add reminder" is hidden at 3. Changing between timed and all-day resets offsets that are not valid to the first valid option.
   - Row "Repeat": the same options as the Event form ("Does not repeat", "Every day", "Every week on…", "Every month on day N", "Custom…" with every N days/weeks). Repeat requires a due date; if none is set, choosing a repeat sets the due date to today.
   - Buttons: "Clear" (removes date, time, reminders, repeat) and "Done" (ink).
7. Task detail (route "task/{taskId}", full screen, NavigationBar hidden):
   - Top bar: back arrow; actions: priority flag IconButton (menu), overflow with "Move to…" and "Delete" (ConfirmDialog, then go back and show Snackbar with Undo).
   - Body: checkbox + title TextField (20sp SemiBold, no underline, multi-line); a due chip ("Add date" or the due text with repeat/reminder icons) that opens the Date sheet; the list chip (dot + name, tap = Move to); tags as InputChips with × plus an "+ Tag" AssistChip (dialog with a text field; stored lowercase without "#"); notes TextField (16sp, placeholder "Notes", no underline, grows with content); "Subtasks" header then subtask rows (20dp checkbox inside a 48dp touch target, title, × IconButton to delete) and an "Add subtask" field (Done adds and keeps focus); a 12sp tertiary line "Created <date>".
   - Every edit saves immediately (title and notes debounced 500 ms). Back simply returns. A task whose title is blank when leaving is deleted without asking.
8. Calendar smart list (Phase 11): the same month grid component as History (no completion colours). Each day with open tasks due shows up to 3 small 4dp dots in the priority colours of its first 3 tasks. Tapping a day selects it (1.5dp ink outline) and lists that day's tasks under the grid (same rows). Quick add from this view defaults to the selected day.
9. Tasks never appear on the Today meal screen and never affect the meal schedule.

SCHEDULE TAB (route "schedule", Phase 4)
Top: SingleChoiceSegmentedButtonRow with "Week", "Events", "Semester".
Week segment: if no semester is saved, a card "Set your semester dates so this timetable repeats every week" with button "Set semester" (switches to Semester). Grid: header row with Mon–Sun; hours 7:00 AM (420) to 11:00 PM (1380) at 48dp per hour; left gutter 40dp with hour labels; columns share the remaining width equally. Class blocks in class colours (block C COLOURS) with name (12sp SemiBold, max 2 lines) and start time.
  Create by drag (built in Phase 8 only; before that the grid is view-only and classes are added with the FAB): long-press an empty spot -> start = that time floored to 15 minutes; drag down -> end follows the finger, snapped to 15 minutes, minimum length 30 minutes; a translucent ink block shows the selection; on release open the Class form prefilled with weekday, start and end.
  Tap a class block -> Class form in edit mode. FAB "+ Add class" -> empty Class form.
Class form (ModalBottomSheet): Name (required), Location (optional), Days: 7 FilterChips Mon–Sun (at least one; one ClassEntity is created per selected day), Start and End (Material 3 TimePicker dialogs, 12-hour). Error text "End must be after start" if end <= start. Buttons "Save" and, in edit mode, "Delete" (ConfirmDialog "Delete <name> from every week of the semester?").
Events segment: button row at top "+ Personal event" and "+ Extra class". List of events sorted by next occurrence: title (16sp), repeat summary (13sp secondary), e.g. "Every Tue, Thu · 6:00–7:30 PM · until 20 Dec", "Once · Sat 11 Oct · 5:00–9:00 PM". Tap -> Event form in edit mode.
Event form: Title (required), Type (SegmentedButton Personal / Extra class), Date (DatePicker, first occurrence), Start, End, Repeat (ExposedDropdownMenu): "Does not repeat", "Every day", "Every week on…", "Every month on day <N>", "Custom…". "Every week on…" shows 7 weekday chips. "Custom…" shows "Every [number] [days|weeks]" and, for weeks, weekday chips. Ends (shown when repeating): RadioButtons "Never", "On [date]", "After [N] times". A 13sp preview line repeats the summary. Saving a repeating event opens ConfirmDialog with the summary.
Editing or deleting an event that repeats asks scope first: "Only <date>" or "All" (when opened from the list, only "All" is offered; there is no "This and following"), then ConfirmDialog. Rules in block D.
Semester segment: Start date and End date (DatePickers) and "Save" (ConfirmDialog). Below: "Holidays and breaks" list (name, "20–26 Oct"), button "+ Add holiday" (name, start date, end date), swipe or long-press to delete with ConfirmDialog.

HISTORY SCREEN (route "history", Phase 6): one vertically scrolling screen. Part 1 is the calendar, Part 2 is progress, separated by a 24dp gap.
Part 1 — calendar:
Month header with previous/next IconButtons; horizontal swipe also changes month. 7-column grid, Monday first. Day cell: at least 48dp tall, containing a 40dp circle filled with the status colour at 20% alpha by completion (same thresholds as the week strip) and the same StatusMarker under the number; plain if nothing logged, nothing for future days; today has a 1.5dp ink outline; days inside holiday ranges show a 3dp grey dash under the number. Below: stats card for the month: days logged, average kcal, average protein, current streak (consecutive days ending today with completion >= 90%), best streak. Tap a day -> SelectedDate = that day, navigate to "today".

Part 2 — progress:
Weight card: latest kg (28sp), change since first weigh-in, average of last 7 days vs previous 7 days. Line chart drawn with Compose Canvas only: x = date, y = kg, 1.5dp ink line, 6dp ink dots, dashed success line at goalWeightKg, dashed textTertiary straight line from the first weigh-in rising weeklyGainTargetKg per week. Y range = data and goal padded 1 kg. X labels "6 Oct". 0 points -> text "Log your first weigh-in". "+ Log weight" button -> dialog (kg, one decimal; date, default today). History list newest first, swipe to delete with Undo. Weekly table for the last 8 Monday–Sunday weeks: dates, average kcal, average protein, days logged, average water glasses.

SETTINGS SCREEN (route "settings", Phase 7)
List with section headers:
- Targets: calories, protein, water glasses, goal weight, weekly gain.
- Day & schedule: "New day starts at" (0–6 AM), wake before first class (minutes), wake time on days without class, morning shake after waking (minutes), big-meal gap (minutes), snack gap (minutes), max automatic move for fixed meals (minutes, default 120), gym after dinner, gym length, night shake after gym, latest night shake, usual gym rest days (7 FilterChips Mon–Sun), night shake after dinner on rest days (minutes).
- Meals: "Edit meals and items" -> editor (reorder with up/down IconButtons, edit all fields of a section or item, change a section's time and timing type, add via ScopeSheet, remove via ScopeSheet).
- Alarms: master switch; volume slider 0–100% of maximum alarm volume, step 10 (default 50); ring length 30 / 60 / 120 s; snooze 5 / 10 / 15 min; max snoozes 1 / 3 / 5 (default 3); Sunday weigh-in alarm switch; follow-up Off / 30 / 45 / 60 min; per-meal switches (SectionEntity.alarmEnabled); the Permissions card from block D.
- Appearance: Theme (System / Light / Dark), default Today view (Cards / Timeline).
- Backup: "Export backup", "Import backup", "Last export: <date>"; "Weekly auto-backup" switch with "Choose folder" (shows the folder name) and "Last auto-backup: <date>".
- About: version name, a row "Engine check" that opens EngineCheckScreen (Phase 5), and a row "Run setup again" (Phase 7) that sets onboardingDone = false.

SETUP SCREEN (route "setup", Phase 7)
Shown full-screen instead of the tabs whenever settings.onboardingDone == false. Top: "Step 1 of 4" (12sp tertiary) and a 4-segment progress bar in ink. Bottom: text button "Skip" on the left, ink button "Next" on the right ("Finish" on step 4). Skip and Next both move on; nothing is required.
  Step 1 "Alarms": one line "Bulk Tracker rings when it's time to eat.", the Alarms master switch, and the Permissions card from block D3.
  Step 2 "Semester": start and end DatePickers and a line "Your timetable repeats between these dates. Add classes later in the Schedule tab."
  Step 3 "Gym rest days": 7 FilterChips Mon–Sun writing settings.gymRestDaysMask.
  Step 4 "Current weight": kg field (one decimal); Finish saves a WeightEntity for logicalToday if a value was entered, sets onboardingDone = true and opens Today.

ALARM SCREEN (AlarmActivity, Phase 3)
Always black background #000000 and white text, whatever the theme. Top: current time (64sp Light, tnum). Then SectionDot + section name (28sp SemiBold). Then the list of that meal's UNCHECKED items (16sp, portion in 13sp #A3A3A3, kcal right-aligned) and a line "710 kcal left in this meal". Bottom, full-width 56dp buttons stacked with 12dp gaps: "Mark all eaten" (white fill, black text), "Snooze 10 min" (1dp white outline, white text; uses settings.snoozeMin in the label; hidden once the snooze limit is reached), "Skip this meal today" (text button), "Open app" (text button).
```

## Block D — Scheduling engine and alarms

Meal times are worked out by a fixed step-by-step procedure, with six worked examples Gemini must reproduce exactly. The alarm section names every Android permission, class and API call, because alarms are where generated apps most often break on OnePlus phones.

```text
BLOCK D — SCHEDULING ENGINE AND ALARMS
All times are "mold" (minute of logical day, block A T2). domain/ files are pure Kotlin.

=== D1. BUSY BLOCKS FOR DAY D (ScheduleRepository + domain/RecurrenceExpander.kt) ===
A busy block = (startMold, endMold, label, kind). Two blocks touching end-to-start (end == start) do NOT overlap.
Classes: only if a semester exists AND semester.startDay <= D <= semester.endDay AND D is not inside any holiday range. Then every ClassEntity with weekday == D.dayOfWeek.value, minus those with a ClassCancellationEntity for (classId, D). kind = "CLASS".
Events: every EventEntity where occursOn(event, D) is true (below). kind = "CLASS" for EXTRA_CLASS, "EVENT" for PERSONAL. If an EventException (eventId, D) is MOVED, use newStartMold/newEndMold.

occursOn(e, D):
  if D < e.startDay -> false
  base = when (e.repeatType):
    "NONE"    -> D == e.startDay
    "DAILY"   -> (D - e.startDay) % e.repeatInterval == 0
    "WEEKLY"  -> bit(D.dayOfWeek.value - 1) set in e.weekdaysMask AND ((mondayOf(D) - mondayOf(e.startDay)) / 7) % e.repeatInterval == 0   // mondayOf = epochDay of that week's Monday
    "MONTHLY" -> D.dayOfMonth == startDate.dayOfMonth   // months without that day are skipped
  if !base -> false
  if e.endType == "UNTIL" and D > e.untilDay -> false
  if e.endType == "COUNT": count occurrences from e.startDay up to and including D by iterating day by day (stop after 1100 days); if that number > e.count -> false
  if an EventException (e.id, D) has type "CANCELLED" -> false
  else true

Editing a repeating event (always after ConfirmDialog). D = the occurrence the user opened:
  "Only <date>": edit -> upsert exception (e.id, D, MOVED, new start, new end); only start and end are editable in this mode. Delete -> upsert exception (e.id, D, CANCELLED).
  There is NO "This and following" option. Do not build one.
  "All": edit -> update the event row. Delete -> delete the event and its exceptions.

=== D2. MEAL TIMES FOR DAY D (domain/MealScheduler.kt) ===
interface DayPlanner { fun plan(day: LocalDate, sections: List<SectionEntity>, overrides: List<DayOverrideEntity>, busy: List<BusyBlock>, habit: DailyHabitEntity?, settings: SettingsEntity): DayPlan } in domain/DayPlanner.kt (sections = those visible on the day).
IdealTimePlanner (Phase 3) implements it WITHOUT busy blocks: wake = settings.noClassWakeMold; steps 2 and 3 below; MORNING_SHAKE at wake + morningShakeAfterWakeMin; every other section at idealMold, except the NIGHT_SHAKE on a rest day (step 5 rule) at dinner idealMold + restDayNightShakeAfterDinnerMin; all NORMAL; no warnings; no gym block on rest days, otherwise gym as step 5.
MealScheduler (Phase 5) implements it exactly as the steps below. In Phase 5, AppContainer switches from IdealTimePlanner to MealScheduler; nothing else changes.
DayPlan = wakeMold, gymStart?, gymEnd?, slots: List<SlotPlan(sectionId, mold, status, reason)> sorted by mold, warnings: List<String>, notFitting: List<SlotPlan>.
status = "NORMAL" | "MOVED" | "PINNED" | "SKIPPED" | "MERGED" | "NOT_FITTING".

Helpers:
  free(t, dur) = for every busy block b: t + dur <= b.start OR t >= b.end
  nearestFree(ideal, dur, lo, hi): for k = 0, 5, 10, ... while (ideal - k >= lo OR ideal + k <= hi): first test t = ideal - k (only if t >= lo and t <= hi), then t = ideal + k (only if t >= lo and t <= hi); return the first t where free(t, dur). If none, return null. (So at equal distance the EARLIER time wins.)
  gap(a, b) = if a.size == "BIG" and b.size == "BIG" then settings.minGapBigMin else settings.minGapSnackMin
  firstClass = smallest startMold among busy blocks with kind "CLASS", or null
  dayEnd = settings.latestNightShakeMold

Steps, in this exact order:
 1. wake = if firstClass != null then firstClass - settings.wakeBeforeFirstClassMin else settings.noClassWakeMold.
 2. Sections with override SKIP -> status SKIPPED, mold = their ideal, not placed. Sections with override MERGE_INTO -> status MERGED, not placed.
 3. Sections with override PIN_TIME -> status PINNED, mold = pinnedMold, placed. If not free(mold, dur): warning "<Name> is pinned at <time> but overlaps <label>".
 4. Fixed group, sorted by their ideal: the MORNING_SHAKE section (ideal = wake + settings.morningShakeAfterWakeMin) and every section with timing "FIXED" (ideal = idealMold).
    For each: if free(ideal, dur) -> NORMAL at ideal. Else t = nearestFree(ideal, dur, lo = max(wake, ideal - settings.maxFixedShiftMin), hi = min(dayEnd - dur, ideal + settings.maxFixedShiftMin)); if t != null -> MOVED at t, warning "<Name> moved to <time> — clashes with <label> (<start>–<end>)" naming the first busy block that overlapped the ideal; if t == null -> NOT_FITTING, reason "No free time within 2 h of <ideal time>" (use maxFixedShiftMin formatted as "2 h" / "1 h 30 min").
 5. Gym: isRest = (habit?.gymPlan == "REST") OR (habit?.gymPlan != "TRAIN" AND bit (D.dayOfWeek.value - 1) is set in settings.gymRestDaysMask). If isRest: no gym block. Otherwise, only if the DINNER section was placed at time d: gymStart = d + settings.gymAfterDinnerMin, gymEnd = gymStart + settings.gymDurationMin. If not free(gymStart, gymDurationMin): warning "Gym (<start>–<end>) overlaps <label>". The gym is never moved separately.
 6. NIGHT_SHAKE (timing AFTER_GYM): ideal = gymEnd + settings.nightShakeAfterGymMin if a gym block exists; else if isRest and DINNER was placed at d: ideal = d + settings.restDayNightShakeAfterDinnerMin; else idealMold. If ideal > dayEnd: ideal = dayEnd and warning "Night Shake capped at <dayEnd time>". Search forward only: t = ideal, ideal + 5, ... up to dayEnd; take the first free(t, dur). Found -> NORMAL if t == ideal else MOVED. None -> NOT_FITTING, reason "Busy until after <dayEnd time>".
 7. Big-gap check: sort all placed BIG slots by time; for each neighbouring pair with b.mold - a.mold < settings.minGapBigMin: warning "Only <Xh Ym> between <A> and <B> (target <2h 30m>)". Nothing is moved.
 8. Flexible group (timing "FLEXIBLE"), sorted by idealMold. For each section S:
      prev = the placed slot with the largest mold <= S.idealMold (or null); next = the placed slot with the smallest mold > S.idealMold (or null).
      lo = if prev != null then prev.mold + gap(prev, S) else wake
      hi = if next != null then next.mold - gap(S, next) else dayEnd - S.durationMin
      if lo > hi -> NOT_FITTING, reason "Not enough time between <prev name> (<time>) and <next name> (<time>)".
      else t = nearestFree(S.idealMold.coerceIn(lo, hi), S.durationMin, lo, hi); null -> NOT_FITTING, reason "Busy from <lo time> to <hi time>"; else placed at t, NORMAL if t == S.idealMold else MOVED (no warning card for snacks).
 9. NOT_FITTING sections are shown at their ideal time and produce a Doesn't-fit card (block C). Its "Merge into" buttons use prev and next from step 8 (for fixed sections: the placed slots just before and after the ideal time).
10. The Today screen's DayPlan is recomputed whenever any input Flow changes.

WORKED EXAMPLES (defaults: wake 90 min before first class, gaps 150/90, gym +60 for 60, night +45, latest 12:30 AM). Write unit tests in app/src/test for all six and make the engine produce exactly these results. ALSO build domain/EngineCheck.kt: it constructs the inputs of Ex1–Ex6 in code, never from the database: dates Ex1 = Mon 2026-10-05, Ex2 = Sat 2026-10-10, Ex3 = Wed 2026-10-07, Ex4 = Tue 2026-10-06, Ex5 = Fri 2026-10-09, Ex6 = Sat 2026-10-10; a default SettingsEntity; the 6 seed sections from block B built in code with activeFromDay = 0 and weekdaysMask 127; no overrides; habit = null except Ex6 (gymPlan "REST"); busy blocks passed directly as BusyBlock lists. The unit tests use the same inputs. EngineCheck then runs MealScheduler and compares wake, every slot time and status, the gym block and every warning text with the expected values. EngineCheckScreen (Settings > About > Engine check) lists Ex1–Ex6, each with PASS in success colour or FAIL in danger colour; a FAIL row expands to show expected vs actual for each mismatch.
Ex1 Monday, classes 10:30–12:00, 12:15–1:15 PM, 2:00–5:00 PM:
  wake 9:00 AM. Morning Shake 9:30 AM. Lunch 1:30 PM (touches the 2:00 PM class, allowed). Between Classes: lo 11:00 AM, hi 12:00 PM, 12:00–12:15 is free -> 12:00 PM. 4 PM Snack: 4:00 is in class; nearest free in [3:00 PM, 7:00 PM] is 5:00 PM -> MOVED 5:00 PM. Dinner 8:30 PM. Gym 9:30–10:30 PM. Night Shake 11:15 PM. No warnings.
Ex2 Saturday, no classes, no events:
  wake 9:00 AM; 9:30 AM, 12:00 PM, 1:30 PM, 4:00 PM, 8:30 PM; gym 9:30–10:30 PM; 11:15 PM. No warnings.
Ex3 No classes, personal event "Club meeting" 8:00–9:30 PM:
  Dinner ideal 8:30 PM clashes. 7:30 PM and 9:30 PM are both 60 min away -> earlier wins -> MOVED 7:30 PM, warning "Dinner moved to 7:30 PM — clashes with Club meeting (8:00–9:30 PM)". Gym 8:30–9:30 PM, warning "Gym (8:30–9:30 PM) overlaps Club meeting". Night Shake 10:15 PM. 4 PM Snack 4:00 PM.
Ex4 Tuesday, classes 10:30–12:00 and 1:00–2:30 PM:
  wake 9:00 AM, Morning Shake 9:30 AM. Lunch ideal 1:30 PM clashes; 12:30 PM and 2:30 PM are both 60 min away -> MOVED 12:30 PM with warning. Between Classes: lo 11:00 AM, hi 11:00 AM, 11:00 is in class -> NOT_FITTING, card offers "Merge into Morning Shake" and "Merge into Lunch". 4 PM Snack 4:00 PM. Dinner 8:30 PM, Night Shake 11:15 PM.
Ex5 No classes, personal event "Movie" 10:00 PM–12:45 AM:
  Dinner 8:30 PM. Gym 9:30–10:30 PM with warning "Gym (9:30–10:30 PM) overlaps Movie". Night Shake: ideal 11:15 PM is busy and nothing is free before 12:30 AM -> NOT_FITTING, reason "Busy until after 12:30 AM".
Ex6 Saturday with gymPlan "REST", no classes, no events:
  Same as Ex2 except: no gym block, and Night Shake at 11:00 PM (dinner 8:30 PM + 150 min). No warnings.

=== D3. ALARMS (package alarm/, Phase 3; they use whichever DayPlanner AppContainer provides) ===
Manifest (exactly these):
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
  <uses-permission android:name="android.permission.USE_EXACT_ALARM"/>
  <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" android:maxSdkVersion="32"/>
  <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT"/>
  <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
  <uses-permission android:name="android.permission.VIBRATE"/>
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
  <uses-permission android:name="android.permission.WAKE_LOCK"/>
  AlarmReceiver (exported=false). BootReceiver (exported=true) for BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED, MY_PACKAGE_REPLACED. BootReceiver ONLY calls rescheduleAll(); it never starts AlarmRingService (Android 15+ blocks media-playback services started from boot). A meal or task alarm whose time passed while the phone was off is not rung late; rescheduleAll() simply skips past times. AlarmRingService with android:foregroundServiceType="mediaPlayback". AlarmActivity with showWhenLocked="true", turnScreenOn="true", excludeFromRecents="true", launchMode="singleInstance", theme with black background.
Notification channels (NotificationHelper, created at app start):
  "meal_alarms": name "Meal alarms", IMPORTANCE_HIGH, setSound(null, null), enableVibration(false) (the service plays sound and vibration itself).
  "meal_reminders": name "Meal reminders", IMPORTANCE_DEFAULT, default sound off, vibration on.

AlarmScheduler.rescheduleAll(): runs on app start, from BootReceiver (inside goAsync), and from a coroutine in AppContainer that collects all repository Flows with debounce(500 ms).
  1. If settings.alarmsEnabled is false: cancel all MAIN codes and code 9000, and skip steps 2–6 (meal and weigh-in alarms). Task reminders (block D4) and the maintenance alarm (step 7) are still scheduled; they do not depend on this switch.
  2. For logical today and logical tomorrow, build the DayPlan. For every slot with status NORMAL, MOVED or PINNED, whose section has alarmEnabled == true, and whose trigger time (moldToLocalDateTime) is in the future: schedule.
  3. Schedule with alarmManager.setAlarmClock(AlarmClockInfo(triggerMillis, PendingIntent to MainActivity), PendingIntent.getBroadcast(AlarmReceiver, requestCode, extras type="MAIN", epochDay, sectionId, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)).
  4. Request codes, with k = (epochDay % 4).toInt() and s = (sectionId % 1000).toInt(): MAIN = 100000 + k * 1000 + s; FOLLOW_UP = 200000 + k * 1000 + s; SNOOZE = 300000 + k * 1000 + s; WEIGH_IN = 9000; WEIGH_IN snooze = 9001. Before scheduling, rescheduleAll() cancels ONLY the MAIN codes of every section for today and tomorrow, and code 9000. It NEVER cancels FOLLOW_UP, SNOOZE or 9001 alarms; those check the current state themselves when they fire.
  5. On Android 12+ if alarmManager.canScheduleExactAlarms() is false, do not schedule; the Permissions card shows the problem.
  6. Weigh-in alarm: if settings.weighInAlarmEnabled, take the next Sunday (today, if today is Sunday and its wake time is still ahead), build its DayPlan and schedule type "WEIGH_IN" at DayPlan.wakeMold with requestCode 9000. It rings exactly like MAIN (same service, volume and quiet rules), but the alarm screen and notification show "Weigh-in" and "Weigh yourself before the morning shake", with buttons "Log weight" (opens the app on History with the log-weight dialog open), "Snooze" and "Dismiss". It never schedules a follow-up.
  7. Maintenance alarm (always scheduled, whatever the switches say): setExactAndAllowWhileIdle at the next occurrence of dayStartHour:05 (4:05 AM by default), requestCode 9100, extras type "MAINTENANCE". When it fires, AlarmReceiver calls rescheduleAll(), updates the widget (Phase 9) and runs the weekly auto-backup check (Phase 7). This keeps alarms rolling forward even if the app is not opened for days, because the plan only ever schedules today and tomorrow.

AlarmReceiver.onReceive (use goAsync and a coroutine on Dispatchers.IO):
  FIRST, before any other work: acquire a PARTIAL_WAKE_LOCK tagged "bulktracker:alarm" with a 60-second timeout and store it in a companion object; AlarmRingService releases it right after startForeground(). Without this the phone can fall asleep between the receiver and the service starting.
  LAST, after handling any type: call AlarmScheduler.rescheduleAll(), then release the wake lock if no service took it, then pendingResult.finish().
  type MAIN: load the section's visible non-optional items for epochDay. If all are checked, or the section is SKIPPED or MERGED that day: do nothing. Otherwise ContextCompat.startForegroundService(AlarmRingService with the same extras), and schedule FOLLOW_UP with setExactAndAllowWhileIdle at now + settings.followUpMin (skip if followUpMin == 0).
  type FOLLOW_UP: if unchecked non-optional items remain and the section is not skipped: post a "meal_reminders" notification "<Name> — <n> items left (<kcal> kcal)" with the 4 action buttons. No sound loop, no full screen.
  actions MARK_ALL, SNOOZE, SKIP, OPEN: see below.

AlarmRingService:
  - Within onStartCommand, call startForeground(1001, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) immediately. Notification: channel "meal_alarms", category CATEGORY_ALARM, priority MAX, ongoing, title "<Section name>", text "<n> items · <kcal> kcal", setFullScreenIntent(PendingIntent to AlarmActivity with the extras, true), and 4 actions "Mark all eaten", "Snooze <snoozeMin> min", "Skip today", "Open app".
  - Only one alarm rings at a time. If AlarmRingService is already ringing when another MAIN, WEIGH_IN or TASK alarm arrives, do not start a second ring: post the new one as a normal notification ("meal_reminders" or "task_reminders") with its action buttons.
  - setDeleteIntent on the ringing notification: if the user swipes it away (Android 14+ allows this even for ongoing notifications), stop ringing exactly like a timeout, but do not post the "Missed" notification.
  - quiet = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL OR notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL OR audioManager.mode is MODE_IN_CALL or MODE_IN_COMMUNICATION (never play sound over a phone call).
  - When playing sound, request audio focus with AudioFocusRequest(AUDIOFOCUS_GAIN_TRANSIENT, USAGE_ALARM) so music and videos pause; abandon focus when ringing stops.
  - If NOT quiet: MediaPlayer with RingtoneManager.getDefaultUri(TYPE_ALARM) (fallback TYPE_NOTIFICATION), AudioAttributes(USAGE_ALARM, CONTENT_TYPE_SONIFICATION), isLooping = true. Volume is a share of the alarm stream's MAXIMUM: save original = audioManager.getStreamVolume(AudioManager.STREAM_ALARM); call audioManager.setStreamVolume(AudioManager.STREAM_ALARM, max(1, (getStreamMaxVolume(STREAM_ALARM) * settings.alarmVolumePercent / 100.0).roundToInt()), 0); mediaPlayer.setVolume(1f, 1f); start. Restore the saved original alarm volume every time ringing stops (any action, timeout, or onDestroy), in a finally block. Crash safety: before changing the volume, write the original value to SettingsEntity.savedAlarmVolume (only if it is currently null); after restoring, set it back to null. In BulkTrackerApp.onCreate, if savedAlarmVolume != null and no alarm is ringing, restore STREAM_ALARM to that value and set it to null. This guarantees the phone's alarm volume is never left at the app's level if the OS kills the app mid-ring. Also vibrate.
  - Vibration (both cases): pattern longArrayOf(0, 600, 400) repeating at index 0, using VibratorManager on API 31+, Vibrator before.
  - If quiet: vibration only, never sound.
  - After settings.alarmRingSeconds: stop sound and vibration, stopForeground(STOP_FOREGROUND_REMOVE), post a "meal_reminders" notification "Missed: <Name> — <n> items left" with the 4 actions, stopSelf().
  - Any action or AlarmActivity button stops sound and vibration immediately and stops the service.
Actions (handled by AlarmReceiver, then cancel that section's notification):
  MARK_ALL: in one transaction, insert log rows (multiplier 1.0) for every visible unchecked non-optional item of that section and day; cancel its FOLLOW_UP and SNOOZE alarms.
  SNOOZE: snoozeCount = (extra "snoozeCount", default 0) + 1. Schedule it again with setExactAndAllowWhileIdle at now + settings.snoozeMin minutes using the SNOOZE request code (9001 for WEIGH_IN), with extras type "MAIN" (or "WEIGH_IN"), epochDay, sectionId and snoozeCount. When it fires, AlarmReceiver applies the normal MAIN checks (all checked or skipped -> do nothing). When an alarm rings with snoozeCount == settings.maxSnoozes, the Snooze button is hidden on the alarm screen and left out of the notification.
  SKIP: upsert DayOverride (epochDay, SECTION, sectionId, SKIP); cancel its FOLLOW_UP and SNOOZE alarms.
  OPEN: start MainActivity with extra epochDay; MainActivity sets SelectedDate to it and opens "today".
AlarmActivity: UI from block C ALARM SCREEN. Its buttons send the same 4 actions, then finish().

Permissions card (Settings > Alarms, and at the top of Today when alarmsEnabled is true and any row is red). One row each, with a green check or a red cross and a "Fix" button:
  - Notifications: granted? (API 33+). Fix = request POST_NOTIFICATIONS at runtime.
  - Exact alarms: alarmManager.canScheduleExactAlarms() (API 31+). Fix = open Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM for the package.
  - Full-screen alarms: notificationManager.canUseFullScreenIntent() (API 34+). Fix = open Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT with Uri "package:<packageName>".
  - Battery: always shown as a grey info row "OnePlus can stop alarms. Set battery usage to Unrestricted." Fix = open Settings.ACTION_APPLICATION_DETAILS_SETTINGS with Uri "package:<packageName>".
Turning the Alarms master switch on asks for POST_NOTIFICATIONS first; if denied, the switch turns back off and a Snackbar says "Notifications are needed for alarms".

=== D4. TASKS (domain/TaskRules.kt Phase 10, reminders Phase 11, domain/QuickAddParser.kt Phase 11) ===
Times: a task's dueMold is minutes after midnight of the calendar date dueDay (0..1439). It does NOT use the extended logical-day range.
"today" for tasks = logicalToday (block A T1). Overdue = open AND (dueDay < today OR (dueMold != null AND dueDay == today AND due date-time < now)).

Repeating tasks — TaskRules.nextDueDay(task): build an EventEntity-like value with startDay = task.repeatStartDay ?: task.dueDay, repeatType/repeatInterval/weekdaysMask from the task, endType "NEVER"; return the smallest day D with D > task.dueDay AND D >= today where RecurrenceExpander.occursOn is true (so completing an overdue repeating task jumps to the next occurrence from today instead of leaving a chain of overdue copies; signature nextDueDay(task, today)) (search at most 800 days; if none, return null and treat as non-repeating).
Completing a repeating task (one Room transaction): set completedAtMillis = now on the task; insert a copy with dueDay = nextDueDay, completedAtMillis = null, createdAtMillis = now, every other field the same; copy its subtasks with done = false; copy its reminders. Undo (Snackbar): delete the copy with its subtasks and reminders, and set the original's completedAtMillis back to null.
Completing a non-repeating task: set completedAtMillis = now. Reopening: set it to null.

Task reminders (Phase 11):
  Trigger time: timed task -> LocalDateTime(dueDay, dueMold) minus offsetMin minutes. All-day task -> dueDay at 9:00 AM minus offsetMin minutes (offsetMin is 0 or 1440).
  Request codes: TASK = 400000 + (reminderId % 100000).toInt(); TASK snooze = 500000 + (reminderId % 100000).toInt().
  rescheduleAll() also: cancels and re-creates the TASK code of every reminder whose task is open and whose trigger is in the future and within the next 14 days, using setAlarmClock for style "ALARM" and setExactAndAllowWhileIdle for style "NOTIFICATION". It never cancels TASK snooze codes. Task reminders do NOT depend on settings.alarmsEnabled (that switch is for meal alarms only).
  Completing or deleting a task cancels its TASK and TASK snooze codes immediately.
  AlarmReceiver types "TASK" (extras reminderId, taskId, snoozeCount): if the task is completed or deleted, do nothing. Style "ALARM" -> start AlarmRingService in task mode (same sound, volume, vibration and quiet rules as D3); the alarm screen shows "Task" instead of the section dot, the task title (28sp), the due text, and buttons "Complete", "Snooze <snoozeMin> min" (same maxSnoozes rule), "Open". Style "NOTIFICATION" -> post a notification on channel "task_reminders" (name "Task reminders", IMPORTANCE_HIGH, default sound and vibration) with visibility VISIBILITY_PRIVATE and a public version whose title is just "Task reminder" (so task titles are hidden on the lock screen), title = task title, text = due text, actions "Complete" and "Snooze <snoozeMin> min"; tapping it opens "task/{taskId}".
  Actions: COMPLETE -> complete the task (rules above), cancel its alarm/notification. SNOOZE -> setExactAndAllowWhileIdle at now + snoozeMin with the TASK snooze code. OPEN -> open MainActivity on "task/{taskId}".
  If any task has a reminder and POST_NOTIFICATIONS is not granted, the Tasks tab shows a one-line card "Reminders need notification permission" with a "Fix" button (same request as the Permissions card).

SMART QUICK-ADD GRAMMAR (QuickAddParser.parse(text, now: LocalDateTime, today: LocalDate, lists: List<TaskListEntity>) -> ParsedTask(title, dueDay?, dueMold?, priority?, tags, listId?))
Split the text on spaces. Matching is case-insensitive. Each step removes the words it matched; later steps see only the remaining words.
 1. Priority: a word "!high", "!h" or "!3" -> 3; "!medium", "!med", "!m" or "!2" -> 2; "!low", "!l" or "!1" -> 1. If several, the last wins.
 2. Tags: every word matching #[\p{L}0-9-]+ -> tag (lowercase, without #).
 3. List: a word ~name -> the list whose name, lowercased with spaces removed, equals name lowercased. If no list matches, leave the word in the title.
 4. Date (first rule that matches, checked in this order; only one date per text):
    a. "day after tomorrow" -> today + 2
    b. "today" -> today
    c. "tomorrow" or "tmr" -> today + 1   ("tom" and "tod" are NOT shortcuts: they are common words and names)
    d. "next week" -> Monday of next week
    e. "in N days" / "in N weeks" (N = 1..99) -> today + N days / N*7 days
    f. "next <weekday>" -> that weekday in next week (Monday-start weeks)
    g. "<weekday>" -> the first such day strictly after today. A full weekday name (monday ... sunday) matches anywhere. A 3-letter abbreviation (mon tue wed thu fri sat sun) matches ONLY when it is the last word of the text, or when it directly follows "on" (then "on" is removed too), because words like "sun" and "sat" appear in ordinary titles.
    h. "<D> <month>" or "<month> <D>" (month = full name or 3 letters; D = 1..31) -> that date this year; if it is before today, next year
    i. "<D>/<M>" or "<D>/<M>/<YYYY>" (day first, Indian order) -> that date; without a year and before today -> next year
    Invalid dates (e.g. 31/2) do not match.
 5. Time (first match only): "<H>am"/"<H>pm"/"<H> am"/"<H> pm" or "<H>:<MM>am|pm" (H = 1..12) -> 12-hour time; "<H>:<MM>" without am/pm -> 24-hour time (H = 0..23); "at <H>" (H = 1..12) -> 8..11 = AM, 12 = 12 PM, 1..7 = PM; "noon" -> 12:00 PM. A bare number with no am/pm/colon/"at" is NOT a time.
    If a time matched but no date: due today if that time is later than now, otherwise tomorrow.
 6. Title = remaining words joined by single spaces, trimmed. If the title would be empty, ignore every parsed part and use the full original text as the title.
The chips under the quick-add field show parsed parts live as the user types; tapping a chip's × disables that part (its words stay in the title).

PARSER EXAMPLES (now = Mon 2026-10-05 17:00, today = 2026-10-05, one list named "Shopping" exists). Add these to the unit tests and to EngineCheckScreen as a second section "Parser check" with PASS/FAIL rows:
Q1 "gym tomorrow 6pm !high #fitness" -> title "gym", due 2026-10-06 18:00, priority 3, tags [fitness]
Q2 "submit SAPM report fri" -> title "submit SAPM report", due 2026-10-09, all-day
Q3 "call home 9pm" -> title "call home", due 2026-10-05 21:00
Q4 "call home 9am" -> title "call home", due 2026-10-06 09:00
Q5 "pay fees 15 oct !med" -> title "pay fees", due 2026-10-15, all-day, priority 2
Q6 "buy gainer in 3 days ~shopping #supplies" -> title "buy gainer", due 2026-10-08, all-day, list Shopping, tags [supplies]
Q7 "renew passport 2/1" -> title "renew passport", due 2027-01-02, all-day
Q8 "read !low" -> title "read", no date, priority 1
Q9 "next mon standup at 10" -> title "standup", due 2026-10-12 10:00
Q10 "buy 2 bananas" -> title "buy 2 bananas", no date (a bare number is not a time)
Q11 "buy sun cream" -> title "buy sun cream", no date (3-letter weekday not last and not after "on")
Q12 "call tom" -> title "call tom", no date ("tom" is not a shortcut)
```

## Block E — Known build issues and workarounds

These are the build errors and runtime bugs this app is most likely to hit, from Gradle version clashes to Android 14–16 restrictions and OnePlus behaviour. Each entry gives Gemini the symptom to recognise and the exact fix, so it applies a known fix instead of improvising. Part 1 also pins down small details the spec left open, so Gemini doesn't stop to ask.

```text
BLOCK E — KNOWN ISSUES AND WORKAROUNDS
Rule: when a build error, crash or wrong behaviour matches an entry below, apply that entry's fix FIRST, exactly as written, before trying anything else. Mention the entry number (e.g. "applied E7") in your reply.

=== PART 1. DEFAULTS FOR DETAILS NOT SPELLED OUT ELSEWHERE (use these; do not ask) ===
P1. Project identity: namespace and applicationId = "com.bulktracker.app", set in Phase 1 and NEVER changed afterwards (a new applicationId installs as a separate app with empty data). If the template created files in another package, move them to com.bulktracker.app in Phase 1.
P2. versionCode: increase by exactly 1 at the end of every phase; never decrease it. versionName = "0.<phase>" (e.g. "0.3" after Phase 3).
P3. minSdk = 26 (java.time needs no desugaring at 26). Keep the template's compileSdk and targetSdk unless the build fails.
P4. Room: @Database(exportSchema = false). Get the database with Room.databaseBuilder(context, AppDatabase::class.java, "bulk_tracker.db").build(); no allowMainThreadQueries.
P5. Settings row: the DAO returns Flow<SettingsEntity?>; the repository maps null to SettingsEntity() (all defaults), so the UI never crashes before seeding finishes.
P6. Receivers and services reach the database with (context.applicationContext as BulkTrackerApp).container. Inside receivers read current data with suspend functions or flow.first(), never by collecting.
P7. Week strip paging: HorizontalPager(pageCount = 2001), initialPage = 1000; page p shows the week starting mondayOf(logicalToday) + (p - 1000) * 7 days. Month grid paging uses the same pattern with months.
P8. App-wide coroutine scope in AppContainer: CoroutineScope(SupervisorJob() + Dispatchers.Default).
P9. Number formatting: NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN")). Do not use the deprecated Locale(String, String) constructor.
P10. Rounding: use kotlin.math.roundToInt() everywhere a Double becomes an Int (Math.round on a Double returns Long and will not compile where an Int is expected).
P11. Unit test dependency: testImplementation("junit:junit:4.13.2"). If you cannot run unit tests in this environment, say so; the Engine check screen (Phase 5) is the fallback.
P12. Strings may stay as Kotlin string literals; strings.xml is not required.

=== PART 2. BUILD ERRORS ===
E1. "ksp-<x> is too old/new for kotlin-<y>" or KSP plugin not found
    Cause: KSP and Kotlin versions don't match.
    Fix: read the Kotlin version in the project's build files and use the KSP release made for exactly that Kotlin version (for Kotlin 2.0.x/2.1.x the KSP version starts with the same Kotlin version, e.g. "2.1.10-1.0.31"; newer KSP releases may use their own version numbers — then pick the one whose release notes list this Kotlin version). Never switch to kapt. Never downgrade Kotlin to fix this.
E2. "Compose compiler plugin is required" / "Starting in Kotlin 2.0, the Compose Compiler Gradle plugin is required"
    Fix: apply plugin org.jetbrains.kotlin.plugin.compose with the SAME version as Kotlin. Remove any composeOptions { kotlinCompilerExtensionVersion }.
E3. kotlinx.serialization plugin errors (Phase 7)
    Fix: plugin org.jetbrains.kotlin.plugin.serialization with the SAME version as Kotlin; library org.jetbrains.kotlinx:kotlinx-serialization-json. Use Json { ignoreUnknownKeys = true; encodeDefaults = true }. Serialize separate @Serializable DTO data classes (one per table), not the Room entities themselves.
E4. "Unresolved reference" for Icons.Outlined.CalendarMonth / ViewTimeline / ViewAgenda / PushPin
    Fix: add androidx.compose.material:material-icons-extended (use the version from the Compose BOM). If that artifact cannot be resolved, create the 5 icons as vector drawables in res/drawable (Material Symbols outlined, 24dp) and load them with painterResource.
E5. "This material API is experimental" (DatePicker, TimePicker, ExposedDropdownMenuBox, SegmentedButton, ModalBottomSheet, TopAppBar)
    Fix: add @OptIn(ExperimentalMaterial3Api::class) to the composable or file. Same for HorizontalPager with @OptIn(ExperimentalFoundationApi::class) if the compiler asks.
E6. menuAnchor() deprecated/ambiguous in ExposedDropdownMenuBox
    Fix: use Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true) if available; otherwise the plain menuAnchor().
E7. No TimePickerDialog composable exists in this Material 3 version
    Fix: show TimePicker(state) inside an AlertDialog with "Cancel" and "OK" text buttons. Always TimePickerState(is24Hour = false).
E8. "Resource not found: Theme.Material3..." / "Theme.MaterialComponents" in AndroidManifest
    Cause: the project has no com.google.android.material dependency (Compose-only).
    Fix: use android:theme="@android:style/Theme.Material.NoActionBar" for MainActivity and AlarmActivity (AlarmActivity: android:windowBackground black). All real theming is done in Compose.
E9. combine(...) does not compile with more than 5 flows
    Fix: use combine(listOf(f1, f2, ...)) { arr -> ... } with casts, or nest: combine(combine(a, b, c){...}, combine(d, e, f){...}) { x, y -> ... }.
E10. Lint errors on release-type builds (ExactAlarm, FullScreenIntentPolicy, ForegroundServiceType, MissingPermission)
    Fix: keep the permissions; add tools:ignore="ProtectedPermissions,ExactAlarm" on the uses-permission lines, and in build.gradle.kts lint { abortOnError = false }.
E11. API-level compile errors ("Call requires API level 31/33/34")
    Fix: guard with Build.VERSION.SDK_INT checks: VibratorManager (31), canScheduleExactAlarms (31), POST_NOTIFICATIONS request (33), canUseFullScreenIntent and ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT (34), setShowWhenLocked/setTurnScreenOn (27; on 26 use WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED and FLAG_TURN_SCREEN_ON). For startForeground with a type use ServiceCompat.startForeground(service, id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK).
E12. Glance widget crashes or does not compile (Phase 9)
    Cause: mixing androidx.compose.ui / material3 imports into Glance code.
    Fix: widget files import only androidx.glance.* (GlanceModifier, glance Text, Row, Column, LinearProgressIndicator from glance.appwidget). Read data inside provideGlance with repository suspend functions before calling provideContent.

=== PART 3. RUNTIME BUGS ===
R1. Gap above the bottom navigation bar, or content hidden under it
    Cause: nested Scaffolds each adding system-bar insets.
    Fix: only the top-level Scaffold (with the NavigationBar) handles system insets; every screen-level Scaffold uses contentWindowInsets = WindowInsets(0, 0, 0, 0).
R2. Bottom sheet content behind the gesture bar or keyboard
    Fix: inside every ModalBottomSheet, add Modifier.navigationBarsPadding().imePadding() to the content column.
R3. Status bar icons invisible (white on white or black on black) when the in-app theme differs from the system
    Fix: call enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDarkTheme }, navigationBarStyle = same) with isDarkTheme computed from settings.themeMode, and call it again whenever themeMode changes.
R4. Purple or tinted surfaces appear
    Fix: block C lists every ColorScheme role; set all of them and surfaceTint = Color.Transparent. Pass tonalElevation = 0.dp to Card, Surface, NavigationBar, ModalBottomSheet and TopAppBar.
R5. Dates off by one in date pickers
    Fix: block A rule T6 (UTC conversion).
R6. Today screen empty for a moment on first launch
    Expected while seeding runs. If it stays empty, check that seedIfEmpty ran and that activeFromDay = logicalToday (not epochDay of the UTC date).
R7. ForegroundServiceStartNotAllowedException when an alarm fires
    Fix: in AlarmReceiver wrap startForegroundService in try/catch; on failure post the "meal_alarms" notification directly with setFullScreenIntent(AlarmActivity) (sound comes only from AlarmActivity, which plays it with the same MediaPlayer and volume rules while it is visible).
R8. Alarm silent or vibration missing on silent mode
    Fix: vibrate with VibrationAttributes.USAGE_ALARM on API 33+ (vibrator.vibrate(effect, VibrationAttributes.createForUsage(USAGE_ALARM))), and with AudioAttributes USAGE_ALARM before 33. MediaPlayer: setAudioAttributes BEFORE setDataSource/prepare. If RingtoneManager returns null for both the alarm and notification URIs, vibrate only.
R9. SecurityException from audioManager.setStreamVolume (seen on some phones in Do Not Disturb)
    Fix: wrap in try/catch; on failure keep the current alarm volume and continue ringing; do not set savedAlarmVolume.
R10. Receiver work cut off (alarm never shows after reboot or on fire)
    Fix: in onReceive call val pending = goAsync(), run the work in the app scope on Dispatchers.IO, and call pending.finish() in a finally block. Keep the work under 10 seconds (no network, small queries only).
R11. Alarm screen does not appear when the phone is unlocked
    Expected Android behaviour: with the screen on and unlocked, a full-screen intent shows as a heads-up notification instead. The sound and buttons still work. Not a bug; do not try to start the activity directly from the background (Android blocks it).
R12. A status-bar alarm-clock icon appears and the lock screen shows the next meal as "next alarm"
    Expected: this is how setAlarmClock works, and it is what makes alarms reliable on OnePlus. Keep it.
R13. Exact alarms rejected on Android 12–13 even with USE_EXACT_ALARM
    Fix: USE_EXACT_ALARM only exists from API 33. On API 31–32 SCHEDULE_EXACT_ALARM is used (declared with maxSdkVersion 32); if canScheduleExactAlarms() is false, the Permissions card's Fix button opens ACTION_REQUEST_SCHEDULE_EXACT_ALARM.
R14. Room "Cannot access database on the main thread"
    Fix: move the call into a suspend repository function on Dispatchers.IO; never add allowMainThreadQueries().
R15. Duplicate log rows after fast double taps
    Fix: toggleItem must be a single @Transaction DAO method (block B). Also ignore taps on the same row for 300 ms in the UI.
R16. App shows an old day after midnight
    Fix: block A rule T4 (recompute logicalToday on ON_RESUME with a LifecycleEventObserver in MainActivity).
R17. Persisted folder lost after reinstall (Phase 7)
    Expected: persisted URI permissions are deleted on uninstall. Show the "Auto-backup failed — choose the folder again" Snackbar; never crash.
R18. Test on the real phone, not only the emulator, for: full-screen alarms over the lock screen, alarms after reboot, vibration on silent, OnePlus battery behaviour. The cloud emulator cannot show these reliably.

=== PART 4. TASKS TAB (Phases 10–11) ===
T1. Icons.Outlined.Sort or Icons.Filled.Send marked deprecated or unresolved
    Fix: use Icons.AutoMirrored.Outlined.Sort and Icons.AutoMirrored.Filled.Send. Any other missing task icon: apply E4.
T2. Quick-add keyboard does not open
    Fix: inside the sheet: val fr = remember { FocusRequester() }; LaunchedEffect(Unit) { delay(150); fr.requestFocus(); keyboardController?.show() }; TextField(modifier = Modifier.focusRequester(fr)). Content column uses Modifier.imePadding().
T3. "Key was already used" crash in the task list
    Fix: LazyColumn keys must be unique strings: "h-<group name>" for headers, "t-<taskId>" for open tasks, "c-<taskId>" for completed tasks, "done-header" for the Completed row.
T4. Drawer opens when using the Android back gesture from the left edge
    Fix: ModalNavigationDrawer(gesturesEnabled = drawerState.isOpen); open it only with the menu button. Add BackHandler(drawerState.isOpen) { scope.launch { drawerState.close() } }.
T5. Navigation argument crash for "task/{taskId}"
    Fix: navArgument("taskId") { type = NavType.LongType }. Navigate with navController.navigate("task/$id").
T6. Title or notes typing lags or loses characters in task detail
    Fix: keep the text in local state (remember { mutableStateOf(initial) }) and save to the database with a 500 ms debounce; never bind the TextField directly to a Flow from Room.
T7. Repeating task creates two copies on one tap
    Fix: completion of a repeating task is one @Transaction repository method, and the checkbox ignores taps for 300 ms (same as R15).

=== PART 5. ANDROID 15/16 SPECIFICS ===
A1. Back gesture: Android 16 always uses predictive back. Never override onBackPressed(). Use Compose BackHandler (drawer, sheets, task detail) and set android:enableOnBackInvokedCallback="true" on <application>.
A2. Alarm screen status-bar icons invisible on the black background (a known bug in other alarm apps)
    Fix: in AlarmActivity call enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)).
A3. MissingForegroundServiceTypeException
    Fix: AlarmRingService must have android:foregroundServiceType="mediaPlayback" in the manifest AND pass FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK to ServiceCompat.startForeground.
A4. ForegroundServiceStartNotAllowedException from BootReceiver
    Cause: Android 15+ forbids starting media-playback services from boot. Fix: BootReceiver only reschedules (block D3); see R7 for the fallback in AlarmReceiver.
A5. Alarm sometimes never rings when the phone has been idle for hours
    Fix: block D3 wake lock: acquire PARTIAL_WAKE_LOCK in AlarmReceiver before starting the service and release it in the service after startForeground(); declare WAKE_LOCK.
A6. Swiping away the ringing notification leaves sound playing (Android 14+ makes ongoing notifications dismissible)
    Fix: block D3 setDeleteIntent stops ringing.
A7. TalkBack announcements: Android 16 ignores announceForAccessibility(); use liveRegion semantics only (block C).


## Phase prompts 2–11

Paste these one at a time, only after the previous phase passes its checklist. Alarms now come in Phase 3 at your normal meal times, because they help with consistency, and they switch to smart times automatically in Phase 5. The riskiest gesture work (drag-to-create) has its own phase near the end.

| Phase | Adds | Risk |
| --- | --- | --- |
| 2 | Custom entries, portions, add/remove with scopes, new meals, bare-minimum day | Medium |
| 3 | Full-screen alarms at normal meal times, rest-day toggle, permissions | High on OnePlus |
| 4 | Schedule tab: semester, holidays, classes (form), events, repeats | Medium |
| 5 | Meal scheduling engine, Engine check, warnings, timeline view | High (logic) |
| 6 | History tab, habits, weigh-ins, Sunday weigh-in alarm | Low |
| 7 | Settings, meal editor, theme, backup + weekly auto-backup | Medium |
| 8 | Drag-to-create classes on the week grid | Medium |
| 9 | Home-screen widget, final audit | Medium |
| 10 | Tasks core: lists, smart lists, quick add, task detail, subtasks, tags, priority, repeats | Medium |
| 11 | Task reminders, smart quick-add, calendar view | Medium |

**Phase 2 — editing the checklist**

```text
Re-read docs/SPEC.md. Phase 1 passed. Build PHASE 2 only, exactly as the spec says.
1. Add sheet, Item sheet, ScopeSheet and ConfirmDialog exactly as block C.
2. Custom entries: LogEntryEntity with isCustom = true, foodItemId = null. Meal sections show them under "What I ate"; other sections list them under the checklist.
3. "Also add to this meal's checklist" creates a FoodItemEntity using the ADD scope rules in block B.
4. Remove item and Remove meal use the REMOVE scope rules in block B: ScopeSheet -> ConfirmDialog -> change.
5. Card overflow menu: "Pin time for this day" (TimePicker -> ConfirmDialog -> DayOverride PIN_TIME; card shows the time and pin icon); "Skip this meal this day" (ConfirmDialog -> DayOverride SKIP; card shows "Skipped" and collapses; the menu then shows "Restore", which deletes the override).
6. "+ Add meal" text button at the end of the card list: Name, Time (TimePicker), Size (SegmentedButton "Snack" / "Big meal"), Colour (6 circles #3B82F6 #0D9488 #EA580C #8B5CF6 #DC2626 #6366F1). Save opens ScopeSheet in Add mode. New section: role "CUSTOM", timing "FIXED", durationMin 20 (Snack) or 30 (Big), isMealSection false.
7. Bare-minimum day: the Today overflow item and all block B bare-minimum rules (only the "Bare-minimum day" item now; "Rest day" comes in Phase 3).
8. Until Phase 5, cards are ordered by pinnedMold if pinned, else idealMold.
9. Snackbar with Undo for uncheck and delete, restoring the exact row.

PHASE 2 CHECKLIST
[ ] Dinner > "+ Add what I ate" > "Normal plate" > "Paneer + 2 roti": row shows 450 kcal; totals +450.
[ ] "Samosa" with calories "I don't know": summary shows " · 1 unknown".
[ ] Long-press Bananas > ×1.5: 270 kcal; totals update.
[ ] On a Tuesday, remove Apple > "Every Tuesday from now" > confirm: next Tuesday has no Apple, Wednesday has it, last Tuesday's logged Apple still shows.
[ ] Remove Honey > "Just this day": gone today, back tomorrow.
[ ] "+ Add meal" "Sunday brunch" 11:00 AM, "Every Sunday from now": appears on Sundays only.
[ ] Bare-minimum day on: only starred items show and the calorie target becomes their total; tomorrow is normal.
[ ] Every scope action showed a confirmation sentence first.
```

**Phase 3 — alarms at normal meal times**

```text
Re-read docs/SPEC.md. Phase 2 passed. Build PHASE 3 only.
1. domain/DayPlanner.kt (interface and DayPlan classes) and domain/IdealTimePlanner.kt exactly as block D2's opening paragraph. AppContainer provides IdealTimePlanner as the DayPlanner. Today's card times and the Next meal card now come from the DayPlan.
2. The whole of block D3 (manifest, channels, AlarmScheduler, AlarmReceiver, AlarmRingService, AlarmActivity, BootReceiver, actions, snooze limit, volume handling, Permissions card), EXCEPT the WEIGH_IN alarm (step 6), which comes in Phase 6. ScheduleRepository does not exist yet: use an empty busy list.
3. Today overflow item "Rest day (no gym)" writing DailyHabitEntity.gymPlan as block C describes.
4. Settings tab, only an "Alarms" section for now: master switch, per-meal switches, Permissions card. Other alarm values use SettingsEntity defaults.
Do not use WorkManager. Do not request REQUEST_IGNORE_BATTERY_OPTIMIZATIONS.

PHASE 3 CHECKLIST (test on the real phone)
[ ] Alarms on: notification permission prompt; denying turns the switch off.
[ ] Permissions card all green after using its Fix buttons.
[ ] Pin a meal 2 minutes ahead: full-screen alarm over the lock screen at half of maximum alarm volume; your normal alarm volume is back afterwards.
[ ] Phone on vibrate or silent: vibration only.
[ ] Snooze works 3 times; the 4th ring has no Snooze button.
[ ] Mark all eaten checks every item; Skip marks the card Skipped.
[ ] Ignored alarm stops after 60 s, leaves "Missed", follow-up 45 min later.
[ ] Rest day toggle: Night Shake shows 2.5 h after dinner.
[ ] After restarting the phone, the next alarm still rings.
```

**Phase 4 — schedule tab**

```text
Re-read docs/SPEC.md. Phase 3 passed. Build PHASE 4 only.
1. Schedule tab exactly as block C: Week, Events and Semester segments, Class form, Event form. The week grid is VIEW-ONLY in this phase (no long-press, no drag); classes are added with the FAB.
2. domain/RecurrenceExpander.kt implementing occursOn and the "Only <date>" / "All" edit and delete rules exactly as block D1. There is no "This and following".
3. ScheduleRepository.busyBlocksFor(day): Flow<List<BusyBlock>> exactly as block D1. AlarmScheduler and Today now pass these busy blocks to the DayPlanner (IdealTimePlanner still ignores them).
4. Unit tests for occursOn: (a) NONE on its day and the day after; (b) WEEKLY Tue+Thu every week; (c) WEEKLY Mon every 2 weeks: true, false, true on three consecutive Mondays from the start; (d) MONTHLY on the 31st skips 30-day months; (e) DAILY COUNT 3: true days 1–3, false day 4; (f) UNTIL is inclusive; (g) a CANCELLED exception.

PHASE 4 CHECKLIST
[ ] Semester 1 Aug – 5 Dec 2026 saved; holiday "Diwali break" 20–26 Oct saved.
[ ] A class saved for Mon + Wed appears in both columns of the grid.
[ ] Event "Gym with friends" every Tue and Thu, 6–7 PM, after 4 times: summary text is correct.
[ ] Editing one occurrence with "Only <date>" changes only that date.
[ ] All unit tests pass.
```

**Phase 5 — scheduling engine, Engine check and timeline**

```text
Re-read docs/SPEC.md. Phase 4 passed. Build PHASE 5 only.
1. domain/MealScheduler.kt exactly as block D2 steps 1–10, with no extra rules. AppContainer switches the DayPlanner from IdealTimePlanner to MealScheduler.
2. Unit tests reproducing Ex1–Ex6 exactly, and domain/EngineCheck.kt + ui/settings/EngineCheckScreen.kt as described under the worked examples. Add the "Engine check" row to Settings (it can sit alone under an "About" header for now).
3. Today uses the DayPlan fully: card order and times, clock icon for moved, pin icon for pinned, warning cards, doesn't-fit cards, schedule chip and Day Schedule sheet (block C items 3–5). Class cancel writes ClassCancellationEntity; Restore deletes it; event cancel writes a CANCELLED exception.
4. Timeline view exactly as block C item 8, with the top-bar toggle.
5. Doesn't-fit actions write DayOverrides MERGE_INTO (items appear under the target card as "From <section>"), PIN_TIME and SKIP.

PHASE 5 CHECKLIST
[ ] Settings > Engine check shows PASS for Ex1–Ex6.
[ ] With Ex1's classes on a Monday, Today shows 9:30, 12:00, 1:30, 5:00 (clock icon), 8:30, 11:15, and alarms follow those times.
[ ] Cancelling the 2:00 PM class for that day moves the 4 PM Snack back to 4:00 PM immediately.
[ ] Ex4's day shows "Between Classes doesn't fit today"; "Merge into Lunch" moves those items under Lunch for that day only.
[ ] Timeline shows classes, gym, meal pills and the red now line; meal pills never sit on top of classes.
```

**Phase 6 — history, habits and weigh-ins**

```text
Re-read docs/SPEC.md. Phase 5 passed. Build PHASE 6 only.
1. History tab exactly as block C HISTORY SCREEN (Part 1 calendar, Part 2 progress). The week strip's chevron on Today expands the same month grid in place (animateContentSize with FAST_MS); tapping a date selects it and collapses the grid.
2. Habits card on Today, right after the summary card: "Water" ProgressRow "6 / 12 glasses" with − and + IconButtons (never below 0), then FilterChips "Sunlight 15 min" and "Went to gym" (records attendance; it is separate from the planned "Rest day" toggle that moves the night shake). Stored in DailyHabitEntity for SelectedDate.
3. On Sundays without a weigh-in for that day, the summary card adds a line "Weigh-in day" with a text button "Log weight" (opens the log-weight dialog).
4. The WEIGH_IN alarm (block D3 step 6) and its "Sunday weigh-in alarm" switch in Settings > Alarms.
5. Month completion is computed with one query per visible month, not one per day.

PHASE 6 CHECKLIST
[ ] Completed days turn green on the month grid and week strip; holidays show the grey dash.
[ ] Tapping a past date opens it on Today with the "Today" button visible.
[ ] Log 42.4 kg, then 42.8 kg a week later: 2 dots, rising line, dashed goal at 54 kg, dashed target line.
[ ] Sunday weigh-in alarm rings at wake time; "Log weight" opens the dialog.
```

**Phase 7 — settings, meal editor and backups**

```text
Re-read docs/SPEC.md. Phase 6 passed. Build PHASE 7 only.
1. Settings screen exactly as block C SETTINGS SCREEN, keeping the Alarms section and Engine check row you already built. Every value saves immediately; DayPlan and alarms update at once.
2. Meal editor: up/down IconButtons to reorder; edit every field; changing a section's time edits idealMold; add and remove only through ScopeSheet -> ConfirmDialog; never hard-delete anything that has log entries.
3. Theme override and default Today view.
4. Manual backup with the Storage Access Framework (no storage permissions); add kotlinx-serialization and androidx.documentfile.
   Export: ActivityResultContracts.CreateDocument("application/json"), name "bulk-tracker-backup-YYYY-MM-DD.json", every table from block B (the settings row WITHOUT autoBackupTreeUri, lastAutoBackupMillis and savedAlarmVolume; an import keeps this phone's current values of those three) plus "schemaVersion": 1 and "exportedAt" (ISO date-time). Update lastExportMillis.
   Import: ActivityResultContracts.OpenDocument(arrayOf("application/json")). Parse and validate fully first, rejecting: files over 10 MB, schemaVersion other than 1, any text longer than its block C limit, any number outside its block C range, more than 100,000 rows in any table, and task reminders over 3 per task. ConfirmDialog "Replace all data with <n> log entries, <n> classes, <n> events, <n> weigh-ins?" with confirm label "Replace data". On confirm replace every table in ONE Room transaction, then AlarmScheduler.rescheduleAll(). Invalid file: Snackbar "This file is not a Bulk Tracker backup", nothing changes.
5. Weekly auto-backup: "Choose folder" uses ActivityResultContracts.OpenDocumentTree(), then contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION) and saves the uri string in autoBackupTreeUri. In MainActivity ON_RESUME: if autoBackupEnabled, the uri is set, and lastAutoBackupMillis is null or at least 7 days old, write "bulk-tracker-auto-YYYY-MM-DD.json" (same JSON as export) into that folder with DocumentFile.fromTreeUri on Dispatchers.IO, update lastAutoBackupMillis, then delete older files starting with "bulk-tracker-auto-" so only the newest 4 remain. If writing fails, show Snackbar "Auto-backup failed — choose the folder again".
6. Setup screen exactly as block C SETUP SCREEN, plus the "Run setup again" row in Settings > About. On this phone onboardingDone is still false after updating, so the setup appears once; that is expected.

PHASE 7 CHECKLIST
[ ] Calorie target 3,000: Today's bar updates at once.
[ ] Big-meal gap 180: a real Monday with Ex1's classes changes immediately; Engine check still shows PASS because it always uses default settings. Set the gap back to 150.
[ ] Renaming an item changes today; past days keep the old name.
[ ] Export, check extra items, import: extra checks gone; classes and events intact.
[ ] Random JSON import changes nothing.
[ ] Auto-backup folder chosen: a file appears there on the next app open.
[ ] Setup shows 4 steps; Skip and Next both work; Finish opens Today and saves the weight. "Run setup again" shows it again.
```

**Phase 8 — drag-to-create classes**

```text
Re-read docs/SPEC.md. Phase 7 passed. Build PHASE 8 only: the "Create by drag" behaviour on the Schedule week grid exactly as block C. Use pointerInput with detectDragGesturesAfterLongPress. Snap start and end to 15 minutes, minimum 30 minutes. On release open the Class form prefilled. Do not change anything else on the grid or the form.

PHASE 8 CHECKLIST
[ ] Long-press Monday 10:30 and drag to 12:00: Class form opens with Mon 10:30 AM–12:00 PM.
[ ] Vertical scrolling of the grid still works when you are not long-pressing.
[ ] Tapping an existing class still opens it for editing.
```

**Phase 9 — widget and final audit**

```text
Re-read docs/SPEC.md. Phase 8 passed. Build PHASE 9 only.
1. Home-screen widget with Jetpack Glance (androidx.glance:glance-appwidget and glance-material3). Default 4x2 cells, resizable horizontally. Monochrome per block C; the section dot is the only colour.
   Line 1 "Next: <Section> · <time>" (same rule as the Next meal card; if none: "All meals done"). Line 2 "<n> items · <kcal> kcal". Line 3 "Today <kcal> / <target> kcal" with a thin progress bar. Tapping opens Today.
   Update it from the same debounced coroutine that calls AlarmScheduler.rescheduleAll() (GlanceAppWidget.updateAll), plus updatePeriodMillis 1800000 in the widget info XML.
2. Final audit, fixing everything found:
   - Every IconButton has a contentDescription.
   - At font size Large and display size Large nothing is clipped; long names wrap to 2 lines, then ellipsis.
   - No Room query on the main thread; no list recomputed on every recomposition (remember / derivedStateOf).
   - App icon: adaptive icon, white check mark on a #111111 rounded square.
   - Reply with every manifest permission and why it is needed.

PHASE 9 CHECKLIST
[ ] Widget shows the next meal and updates within seconds of checking an item.
[ ] Tapping the widget opens Today.
[ ] Light and dark mode look right at font size Large.
```

**Phase 10 — Tasks core**

```text
Re-read docs/SPEC.md. Phase 9 passed. Build PHASE 10 only. The task tables already exist from Phase 1; do NOT change the database version or schema.
1. TaskRepository and domain/TaskRules.kt (block D4: overdue rule, nextDueDay, completing and reopening, repeating completion and its Undo).
2. Tasks tab exactly as block C TASKS TAB items 1–7 and 9: drawer with smart lists Inbox, Today, Tomorrow, Next 7 Days, Completed (leave out Calendar), user lists and tags; sorting and groups; task rows; collapsed Completed section; long-press menu; quick add WITHOUT the parse chips (plain title plus the Date, Priority, List and Tag buttons); the Date sheet WITHOUT the Reminders row; task detail.
3. Repeat options in the Date sheet work now (completion creates the next copy).
4. Unit tests for TaskRules: (a) weekly Mon+Thu task due Mon 2026-10-05 -> next due Thu 2026-10-08; (b) daily every 2 days due 2026-10-05 -> 2026-10-07; (c) monthly on the 31st due 2026-10-31 -> 2026-12-31; (d) overdue rule for a timed task due today 1 hour ago; (e) daily task due 2026-10-01 completed with today = 2026-10-05 -> next due 2026-10-05. Tests (a)–(c) use today = the task's due date.
Do not touch any meal, schedule or alarm code except adding the Tasks route and tab.

PHASE 10 CHECKLIST
[ ] Quick add "Buy creatine" in Today: appears under "Today".
[ ] A task due yesterday shows under "Overdue" in red on Today and Next 7 Days.
[ ] New list "College" with a colour; a task moved there shows the list dot in smart lists.
[ ] Task detail: add 3 subtasks, tick one: the row shows "1/3".
[ ] Weekly repeating task: ticking it moves it to Completed and a new copy appears on the next due day; Undo removes the copy.
[ ] Deleting the list moves its tasks to the Inbox (after a confirmation).
```

**Phase 11 — task reminders, smart quick-add and calendar**

```text
Re-read docs/SPEC.md. Phase 10 passed. Build PHASE 11 only.
1. The Reminders row in the Date sheet (block C TASKS TAB item 6) and all task reminder rules in block D4: codes, scheduling inside rescheduleAll(), AlarmReceiver type "TASK", the task mode of AlarmRingService and the alarm screen, the "task_reminders" channel, Complete/Snooze/Open actions, the permission card on the Tasks tab.
2. domain/QuickAddParser.kt exactly as the grammar in block D4, the parse chips under the quick-add field, unit tests for Q1–Q12, and a "Parser check" section on EngineCheckScreen with PASS/FAIL rows for Q1–Q12.
3. The Calendar smart list (block C TASKS TAB item 8).
4. Re-run the Phase 9 audit items on everything added in Phases 10–11.

PHASE 11 CHECKLIST
[ ] Engine check: Parser check shows PASS for Q1–Q12.
[ ] Typing "gym tomorrow 6pm !high #fitness" shows chips for date/time, high priority and #fitness; tapping the date chip's × keeps "tomorrow 6pm" in the title.
[ ] A task 2 minutes ahead with an "On time" Notification reminder: notification arrives; "Complete" ticks the task.
[ ] Same with an "Alarm" reminder: full-screen alarm with Complete / Snooze / Open.
[ ] Turning meal alarms off in Settings does not stop task reminders.
[ ] Calendar view shows dots on days with tasks; tapping a day lists them.
```

## Final review log

A last end-to-end pass found 6 bugs that would have broken alarms or data, 5 security and privacy gaps, and 8 behaviour or consistency issues. All are fixed in the blocks above; this table records what changed and why.

| Area | Problem found | Fix now in the spec |
| --- | --- | --- |
| Alarms | Turning meal alarms off also cancelled every task reminder (the off switch said "cancel everything and return") | Off switch now only affects meal and weigh-in alarms (D3 step 1) |
| Alarms | Alarms are only planned for today and tomorrow, so not opening the app for 2 days meant no alarms at all | Daily 4:05 AM maintenance alarm re-plans everything; every alarm also re-plans when it fires (D3 step 7) |
| Alarms | Phone could fall asleep between the alarm firing and the sound starting | Wake lock held from receiver to service, as real alarm-clock apps do (D3, E A5) |
| Alarms | Two alarms at once (a meal and a task) would fight over one ringing service | Only one rings; the second becomes a notification (D3) |
| Alarms | Rang over phone calls, didn't pause music, and kept ringing if the notification was swiped away | Quiet during calls, audio focus, swipe-to-dismiss stops it (D3) |
| Alarms | Boot receiver starting a media service crashes on Android 15+ | Boot receiver only reschedules (D3, E A4) |
| Tasks | Completing an overdue daily task created another overdue copy, then another | Next occurrence is never earlier than today (D4, new test e) |
| Tasks | Quick-add read "call tom" as tomorrow and "buy sun cream" as Sunday | Removed "tom"/"tod" shortcuts; 3-letter weekdays only at the end or after "on" (D4, Q11–Q12) |
| Security | Tags with \_ acted as SQL wildcards, so #a\_b matched other tags | Tags limited to letters, digits and -; bound parameters only (B, S5) |
| Security | Launcher intent extras were trusted blindly | Extras validated (A S4) |
| Privacy | Task titles showed on the lock screen | Private notifications with a generic public version (D4) |
| Security | Backup import had no size or content limits | 10 MB, row-count and field-limit checks before anything is written (Phase 7) |
| Security | USB debugging left on after installing | Install steps now say to switch it off (first section) |
| Data | Splitting a meal for "every Tuesday" orphaned future pins and skips | Future overrides move to the new ids (B) |
| Data | Pin and Skip on the same day silently replaced each other | Documented; menu shows only valid options (B) |
| Consistency | Phase 7 checklist expected Engine check to FAIL after changing settings, but it always uses defaults | Checklist corrected |
| Consistency | "Gym" habit chip and "Rest day" toggle looked like the same thing | Chip renamed "Went to gym" (Phase 6) |
| Android 16 | Predictive back and the removed accessibility announcement API | Rules A1 and A7 in block E |
| Design | Alarm screen status-bar icons invisible on black (a bug other alarm apps shipped) | Light status-bar icons on the alarm screen (E A2) |

## Pitfalls and fix-it prompts

The three things most likely to go wrong are lost data, alarms that never ring on OxygenOS, and meal times that don't match the worked examples. The spec guards against all three; this section is for when something slips through anyway.

| Problem | Why it happens | What to do |
| --- | --- | --- |
| Build fails mentioning Room, KSP or kapt | KSP version doesn't match Kotlin | Fix-it prompt 1 |
| Gemini rewrites working screens or changes the look | Flash models drift in long sessions | Fix-it prompt 2; annotation mode for small tweaks |
| Engine check shows FAIL, or meal times look wrong | The engine added or skipped a rule | Fix-it prompt 3 |
| Repeating event shows on the wrong days | Week maths off by one (Sunday vs Monday start) | Fix-it prompt 4 |
| All data gone after an update | Destructive migration, or a build signed by another project | Same AI Studio project only; export before every update |
| Alarm shows only a small banner, not full screen | Phone unlocked (Android shows a banner then), or full-screen permission off | Normal when unlocked; otherwise fix in the Permissions card |
| Alarms never ring, or ring very late | OxygenOS battery management | Phone settings below |
| App shows yesterday after midnight | "Today" computed once | Block A rule T4; fix-it prompt 5 |
| Content under the status bar or gesture bar | Android 15+ forces edge-to-edge | Fix-it prompt 6 |
| USB install blocked | Charge-only cable, wrong browser, or OnePlus permission monitoring | Install steps in the first section |

**OnePlus 13 settings for alarms** (recheck after the ColorOS update; system updates can reset them)

1. Long-press the Bulk Tracker icon > App info > Battery usage: **Unrestricted** (or turn on **Allow background activity**). Turn on **Auto-launch** if it is listed.
2. App info > Notifications: allow all, including **Lock screen** and **Banners** for "Meal alarms".
3. In the app, Settings > Alarms: every Permissions row green.
4. In recent apps, lock Bulk Tracker if your launcher offers a lock icon, so cleaning recents doesn't stop it.

**Fix-it prompt 1 — build errors**

```text
The build is failing. First check block E in docs/SPEC.md; if an entry matches the error, apply it exactly. Do not add features and do not change any UI. Read the full error, find the root cause, and fix only that. If it involves Room or KSP, make the KSP plugin version match the project's Kotlin version exactly and keep KSP (never kapt). Do not remove Room. Do not use fallbackToDestructiveMigration. Confirm the app builds and launches, then list the files you changed.
```

**Fix-it prompt 2 — Gemini drifted**

```text
Stop. Re-read docs/SPEC.md, especially golden rules G1–G8, the ARCHITECTURE RULES in block A, and block C. Your last change broke them: <describe what you saw>. Revert that change so it matches the spec, without touching any other file. List every file you changed.
```

**Fix-it prompt 3 — wrong meal times**

```text
MealScheduler gives <what you saw> for <the situation>, but block D2 says <the expected result>. Follow block D2 steps 1–10 literally, in that order, and use nearestFree exactly as written (earlier time wins at equal distance; touching blocks do not overlap). Do not add rules that are not in D2. Make all Ex1–Ex6 unit tests pass and Settings > Engine check show PASS for all six, then list the files you changed.
```

**Fix-it prompt 4 — repeating events on wrong days**

```text
Event "<title>" appears on <wrong dates> but should appear on <right dates>. Implement occursOn exactly as block D1: weekday bit = DayOfWeek.value - 1 (Monday = bit 0), mondayOf(day) = epochDay of that week's Monday, COUNT counted by iterating days from startDay. Add a unit test for this exact case. Change nothing else.
```

**Fix-it prompt 5 — wrong day**

```text
The app shows the wrong day. Every place that needs today's date must call logicalToday(LocalDateTime.now(), settings.dayStartHour) from util/DateUtils.kt, and MainActivity must recompute it on ON_RESUME (block A rule T4). Do not hardcode a time zone. Change nothing else.
```

**Fix-it prompt 6 — layout overlap or clipping**

```text
On the real phone, <describe: e.g. the top bar is under the status bar / the FAB is behind the gesture bar / names are cut off at font size Large>. Fix it with Scaffold innerPadding and WindowInsets, let text wrap to 2 lines then ellipsis, and use sp for all text. Change nothing else.
```

**Fix-it prompt 7 — a checklist item fails**

```text
Re-read docs/SPEC.md. Phase <N> checklist item "<paste the line>" fails: <what happens instead>. Fix only this. Tell me the cause in one sentence and list the files you changed.
```

## Sources

- [Build Android apps in Google AI Studio](https://ai.google.dev/gemini-api/docs/aistudio-android) — Kotlin/Compose stack, emulator, USB install, limitations
- [Build apps in Google AI Studio](https://ai.google.dev/gemini-api/docs/aistudio-build-mode) — Build mode, Antigravity agent, annotation mode
- [Build and publish your first Android app with AI Studio (codelab)](https://codelabs.developers.google.com/build-with-ai/build-and-publish-android-app-with-ai-studio) — Android chip, style picker, Fix errors button
- [Gemini 3.8 Flash model page](https://ai.google.dev/gemini-api/docs/models/gemini-3.8-flash)
- [Overview of AppFunctions](https://developer.android.com/ai/appfunctions) — why Gemini voice control isn't available for personal apps yet
- [OnePlus community: ADB permissions on OnePlus](https://community.oneplus.com/thread/1581789) — Disable permission monitoring
