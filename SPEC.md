# OTasks — Product and Engineering Specification

Version 1.3 of this spec · Applies to app version 1.x · Package `com.otasks.app`

OTasks is an offline Android app that helps a person eat the right amount on time, around a busy student timetable, and keep track of everything else they need to do. It combines four things that normally live in separate apps: a meal checklist with calorie and protein tracking, meal times that move around classes and events, full-screen meal alarms, and a task manager. History and a weekly weight chart show progress over time.

This document is the single source of truth for building OTasks from an empty repository. It is written for an engineer (human or Claude Code) working through it phase by phase. Every number, name, colour, duration and piece of user-facing text that matters is given exactly.

---

## 0. How to use this document

0.1 **Reading order.** Read sections 1–9 completely before writing any code. Sections 10–19 are reference material for the phase you are working on. Section 20 lists the build phases in order; build one phase at a time and do not start the next until every acceptance check of the current one passes.

0.2 **Rule IDs.** Rules have short IDs (A3, T2, D5, AL12 …) so commits, tests and PROGRESS.md can refer to them. When code implements a rule in a non-obvious way, cite the ID in a short comment.

0.3 **Keywords.** MUST / MUST NOT are hard requirements. SHOULD means "do this unless there is a concrete reason not to; record the reason in PROGRESS.md". MAY is optional.

0.4 **When something is not specified.** If a gap affects what the user sees or how their data behaves, stop and ask the project owner one short question. If it is purely internal (a private helper's name, a file split), choose the simplest option consistent with this spec and note it under "Decisions" in PROGRESS.md.

0.5 **Changing the spec.** The spec is updated before the code. A change to behaviour, data model or text is first written here (with the version line at the top bumped), then implemented.

0.6 **Companion files.** `CLAUDE.md` holds the working rules for Claude Code. `PROGRESS.md` tracks phase status, decisions, deviations and device test results. Neither is compiled into the app. `docs/SPEC-CORRECTIONS.md` records the 10 October 2026 correction decisions and regression cases; the rules here remain authoritative.

0.7 **The prototype is reference only.** The repository starts with an earlier prototype (package `com.bulktracker.app`, about 60 Kotlin files and a `docs/SPEC.md`). It is a reference for what was tried, not a starting point:
- Read it at the start of Phase 1 to learn from it (Phase 1 step 1). Useful things to look at: how alarms, the ring service and the reschedule trigger were wired, which Android 14–16 problems came up, how screens were laid out.
- **Nothing from it is kept.** No file, class, function, resource, Gradle setting, icon or line of code is copied, moved or renamed into the new project. Everything is rewritten from this spec. Where the prototype and this spec differ, this spec wins; the prototype's `docs/SPEC.md` is obsolete.
- Before deleting it, tag the last prototype commit `prototype-final` so it stays in git history for later reference (`git show prototype-final:<path>`). Then delete every prototype file in one commit ("remove prototype"), preserving the root specification and tracker, `docs/SPEC-CORRECTIONS.md`, `.git/` and the GitHub settings. The preservation tag points to the prototype commit, not to the documentation revision.
- The new app has a different package (`com.otasks.app`), so it installs as a separate app. Prototype data is not migrated.

---

## 1. Product overview

### 1.1 Who it is for

- Students who want to hit a daily calorie and protein target — whether they are gaining, losing or maintaining weight — and who keep missing meals because of classes, events and a full day.
- They use one Android phone and possibly an Android tablet. They are not expected to be technical.
- The app is for one person per device. There are no accounts and nothing is shared.

### 1.2 What it does

| Area | What the user gets |
|---|---|
| Meals | A daily checklist of meals and the foods in them, built by the user from their own food library (single foods and recipes made of several foods). Tap to check a food off; calories and protein add up against the user's own targets, which can differ by weekday. |
| Smart timing | Each meal has a timing rule (fixed, flexible, after waking, after another meal, after a workout). Meals move automatically to avoid classes and events, keep sensible gaps, and warn when something does not fit. |
| Alarms | A full-screen alarm when a meal is due (like an alarm clock), with Mark all eaten, Snooze, Skip and Open app. A follow-up notification if foods are still unchecked. |
| Schedule | A calendar for students: semesters, holidays, a weekly or alternating-week (A/B) class timetable, extra classes and personal events with repeats, shown Canvas-style as a week strip that expands into a month grid with a day list underneath. Events can be imported from an .ics calendar file (for example a Canvas calendar export). |
| Tasks (version 1.1) | A task manager with lists, smart lists, subtasks, notes, priorities, tags, repeats (from the due date or from completion), reminders (notification or full-screen alarm), "Won't do", a Trash, course-linked deadlines and a smart quick-add field. Timed tasks can also appear in the schedule's day list. Ships in 1.1, after the 1.0 release. |
| History | A month calendar of how complete each day was, streaks, monthly averages, a weekly summary table, and weekly weight tracking with a chart (weigh-ins, a smoothed trend line, the plan line and the goal) and CSV export. |
| Widget | A home-screen widget with the next meal and today's calories. |

### 1.3 Product principles

P1. **Offline and private.** No internet permission, no accounts, no analytics, no ads, no crash reporting service. Personal data stays on the device unless the user exports backup/CSV files, shares a report, or enables assistant access (13.18). The app itself makes no network requests.
P2. **Reliable alarms first.** An alarm that does not ring is the worst failure. Alarm code follows section 12 exactly, on every Android version and manufacturer the app supports.
P3. **Never lose data.** No destructive migrations, ever. Every destructive or multi-day action shows a confirmation that states exactly what will change.
P4. **Calm and fast.** Warm neutral design, one accent colour, near-instant motion (100 ms, no bounce), no spinners, smooth on 60–144 Hz displays.
P5. **Works everywhere.** Any phone or tablet on Android 12 or newer, in portrait, landscape, split-screen and free-form windows, with gesture or 3-button navigation, any font size, TalkBack, and with or without Google Play services. Manufacturer-specific fixes are allowed, but nothing may assume one manufacturer's behaviour.
P6. **The user's numbers, not ours.** The app ships with no foods, meals or targets. It never recommends a calorie target. Users type in what they want.
P7. **Not medical advice.** The app tracks what the user tells it. It shows no diet, weight or health recommendations.

### 1.4 Out of scope for version 1

Cloud sync or multi-device sync · accounts · a built-in or online food database · barcode scanning · photo logging · micronutrients, carbs and fat · kJ energy units · water tracking · wearables · an in-app voice assistant or any AI service called by the app (voice control through the phone's own assistant is planned for 1.2, 13.18) · calendar sync or subscriptions (a one-time .ics file import is in scope, 14.7) · non-student work schedules · sharing or social features · Play Store distribution (see 18.6 for what would change) · any server.

---

## 2. Decisions

Every row is a fixed decision. The sections that follow implement them.

| Topic | Decision |
|---|---|
| App name / package | OTasks / `com.otasks.app` (never changes after the first release) |
| Audience | Any nutrition goal: Gain, Lose or Maintain weight, chosen in setup and Settings |
| Targets | Daily calories and protein typed in by the user (both optional). No calculator, no recommendations |
| Starting content | Completely blank: no foods, meals, classes, events, lists or targets |
| Foods | Manual entry into a personal food library; foods are reused across meals. Fully offline |
| Recipes | A recipe is a library food made of other foods (no nesting) that makes N servings; its calories and protein per serving are computed from its ingredients and kept up to date |
| Weekday targets | Optional: different calorie and protein targets on chosen weekdays (off by default); every other day uses the everyday targets |
| Units | Body weight in kg or lb (chosen in setup, default from locale). Energy in kcal, protein in grams |
| Schedule | Students: semesters, holidays, weekly class timetable, extra classes, personal events |
| Meal timing | Per-meal timing rules (fixed, flexible, after waking, after another meal, after workout) plus an optional recurring workout; meals dodge classes and events and keep minimum gaps |
| Meal alarms | Full-screen alarm via `setAlarmClock`; sound + vibration normally; vibration only on vibrate, silent, Do Not Disturb or during a call; volume follows the phone's alarm volume by default, or a fixed percentage |
| Alarm buttons | Mark all eaten, Snooze, Skip this meal today, Open app |
| Snooze | 10 min, at most 3 times, then "Missed" notification. A snooze that will ring is scheduled with `setAlarmClock` like the alarm itself |
| Follow-up | One notification 45 min after the meal alarm if foods are still unchecked |
| Alarm screen | Meal name large, one summary line ("7 items · 630 kcal"), four buttons that always sit fully above the navigation bar |
| Task reminders | Up to 3 per task, each a notification or a full-screen alarm |
| Edit scopes | Changing which foods/meals appear asks: Just this day, Every <weekday> from now, From now on, or a date range; every scope change is confirmed |
| Bare-minimum day | Per-day toggle: only starred foods show; the day's targets become their totals |
| Class weeks | A class session repeats every week or every 2, 3 or 4 weeks (A/B weeks), counted from the semester's first week |
| Calendar import | One-time import of an .ics file chosen by the user (events in 1.0; deadlines as tasks in 1.1). No subscriptions, no network |
| Schedule tab | Two segments: Calendar and Semester. Calendar = week strip with coloured dots, expands to a month grid; tapping a day lists its classes, events and (optionally) timed tasks in time order |
| Colours for classes/events | Each course and event has a colour from an 8-colour palette, auto-assigned and editable |
| Editing repeating items | "Only this date" or "All" |
| Weight tracking | Weekly weigh-in day and time chosen by the user (default Sunday), optional reminder; log any day; chart with ranges 1M/3M/6M/1Y/All: weigh-ins, a smoothed trend line (exponential moving average, 10% per day by default), a plan line from the weekly-change goal and the goal line; weights export as CSV |
| Task extras (1.1) | Repeat from due date (default) or from completion date; "Won't do"; deleted tasks go to a Trash kept 30 days (adjustable); a task can be linked to a course as a deadline |
| Restart before unlock | Ringing alarms for the next 48 hours are kept in a small boot snapshot (times and kinds only) so they ring even if the phone restarts overnight and is not unlocked (AL3) |
| Engine check | Hidden screen in Settings › About that runs the timing and parser examples and shows pass/fail |
| Look | Warm neutral "paper and ink" theme with serif titles, one clay accent; optional "Use wallpaper colours" (Material You) |
| Customisation | Everything that is a matter of taste is a preference. Settings has a search bar and a **Preferences** hub with five pages: Alarms & sound, Notifications & reminders, Display & motion, Calendar & time, Behaviour. Each page has "Reset to defaults"; the hub has "Reset all preferences" (never touches user data) |
| Alarm behaviour per phone mode | Separate choice for Normal, Vibrate, Silent, Do Not Disturb and During a call: Sound + vibrate / Sound only / Vibrate only / Screen only. Defaults: Normal = Sound + vibrate, all others = Vibrate only (calls can never play sound) |
| Alarm profiles | One default alarm profile (sound, volume, gradual volume, vibration pattern, ring length, snooze, mode behaviour); meal, task and weigh-in alarms each "Use default" or have their own |
| Theme | Follows system by default; Light / Dark override |
| Licence | GPL-3.0-or-later for the app's code; bundled fonts under the SIL Open Font Licence |
| Motion | 100 ms, no springs, no bounce; Animations preference: Normal / Reduced / Off |
| Refresh rate | Preference: Highest available (default), Standard (60 Hz) or Let the system decide; "Keep highest in battery saver" switch (off by default) |
| Navigation | 1.0: 4 destinations — Today, Schedule, History, Settings. 1.1 adds Tasks as the second item (5). Bottom bar on compact width, navigation rail on medium and expanded width |
| Tablets | Fully adaptive: rail, two panes where useful, all orientations, split-screen, free-form windows |
| Min Android | Android 12 (API 31). Target = latest stable API |
| Releases | 1.0 = meals, smart timing, alarms, schedule, history, weight, widget, settings and backup. 1.1 = the Tasks tab. Every task table and column in section 7.4 is created in 1.0, so 1.1 normally needs no migration; if 1.1 does change them, it adds a normal migration (D2). The Tasks destination, the "Task" option in the schedule's "+" menu, "Show tasks" and the Parser check are hidden until 1.1 |
| Review | Each phase is built with Claude Sonnet 5.5 and reviewed against this spec by a fresh Claude Opus session before its alpha is released (20.0) |
| Alpha testers | The owner plus a few friends; bugs are reported through GitHub Issues with a template; Settings shows an "Alpha" banner with a "Report a bug" link |
| Two devices | Phone and tablet keep separate data (no sync); alarms ring on every device that has them enabled. Backups move a plan between devices |
| Distribution | Signed APKs on GitHub Releases, sideloaded. Every finished phase is published as an Alpha pre-release so the owner can install and test it on real devices |
| Backup | Manual export/import of one JSON file; optional weekly auto-backup to a folder the user picks. Moving to a new phone = export, install the APK there, "Restore from a backup" in setup. Android cloud backup and Android phone-to-phone transfer are both off (B6) |
| Fonts | Lora (titles, never below 18sp) and Inter (everything else), bundled in the APK (no Google Play services needed) |
| Language | English at launch; every string translatable; RTL-safe |
| Haptics | Only when an item or task becomes checked; can be turned off |
| Widget | Next meal and today's calories (Jetpack Glance), resizable |
| Voice control (1.2) | The phone's assistant (e.g. Gemini) can run a small set of safe, single-day OTasks actions through Android AppFunctions, on the device; off by default; built only once AppFunctions is open to all developers and works on the owner's phone (13.18) |

---

## 3. Platform and device compatibility

### 3.1 Android versions

PL1. `minSdk = 31` (Android 12). `targetSdk` and `compileSdk` = the latest stable API level when the project is created (36 or newer). Raising targetSdk later is a planned task: re-read section 19 for the new version's behaviour changes first.
PL2. Test the alarm and permission flows on Android 12, 13, 14, 15 and 16 (emulators are fine for 12–14; real devices for the newest two, see 19.4).

### 3.2 Devices and form factors

PL3. Phones, foldables (folded and unfolded), tablets (7"–14"), Chromebooks running Android apps (best effort) and free-form desktop windows (best effort).
PL4. **No orientation lock.** Do not set `android:screenOrientation` on any activity, and keep `android:resizeableActivity` at its default (true). Android 16 ignores orientation and resizability restrictions on large screens for apps targeting API 36 anyway.
PL5. The UI adapts to window size, not device type (section 13.12). Never branch on "is tablet"; branch on `WindowSizeClass`.
PL6. All state survives configuration changes (rotation, fold, window resize, theme or font change, language change): UI state lives in ViewModels or `rememberSaveable`.
PL7. Multi-window and split-screen MUST work. Resuming from another window recomputes the logical day (T4) like any other resume.
PL8. Keyboard, mouse and trackpad: every interactive element is reachable with Tab and arrow keys, has a visible focus indicator, and responds to Enter/Space. Esc closes sheets and dialogs. Mouse hover shows the same pressed-state tint at 8% alpha. Right-click on a row opens the same menu as long-press. Scroll wheel scrolls lists and pagers.
PL9. Hardware differences: many tablets have no vibrator, no telephony and no default alarm sound. Every use of these MUST check availability first (`Vibrator.hasVibrator()`, `PackageManager.FEATURE_TELEPHONY`, null ringtone URIs) and degrade gracefully. Declare no `<uses-feature>` as required.

### 3.3 Manufacturers

PL10. The app MUST work on stock Android (Pixel) and on every major manufacturer skin: Samsung One UI, OnePlus OxygenOS / OPPO ColorOS / realme UI, Xiaomi HyperOS/MIUI (incl. Redmi, POCO), vivo Funtouch/OriginOS (incl. iQOO), Motorola, Nothing, Huawei/Honor without Google services, Lenovo, Asus.
PL11. Manufacturer-specific behaviour is isolated in one place: `platform/DeviceQuirks.kt`, keyed on `Build.MANUFACTURER` (lowercase) and, where needed, `Build.BRAND`. It provides the battery-settings help text and the best settings intent for that manufacturer (AL31). No other file may check the manufacturer.
PL12. No feature may depend on Google Play services. (Fonts are bundled, there is no sign-in, no Play APIs.)

### 3.4 Display

PL13. Highest refresh rate: MainActivity calls `applyBestRefreshRate()` in `onCreate`, on every `ON_RESUME`, on display changes (`DisplayManager.DisplayListener.onDisplayChanged` for the current display) and when `PowerManager.ACTION_POWER_SAVE_MODE_CHANGED` is received (receiver registered in `onStart` with `ContextCompat.RECEIVER_NOT_EXPORTED`, unregistered in `onStop`). It:
  1. Reads `settings.refreshRateMode`. SYSTEM → `preferredDisplayModeId = 0` (no preference) and return. If `PowerManager.isPowerSaveMode` is true and `refreshHighestInBatterySaver` is false → `preferredDisplayModeId = 0` and return.
  2. STANDARD → the supported mode with the same `physicalWidth`/`physicalHeight` whose `refreshRate` is closest to 60 (ties: the lower one). HIGHEST → if `Build.VERSION.SDK_INT >= 36` and the display reports adaptive refresh (`Display.hasArrSupport()`, added in Android 16 — check the name against the current SDK; LTPO panels that drop to 1–10 Hz when nothing moves), set `preferredDisplayModeId = 0` and return: pinning a mode would stop the panel idling down while OTasks is on screen and cost battery, and the system already raises the rate for touch, scrolling and animation. Otherwise take `display.mode` and pick, from `display.supportedModes`, the mode with the same `physicalWidth` and `physicalHeight` and the highest `refreshRate`, and set `preferredDisplayModeId` to its `modeId`. It never changes resolution. (On adaptive panels "Show refresh rate" in Developer options shows a lower rate while the screen is still; that is expected.)
  3. Wraps everything in try/catch; a missing display or exception is ignored. It also re-runs when either refresh preference changes.
  The system and the user's display settings still have the final say (for example a 60 Hz "standard" setting wins); that is expected.
PL14. Edge-to-edge everywhere (Android 15+ enforces it). Status and navigation bars are transparent; content handles insets (section 13.11).

---

## 4. Technology stack and project structure

### 4.1 Stack

Use the latest stable versions available when the project is created, recorded in `gradle/libs.versions.toml`. Do not add libraries that are not listed here without updating this section first.

| Purpose | Library / tool |
|---|---|
| Language | Kotlin (latest stable), JVM target 17 |
| Build | Android Gradle Plugin, Gradle wrapper, version catalog, Kotlin DSL |
| UI | Jetpack Compose (BOM), Material 3, `material3-adaptive` + `material3-adaptive-navigation-suite` + `adaptive-layout`/`adaptive-navigation` (window size classes, list-detail panes), `material-icons-extended` |
| Navigation | Navigation Compose (type-safe routes with Kotlin serialization) |
| Lifecycle | `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose` |
| Database | Room (KSP; never kapt), framework SQLite driver (no bundled native SQLite, so no 16 KB page-size concerns) |
| Concurrency | Kotlin coroutines and Flow |
| Serialization | `kotlinx-serialization-json` (navigation routes and backups) |
| Files | `androidx.documentfile` (auto-backup folder) |
| Widget | Jetpack Glance (`glance-appwidget`, `glance-material3`) |
| Voice control | `androidx.appfunctions` (Phase 14 only, 13.18) |
| Startup | `androidx.profileinstaller`; Baseline Profile Gradle plugin and Macrobenchmark (in `:baselineprofile` only) |
| Dates | `java.time` only (never `java.util.Date` or `Calendar`) |
| Fonts | Lora and Inter as bundled font resources (OFL licence files included in `app/src/main/assets/licenses/`) |
| Licence | App code GPL-3.0-or-later. Every added dependency must be GPL-3.0-compatible (Apache-2.0, MIT, BSD and OFL are fine); check before adding |
| Tests | JUnit 4, `kotlinx-coroutines-test`, Turbine, Robolectric, Compose UI test, Room testing (`MigrationTestHelper`), Roborazzi (screenshot tests) |
| Static checks | Android Lint (warnings as errors for the rules in 17.5), ktlint or Spotless with ktlint |

**Forbidden:** Hilt, Dagger, Koin (manual DI is enough), DataStore and SharedPreferences (settings live in Room so they are backed up with everything else), WorkManager (exact alarms handle scheduling), any chart library (charts are drawn with Compose Canvas), image-loading libraries, Firebase, Google Play services, analytics, crash reporters, ads, any networking library, the `INTERNET` permission.

### 4.2 Modules

| Module | Type | Contents |
|---|---|---|
| `:domain` | Pure Kotlin/JVM library | Timing engine, recurrence, visibility, totals, goal maths, task rules, quick-add parser, engine-check fixtures. No Android imports. Fast JVM tests. |
| `:app` | Android application | Everything else. Depends on `:domain`. |
| `:baselineprofile` | Android test module (Baseline Profile Gradle plugin + Macrobenchmark) | Generates the baseline profile for `:app` (15.6). Not shipped. |

Domain types that mirror database rows are plain data classes in `:domain` (`MealSection`, `MealItem`, `BusyBlock`, …). The `:app` data layer maps Room entities to them. This keeps the engine testable without Android.

### 4.3 Package layout (`com.otasks.app`)

```
app/
  OTasksApp.kt               Application: creates AppContainer and channels; when the user is unlocked, starts the
                             background volume-restore (AL21) and the A11 trigger
  AppContainer.kt            Manual DI
  MainActivity.kt            Single activity: edge-to-edge, refresh rate, nav host, intent handling
  data/db/                   Entities.kt, Daos.kt, AppDatabase.kt, Migrations.kt, Converters.kt
  data/prefs/                PreferenceStore (key/value rows <-> typed Settings), PreferenceCodec
  data/repo/                 SettingsRepository, FoodRepository, MealRepository, LogRepository,
                             ScheduleRepository, TaskRepository, WeightRepository, BackupRepository,
                             DiagnosticsRepository
  data/mapper/               Entity <-> domain mappers
  alarm/                     AlarmScheduler, AlarmReceiver, AlarmRingService, AlarmActivity,
                             BootReceiver, NotificationHelper, AlarmSound, AlarmIntents (12.3), BootSnapshot (AL3)
  platform/                  DeviceQuirks, PermissionState, RefreshRate, Haptics
  backup/                    BackupDtos, BackupCodec, AutoBackup, WeightCsv, IcsImport (app side: file reading, preview, writing)
  widget/                    NextMealWidget, NextMealWidgetReceiver
  ui/theme/                  Color, Theme, Type, Tokens, Motion, Fonts
  ui/components/             Shared composables, one per file
  ui/navigation/             Routes, AppNavHost, AdaptiveNavigation (bottom bar + rail)
  ui/setup/ ui/today/ ui/food/ ui/schedule/ ui/tasks/ ui/history/ ui/settings/
                             One XScreen.kt + XViewModel.kt per screen, plus that feature's sheets
  util/                      DateUtils, Format, Validation
domain/
  time/                      LogicalDay, Mold
  meals/                     VisibilityResolver, Totals, Goals, DayTargets, Recipes, Scopes, Completion
  schedule/                  Recurrence, BusyBlocks, ClassWeeks
  ics/                       IcsParser (RFC 5545 subset, 14.7), IcsMapper
  weight/                    WeightSeries, WeightTrend (W3, W8)
  planner/                   DayPlanner, MealScheduler, DayPlan
  tasks/                     TaskRules, QuickAddParser
  check/                     EngineCheck (fixtures + expected results shared with unit tests)
```

### 4.4 Build configuration

B1. `namespace` and `applicationId` = `com.otasks.app`. Debug builds use `applicationIdSuffix = ".debug"` and app name "OTasks Debug" so a debug and a release build can be installed side by side.
B2. **Versioning.** `versionName` is semantic: Phases 1–9 use `0.<phase>.0-alpha.<n>`; Phase 10 uses `1.0.0-alpha.<n>` then `1.0.0`; Phases 11–13 share `1.1.0-alpha.<n>` with n continuing across those phases, then `1.1.0`; Phase 14 uses `1.2.0-alpha.<n>` then `1.2.0`. Alpha n is 1–98; a final uses sequence 99. `versionCode = MAJOR*10000000 + MINOR*100000 + PATCH*100 + sequence`; supported components: MAJOR 0–199, MINOR 0–99, PATCH 0–999. Reject out-of-range or noncanonical versions and codes above 2,100,000,000. Examples: `0.3.0-alpha.2` → 300002; `1.0.0` → 10000099; `1.0.10` → 10001099; `1.1.0` → 10100099. Define `otasks.versionName` once in `gradle.properties`; Gradle derives the code. A release must also exceed every previously published versionCode; semantic order alone does not allow a backported release to downgrade an installed app. Unit tests cover all phase transitions, alpha-to-final, patch 999, invalid components, and the former 1.0.10/1.1.0 collision.
B3. Release builds: `isMinifyEnabled = true`, `isShrinkResources = true`, `proguard-android-optimize.txt` plus `proguard-rules.pro`. Keep rules are added only when a test or a release smoke test proves they are needed, each with a comment saying why.
B4. Release signing reads the keystore path and passwords from environment variables / Gradle properties that are never committed (section 18). The same keystore MUST be used for every release forever, or users cannot update.
B5. Room: `exportSchema = true`, schemas written to `app/schemas/` and committed. Database file name `otasks.db`.
B6. **No Android backup or transfer.** `android:allowBackup="false"` and `android:dataExtractionRules="@xml/data_extraction_rules"` excluding every domain (`root`, `file`, `database`, `sharedpref`, `external`, `device_root`, `device_file`, `device_database`, `device_sharedpref`) under both `<cloud-backup>` and `<device-transfer>`. Reasons: a restored database would skip the import checks of 14.4; a phone-to-phone transfer of a sideloaded app is unreliable (setup tools mostly restore Play-installed apps) and could hand an older app a newer database, which Room cannot downgrade; and device-specific values (folder permission, saved alarm volume, boot snapshot) must not move. The supported way to move is the JSON backup, offered in setup (13.14).
B7. Lint: `abortOnError = true` with a baseline only for third-party issues; `ExactAlarm`, `FullScreenIntentPolicy`, `ProtectedPermissions` warnings are suppressed on the specific `<uses-permission>` lines with `tools:ignore`, never globally.

---

## 5. Architecture rules

A1. **Manual DI.** `AppContainer` is created in `OTasksApp.onCreate` and owns the database, every repository, the `DayPlanner`, `AlarmScheduler`, the app-wide `CoroutineScope(SupervisorJob() + Dispatchers.Default)` and the `SelectedDate` holder. Activities, receivers, services and the widget reach it with `(context.applicationContext as OTasksApp).container`. Each ViewModel has a `Factory` in its companion object that takes what it needs from the container.
A2. **Data flow.** DAOs expose `Flow` for reads and `suspend` functions for writes. Only repositories call DAOs. Repositories map entities to domain models and run writes on `Dispatchers.IO`. Multi-step writes are single `@Transaction` methods.
A3. **One state per screen.** Each ViewModel exposes exactly one `StateFlow<XUiState>` built with `combine(...)` → `map` (on `Dispatchers.Default`) → `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`. Screens read it with `collectAsStateWithLifecycle()`. User actions are ViewModel functions; one-off effects (snackbars, navigation) go through a `Channel` collected in a `LaunchedEffect`.
A4. **UI state is ready to draw.** All sorting, filtering, totals, formatting of numbers, dates and times, and grouping happen in the ViewModel's mapping, never in composables (section 15).
A5. **Immutable UI models.** UI state classes are `@Immutable` data classes holding only read-only `List`s and primitives or other immutable classes.
A6. **Selected date.** `SelectedDate` (a `MutableStateFlow<LocalDate>` in AppContainer) holds which day Today shows. History, Schedule, the widget, alarms and notifications set it before navigating to Today.
A7. **Navigation.** Type-safe routes (`@Serializable` objects/classes): `Today`, `Tasks`, `Schedule`, `History`, `Settings`, `Setup`, `TaskDetail(taskId: Long)`, `FoodLibrary`, `RecipeEditor(foodId: Long?)`, `MealEditor`, `EngineCheck`, `Diagnostics`, `Licenses`. The navigation bar/rail is hidden on `Setup` and, on compact width, on `TaskDetail`, `FoodLibrary`, `RecipeEditor`, `MealEditor`, `EngineCheck`, `Diagnostics` and `Licenses`.
A8. **Errors.** Repository writes that can fail for user-caused reasons (validation, file access) return a sealed `Result`-like type; the ViewModel turns failures into a snackbar with a specific message. Unexpected exceptions are not swallowed silently except where a rule says so (for example PL13 and AL-rules marked "never crash").
A9. **No main-thread I/O.** No database, file or `ContentResolver` call on the main thread. StrictMode is enabled in debug builds with `detectAll().penaltyLog()` for the thread policy and VM policy.
A10. **Receivers.** BroadcastReceivers call `goAsync()`, do their work in the container scope on `Dispatchers.IO`, and call `pendingResult.finish()` in a `finally` block. Work stays under 10 seconds. Inside receivers read data with suspend functions or `flow.first()`, never by collecting.
A11. **Reschedule trigger.** AppContainer starts one coroutine — only once the user is unlocked (`UserManager.isUserUnlocked()` at app start, otherwise on the first `Intent.ACTION_USER_UNLOCKED`, registered dynamically; never in direct boot, AL3) — that merges (a) `database.invalidationTracker.createFlow(*ALARM_TABLES)` and (b) the `Settings` flow mapped to the alarm- and widget-relevant keys only (`distinctUntilChanged`), with `debounce(500)`, and then calls `AlarmScheduler.rescheduleAll()` and updates the widget. So writing `lastDestination` or `lastExportMillis` does not reschedule. `ALARM_TABLES` = every table whose change can affect an alarm or the widget: alarm_profiles, weekday_goals, sections, meal_items, foods, day_overrides, log_entries, day_states, semesters, holidays, courses, class_sessions, class_exceptions, events, event_exceptions, tasks, task_reminders, weights.

---

## 6. Time, number and unit rules

### 6.1 Time

T1. **Logical day.** A day is a `LocalDate`, stored as `epochDay` (Long). A logical day starts at `settings.dayStartHour` (default 4, range 0–6), so 1:30 AM still belongs to the previous day. `logicalToday(now: LocalDateTime, dayStartHour: Int): LocalDate = if (now.hour < dayStartHour) now.toLocalDate().minusDays(1) else now.toLocalDate()`.
T2. **Mold.** A time inside a logical day is an Int "minute of logical day" (mold): minutes after 00:00 of that date. Times after midnight continue past 1440 (12:30 AM the next calendar morning = 1470). Valid range: `dayStartHour*60` to `1440 + dayStartHour*60 - 1` (240..1679 with the default). Meal times, class and event times use mold.
T3. **Task times are different.** A task's `dueMold` is minutes after midnight of its calendar date `dueDay` (0..1439), not the extended range.
T4. **Day rollover.** MainActivity recomputes `logicalToday` on every `ON_RESUME`, and a ticker in AppContainer re-emits it at the next day boundary while the app is open. If Today was showing the old today, it switches to the new today.
T5. **Time zone.** Always `ZoneId.systemDefault()`, read fresh each time (never cached at startup). Never hard-code a zone. `TIMEZONE_CHANGED` and `TIME_SET` trigger a reschedule (AL5).
T6. **Date pickers.** Material 3 `DatePicker` works in UTC: set `initialSelectedDateMillis = localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()` and read results with `Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()`. Never use the system zone for picker values.
T7. **Time pickers.** Material 3 `TimePicker` inside an `AlertDialog` with Cancel / OK (`ui/components/TimePickerDialog.kt`). `is24Hour` = true for `timeFormat` H24, false for H12, `DateFormat.is24HourFormat(context)` for SYSTEM. Converting a picked hour/minute to mold: `mold = hour*60 + minute; if (hour < dayStartHour) mold += 1440`. Converting a mold back to the picker's initial hour: `(mold % 1440) / 60`, minute `mold % 60` (passing a value ≥ 24 crashes the picker).
T8. **Displaying times.** `Format.time(mold)` uses `settings.timeFormat` (SYSTEM = the system's 12/24-hour setting) and the current locale (`DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)`), e.g. "9:30 AM" or "09:30". Ranges use an en dash: "6:00–7:30 PM" in 12-hour (drop the first AM/PM when both match), "18:00–19:30" in 24-hour.
T9. **Displaying dates.** Locale-aware patterns via `DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton))`: short "Mon, 6 Oct" (skeleton `EEEMMMd`), medium "6 Oct 2026" (`dMMMy`), day only "6 Oct" (`dMMM`). Relative labels "Today", "Yesterday", "Tomorrow" come from string resources. When `dateFormat` is not LOCALE, numeric dates use that order with the locale's separator; formats with a month name keep the locale pattern.

T10. **Local date-times to instants.** Use `LocalDateTime.atZone(currentZone)` consistently for meal and task alarm triggers and timed overdue comparisons: a DST gap moves forward by the gap; an overlap uses the earlier offset. Inject the zone/Clock in tests. Recompute future alarms on zone/time changes; stored calendar due dates are not rewritten.

### 6.2 Numbers

N1. kcal is `Int?`; protein grams is `Double?`. `null` means "I don't know" and is shown as "—".
N2. A food stores kcal and protein either per serving (`basis = SERVING`) or per 100 g (`PER_100G`). A meal item or custom entry has an amount: `servings` for SERVING foods, `grams` for PER_100G foods. Amount factor = `servings` or `grams / 100`. Item kcal = `(food.kcal * factor).roundToInt()`, protein = `food.proteinG * factor`. The portion text is "2 × 1 medium (118 g)" for servings and "150 g" for grams.
N3. A log entry stores snapshots for one serving-set and a `multiplier` (default 1.0). Effective kcal = `(kcalSnapshot * multiplier).roundToInt()`; effective protein = `proteinSnapshot * multiplier`.
N4. Day totals sum effective values that are not null and count entries whose kcal is null or whose `partialSnapshot` is true (`unknownCount`).
N5. Formatting uses the current locale (`NumberFormat.getIntegerInstance(locale)` for kcal: "2,500" / "2.500" / "2 500"). Protein shows no decimals when whole, else one decimal. Weight shows one decimal.
N6. Number fields use `KeyboardType.Decimal` and accept both "." and "," as the decimal separator regardless of locale.
N7. Use `kotlin.math.roundToInt()` wherever a Double becomes an Int.

### 6.3 Units

U1. Body weight is stored in kilograms (Double). `settings.weightUnit` = `"KG"` or `"LB"` controls display and input; 1 lb = 0.45359237 kg. Inputs in lb are converted to kg before saving, rounded to 0.01 kg; displays convert back and round to 0.1.
U2. The default weight unit is `"LB"` when the device locale's country is US, LR or MM, otherwise `"KG"`. The user can change it in setup and Settings; changing it never changes stored values.
U3. Energy is always kcal and protein always grams in version 1.

---

## 7. Data model

Room, database version 1 at the first release. **Every table in this section is created in Phase 1** (including those first used by later phases), so later phases add no tables before 1.0. Enums are stored as `String` columns with exactly the values listed. Weekday masks: bit 0 = Monday … bit 6 = Sunday (bit = `DayOfWeek.value - 1`); 127 = every day. All ids are `Long` auto-generated unless stated. Foreign keys are declared with Room `@ForeignKey`; every foreign-key column has an index.

### 7.1 Settings

**`preferences`** — key/value rows: `key` String PK, `value` String. Values are stored as text: Int/Long as decimal digits, Double with "." as the decimal separator, Boolean "true"/"false", String as is, and an explicit null as the text `null` (so a nullable key whose default is not null — e.g. `starredNudgeMold`, default 6:00 PM, null = off — can be turned off). **Only a missing row means "use the default".** `data/prefs/PreferenceStore` reads every row as one `Flow` and maps it to the immutable `Settings` data class (in `:domain`, one property per key below), filling the 7.1 default for any key that is missing or whose value does not parse or is outside its allowed values (it logs the key only, never the value). Tests: a nullable key set to null stays null after a restart; a missing key reads its default; a garbled value reads its default. Writing one preference upserts one row in a transaction; resetting deletes the rows of the keys being reset. The repository emits `Settings()` (all defaults) before the first read completes, so the UI never waits.

Why key/value: a new preference added later needs only a new `Settings` property and a `PreferenceRegistry` entry (13.13.5), never a schema migration. Unknown keys found in the table (from a newer app version) are kept untouched.

The keys, their types and defaults (the `Settings` property names are the keys):

| Key | Type | Default | Notes |
|---|---|---|---|
| onboardingDone | Boolean | false | |
| goalType | String | "MAINTAIN" | "GAIN" \| "LOSE" \| "MAINTAIN" |
| kcalTarget | Int? | null | 500–10000 |
| proteinTargetG | Double? | null | 10–500 |
| goalWeightKg | Double? | null | 20–300 kg |
| weeklyChangeKg | Double? | null | 0.05–1.5; direction comes from goalType; ignored for MAINTAIN |
| planStartDay | Long? | null | epochDay the plan line starts (G4); set automatically, not shown as a preference |
| historyStartDay | Long? | null | internal, exported; set to logical today when setup finishes; never changed by preference reset (H1) |
| weekdayGoalsEnabled | Boolean | false | "Different targets on some days" (G0); the per-weekday values live in `weekday_goals` |
| weightUnit | String | locale (U2) | "KG" \| "LB" |
| dayStartHour | Int | 4 | 0–6 |
| wakeMold | Int | 480 | wake time on days without classes (8:00 AM) |
| wakeBeforeFirstClassMin | Int? | null | null = always use wakeMold; else 15–240 |
| minGapBigMin | Int | 150 | 30–360 |
| minGapSnackMin | Int | 90 | 15–240 |
| maxFixedShiftMin | Int | 120 | 0–240 |
| latestMealMold | Int | 1440 | latest automatic meal START (12:00 AM); its full duration must still fit inside the logical day (9.4) |
| workoutEnabled | Boolean | false | |
| workoutDaysMask | Int | 0 | usual workout weekdays |
| workoutTiming | String | "AFTER_MEAL" | "AFTER_MEAL" \| "FIXED" |
| workoutAnchorSectionId | Long? | null | AFTER_MEAL: the meal the workout follows |
| workoutOffsetMin | Int | 60 | AFTER_MEAL: minutes after that meal starts |
| workoutStartMold | Int | 1080 | FIXED: start time (6:00 PM) |
| workoutDurationMin | Int | 60 | 15–300 |
| *Preferences › Notifications & reminders* | | | |
| mealAlarmsEnabled | Boolean | false | master switch for meal alarms |
| followUpMin | Int | 45 | 0 (off) \| 15 \| 30 \| 45 \| 60 \| 90 |
| missedNotification | Boolean | true | post "Missed" when an alarm times out |
| weighInEnabled | Boolean | false | weigh-in reminder |
| weighInWeekday | Int | 7 | 1 = Monday … 7 = Sunday |
| weighInMold | Int | 480 | |
| weighInStyle | String | "NOTIFICATION" | "NOTIFICATION" \| "ALARM" |
| defaultTaskReminderStyle | String | "NOTIFICATION" | style preselected when adding a task reminder |
| starredNudgeMold | Int? | 1080 | time after which Today warns about unchecked starred items (6:00 PM); null = off |
| stopAlarmsAfterTarget | Boolean | false | skip remaining meal alarms once the day's calorie target is reached |
| dailySummaryEnabled | Boolean | false | nightly summary notification |
| dailySummaryMold | Int | 1320 | 10:00 PM |
| upcomingNoticeMin | Int | 0 | "Upcoming alarm" notice before each meal alarm (AL6b): 0 (off) \| 15 \| 30 \| 60 \| 120. Clock apps show this 2 h ahead for one wake-up alarm; with several meal alarms a day that would keep a notification up all day, so it is off by default |
| dayOverviewEnabled | Boolean | false | morning overview of the day's alarms (AL6c) |
| dayOverviewMold | Int? | null | time of the morning overview; null = at the planned wake time |
| *Preferences › Alarms & sound (general)* | | | |
| alarmDetailsOnLockScreen | Boolean | true | show the meal or task name and summary on the alarm screen while the phone is locked (clock apps show the alarm label); off = "Meal time" / "Task reminder" / "Weigh-in" until unlocked (AL35) |
| ringBeforeUnlock | Boolean | true | keep the boot snapshot so alarms ring after a restart before the first unlock (AL3) |
| *Preferences › Display & motion* | | | |
| themeMode | String | "SYSTEM" | "SYSTEM" \| "LIGHT" \| "DARK" |
| dynamicColor | Boolean | false | "Use wallpaper colours" (overrides accentKey) |
| accentKey | String | "clay" | accent palette key (13.4) |
| refreshRateMode | String | "HIGHEST" | "HIGHEST" \| "STANDARD" \| "SYSTEM" (PL13) |
| refreshHighestInBatterySaver | Boolean | false | |
| animations | String | "NORMAL" | "NORMAL" \| "REDUCED" \| "OFF" (13.5) |
| haptics | Boolean | true | |
| appTextScale | Int | 100 | 90–130 step 5; multiplies the system font scale inside the app |
| density | String | "COMFORTABLE" | "COMFORTABLE" \| "COMPACT" (13.6) |
| todayView | String | "CARDS" | "CARDS" \| "TIMELINE" |
| showWeightTrend | Boolean | true | trend line on the weight chart (W8) |
| trendSmoothingPct | Int | 10 | 5 \| 10 \| 20 — how fast the trend follows new weigh-ins (W8; 10% per day is the common "Hacker's Diet" default) |
| showPlanLine | Boolean | true | plan line from weeklyChangeKg (G4) |
| *Preferences › Calendar & time* | | | |
| timeFormat | String | "SYSTEM" | "SYSTEM" \| "H12" \| "H24" (T7, T8) |
| firstDayOfWeek | String | "LOCALE" | "LOCALE" \| "MONDAY" \| "SUNDAY" \| "SATURDAY" |
| dateFormat | String | "LOCALE" | "LOCALE" \| "DMY" \| "MDY" \| "YMD"; display order of numeric dates and the quick-add numeric date order (LOCALE → from the locale's short date pattern) |
| startDestination | String | "TODAY" | "TODAY" \| "TASKS" \| "SCHEDULE" \| "HISTORY" \| "LAST" (the last one used) |
| showTasksInCalendar | Boolean | true | |
| markerHighPct | Int | 90 | completion % for the "complete" marker, 50–100 |
| markerMidPct | Int | 60 | completion % for the "partly" marker, 10 to markerHighPct − 5 |
| *Preferences › Behaviour* | | | |
| confirmSingleDay | Boolean | true | confirm reversible single-day actions (C-CONFIRM) |
| autoCollapseDoneMeals | Boolean | true | |
| showCalories | Boolean | true | hide calories everywhere when false (rows, totals, widget, alarm summary) |
| showProtein | Boolean | true | |
| showNextMealCard | Boolean | true | |
| undoSeconds | Int | 10 | 4 \| 10 \| 20 |
| trashDays | Int | 30 | 1.1: days a deleted task stays in the Trash before it is removed for good: 7 \| 30 \| 90 \| 0 (never removed automatically) |
| voiceControlEnabled | Boolean | false | 1.2: let the phone's assistant run OTasks actions (13.18 VC7); off by default because spoken requests leave the device through the assistant |
| voiceAllowChanges | Boolean | true | 1.2: off = the assistant can only ask questions (VC7) |
| defaultRepeatFrom | String | "DUE_DATE" | 1.1: "DUE_DATE" \| "COMPLETION", preselected when a repeat is turned on (TK2a) |
| showWontDo | Boolean | true | 1.1: show "Won't do" tasks in Completed lists |
| quickAddSmartRecognition | Boolean | true | 1.1: parse dates, times, priority, #tags, ~lists and @courses in quick add (10.2); off = the text is the title |
| quickAddSuggestions | Boolean | true | 1.1: typing `*`, `~`, `#` or `@` in quick add shows date, list, tag or course suggestions (13.9.5) |
| *Backup, device-local and internal* (rows marked device-local are never exported; an import keeps this device's values) | | | |
| lastExportMillis | Long? | null | internal |
| autoBackupEnabled | Boolean | false | exported; after importing on another phone it stays on without a folder, so 14.3's "choose the folder again" snackbar appears |
| autoBackupTreeUri | String? | null | device-local, never exported |
| lastAutoBackupMillis | Long? | null | device-local, never exported |
| batteryHelpDismissed | Boolean | false | device-local |
| lastDestination | String | "TODAY" | for startDestination LAST; device-local |

**`alarm_profiles`** — exactly four rows, primary key `kind` (String): "DEFAULT", "MEAL", "TASK", "WEIGH_IN". Created with these defaults the first time the database opens (they are defaults, not seed content). MEAL, TASK and WEIGH_IN start with `useDefault = true`.

| Field | Type | Default | Notes |
|---|---|---|---|
| kind | String PK | | |
| useDefault | Boolean | true | ignored for DEFAULT; true = every other field is taken from DEFAULT |
| soundUri | String? | null | null = system default alarm sound; "SILENT" = no sound |
| volumeMode | String | "SYSTEM" | "SYSTEM" (phone's alarm volume) \| "FIXED" |
| volumePercent | Int | 70 | FIXED: percent of the alarm stream's maximum, 10–100 step 10 |
| rampSeconds | Int | 0 | gradual volume increase: 0 (off) \| 15 \| 30 \| 60 |
| vibrationPattern | String | "STANDARD" | "STANDARD" \| "PULSE" \| "HEARTBEAT" \| "LONG" \| "NONE" (AL22) |
| ringSeconds | Int | 60 | 30 \| 60 \| 120 \| 300 |
| snoozeMin | Int | 10 | 5 \| 10 \| 15 \| 20 \| 30 |
| maxSnoozes | Int | 3 | 0 (no snooze) \| 1 \| 3 \| 5 \| 10 |
| flashScreen | Boolean | false | alarm screen background pulses slowly (AL35) |
| whenNormal | String | "SOUND_VIBRATE" | "SOUND_VIBRATE" \| "SOUND" \| "VIBRATE" \| "SCREEN" |
| whenVibrate | String | "VIBRATE" | same values |
| whenSilent | String | "VIBRATE" | same values |
| whenDnd | String | "VIBRATE" | same values |
| whenInCall | String | "VIBRATE" | "VIBRATE" \| "SCREEN" only (sound is never played over a call) |

`AlarmProfiles.effective(kind)` returns the DEFAULT row when `useDefault` is true, else the row itself. The TEST alarm uses the DEFAULT profile, or the profile being edited when started from that profile's page.

**`weekday_goals`** — `weekday` Int PK (1 = Monday … 7 = Sunday), `kcalTarget` Int? (500–10000), `proteinTargetG` Double? (10–500). A row exists only for a weekday the user gave its own targets; a null field in a row means "same as every day" for that value. Used only when `weekdayGoalsEnabled` is true (G0). Turning the switch off keeps the rows.

### 7.2 Food side

**`foods`** — the personal food library.

| Field | Type | Notes |
|---|---|---|
| id | Long PK | |
| name | String | 1–40 chars |
| basis | String | "SERVING" \| "PER_100G" \| "RECIPE" — what kcal and protein refer to. RECIPE foods behave exactly like SERVING foods everywhere outside the recipe editor (amounts in servings) |
| servingLabel | String | 0–30 chars, e.g. "1 medium (118 g)" or "1 bowl"; SERVING and RECIPE foods |
| recipeServings | Double? | RECIPE only: how many servings the whole recipe makes, 0.5–100 (default 1) |
| isPartial | Boolean = false | RECIPE only, computed (RX2): at least one ingredient's kcal is unknown |
| defaultGrams | Double? | PER_100G foods: grams suggested when adding (1–2000), e.g. 30 |
| kcal | Int? | per serving or per 100 g (by basis), 0–5000 (PER_100G: 0–900), null = unknown. RECIPE: computed per serving (RX2), never typed, 0–100000 |
| proteinG | Double? | per serving or per 100 g, 0–300 (PER_100G: 0–100), null = unknown. RECIPE: computed (RX2) |
| isFavorite | Boolean = false | |
| isArchived | Boolean = false | hidden from search; kept for history |
| createdAtMillis | Long | |
| lastUsedAtMillis | Long | updated whenever it is added to a meal or logged |

Index on `name`. A food used by any meal item, log entry or recipe is never hard-deleted; "Delete" archives it.

**`recipe_ingredients`** — `id`, `recipeFoodId` FK → foods (CASCADE; must be a RECIPE food), `ingredientFoodId` FK → foods (RESTRICT; must NOT be a RECIPE food — recipes do not nest, so there are no cycles), `servings` Double? (SERVING ingredients, 0.25–20), `grams` Double? (PER_100G ingredients, 1–5000), `sortOrder`. Index on both FKs. At most 50 ingredients per recipe.

RX1. A recipe's ingredients are edited only in the recipe editor (13.16.5). Saving a recipe writes the food row and all its ingredient rows in one transaction.
RX2. **Computed values.** Whole-recipe kcal = sum of each ingredient's item kcal (N2) whose kcal is known; per serving = `(whole / recipeServings).roundToInt()`. Protein likewise (one decimal kept). If every ingredient's kcal is unknown, the recipe's kcal is null (same for protein). If only some are unknown, `isPartial = true`: the value is shown with a "≥" prefix in the library and editor, and log entries made from it carry `partialSnapshot = true`, which N4 counts in `unknownCount` so the day's total is never presented as complete. `unknownIngredientCount` (shown in the editor as "1 ingredient has unknown calories") is computed, not stored. Pure function `Recipes.totals(...)` in `:domain/meals` with unit tests.
RX3. **Keeping them current.** Editing a food's kcal or protein recomputes, in the same transaction, every recipe that uses it (FoodRepository). A food's basis can never change to or from RECIPE (the editor offers "Recipe" only for a new food). Changing the basis of a food referenced by meal items or ingredients is blocked with "Used in meals or recipes — create a new food for a different basis" (13.16.1), so an ingredient's servings/grams always match its food. Archiving an ingredient food is allowed (the recipe keeps it). As with any food edit (S4), past log entries keep their snapshots.

**`sections`** — a meal ("Breakfast", "Post-workout shake" …).

| Field | Type | Notes |
|---|---|---|
| id | Long PK | |
| name | String | 1–40 |
| size | String | "BIG" \| "SNACK" — decides the minimum gap (section 9) |
| timing | String | "FIXED" \| "FLEXIBLE" \| "AFTER_WAKE" \| "AFTER_MEAL" \| "AFTER_WORKOUT" |
| idealMold | Int | FIXED/FLEXIBLE: preferred time. Others: fallback time when the anchor is missing |
| offsetMin | Int = 0 | AFTER_WAKE/AFTER_MEAL/AFTER_WORKOUT: minutes after the anchor (0–600) |
| anchorSectionId | Long? | AFTER_MEAL: the meal it follows. AFTER_WORKOUT: the meal it follows on non-workout days (null = use idealMold) |
| restOffsetMin | Int? | AFTER_WORKOUT on non-workout days: minutes after anchorSectionId |
| durationMin | Int | 5–120; how long eating takes |
| colorKey | String | item-palette key (13.4), e.g. "blue" |
| sortOrder | Int | |
| alarmEnabled | Boolean = true | |
| activeFromDay | Long | epochDay |
| activeUntilDay | Long? | inclusive; null = no end |
| weekdaysMask | Int = 127 | |

Validation (enforced in the editor): an AFTER_MEAL section's anchor MUST be a FIXED or AFTER_WAKE section (so it is always placed before it, 9.4); the workout's anchor likewise. A section cannot anchor to itself.

**`meal_items`** — a food placed in a meal.

| Field | Type | Notes |
|---|---|---|
| id | Long PK | |
| sectionId | Long FK → sections (CASCADE) | |
| foodId | Long FK → foods (RESTRICT) | |
| servings | Double = 1.0 | SERVING foods: 0.25–20 |
| grams | Double? | PER_100G foods: amount in grams, 1–2000 (required for them) |
| isPriority | Boolean = false | star; used by Bare-minimum day |
| isOptional | Boolean = false | excluded from completion % and "Mark all eaten" |
| sortOrder | Int | |
| activeFromDay | Long | |
| activeUntilDay | Long? | |
| weekdaysMask | Int = 127 | |

**`day_overrides`** — per-day changes. Unique index on (`epochDay`, `targetType`, `targetId`); inserts use REPLACE, so a target holds at most one override per day.

| Field | Type | Notes |
|---|---|---|
| id | Long PK | |
| epochDay | Long | |
| targetType | String | "ITEM" \| "SECTION" |
| targetId | Long | meal item id or section id |
| action | String | "ADD" \| "REMOVE" \| "SKIP" \| "MERGE_INTO" \| "PIN_TIME" (last three for sections only) |
| mergeIntoSectionId | Long? | MERGE_INTO |
| pinnedMold | Int? | PIN_TIME |

**`log_entries`** — what was eaten. Index on `epochDay`; a database UNIQUE index on (`epochDay`, `mealItemId`). SQLite permits multiple null mealItemIds, so custom entries are unrestricted while checklist logs are protected even against concurrent writers. The DAO transaction also makes toggle and bulk actions atomic and idempotent.

| Field | Type | Notes |
|---|---|---|
| id | Long PK | |
| epochDay | Long | |
| sectionId | Long | no FK (sections are never hard-deleted while logs exist) |
| sectionNameSnapshot | String | meal name at logging; retained for custom or hidden-item rows after a rename/split |
| mealItemId | Long? | null for custom entries |
| foodId | Long? | the library food, if any |
| nameSnapshot | String | |
| portionSnapshot | String | e.g. "2 × 1 medium (118 g)" |
| kcalSnapshot | Int? | item kcal at the time (servings applied) |
| proteinSnapshot | Double? | |
| multiplier | Double = 1.0 | 0.25–4 |
| isCustom | Boolean | |
| partialSnapshot | Boolean = false | true when logged from a partial recipe (RX2) |
| createdAtMillis | Long | |

A meal item is **checked** on a day when a log row exists with that `epochDay` and `mealItemId`. `toggleItem(day, mealItemId)` is one `@Transaction`: delete the row if it exists, else insert one with snapshots taken from the item and its food. A double tap can never create two rows.

**`day_states`** — `epochDay` Long PK, `bareMinimum` Boolean = false, `alarmsPaused` Boolean = false (13.18 VC2; until 1.2 nothing sets it), `workoutPlan` String = "DEFAULT" ("DEFAULT" \| "REST" \| "TRAIN"; REST/TRAIN override the usual workout days for that day).

**`history_days`** — `epochDay` Long PK, `goalType` String, `kcalTarget` Int?, `proteinTargetG` Double?, `targetSource` String ("EVERYDAY" | "WEEKDAY" | "BARE_MINIMUM"), `bareMinimum` Boolean, `isHoliday` Boolean. Immutable plan/target metadata for an elapsed tracked day (H1). Do not store completion or eaten totals: those come from the current log rows so deliberate corrections remain possible.

**`history_items`** — primary key (`epochDay`, `mealItemId`), `epochDay` FK → history_days (CASCADE), `mealItemId` Long (identity only, no FK), `sectionId` Long, `sectionName` String, `sectionColorKey` String, `sectionSortOrder` Int, `plannedMold` Int?, `durationMin` Int, `slotStatus` String, `itemSortOrder` Int, `name` String, `portion` String, `kcal` Int?, `proteinG` Double?, `partial` Boolean, `isPriority` Boolean, `isOptional` Boolean, `sectionSkipped` Boolean. One row per item visible in that day's resolved plan, including skipped sections and merged items under their displayed section. Meal times/status (the phase-available planner result), names, amounts, colours and checklist membership are snapshots, independent of later live rows. No row for a hidden item; its existing log still appears using its own snapshots (V4).

**`weights`** — `id`, `epochDay` (unique), `kg` Double (20–300), `note` String (0–200). Logging a second weight on the same day replaces the first after confirmation.

### 7.3 Schedule side

**`semesters`** — `id`, `name` (1–40, e.g. "Fall 2026"), `startDay`, `endDay` (inclusive). Semesters MUST NOT overlap (validated on save: "Overlaps <name>").
**`holidays`** — `id`, `semesterId` FK (CASCADE), `name` (1–40), `startDay`, `endDay` (inclusive, within the semester).
**`courses`** — `id`, `semesterId` FK (CASCADE), `name` (1–60, e.g. "Intro to Economics"), `code` (0–20, e.g. "ECON101"), `location` (0–40), `colorKey` (item-palette key, 13.4).
**`class_sessions`** — a regular meeting of a course: `id`, `courseId` FK (CASCADE), `weekday` Int (1 = Monday … 7 = Sunday), `startMold`, `endMold`, `location` String? (null = the course's location), `intervalWeeks` Int = 1 (1–4: every week, every 2nd, 3rd or 4th week), `weekOffset` Int = 0 (0 … intervalWeeks − 1: which week of the cycle, counted from the semester's first week; with 2 weeks, 0 = "Week A", 1 = "Week B"). See BB2a.
**`class_exceptions`** — unique index (`sessionId`, `epochDay`): `id`, `sessionId` FK (CASCADE), `epochDay`, `type` ("CANCELLED" \| "MOVED"), `newStartMold` Int?, `newEndMold` Int?.

**`events`**

| Field | Type | Notes |
|---|---|---|
| id | Long PK | |
| kind | String | "PERSONAL" \| "EXTRA_CLASS" |
| courseId | Long? FK → courses (SET NULL) | EXTRA_CLASS: which course; colour follows the course |
| title | String | 1–60 |
| colorKey | String | PERSONAL: own colour; EXTRA_CLASS: follows its course (copied on save and when the course colour changes) |
| location | String | 0–40 |
| note | String | 0–500 |
| allDay | Boolean = false | all-day events never block meals |
| startDay | Long | first occurrence |
| startMold | Int | ignored when allDay |
| endMold | Int | ignored when allDay |
| repeatType | String | "NONE" \| "DAILY" \| "WEEKLY" \| "MONTHLY" |
| repeatInterval | Int = 1 | every N days (DAILY) or weeks (WEEKLY), 1–99 |
| weekdaysMask | Int = 0 | WEEKLY only; MUST be > 0 for WEEKLY |
| endType | String = "NEVER" | "NEVER" \| "UNTIL" \| "COUNT" |
| untilDay | Long? | UNTIL, inclusive |
| count | Int? | COUNT, total occurrences, 1–999 |
| importUid | String? | original UID, max 255; unique together with importRecurrenceId when non-null (14.7) |
| importRecurrenceId | String = "" | empty for a master/one-off; canonical original RECURRENCE-ID for a detached moved occurrence |

**`event_exceptions`** — unique index (`eventId`, `epochDay`): `id`, `eventId` FK (CASCADE), `epochDay`, `type` ("CANCELLED" \| "MOVED"), `newStartMold` Int?, `newEndMold` Int?.

### 7.4 Tasks side

**`task_lists`** — `id`, `name` (1–30), `colorKey` (item-palette key, 13.4), `sortOrder`. There is no Inbox row: a task with `listId == null` is in the Inbox. Deleting a list moves its tasks to the Inbox in the same transaction.

**`tasks`** — indices on `listId`, `dueDay`, `completedAtMillis`.

| Field | Type | Notes |
|---|---|---|
| id | Long PK | |
| listId | Long? FK → task_lists (SET NULL) | null = Inbox |
| title | String | 1–200 |
| notes | String | 0–5000 |
| priority | Int = 0 | 0 none, 1 low, 2 medium, 3 high |
| dueDay | Long? | null = no date |
| dueMold | Int? | 0–1439; null = all-day (T3) |
| courseId | Long? FK → courses (SET NULL) | a deadline for that course (13.9.8); index |
| repeatType | String = "NONE" | as events |
| repeatInterval | Int = 1 | |
| repeatWeekdaysMask | Int = 0 | |
| repeatStartDay | Long? | first due day of the series |
| repeatFrom | String = "DUE_DATE" | "DUE_DATE" \| "COMPLETION" (TK2a); DUE_DATE is the default in comparable task apps |
| completedAtMillis | Long? | null = open; set when the task is closed as done or won't do |
| closeType | String = "DONE" | "DONE" \| "WONT_DO"; meaningful only when completedAtMillis is set |
| deletedAtMillis | Long? | null = not deleted; set = in the Trash (TK7); index |
| importUid | String? | original UID for imported deadlines; unique together with importRecurrenceId when non-null (14.7) |
| importRecurrenceId | String = "" | canonical original occurrence identity, not its moved due date |
| previousOccurrenceId | Long? FK → tasks (SET NULL) | unique when non-null; records the predecessor of a generated repeat (TK3) |
| createdAtMillis | Long | |
| sortOrder | Long | manual order within a list (reserved for a later version; set to createdAtMillis) |

**`subtasks`** — `id`, `taskId` FK (CASCADE), `title` (1–200), `done` Boolean, `sortOrder`.
**`tags`** — `id`, `name` String (1–24 of letters, digits or "-", stored lowercase with `lowercase(Locale.ROOT)`, unique index; longer names from quick add are cut to 24). Created when first used; renamed or deleted from the Tasks drawer (13.9.1).
**`task_tags`** — `taskId` FK → tasks (CASCADE), `tagId` FK → tags (CASCADE); primary key (`taskId`, `tagId`); index on `tagId`. A tag with no tasks left stays until deleted.
**`task_reminders`** — `id`, `taskId` FK (CASCADE), `offsetMin` Int, `style` ("NOTIFICATION" \| "ALARM"). At most 3 per task (enforced in TaskRepository). Timed tasks: offsets 0, 5, 10, 30, 60, 120, 1440 minutes before. All-day tasks: 0 = on the day at 9:00 AM, 1440 = the day before at 9:00 AM.

### 7.5 Diagnostics

**`alarm_log`** — `id`, `atMillis`, `event` String ("SCHEDULED" \| "FIRED" \| "RANG" \| "SKIPPED" \| "FALLBACK" \| "ERROR" \| "ACTION" \| "NOTICE"), `kind` String ("MEAL" \| "TASK" \| "WEIGH_IN" \| "MAINTENANCE" \| "OTHER"), `refId` Long?, `detail` String (0–200, never contains food, task or event names — only ids, times and reasons). Trimmed to the newest 500 rows after each insert.

**`alarm_state`** — device-local operational state, never exported. `uri` String PK (canonical identity including generation), `kind` String, `refId` Long?, `epochDay` Long?, `triggerMillis` Long?, `snoozeCount` Int = 0, `consumed` Boolean = false, `noticeSent` Boolean = false. Keep a row for every outstanding alarm/action identity and a noticeSent row until that meal day has ended; diagnostics trimming never removes operational state. Reconciliation uses this table to cancel obsolete identities even after their source rows are deleted (AL0). The device-protected snapshot mirrors the minimal future ringing entries and consumed/snooze state needed before first unlock; the database remains the source after unlock.

### 7.6 Database safety rules

D1. Never call `fallbackToDestructiveMigration()` or any destructive variant. Never delete or recreate the database file.
D2. During development (before 1.0.0) the schema may still change, but every change bumps the version and adds an explicit `Migration` with SQL in `Migrations.kt`, plus a `MigrationTestHelper` test that migrates a populated database and checks the data. Testers keep their data across every build.
D3. All schema JSON files in `app/schemas/` are committed.
D4. Never hard-delete a section, meal item or food that has log entries or historical references; end it (activeUntilDay) or archive it instead. History item identities and snapshots must remain usable after a scope split.
D5. Every query uses bound parameters. User text is never concatenated into SQL. Search text used in `LIKE` patterns has `\`, `%` and `_` escaped with `\` and the query says `ESCAPE '\'`.
D6. `toggleItem`, "Mark all eaten", historical log correction, repeating-task completion, list deletion, tag rename/delete, recipe save and recompute (RX1, RX3), backup import, .ics import, emptying the Trash and every scope operation are single transactions. H1 materialization and S1 log/override remapping are part of the same transaction as the affected mutation.

---

## 8. Meal domain rules (`:domain/meals`)

### 8.1 Visibility

V1. `InRange(x, D)` = `x.activeFromDay <= D` AND (`x.activeUntilDay == null` OR `D <= x.activeUntilDay`) AND bit `(D.dayOfWeek.value - 1)` is set in `x.weekdaysMask`.
V2. **Section S on day D:**
  1. Override (D, SECTION, S.id): REMOVE → hidden. MERGE_INTO → hidden, and its visible items are shown under section `mergeIntoSectionId` that day, after its own items, under a label "From <S name>". ADD → visible. SKIP → visible, state "skipped". PIN_TIME → visible, time = `pinnedMold`.
  2. No override → visible if `InRange(S, D)`.
V3. **Meal item I on day D** (only when its section is visible or merged): override (D, ITEM, I.id) REMOVE → hidden; ADD → visible; no override → visible if `InRange(I, D)`.
V4. Log entries for day D are always shown in their section, even if their item is now hidden (they appear as checked rows independently of checklist visibility; unchecking deletes the log row and does not unhide the item).
V5. **Bare-minimum day** (`day_states.bareMinimum` for D): `VisibilityResolver` takes the day state as input, so every consumer (Today, planner, alarms, widget, totals) gets the same result:
  - Only priority items are visible (log rows already saved that day still show).
  - A section with no visible priority item is hidden that day (no time slot, no alarm).
  - Completion % counts only priority items.
  - The day's calorie and protein targets become the sums of the visible priority items' kcal and protein (unknown values count as 0). The summary card's title reads "Bare-minimum target".

V6. **Lifecycle before overrides.** For live rows, D outside `[activeFromDay, activeUntilDay]` is hidden before considering any override. An ADD can override the weekday mask within that range, never resurrect an ended row. Removing from now deletes its overrides on/after D, including stale PIN_TIME/ADD/MERGE_INTO references. Bringing an ended item back creates a new active row via S2. A pinned or skipped section still requires at least one visible item (or an existing log for display); it never creates an alarm for an empty meal. H1 resolves elapsed days from history instead of live rows.

### 8.2 Completion and totals

C1. Completion % for day D = checked visible non-optional items ÷ visible non-optional items, rounded to a whole number. Skipped sections count as unchecked. Custom entries do not count. 0 when the denominator is 0.
C2. Totals for D = sums over all log entries of D (N3, N4): `kcal`, `proteinG`, `unknownCount`.
C3. For elapsed tracked days, C1 uses history_items as its denominator and joins logs by (`epochDay`, `mealItemId`); changing a current star, optional flag, meal name or goal never changes it. Unchecking/correcting a historical log intentionally changes that day's actual totals and completion. The per-day completion used by the week strip, month grid and history is computed by one query per visible range (week or month), never one query per day.

### 8.3 Goal semantics (`Goals.kt`, `DayTargets.kt`)

G0. **Targets for day D** (`DayTargets.forDay(D, settings, weekdayGoals, dayState, visibleItems, historyDay?)`), used by every place that shows or compares against a target (summary card, Next meal card, widget, daily summary, `stopAlarmsAfterTarget`):
  0. If historyDay is supplied for D, return its preserved targets/source/goalType; the caller must supply it for elapsed tracked days. Past summary status uses that preserved goalType.
  1. Otherwise start from `kcalTarget` and `proteinTargetG`.
  2. If `weekdayGoalsEnabled` and `weekday_goals` has a row for D's weekday, each non-null value in that row replaces the everyday one.
  3. A Bare-minimum day (V5) replaces both with the starred totals, whatever steps 1–2 gave.
  Unit tests: everyday only; a Saturday row with only kcal set (protein stays everyday); switch off ignores rows; Bare-minimum wins over a weekday row.
G1. **Calories**, by `goalType`:
  - GAIN — the target is a minimum. Progress fill uses accent; turns success at ≥ 100%. Status text: "<n> kcal to go" / "Target reached".
  - MAINTAIN — success when within ±10% of the target. Below 90%: accent, "<n> kcal to go". 90–110%: success, "On target". Above 110%: warning, "<n> kcal over".
  - LOSE — the target is a limit. Below 90%: accent, "<n> kcal left". 90–100%: warning, "<n> kcal left". Above 100%: danger, "<n> kcal over limit".
G2. **Protein** is always a minimum: accent until 100%, then success. "<n> g to go" / "Target reached".
G3. When a target is null, its progress row shows only the total ("1,240 kcal") with no bar and no status text.
G4. **Plan line** (History chart, when `showPlanLine` is on): from the plan start — the day in `planStartDay` (set to logical today whenever goalType, weeklyChangeKg or goalWeightKg changes; null = the first weigh-in), starting at the latest weight on or before that day (or the first one after it) — a straight line changing by `weeklyChangeKg` per week — upward for GAIN, downward for LOSE, none for MAINTAIN or when weeklyChangeKg is null; it stops at `goalWeightKg` if it reaches it. It shows the plan, not what is happening — that is the trend line (W8). The goal line is drawn at `goalWeightKg` when set.
G5. Gentle floor: if `kcalTarget < 1200`, the target field shows a non-blocking supporting line "Very low targets can be unsafe. Talk to a health professional." Nothing else in the app comments on the user's numbers.

### 8.4 Scope operations (`Scopes.kt` + MealRepository)

Plan edits and scopes apply to logical today or a future day; a date range cannot begin before logical today. Past-day views allow only log/weight corrections (H1), and plan-edit controls are disabled there with "Past plans are preserved". Every scope operation is called only after the Scope sheet and a confirmation dialog (UI rule C-CONFIRM, 13.6). D = the day being viewed; W = D's weekday bit.

S1. **REMOVE an item or a section**
  - "Just this day": upsert override (D, REMOVE). Also delete D's log entries for that item (for a section: for all its items). The confirmation says so if any exist.
  - "Every <Weekday> from now": `newMask = mask & ~W`. If `activeFromDay < D`: set `old.activeUntilDay = D - 1` and insert a copy (new id) with `activeFromDay = D` and `weekdaysMask = newMask`. For a section, copy only its items still active on or after D (`activeUntilDay == null || activeUntilDay >= D`), pointing to the new section id, with `activeFromDay = max(item.activeFromDay, D)`; items that already ended are not copied. In the same transaction build an old→new section/item id map, move every `day_overrides` row with `epochDay >= D` to mapped ids (including merge targets), and remap every log on/after D to its replacement item and section id. Preserve log id, nutrients, multiplier, sectionNameSnapshot and all other snapshots; earlier logs/history keep old ids. Custom logs move to the replacement section too. Overrides for ended items with no replacement are deleted. Repoint any section/workout anchor to the replacement section. If a row already starts on/after D, no new id is needed. A retained log for the removed weekday remains a hidden checked row (V4), never a duplicate visible item. If `activeFromDay >= D`, just set `mask = newMask`. If `newMask == 0`, do "From now on" instead.
  - "From now on": if `activeFromDay < D`, set `activeUntilDay = D - 1`. Otherwise (the row starts on or after D) delete it if it has no log entries, or else set `activeUntilDay = activeFromDay - 1` so it is never visible again.
  - "Date range" (start ≤ end, at most 366 days): upsert REMOVE overrides for every day in the range.
S2. **ADD a new item or section**
  - "Just this day": `activeFromDay = D`, `activeUntilDay = D`, mask 127.
  - "Every <Weekday> from now": `activeFromDay = D`, `activeUntilDay = null`, mask = W.
  - "From now on": `activeFromDay = D`, `activeUntilDay = null`, mask 127.
  - "Date range": `activeFromDay = start`, `activeUntilDay = end`, mask 127.
S3. **Bring back** something hidden by weekday or a REMOVE override within its active range on D: upsert override (D, ADD). For an ended row use S2 and a new identity (V6).
S4. **Edits that are not scoped:** renaming a meal, changing its time, size, colour or timing rule, editing a food in the library, changing an item's servings, star or optional flag — these apply to the row itself (today and all future days). Before writing, materialize elapsed days under H1; past plans, goals and logged snapshots remain fixed. Existing logs today also keep their nutrient snapshots unless explicitly edited. No scope sheet; a confirmation is shown only when the change also removes something.

### 8.5 Historical plans and corrections

H1. **Historical plan preservation.** `historyStartDay` is set when setup finishes; earlier dates have no planned checklist, but explicit custom logs/weights may still be corrected there. On first unlocked access after a logical-day rollover, and before every mutation that could affect visibility, meal details, targets, bare-minimum state, holiday status, timetable/events, or planner preferences, materialize every missing day from historyStartDay through yesterday using the pre-mutation state. Do this on IO in the same database transaction as the mutation; failed writes roll back both. Each history_days row and its history_items are inserted together, including empty days, and never rebuilt from current rows. Future plans remain live until elapsed. If the app was closed for several days, materialize those days before applying any new edit. Clock/zone/day-boundary changes must not overwrite an existing snapshot; any newly elapsed date is materialized on the next unlocked access. No alarm receiver or direct-boot path performs this backfill while locked.

H2. **Correcting actual intake.** Past Today displays history_items and their checked log rows. Check/uncheck takes planned nutrition and names from that snapshot, never the current food; custom entries use their own entered snapshots. Multipliers, per-day nutrient overrides, custom-log deletion/addition and weight corrections remain available with normal Undo/confirmation. Past plan controls (meal membership, targets, star/optional, pin/skip/merge, bare-minimum/rest-day) are disabled; library edits launched elsewhere apply today/future only. History totals/streaks recompute from corrected logs against the preserved denominator. Tests: log yesterday → advance day → edit names/amounts/flags/goals → yesterday stays identical; repeat after several unopened days; correct one historical log → only actual totals/completion change; empty days do not extend a streak; failed edit leaves no partial snapshots.

---

## 9. Meal timing engine (`:domain/schedule`, `:domain/planner`)

All times are molds (T2). Domain code is pure Kotlin.

### 9.1 Busy blocks for day D

BB1. A busy block = (`startMold`, `endMold`, `label`, `kind`, `refId`). Two blocks touching end-to-start (`end == start`) do NOT overlap.
BB2. **Classes:** find the semester with `startDay <= D <= endDay`. If none, or D is inside one of its holidays, there are no classes. Otherwise every `class_session` of that semester's courses with `weekday == D.dayOfWeek.value`, minus sessions with a CANCELLED exception on D; a MOVED exception replaces the session's times for D. `kind = "CLASS"`, `label` = course name.
BB2a. **Alternating weeks.** A session with `intervalWeeks = n > 1` takes place on D only if `((mondayOf(D) − mondayOf(semester.startDay)) / 7) % n == weekOffset` (mondayOf as in 9.2; week 0 is the week containing the semester's first day). Holidays do not shift the cycle. Unit tests: n = 2, offset 0 → semester weeks 0, 2, 4; offset 1 → weeks 1, 3; n = 1 → every week.
BB3. **Events:** every non-all-day event where `occursOn(event, D)` is true (9.2). `kind = "CLASS"` for EXTRA_CLASS, `"EVENT"` for PERSONAL. A MOVED exception replaces start and end for D.
BB4. **Workout** (added by the planner in step 5, not by the repository): `kind = "WORKOUT"`, label "Workout".
BB5. Tasks never create busy blocks and never move meals.

### 9.2 Recurrence (`Recurrence.kt`, used by events and tasks)

`occursOn(r, D)` where r has `startDay`, `repeatType`, `repeatInterval`, `weekdaysMask`, `endType`, `untilDay`, `count`, and exceptions:

```
if D < r.startDay -> false
base = when (r.repeatType) {
  "NONE"    -> D == r.startDay
  "DAILY"   -> (D - r.startDay) % r.repeatInterval == 0
  "WEEKLY"  -> bit(D.dayOfWeek.value - 1) in r.weekdaysMask
               AND ((mondayOf(D) - mondayOf(r.startDay)) / 7) % r.repeatInterval == 0
               // mondayOf(x) = epochDay of the Monday of x's week
  "MONTHLY" -> D.dayOfMonth == startDate.dayOfMonth   // months without that day are skipped
}
if !base -> false
if r.endType == "UNTIL" && D > r.untilDay -> false
if r.endType == "COUNT": n = the exact 1-based ordinal of D among base occurrences
                         // cancellations do not refund an occurrence
                         if n > r.count -> false
if a CANCELLED exception exists for (r, D) -> false
true
```

RC1. Editing or deleting a repeating event or class session asks "Only <date>" or "All" (UI 13.8.6), then confirms.
  - Only <date>, edit → upsert exception (D, MOVED, new start, new end); only start and end are editable in this mode. Delete → upsert exception (D, CANCELLED).
  - All, edit → update the row (exceptions stay). Delete → delete the row and its exceptions.
  - There is no "This and following". Do not build one.
RC2. `nextOccurrence(r, fromDay)` returns the first occurrence on/after fromDay, or null only when the series is exhausted. Generate recurrence candidates directly, skipping cancelled occurrences; no arbitrary 800-day or 1,100-day cutoff. DAILY ordinal = day difference / interval + 1; WEEKLY counts selected weekdays in eligible cycle weeks (excluding dates before startDay); MONTHLY skips months without the starting day. MONTHLY DUE_DATE recurrence supports interval 1 only; COMPLETION mode has its separate TK2a rule. Tests include every 99 days COUNT 20 (offset 1881 is occurrence 20, offset 1980 is rejected), a weekly interval-99 occurrence more than 800 days away, first partial week, leap days, cancelled COUNT occurrences, and dates far beyond an exhausted series. Iterate occurrences, not every intervening day.

### 9.3 The day plan

```kotlin
interface DayPlanner {
  fun plan(day: LocalDate, sections: List<MealSection>, overrides: List<DayOverride>,
           busy: List<BusyBlock>, dayState: DayState?, settings: PlannerSettings): DayPlan
}
data class DayPlan(val wakeMold: Int, val workout: BusyBlock?, val slots: List<SlotPlan>,
                   val warnings: List<PlanWarning>, val notFitting: List<SlotPlan>)
data class SlotPlan(val sectionId: Long, val mold: Int, val status: SlotStatus, val reason: String?)
enum class SlotStatus { NORMAL, MOVED, PINNED, SKIPPED, MERGED, NOT_FITTING }
```

`sections` = the sections visible on D (8.1). `slots` are sorted by mold. `PlanWarning` carries a string-resource key plus arguments (not English text), so warnings are translatable; the exact English text is given below and in `strings.xml`.

### 9.4 MealScheduler — steps, in this exact order

Helpers:
- `free(t, dur)` = no overlap with any class/event/workout block or already placed non-skipped meal interval: for every interval b, `t + dur <= b.start || t >= b.end`. Touching is allowed. Ignore the candidate's own interval when validating an existing slot.
- `nearestFree(ideal, dur, lo, hi)`: for k = 0, 5, 10, … while `ideal - k >= lo || ideal + k <= hi`: first test `t = ideal - k` (only if `lo <= t <= hi`), then `t = ideal + k` (only if `lo <= t <= hi`); return the first t with `free(t, dur)`. None → null. At equal distance the EARLIER time wins.
- `gap(a, b)` = `minGapBigMin` if both are BIG, else `minGapSnackMin`.
- `firstClass` = smallest `startMold` among busy blocks of kind CLASS, or null.
- `dayEnd` = `settings.latestMealMold`, the latest automatic START. `logicalEnd = 1440 + dayStartHour*60` (exclusive); `latestStart(dur) = min(dayEnd, logicalEnd - dur)`. All automatic starts must be at/after wake and at/before latestStart. Minimum gaps are hard for flexible placement, advisory for fixed/after-anchor meals; actual interval overlap is always disallowed for automatic placement. Ties use `(idealMold, sortOrder, id)` for deterministic output.
- `isWorkoutDay` = `settings.workoutEnabled` AND (`dayState.workoutPlan == "TRAIN"` OR (`dayState.workoutPlan != "REST"` AND bit `(D.dayOfWeek.value - 1)` set in `workoutDaysMask`)).

Steps:

1. **Wake.** `wake = if (wakeBeforeFirstClassMin != null && firstClass != null) firstClass - wakeBeforeFirstClassMin else wakeMold`.
2. **Skipped and merged.** Sections with override SKIP → SKIPPED at their ideal (not placed). MERGE_INTO → MERGED (not placed).
3. **Pinned.** Sections with PIN_TIME → PINNED at `pinnedMold`, placed in `(sortOrder, id)` order. Pins are explicit overrides: an overlap emits `pinned_overlaps` "<Name> is pinned at <time> but overlaps <label>"; a start before wake or after latestStart emits `pinned_outside_window` "<Name> is pinned outside the meal window". The editor must still reject a duration extending outside the logical day.
4. **Fixed group** — AFTER_WAKE sections (ideal = `wake + offsetMin`) and FIXED sections (ideal = `idealMold`), processed in order of ideal:
   if `wake <= ideal <= latestStart(dur)` and `free(ideal, dur)` → NORMAL at ideal; else `t = nearestFree(ideal, dur, lo = max(wake, ideal - maxFixedShiftMin), hi = min(latestStart(dur), ideal + maxFixedShiftMin))`; found → MOVED at t with warning `moved_clash` "<Name> moved to <time> — clashes with <label> (<start>–<end>)" naming the first blocking interval that overlapped the ideal. When only the bounds caused movement, use `moved_window` "<Name> moved to <time> — outside the meal window"; not found → NOT_FITTING, reason `no_free_near` "No free time within <maxFixedShift as '2 h' / '1 h 30 min'> of <ideal time>".
5. **Workout.** If `isWorkoutDay`: FIXED timing → start = `workoutStartMold`; AFTER_MEAL timing → if the anchor section was placed at d, start = `d + workoutOffsetMin`, otherwise no workout today. End = start + `workoutDurationMin`. If it overlaps a busy block or a placed meal: warning `workout_overlaps` "Workout (<start>–<end>) overlaps <label>". The workout is never moved. Add it to the busy list for the remaining steps.
6. **After-anchor group** — AFTER_MEAL and AFTER_WORKOUT sections, processed in order of their computed ideal:
   - AFTER_MEAL: anchor placed at a → ideal = `a + offsetMin`; anchor not placed → ideal = `idealMold`.
   - AFTER_WORKOUT: workout exists → ideal = `workout.end + offsetMin`; else if `anchorSectionId` placed at a and `restOffsetMin != null` → ideal = `a + restOffsetMin`; else ideal = `idealMold`.
   - Clamp ideal into `[wake, latestStart(dur)]` (empty window → NOT_FITTING); a changed ideal emits `capped` "<Name> capped at <clamped time>".
   - Search forward only: t = ideal, ideal + 5, … while t ≤ latestStart(dur); first `free(t, dur)` wins. Found → NORMAL if t == ideal else MOVED (no warning). None → NOT_FITTING, reason `busy_until_after` "Busy until after <dayEnd time>".
7. **Big-gap check.** Sort all placed BIG slots by time; for each neighbouring pair with `b.mold - a.mold < minGapBigMin`: warning `short_gap` "Only <Xh Ym> between <A> and <B> (target <Xh Ym>)". Nothing moves.
8. **Flexible group** (FLEXIBLE), in order of `idealMold`. For each S:
   `prev` = placed slot with the largest mold ≤ `S.idealMold` (or null); `next` = placed slot with the smallest mold > `S.idealMold` (or null).
   `lo = max(wake, prev?.let { it.mold + gap(it, S) } ?: wake)`; `hi = min(latestStart(S.durationMin), next?.let { it.mold - gap(S, it) } ?: latestStart(S.durationMin))`. The free check also protects against non-neighbouring long intervals.
   If `lo > hi` → NOT_FITTING, reason `not_enough_between` "Not enough time between <prev name> (<time>) and <next name> (<time>)" (use "waking" / "the end of the day" when prev / next is null).
   Else `t = nearestFree(S.idealMold.coerceIn(lo, hi), dur, lo, hi)`; null → NOT_FITTING, reason `busy_between` "Busy from <lo> to <hi>"; else placed at t: NORMAL if `t == S.idealMold`, otherwise MOVED (no warning card for flexible meals).
9. **Not fitting.** NOT_FITTING sections are shown at their ideal time with a "doesn't fit" card (13.7). Its "Merge into" buttons use `prev` and `next` from step 8 (for fixed and after-anchor sections: the placed slots just before and after the ideal time).
10. The plan is recomputed whenever any input changes (it is a pure function of its inputs). Tests beyond Ex1–Ex8: two fixed meals at 13:30 never overlap silently; fixed ideal after latest start moves or does not fit; start exactly at latestMealMold is allowed if duration fits; automatic meals avoid pins and earlier meals; workout overlap with an already fixed meal warns without moving that fixed meal; equal ideals have stable order.

### 9.5 Worked examples (unit tests + Engine check)

These run on a fixed **test fixture**, built in code in `:domain/check/EngineCheckFixtures.kt` and shared by the unit tests and the Engine check screen. The fixture is not seed data and never touches the database.

Fixture settings: `wakeMold` 540 (9:00 AM), `wakeBeforeFirstClassMin` 90, `minGapBigMin` 150, `minGapSnackMin` 90, `maxFixedShiftMin` 120, `latestMealMold` 1470 (12:30 AM), workout enabled every day, timing AFTER_MEAL anchored to Dinner, offset 60, duration 60.

Fixture sections (all active from day 0, mask 127, no overrides):

| # | Name | Size | Timing | Ideal | Offset / anchor | Duration |
|---|---|---|---|---|---|---|
| 1 | Breakfast | BIG | AFTER_WAKE | — | +30 after wake | 15 |
| 2 | Morning snack | SNACK | FLEXIBLE | 12:00 PM | | 15 |
| 3 | Lunch | BIG | FIXED | 1:30 PM | | 30 |
| 4 | Afternoon snack | SNACK | FLEXIBLE | 4:00 PM | | 20 |
| 5 | Dinner | BIG | FIXED | 8:30 PM | | 30 |
| 6 | Post-workout | BIG | AFTER_WORKOUT | 11:15 PM | +45 after workout; non-workout days: Dinner + 150 | 15 |

Expected results (dates: Ex1 Mon 2026-10-05, Ex2 Sat 2026-10-10, Ex3 Wed 2026-10-07, Ex4 Tue 2026-10-06, Ex5 Fri 2026-10-09, Ex6 Sat 2026-10-10):

- **Ex1** — classes "Calculus" 10:30–12:00, "Chemistry" 12:15–1:15 PM, "Physics lab" 2:00–5:00 PM. Wake 9:00 AM. Breakfast 9:30 AM. Lunch 1:30 PM (touching the 2:00 PM class is allowed). Morning snack: lo 11:00 AM, hi 12:00 PM, 12:00–12:15 is free → 12:00 PM NORMAL. Afternoon snack: 4:00 PM is in class; nearest free in [3:00 PM, 7:00 PM] is 5:00 PM → MOVED 5:00 PM. Dinner 8:30 PM. Workout 9:30–10:30 PM. Post-workout 11:15 PM. No warnings.
- **Ex2** — no classes, no events. Wake 9:00 AM; 9:30 AM, 12:00 PM, 1:30 PM, 4:00 PM, 8:30 PM; workout 9:30–10:30 PM; 11:15 PM. No warnings.
- **Ex3** — no classes; personal event "Club meeting" 8:00–9:30 PM. Dinner ideal 8:30 PM clashes; 7:30 PM and 9:30 PM are both 60 min away → earlier wins → MOVED 7:30 PM, warning "Dinner moved to 7:30 PM — clashes with Club meeting (8:00–9:30 PM)". Workout 8:30–9:30 PM, warning "Workout (8:30–9:30 PM) overlaps Club meeting". Post-workout 10:15 PM. Afternoon snack 4:00 PM.
- **Ex4** — classes "Calculus" 10:30–12:00 and "Statistics" 1:00–2:30 PM. Wake 9:00 AM, Breakfast 9:30 AM. Lunch ideal 1:30 PM clashes; 12:30 PM and 2:30 PM are both 60 min away → MOVED 12:30 PM with warning "Lunch moved to 12:30 PM — clashes with Statistics (1:00–2:30 PM)". Morning snack: lo 11:00 AM, hi 11:00 AM, 11:00 is in class → NOT_FITTING with reason "Busy from 11:00 AM to 11:00 AM"; its card offers "Merge into Breakfast" and "Merge into Lunch". Afternoon snack 4:00 PM. Dinner 8:30 PM. Workout 9:30–10:30 PM. Post-workout 11:15 PM.
- **Ex5** — no classes; personal event "Movie" 10:00 PM–12:45 AM. Dinner 8:30 PM. Workout 9:30–10:30 PM with warning "Workout (9:30–10:30 PM) overlaps Movie". Post-workout: ideal 11:15 PM is busy and nothing is free up to 12:30 AM → NOT_FITTING, reason "Busy until after 12:30 AM".
- **Ex6** — Saturday with `workoutPlan = "REST"`, no classes, no events. Same as Ex2 except: no workout, and Post-workout at 11:00 PM (Dinner 8:30 PM + 150 min). No warnings.

Plus two examples for the newer timing types (same fixture unless stated):

- **Ex7** — like Ex2, but section 6 changed to timing AFTER_MEAL anchored to Dinner with offset 180, and the workout disabled. Post-workout at 11:30 PM NORMAL. No workout block. No warnings.
- **Ex8** — like Ex2, but `wakeBeforeFirstClassMin = null`, `wakeMold = 420` (7:00 AM) and one class "Early lecture" 7:30–9:00 AM. Wake 7:00 AM (the class does not change it). Breakfast ideal 7:30 AM clashes; searching outward in 5-minute steps within [7:00 AM, 9:30 AM], the first free start is 7:15 AM (7:15–7:30 touches the class) → MOVED 7:15 AM with warning "Breakfast moved to 7:15 AM — clashes with Early lecture (7:30–9:00 AM)". Morning snack: lo 8:45 AM, hi 12:00 PM → 12:00 PM NORMAL. Everything else as Ex2.

Unit tests MUST reproduce every wake time, slot time and status, workout block and warning (by key and arguments) above. The Engine check screen (13.15) runs the same fixtures and shows PASS/FAIL.

---

## 10. Tasks domain (`:domain/tasks`)

### 10.1 Task rules (`TaskRules.kt`)

TK1. Tasks use calendar today = now in the current zone `.toLocalDate()`, independently of the meal logical boundary. Today/Tomorrow/Next 7 Days, parser-relative dates and repeat completion dates use that calendar date. A timed task is overdue iff open and its due date-time converted with T10 is before now; an all-day task is overdue iff open and dueDay < calendarToday. Tasks without dates are not overdue. Test at 01:00 on 6 Oct with dayStartHour 4: due 6 Oct 00:30 is overdue; due 6 Oct 02:00 is not; all-day 5 Oct is overdue; all-day 6 Oct is not.
TK2. **Next due day of a repeating task:** build a recurrence with `startDay = repeatStartDay ?: dueDay`, the task's repeat fields and `endType = "NEVER"`; return the smallest D with `D > task.dueDay` AND `D >= today` where `occursOn` is true (RC2 candidate generation; null only when exhausted). So completing an overdue repeating task jumps to the next occurrence from today instead of leaving a chain of overdue copies.
TK2a. **Repeat from completion** (`repeatFrom = "COMPLETION"`): the next due day is counted from the calendar day the task is closed (TK1), not from its due day: DAILY → today + `repeatInterval` days; WEEKLY → today + `repeatInterval` × 7 days (the weekday chips are hidden and `repeatWeekdaysMask` is ignored in this mode); MONTHLY → today + `repeatInterval` months (same day of month as today, clamped to the month's last day) — as TickTick's and Todoist's "after completion" repeats do. The due time (`dueMold`) is kept. The copy's `repeatStartDay` = its new due day. Unit tests (g) and (h).
TK3. **Completing a repeating task** (one transaction): if already closed, no-op. Set completedAtMillis and closeType on the original; if TK2 gives a next due date, insert one open copy with a fresh id, previousOccurrenceId = original.id, createdAtMillis = now, imported identity cleared, and other editable fields copied. Copy subtasks with done=false, reminders and task_tags. The unique previousOccurrenceId prevents duplicate successors. Undo is offered for undoSeconds and reverses the completion only if the generated successor and its children/tag links are unchanged since creation; delete that untouched copy, then reopen the original. If it has been edited/closed, preserve it and show "Next occurrence changed — undo unavailable". A completed repeat with a surviving successor cannot be reopened from Completed: show "Reopen the latest occurrence instead". Reopening after the successor was hard-deleted is allowed; the next completion may generate one new successor. Tests cover tags, double completion, normal Undo, modified-successor Undo, and reopening without two live next occurrences.
TK4. Completing a non-repeating task sets `completedAtMillis = now` and `closeType = "DONE"`; reopening sets `completedAtMillis` to null.
TK4a. **Won't do.** "Won't do" closes a task like completing it (TK3/TK4, including the next copy of a repeating task) but with `closeType = "WONT_DO"`. Won't-do tasks show in Completed lists (if `showWontDo`) with a circled ×, in textTertiary without strikethrough, and do not count as completed anywhere a count of done tasks is shown. Reopening clears both.
TK5. Turning a repeat on requires a due date; if none is set, the due date becomes today. `repeatStartDay` is set to the due day when a repeat is turned on.
TK6. **Deleting a task moves it to the Trash** (TK7): sets `deletedAtMillis = now` and cancels its reminder alarms (AL28). A task in the Trash appears nowhere except the Trash list, its reminders never ring, and it is not shown in the schedule. A task whose title is blank when leaving detail is removed for good at once (nothing worth keeping).
TK7. **Trash.** Restoring clears `deletedAtMillis` (reminders are rescheduled by A11). "Delete forever" (one task) and "Empty trash" hard-delete the rows with their subtasks, reminders and tag links (FK cascade), each confirmed (C-CONFIRM). The maintenance alarm (AL7) hard-deletes tasks whose `deletedAtMillis` is more than `trashDays` days old (never when `trashDays` is 0). Deleting a list still moves its tasks (including trashed ones) to the Inbox.

Unit tests (today = the task's due date unless stated):
- (a) weekly Mon+Thu task due Mon 2026-10-05 → next due Thu 2026-10-08
- (b) daily every 2 days due 2026-10-05 → 2026-10-07
- (c) monthly on the 31st due 2026-10-31 → 2026-12-31
- (d) a timed task due today 1 hour ago is overdue; one due in 1 hour is not
- (e) daily task due 2026-10-01 completed with today = 2026-10-05 → next due 2026-10-05
- (f) completing then undoing a repeating task leaves exactly the original task, open, with its subtasks and reminders
- (g) repeatFrom COMPLETION, every 3 days, due 2026-10-01, completed with today = 2026-10-05 → next due 2026-10-08
- (h) repeatFrom COMPLETION, monthly, due 2026-01-31, completed 2026-02-10 → next due 2026-03-10; completed 2026-01-31 → next due 2026-02-28
- (i) Won't do on a weekly task creates the next copy and leaves the original with closeType WONT_DO
- (j) a task deleted 31 days ago is purged with trashDays 30; one deleted 29 days ago is kept

### 10.2 Smart quick-add (`QuickAddParser.kt`)

`parse(text, now: LocalDateTime, today: LocalDate /* calendar today, TK1 */, lists: List<TaskListRef>, courses: List<CourseRef>, dateOrder: DateOrder /* from settings.dateFormat */): ParsedTask(title, dueDay?, dueMold?, priority?, tags, listId?, courseId?, spans)`. When `quickAddSmartRecognition` is off the parser is not called and the whole text is the title. `spans` records which character ranges produced each parsed part, so the UI can show chips and let the user cancel one.

Split the text on whitespace. Matching is case-insensitive. Each step removes the words it matched; later steps only see the remaining words.

1. **Priority:** "!high", "!h", "!3" → 3; "!medium", "!med", "!m", "!2" → 2; "!low", "!l", "!1" → 1. If several, the last wins.
2. **Tags:** every word matching `#[\p{L}0-9-]+` → tag (lowercase, without "#").
3. **List:** a word `~name` → the list whose name, lowercased with spaces removed, equals `name` lowercased. No match → the word stays in the title.
3a. **Course:** a word `@code` → the course whose `code`, lowercased with spaces removed, equals `code` lowercased; courses of the semester containing today are checked first, then the most recent other semesters. No match (or the course has no code) → the word stays in the title.
4. **Date** — first rule that matches, in this order; one date per text:
   a. "day after tomorrow" → today + 2
   b. "today" → today
   c. "tomorrow" or "tmr" → today + 1 ("tom" and "tod" are NOT shortcuts; they are common words and names)
   d. "next week" → Monday of next week
   e. "in N days" / "in N weeks" (N = 1..99) → today + N days / N×7 days
   f. "next <weekday>" (full name or 3-letter abbreviation) → that weekday in next week (weeks start on Monday)
   g. "<weekday>" → the first such day strictly after today. A full weekday name matches anywhere. A 3-letter abbreviation (mon tue wed thu fri sat sun) matches only when it is the last word of the text, or directly after "on" (then "on" is removed too), because words like "sun" and "sat" appear in ordinary titles.
   h. "<D> <month>" or "<month> <D>" (month = full name or 3 letters; D = 1..31) → that date this year; if before today, next year.
   i. Numeric dates in `dateOrder`: DMY "<D>/<M>" or "<D>/<M>/<YYYY>"; MDY "<M>/<D>" or "<M>/<D>/<YYYY>"; YMD "<YYYY>-<M>-<D>" only. Without a year and before today → next year.
   Invalid dates (31/2) do not match.
5. **Time** — first match only: "<H>am", "<H>pm", "<H> am", "<H> pm", "<H>:<MM>am|pm" (H = 1..12) → 12-hour time; "<H>:<MM>" without am/pm → 24-hour time (H = 0..23); "at <H>" (H = 1..12) → 8..11 = AM, 12 = 12 PM, 1..7 = PM; "noon" → 12:00 PM; "midnight" → 12:00 AM. A bare number with no am/pm, colon or "at" is NOT a time. If a time matched but no date: compare that time with now.toLocalTime(); due on calendar today if later, otherwise calendar tomorrow (never meal logicalToday).
6. **Title** = remaining words joined by single spaces, trimmed. If it would be empty, ignore every parsed part and use the original text as the title.

English keywords only in version 1 (the parser is not translated; 17.2).

Parser examples (now = Mon 2026-10-05 17:00, today = 2026-10-05, one list named "Shopping", dateOrder DMY unless stated). Unit tests and the Engine check's "Parser check" section use exactly these:

| # | Input | Expected |
|---|---|---|
| Q1 | `gym tomorrow 6pm !high #fitness` | title "gym", due 2026-10-06 18:00, priority 3, tags [fitness] |
| Q2 | `submit lab report fri` | "submit lab report", due 2026-10-09, all-day |
| Q3 | `call home 9pm` | "call home", due 2026-10-05 21:00 |
| Q4 | `call home 9am` | "call home", due 2026-10-06 09:00 |
| Q5 | `pay fees 15 oct !med` | "pay fees", due 2026-10-15, all-day, priority 2 |
| Q6 | `buy oats in 3 days ~shopping #groceries` | "buy oats", due 2026-10-08, list Shopping, tags [groceries] |
| Q7 | `renew passport 2/1` | "renew passport", due 2027-01-02, all-day |
| Q8 | `read !low` | "read", no date, priority 1 |
| Q9 | `next mon standup at 10` | "standup", due 2026-10-12 10:00 |
| Q10 | `buy 2 bananas` | "buy 2 bananas", no date |
| Q11 | `buy sun cream` | "buy sun cream", no date |
| Q12 | `call tom` | "call tom", no date |
| Q13 | `renew passport 2/1` with dateOrder MDY | "renew passport", due 2027-02-01, all-day |
| Q14 | `today` | "today", no date (title would be empty, so nothing is parsed) |
| Q15 | `essay draft fri @econ101` (a course with code "ECON101" exists) | "essay draft", due 2026-10-09, all-day, course ECON101 |
| Q16 | `email @prof` (no such course code) | "email @prof", no date |

---

## 11. Weight and history domain

W1. One weight per day (unique `epochDay`). Logging on a day that already has one asks "Replace <old> with <new> for <date>?".
W2. **Weigh-in day:** `settings.weighInWeekday`. On that weekday, if no weight is logged for logical today, Today's summary card shows a line "Weigh-in day" with a "Log weight" button.
W3. **Weekly series** for the chart: one point per Monday–Sunday week that has at least one weight; the point's value is the weight logged on the weigh-in weekday of that week if present, otherwise the latest weight of that week; its x position is that weight's date. The raw list below the chart shows every entry.
W4. **Change figures:** "since start" = latest − first; "last 4 weeks" = latest − the weekly point closest to 28 days before the latest (omitted if fewer than 2 points); when the trend is shown, "Trend <value> (<±rate>/week)" where, with span = min(28, days between the first and latest entry), rate = (trend at the latest entry − trend `span` days earlier, interpolated between entries) ÷ (span / 7); omitted when span < 14.
W8. **Trend line** (`WeightTrend.kt`, when `showWeightTrend` is on): an exponential moving average over **every** logged weight in date order (not only weekly points), so daily ups and downs from water and food are smoothed out — the method popularised by The Hacker's Diet and used by dedicated weight apps. With α = `trendSmoothingPct` / 100 per day: trend₀ = the first weight; for each next entry with Δ = days since the previous entry (≥ 1), `a = 1 − (1 − α)^Δ` and `trend = trend + a × (weight − trend)`. The chart draws the trend value at each entry's date. Unit tests: constant weights give a flat trend; a single jump of +1 kg moves the trend by 0.1 kg the next day with α = 10%; a 7-day gap uses a = 1 − 0.9⁷.
W5. **Streaks:** current streak = consecutive days ending today (or yesterday, if today is not yet complete) with completion ≥ 90%; best streak = longest such run ever. Days with no visible items do not break or extend a streak.
W6. **Monthly stats:** days logged (days with ≥ 1 log entry), average kcal and average protein over days logged, current and best streak.
W7. **Weekly table** (last 8 Monday–Sunday weeks): dates, average kcal, average protein, days logged, average completion %.

---

## 12. Alarms, reminders and notifications (`app/alarm`)

Alarms are where apps most often fail across manufacturers. Follow these rules literally.

### 12.1 Manifest

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.USE_EXACT_ALARM"/>
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" android:maxSdkVersion="32"/>
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
```
No other permission is requested. In particular: no `INTERNET`, no storage permissions (SAF only), no `QUERY_ALL_PACKAGES` and no `<queries>` block (AL31 does not need one).

AL1. `AlarmReceiver` exported=false, `directBootAware="true"`. `BootReceiver` exported=true, `directBootAware="true"`, for `LOCKED_BOOT_COMPLETED`, `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED` (all protected system broadcasts that other apps cannot send); `onReceive` first checks `intent.action` against exactly these five and returns for anything else. `AlarmRingService` exported=false, `directBootAware="true"`, `android:foregroundServiceType="mediaPlayback"`. `AlarmActivity` exported=false, `directBootAware="true"`, `showWhenLocked="true"`, `turnScreenOn="true"`, `excludeFromRecents="true"`, `launchMode="singleInstance"`, `taskAffinity=""`, black window background, no orientation lock.
AL2. `<application android:enableOnBackInvokedCallback="true">`. Never override `onBackPressed()`; use Compose `BackHandler`.
AL3. **Restart before unlock (direct boot).** A restart clears every scheduled alarm, and `BOOT_COMPLETED` only arrives after the user first unlocks the phone, because the database lives in credential-protected storage. A phone that restarts overnight (for example for a system update) would otherwise miss every alarm until it is unlocked. So:
  1. **Boot snapshot.** When `ringBeforeUnlock` is on, `rescheduleAll()` ends by writing `boot-alarms.json` to `createDeviceProtectedStorageContext().filesDir` (write to a temp file, then rename): the ringing alarms of the next 48 hours (MEAL; TASK and WEIGH_IN with style ALARM) from eligible alarm_state entries as `[{kind, uri, triggerMillis, snoozeCount, consumed}]` (including pending ringing snoozes) and the generation/writtenAtMillis, plus one `profile` object per kind with only the fields needed to ring (soundUri, volumeMode, volumePercent, rampSeconds, vibrationPattern, ringSeconds, snoozeMin, maxSnoozes, the five `when…` values), and `timeFormat`. **No meal, food, task or course names and no amounts**: device-protected storage is readable before the user unlocks. When `ringBeforeUnlock` is off the file is deleted.
  2. **`LOCKED_BOOT_COMPLETED`** (phone still locked): read the snapshot (missing, invalid or generation mismatch → do nothing) and `setAlarmClock` every unconsumed entry still in the future with its own URI (12.3) and extra `fromSnapshot = true`. Touch nothing else.
  3. **A snapshot alarm firing while the user is still locked** (`UserManager.isUserUnlocked()` false): ring with the snapshot's profile for that kind and the AL19–AL23 rules, but the alarm screen shows only the time and the generic title ("Meal time", "Task reminder", "Weigh-in") and two buttons, "Snooze" (snoozes with `setAlarmClock` on the kind's `*-snooze` URI, 12.3, with persisted triggerMillis, snoozeCount and fromSnapshot, retaining it after unlock only if still eligible (AL0)) and "Dismiss". Nothing is written to the database or `alarm_log`, and no follow-up is scheduled. A custom ringtone stored in the user's media may be unreadable before unlock; AL20's fallback chain then plays the default alarm sound (test it). A FIXED volume profile works as usual because the saved volume lives in device-protected storage (AL21).
  3a. A `fromSnapshot` alarm (or snooze) that fires **after** the user has unlocked takes the normal AL12/AL26/AL29 path with the database.
  4. **`BOOT_COMPLETED`** (after the first unlock): the normal `rescheduleAll()`. First reconcile consumed entries and pending snoozes from the snapshot (AL0), then use the same URIs to replace eligible snapshot alarms without duplicating them.
  5. Every code path in BootReceiver, AlarmReceiver, AlarmRingService and AlarmActivity that touches Room, preferences or credential-protected files first checks `UserManager.isUserUnlocked()`. `OTasksApp.onCreate` also runs before unlock: it MUST NOT open the database or write credential-protected files there (the database is opened lazily, 15.6; the A11 trigger starts only after unlock; the crash-log handler of SP10a checks `isUserUnlocked()` first). AL16's `rescheduleAll()` is skipped while locked. A Robolectric test runs the locked path with a database that throws if opened.

### 12.2 Notification channels (created in `OTasksApp.onCreate`)

| Id | Name | Importance | Sound / vibration |
|---|---|---|---|
| `alarms` | Alarms | HIGH | none (the service plays sound and vibration itself) |
| `alarms_fallback_sound` | Alarms (backup, with sound) | HIGH | system default alarm sound with `AudioAttributes(USAGE_ALARM)`, vibration on — used only by AL15's fallback |
| `alarms_fallback_quiet` | Alarms (backup, vibrate only) | HIGH | no sound, vibration on — AL15's fallback when the phone mode allows no sound |
| `upcoming` | Upcoming alarms | LOW | none (AL6b) |
| `assistant` | Changes by your assistant | DEFAULT | none (13.18 VC4; created from 1.2) |
| `meal_reminders` | Meal reminders | DEFAULT | no sound, vibration on |
| `task_reminders` | Task reminders | HIGH | default sound and vibration |
| `weigh_in` | Weigh-in reminders | DEFAULT | default |
| `daily_summary` | Daily summary | LOW | none (also the morning overview, AL6c) |
| `status` | Background status | MIN | none (used only if a foreground notification is ever needed outside ringing) |

Names and descriptions are string resources.

### 12.3 PendingIntent identity (`AlarmIntents.kt`)

Android decides whether two PendingIntents are "the same" with `Intent.filterEquals` (action, data URI, type, component, categories) plus the request code — **extras are ignored**. Two alarms that compare equal silently replace each other. So every alarm and every notification action gets its own **data URI**; ids are never squeezed into request-code arithmetic (an older design with `sectionId % 1000` let sections 7 and 1007 overwrite each other, K35).

| Kind | Data URI |
|---|---|
| MEAL | `otasks://alarm/meal/<epochDay>/<sectionId>` |
| MEAL_FOLLOW_UP | `otasks://alarm/meal-follow-up/<epochDay>/<sectionId>` |
| MEAL_SNOOZE | `otasks://alarm/meal-snooze/<epochDay>/<sectionId>` |
| UPCOMING | `otasks://alarm/upcoming/<epochDay>/<sectionId>` |
| TASK | `otasks://alarm/task/<reminderId>` |
| TASK_SNOOZE | `otasks://alarm/task-snooze/<reminderId>` |
| WEIGH_IN / WEIGH_IN_SNOOZE | `otasks://alarm/weigh-in/<epochDay>` / `otasks://alarm/weigh-in-snooze/<epochDay>` |
| MAINTENANCE / TEST / DAILY_SUMMARY / DAY_OVERVIEW | `otasks://alarm/maintenance` / `…/test` / `…/daily-summary` / `…/day-overview` |
| Notification and alarm-screen actions | the alarm's URI including generation, with `&delivery=<triggerMillis>&action=<MARK_ALL\|SNOOZE\|SKIP\|OPEN\|COMPLETE\|WONT_DO\|LOG_WEIGHT\|DISMISS>` |

Rules:
- Every PendingIntent wraps an **explicit** intent (component = AlarmReceiver or MainActivity/AlarmActivity) and uses `FLAG_IMMUTABLE`, plus `FLAG_UPDATE_CURRENT` where extras change. No implicit PendingIntents anywhere.
- The table gives URI path templates; append the generation to every alarm identity and the delivery/action fields to action identities (AL0).
- The request code is a fixed constant per kind (MEAL 1, MEAL_FOLLOW_UP 2, MEAL_SNOOZE 3, UPCOMING 4, TASK 5, TASK_SNOOZE 6, WEIGH_IN 7, WEIGH_IN_SNOOZE 8, MAINTENANCE 9, TEST 10, DAILY_SUMMARY 11, DAY_OVERVIEW 12; actions 100 + kind). Uniqueness comes from the URI.
- AlarmReceiver reads ids from the URI path (parsed with `Uri.pathSegments`, each segment checked with `toLongOrNull()`; anything malformed is logged as ERROR and ignored), not from extras; extras carry only `snoozeCount`, `triggerMillis` and `fromSnapshot`.
- Cancelling rebuilds the identical intent and calls `alarmManager.cancel(pendingIntent)` then `pendingIntent.cancel()`.
- Notifications use `notify(tag = <the alarm's URI string>, id = 1, …)`, so they are unique per alarm the same way (the ring service's foreground notification keeps id 1001, AL17).
- Unit test: two sections with ids 7 and 1007 on the same day produce two distinct alarms; cancelling one leaves the other.

AL0. **Durable alarm reconciliation.** `alarm_state` records all scheduled identities, including snoozes/follow-ups; do not infer old identities from current source rows. A device-local random UUID generation lives in device-protected storage and is appended as `?generation=<uuid>` to every URI in 12.3 (actions additionally identify the ring/delivery using triggerMillis and action). Reject missing/mismatched generations before navigating or changing data, including locked boot. On backup replacement, rotate the generation and delete/invalidate the boot snapshot before the transaction, stop ringing, cancel every recorded alarm/PendingIntent and notification, then replace data and rebuild state/snapshot. If import fails, keep the old database and rebuild alarms under the new generation. Old actions never act on restored rows with reused ids.

Ordinary reschedule compares persisted outstanding identities with desired alarms: cancel obsolete entries after deletion, ending/splitting, pause, disable, completed checks, trigger edits or zone/time changes. Retain a future snooze/follow-up only if its parent is still eligible; cancel it on due-time edits or a zone/time change. Serialize reconciliation, actions and snapshot writes. Action identities include the originating triggerMillis; reject actions from an earlier delivery after a later snooze/ring, and allow each mutating action only once. Mark delivered identities consumed before starting side effects; duplicate broadcasts do not ring/post twice. Persist snooze trigger/count before scheduling, mirror it in the boot snapshot, and consume snapshot entries atomically before locked delivery; merge locked consumed/snooze state after unlock before normal reconciliation. Before an alarm-relevant DB mutation invalidate the old snapshot, then regenerate after commit; never leave a known stale snapshot available across a reboot. A crash before regeneration can miss a locked-boot alarm; recovery on next unlocked start rebuilds state, and the diagnostics health check reports a missing snapshot instead of promising coverage. Tests include process recreation, repeated broadcasts, pause→locked reboot, snooze→disable→reboot, deletion before reschedule, edited due time, zone change, and restore with reused ids.

AL0a. **Shared meal eligibility.** UI bulk logging, alarms, snoozes, notices, follow-ups and snapshot generation use the same VisibilityResolver/day plan. Reminder eligibility requires global meal alarms on, section alarmEnabled, no day pause, status NORMAL/MOVED/PINNED, and at least one visible unchecked non-optional item after bare-minimum filtering. With stopAlarmsAfterTarget, suppress only when G0 kcal target is non-null and eaten kcal >= target. Empty, hidden, skipped, merged or not-fitting sections are ineligible. Bulk logging itself does not require alarms to be enabled; it uses the same visible item set and inserts missing logs in one transaction under the UNIQUE index.

### 12.4 `AlarmScheduler.rescheduleAll()`

Runs at app start, from BootReceiver (inside `goAsync`), from the debounced database trigger (A11), at the end of every AlarmReceiver run, and from the maintenance alarm.

AL4. **Meal alarms.** Reconcile all persisted MEAL and UPCOMING identities under AL0. For logical today and tomorrow build the DayPlan; schedule only AL0a-eligible future slots with `setAlarmClock(AlarmClockInfo(triggerMillis, showIntent → MainActivity on Today), operation → AlarmReceiver type MEAL)`. Pending snoozes and follow-ups survive only while AL0a eligibility remains true. Use the same eligible set for the boot snapshot; paused/disabled/target-reached meals must not reappear before unlock.
AL5. **Weigh-in.** Reconcile persisted WEIGH_IN and WEIGH_IN_SNOOZE identities. If weighInEnabled, schedule the next future weigh-in weekday within 7 days without a logged weight: setAlarmClock for ALARM, setExactAndAllowWhileIdle for NOTIFICATION. Disabled/logged weigh-ins cancel outstanding snoozes too.
AL6. **Task reminders** (independent of `mealAlarmsEnabled`). Reconcile all persisted TASK and TASK_SNOOZE identities. For reminders whose task is open, not in the Trash, and whose trigger is in the future and within the next 14 days: `setAlarmClock` for style ALARM, `setExactAndAllowWhileIdle` for NOTIFICATION, with extra `triggerMillis` (checked by AL26). Retain a future TASK_SNOOZE only while its reminder exists, the task is open, and the due time is unchanged (AL0). Trigger: timed task → `LocalDateTime(dueDay, dueMold) - offsetMin`; all-day → `dueDay` at 9:00 AM minus `offsetMin` (0 or 1440).
AL6a. **Daily summary.** Cancel DAILY_SUMMARY. If `dailySummaryEnabled`: `setExactAndAllowWhileIdle` at the next `dailySummaryMold` (logical today, or tomorrow if passed), type DAILY_SUMMARY. When it fires, post on `daily_summary` (VISIBILITY_PRIVATE, public version "Daily summary"): title "Today", text "<kcal> / <target> kcal · <protein> / <target> g · <n>% done" (targets from G0; parts hidden per showCalories/showProtein; targets omitted when null) and, if starred items are unchecked, a second line "<n> starred left". Tapping opens Today.
AL6b. **Upcoming notice** (meals only). If `upcomingNoticeMin > 0`: for every MEAL alarm set in AL4, set an UPCOMING alarm with `setExactAndAllowWhileIdle` at trigger − `upcomingNoticeMin` (if that is already past but the meal alarm is still ahead, post the notice at once). Each notice is posted at most once per (day, meal): before posting, atomically claim `alarm_state.noticeSent` for that day/section/generation; a diagnostic NOTICE may also be written but is never the deduplication source (so a dismissed notice never comes back after a reschedule). When it fires and AL0a still allows the meal: post on `upcoming` (VISIBILITY_PRIVATE, public version "Upcoming alarm") "<Meal> at <time>" with one action "Skip today" (writes the SKIP override, like 12.7 SKIP — reversible from Today's Restore) and `setTimeoutAfter` until the meal's trigger time. The meal alarm itself cancels the notice when it rings.
AL6c. **Morning overview.** Cancel DAY_OVERVIEW. If `dayOverviewEnabled` and meal alarms are on: `setExactAndAllowWhileIdle` at `dayOverviewMold`, or when that is null at today's planned wake time (DayPlan.wakeMold); tomorrow's if passed. When it fires, post on `daily_summary` (VISIBILITY_PRIVATE, public version "Today's alarms") "Today: <n> meal alarms, first <Meal> at <time>" (or "No meal alarms today"). Tapping opens Today.
AL7. **Maintenance** (always): `setExactAndAllowWhileIdle` at the next `dayStartHour:05`, kind MAINTENANCE. When it fires: `rescheduleAll()`, update the widget, run the auto-backup check (14.3) and the Trash purge (TK7). This keeps alarms rolling forward even if the app is not opened for days.
AL8. If `alarmManager.canScheduleExactAlarms()` is false (possible on Android 12–13 when SCHEDULE_EXACT_ALARM was revoked), schedule nothing exact, log ERROR "exact alarms not permitted", and the health card shows the problem.
AL9. Times that passed while the phone was off are not rung late; they are simply skipped.
AL10. Every scheduled alarm is written to `alarm_log` (event SCHEDULED, kind, refId, trigger time). The last step of `rescheduleAll()` writes the boot snapshot (AL3).

### 12.5 `AlarmReceiver.onReceive`

AL11. **First**, before anything else: acquire a `PARTIAL_WAKE_LOCK` tagged `otasks:alarm` with a 60-second timeout and hold it in a companion object; `AlarmRingService` releases it right after `startForeground()`. Then `goAsync()` and continue on `Dispatchers.IO` (A10).
AL12. **Type MEAL or MEAL_SNOOZE:** validate generation and delivery identity (AL0), then recheck AL0a against current DB state. Ineligible → log SKIPPED with a nonpersonal reason and cancel related reminders. Eligible → start ringing (AL15) and, if followUpMin > 0, record/schedule one MEAL_FOLLOW_UP at now + followUpMin using setExactAndAllowWhileIdle. Locked snapshot delivery follows the prevalidated AL3 state without opening Room.
AL13. **Type MEAL_FOLLOW_UP:** validate generation/delivery identity and AL0a again; if eligible, post a meal_reminders notification "<Meal> — <n> items left (<kcal> kcal)" with the four actions. Disabled, paused, hidden, empty, merged, skipped or target-reached meals post nothing. No sound loop or full screen.
AL14. **Types TASK, WEIGH_IN, TEST** follow 12.8, 12.9 and 12.10. **Actions** follow 12.7.
AL15. **Starting to ring:** `ContextCompat.startForegroundService(AlarmRingService)` inside try/catch. On `ForegroundServiceStartNotAllowedException` or any failure, ring without the service so the alarm is still **audible whether the phone is locked or in use** (log FALLBACK):
  1. Work out the phone mode and behaviour as in AL19. Post the alarm notification (same content, actions, URI tag and visibility as AL17) on `alarms_fallback_sound` if the behaviour includes sound, otherwise on `alarms_fallback_quiet`, with `CATEGORY_ALARM`, `setFullScreenIntent(AlarmActivity, true)`, `FLAG_INSISTENT` (the channel sound repeats until the user acts) and `setTimeoutAfter(ringSeconds × 1000)`. When the phone is in use this shows as a heads-up that keeps sounding; it is never silent.
  2. If AlarmActivity opens (locked phone, or the user taps the heads-up), it cancels that notification (stopping the insistent sound), re-posts it with `setSilent(true)` under the same tag, and plays sound and vibration itself with the profile (AL19–AL22) while visible.
  For behaviour SCREEN the fallback posts on `alarms` (no sound, no vibration) instead.
  Fallback rings use the system default alarm sound until AlarmActivity takes over; that is acceptable for a fallback. The health card's "Alarm channel" row also checks `alarms_fallback_sound`.
AL16. **Last**, whatever the type: call `rescheduleAll()` (skipped while the user is locked, AL3), release the wake lock if no service took it, `pendingResult.finish()`.

### 12.6 `AlarmRingService`

AL17. In `onStartCommand`, immediately `ServiceCompat.startForeground(this, 1001, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)`. Notification: channel `alarms`, `CATEGORY_ALARM`, `PRIORITY_MAX`, ongoing, title = meal name (or task title / "Weigh-in"), text = summary line, `setFullScreenIntent(AlarmActivity, true)`, the actions for its kind, and `setDeleteIntent` (AL24). Visibility `VISIBILITY_PRIVATE` with a public version titled "Alarm" (meal and task names stay off the lock-screen notification until it is unlocked; AlarmActivity itself shows them over the lock screen only when `alarmDetailsOnLockScreen` is on, AL35).
AL18. **One ring at a time.** If a ring is already active when another MEAL, TASK or WEIGH_IN arrives, do not start a second ring: post the new one as a normal notification on its reminder channel with its actions.
AL19. **Phone mode and behaviour.** The ring uses the effective profile of its kind (7.1 `alarm_profiles`). Detect the phone's mode, first match wins: In call (`audioManager.mode` is `MODE_IN_CALL`, `MODE_IN_COMMUNICATION` or `MODE_CALL_SCREENING`) → Do Not Disturb (`notificationManager.currentInterruptionFilter != INTERRUPTION_FILTER_ALL`) → Silent (`ringerMode == RINGER_MODE_SILENT`) → Vibrate (`RINGER_MODE_VIBRATE`) → Normal. The profile's `whenInCall` / `whenDnd` / `whenSilent` / `whenVibrate` / `whenNormal` value decides: SOUND_VIBRATE = sound and vibration; SOUND = sound only; VIBRATE = vibration only; SCREEN = full-screen alarm with neither. Sound is also skipped when `soundUri == "SILENT"`, and vibration when `vibrationPattern == "NONE"` or the device has no vibrator; if that leaves nothing, the alarm behaves as SCREEN. The detected mode and resulting behaviour are logged to `alarm_log`.
AL20. **Sound (when the behaviour includes sound):** request audio focus `AudioFocusRequest(AUDIOFOCUS_GAIN_TRANSIENT)` with `AudioAttributes(USAGE_ALARM, CONTENT_TYPE_SONIFICATION)` so music pauses; abandon it when ringing stops. Sound source (`AlarmSound.kt`), first that works: the profile's `soundUri` → `RingtoneManager.getActualDefaultRingtoneUri(TYPE_ALARM)` → TYPE_NOTIFICATION → TYPE_RINGTONE → a generated tone (`ToneGenerator(AudioManager.STREAM_ALARM, 100)` playing `TONE_CDMA_ALERT_CALL_GUARD` every 2 s). `MediaPlayer`: `setAudioAttributes` BEFORE `setDataSource`, `isLooping = true`, `prepareAsync`. Any exception moves to the next source.
AL21. **Volume.** Profile `volumeMode` SYSTEM: do not touch the alarm stream volume (the phone's own alarm volume applies). FIXED: read `original = getStreamVolume(STREAM_ALARM)`; if no saved volume exists, write `original` first to the file `saved-alarm-volume` in `createDeviceProtectedStorageContext().filesDir` (device-protected so it works before unlock, AL3; never in the database, never exported or transferred); then `setStreamVolume(STREAM_ALARM, max(1, (getStreamMaxVolume(STREAM_ALARM) * volumePercent / 100.0).roundToInt()), 0)` inside try/catch (some phones throw `SecurityException` in Do Not Disturb — then keep the current volume and delete the saved-volume file). Whenever ringing stops (any action, timeout, `onDestroy`), in a `finally` block, restore the saved volume and delete the file. On app start, `OTasksApp.onCreate` launches a coroutine on the container scope with `Dispatchers.IO` (never a blocking read on the main thread, A9) that, if the saved-volume file exists and no ring is active, restores it and deletes the file (protects against the OS killing the app mid-ring). This file check does not need the user to be unlocked.
AL21a. **Gradual volume.** If `rampSeconds > 0`, the player volume (`MediaPlayer.setVolume`) starts at 0.1 and rises linearly to 1.0 over `rampSeconds`, updated every 250 ms; the stream volume is set as in AL21 from the start.
AL22. **Vibration:** if the behaviour includes vibration and a vibrator exists (`VibratorManager.defaultVibrator.hasVibrator()`), repeat the profile's pattern from index 0 — STANDARD `[0, 600, 400]`, PULSE `[0, 200, 200]`, HEARTBEAT `[0, 150, 100, 150, 600]`, LONG `[0, 1200, 800]` — with `VibrationAttributes.createForUsage(USAGE_ALARM)` on API 33+ and `AudioAttributes(USAGE_ALARM)` on 31–32.
AL23. **Timeout:** after the profile's `ringSeconds` stop sound and vibration, `stopForeground(STOP_FOREGROUND_REMOVE)`, post (if `missedNotification` is on) "Missed: <Meal> — <n> items left" on `meal_reminders` with the four actions (tasks: "Missed reminder: <title>" on `task_reminders`), `stopSelf()`. Log RANG with "timeout".
AL24. **Swiped away** (Android 14+ allows dismissing ongoing notifications): the delete intent stops ringing exactly like a timeout but posts no "Missed" notification.
AL25. Any action or alarm-screen button stops sound and vibration immediately and stops the service.

### 12.7 Meal alarm actions (mutations handled by AlarmReceiver; navigation goes directly to an activity)

- **MARK_ALL** — one transaction: insert log rows (multiplier 1.0) for every visible unchecked non-optional item of that section and day; cancel its FOLLOW_UP and SNOOZE alarms.
- **SNOOZE** — `snoozeCount = extra + 1`; schedule MEAL_SNOOZE (WEIGH_IN_SNOOZE / TASK_SNOOZE for those kinds) at now + the profile's `snoozeMin` with `snoozeCount` in the extras. A snooze of anything that **rings** (meal alarms, TEST, and task or weigh-in reminders with style ALARM) uses **`setAlarmClock`**, exactly like the original alarm: `setExactAndAllowWhileIdle` is rate-limited and can be deferred while the phone is in Doze, and some manufacturers batch it further, so a snooze with the screen off could ring late (K36); `setAlarmClock` is exempt. A snooze of a NOTIFICATION-style reminder uses `setExactAndAllowWhileIdle` and posts the notification again. When it fires, the normal MEAL checks apply. When a ring has `snoozeCount >= maxSnoozes` (the profile's), Snooze is hidden on the alarm screen and left out of the notification.
- **SKIP** — upsert DayOverride (epochDay, SECTION, sectionId, SKIP); cancel FOLLOW_UP and SNOOZE.
- **OPEN** — notification content/Open/Log weight intents use `PendingIntent.getActivity` targeting MainActivity directly, never a receiver/service that calls startActivity (Android 12 notification trampoline restriction). MainActivity validates generation, destination and ids, then performs any required unlock before reading personal data. It receives `epochDay`; it sets SelectedDate and opens Today. From the alarm screen over the lock screen, "Open app" (and every button that opens the app: Open, Log weight) first calls `KeyguardManager.requestDismissKeyguard(activity, callback)` and starts MainActivity only in `onDismissSucceeded`; if the user cancels, the alarm screen stays. The alarm screen never lets someone reach the app's data without unlocking (SP12).

### 12.8 Task reminders

AL26. AlarmReceiver type TASK (reminderId from the URI; extras snoozeCount and `triggerMillis`): if the reminder row no longer exists, or its task is closed, in the Trash or deleted, or (for TASK, not TASK_SNOOZE) the reminder's current trigger time differs from `triggerMillis` by more than 60 s (the due date changed), do nothing — log SKIPPED. Deleting a reminder row (Date sheet ×, or a task's reminders being replaced) cancels its TASK and TASK_SNOOZE alarms in the same operation (TaskRepository). Style ALARM → ring (AL15–AL25) in task mode: the alarm screen shows the label "Task", the task title, the due text and buttons Complete, Snooze, Open. Style NOTIFICATION → post on `task_reminders` with `VISIBILITY_PRIVATE` and a public version titled "Task reminder", title = task title, text = due text, actions Complete and Snooze; tapping opens `TaskDetail(taskId)`.
AL27. Actions: COMPLETE → complete the task (TK3/TK4) and cancel its notification; SNOOZE → as 12.7; OPEN → MainActivity on `TaskDetail(taskId)`.
AL28. Completing, marking won't do, or deleting (moving to the Trash) a task cancels its TASK and TASK_SNOOZE alarms immediately.

### 12.9 Weigh-in reminder

AL29. Style NOTIFICATION → `weigh_in` notification "Weigh-in day" / "Log your weight" with action "Log weight". Style ALARM → rings like a meal alarm; the alarm screen shows "Weigh-in", the line "Log your weight", and buttons Log weight, Snooze, Dismiss. "Log weight" opens MainActivity on History with the log-weight dialog open. No follow-up.

### 12.10 Alarm health and device help

AL30. **Alarm health card** (`ui/components/AlarmHealthCard.kt`), shown in Preferences › Alarms & sound, in Setup, and at the top of Today (and of Tasks if any task has a reminder) whenever a required row is red. One row each, with a success check or danger cross, a one-line explanation and a "Fix" button:

| Row | Check | Fix |
|---|---|---|
| Notifications | `POST_NOTIFICATIONS` granted (API 33+; always true on 31–32 unless the app's notifications are blocked: `NotificationManagerCompat.areNotificationsEnabled()`) | runtime request; if permanently denied, `ACTION_APP_NOTIFICATION_SETTINGS` |
| Exact alarms | `canScheduleExactAlarms()` | `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` with `package:` URI |
| Full-screen alarms | `canUseFullScreenIntent()` (API 34+; true before) | `ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT` with `package:` URI |
| Alarm channel | channel `alarms` importance ≥ HIGH and not blocked | `ACTION_CHANNEL_NOTIFICATION_SETTINGS` for that channel |
| Battery | `PowerManager.isIgnoringBatteryOptimizations(packageName)` | `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` with `package:` URI (fallback `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`) |
| Device settings (info row, grey) | manufacturer has extra background limits (DeviceQuirks) and `batteryHelpDismissed` is false | opens the Device help sheet |

Rows re-check on every `ON_RESUME`. The battery row is "recommended", not "required": it is shown amber, never blocks anything, and the card is not shown on Today for it alone.

AL31. **Device help sheet** (`DeviceQuirks`): a title "Keep alarms working on <Manufacturer>", 2–4 numbered steps of plain text for that manufacturer, an "Open settings" button and a "Done, don't show again" text button (sets `batteryHelpDismissed`). Steps per manufacturer family (match on `Build.MANUFACTURER.lowercase()`):

| Family | Matches | Steps | "Open settings" targets, tried in order |
|---|---|---|---|
| OnePlus / OPPO / realme | oneplus, oppo, realme | App info › Battery usage › allow background activity (Unrestricted); allow Auto-launch if listed; lock the app in Recents | app details settings |
| Xiaomi / Redmi / POCO | xiaomi, redmi, poco | Turn on Autostart; Battery saver › No restrictions; lock the app in Recents | `com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity`, then app details |
| Samsung | samsung | Battery › Unrestricted; add OTasks to "Never sleeping apps" | app details |
| vivo / iQOO | vivo, iqoo | Allow background power consumption / high background usage; allow Auto-start | `com.vivo.permissionmanager/.activity.BgStartUpManagerActivity`, then app details |
| Huawei / Honor | huawei, honor | Battery › App launch › Manage manually with all three switches on | `com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity`, then app details |
| Others | everything else | Battery › Unrestricted (if offered) | app details |

The targets are tried in order by calling `startActivity` inside try/catch (`ActivityNotFoundException`, `SecurityException`); the first one that starts wins, and if none does, `ACTION_APPLICATION_DETAILS_SETTINGS` with a `package:` URI opens. **Do not test them first with `resolveActivity` or `queryIntentActivities`:** since Android 11, package visibility hides other apps' activities from those calls, so they return null even when the screen exists and every phone would fall through to app details (K37). No `<queries>` block is added for this. The component names are fixed constants in DeviceQuirks, never taken from data. A "Learn more" text button opens `https://dontkillmyapp.com/<family>` in the browser with `ACTION_VIEW` and `CATEGORY_BROWSABLE` inside try/catch (the app itself makes no network request).

AL32. **Test alarm** button (Preferences › Alarms & sound, each alarm profile page, and the health card): schedules a TEST alarm 10 seconds ahead with `setAlarmClock`. It rings exactly like a meal alarm (same service, volume, quiet rules), titled "Test alarm" with the line "If you can see and hear this, alarms work", and buttons "It works" and "Snooze". Shows a snackbar "Test alarm in 10 seconds — you can lock your phone".

AL33. **Diagnostics screen** (Settings › About › Alarm diagnostics): the health rows; the next 10 scheduled alarms (kind, date, time — computed from the same plan, not from AlarmManager, which cannot be queried); the boot snapshot line ("Boot snapshot: <n> alarms, written <time>" or "off"); the last 50 `alarm_log` rows; device line (manufacturer, model, Android version, app version); "Simulate restart" (runs exactly what BootReceiver runs — the AL3 snapshot step, then `rescheduleAll()` — and shows "Restart check: <n> alarms restored"); and "Copy report" (plain text to the clipboard; contains no food, task, event or course names).

### 12.11 Alarm screen (`AlarmActivity`)

AL34. Always a black background `#000000` and white text, whatever the theme. `enableEdgeToEdge(SystemBarStyle.dark(TRANSPARENT), SystemBarStyle.dark(TRANSPARENT))` so system icons stay visible on black.
AL35. Content (meal mode), top to bottom, in a column limited to 560dp wide and centred:
  1. Current time — 64sp Light, tabular figures, updates each minute, in the user's time format (T8). If the profile has `flashScreen` on, the background alternates between #000000 and #2A2A2A once per second (1 Hz; never faster).
  2. A 12dp dot in the meal colour and the meal name — Lora 32sp Medium, up to 2 lines. While the phone is locked and `alarmDetailsOnLockScreen` is off (or the ring comes from the boot snapshot, AL3), the dot and summary are hidden and the title is "Meal time" (task: "Task reminder"; weigh-in: "Weigh-in") until the phone is unlocked (`KeyguardManager.isKeyguardLocked`, re-checked on `onResume` and on `ACTION_USER_PRESENT`).
  3. One summary line — 18sp, `#BDBDBD`: "<n> items · <kcal> kcal" for the unchecked non-optional items ("<n> items" alone when every kcal is unknown or `showCalories` is off; plural-aware).
  4. Flexible space.
  5. Buttons, full width of the column, 56dp tall, 12dp apart: "Mark all eaten" (white fill, black text), "Snooze <snoozeMin> min" (1dp white outline; hidden at the snooze limit or when maxSnoozes is 0), then one row of two equal text buttons "Skip today" and "Open app" (white text).
AL36. **Never behind system bars:** the root applies `WindowInsets.safeDrawing` padding plus 24dp on every side, so the last row sits fully above gesture and 3-button navigation and clear of cut-outs. If the content is taller than the window (landscape phones, split-screen, font size 200%), it scrolls, with the buttons kept in view first: in landscape with height < 480dp, use two columns — time, dot, name and summary on the left; buttons on the right.
AL37. The meal name has `semantics { liveRegion = LiveRegionMode.Assertive }` so TalkBack announces "<Meal> alarm". `announceForAccessibility` is not used (ignored on Android 16).
AL38. Task mode: label "Task" (16sp `#BDBDBD`) instead of the dot, title (Lora 32sp), due text as the summary line, buttons Complete / Snooze / (row) Open. Weigh-in mode: "Weigh-in", "Log your weight", buttons Log weight / Snooze / (row) Dismiss. Test mode as AL32.
AL39. Expected behaviour, not bugs: when the phone is unlocked and in use, Android shows a full-screen intent as a heads-up notification instead of the activity (sound and buttons still work). `setAlarmClock` shows an alarm icon in the status bar and the next meal as "next alarm" on the lock screen — this is what makes it reliable; keep it.

---

## 13. User interface

### 13.1 Style

Warm neutral "paper and ink" look: warm near-black (dark) or warm cream (light) backgrounds, serif titles, sans-serif everything else, one clay accent. Layout is a calm list of cards with generous spacing, no gradients and no shadows (cards use a 1dp border). Colour appears only for: the accent (FAB, selected day, progress fills, selected navigation icon), the meal/course/list colour dots and checkboxes, priority flags and stars, status (success, warning, danger), the "now" line, and coloured course/event titles in the Schedule day list. Everything else is greyscale.

### 13.2 Tokens (`ui/theme/Tokens.kt`)

Every dp/sp/ms value in UI code comes from `Tokens`; code outside `ui/theme` never uses a literal dp or sp (lint-style check in code review and a unit test that greps `ui/` for `\.dp\b|\.sp\b` outside `theme/`).

| Group | Tokens |
|---|---|
| Space | xxs 2dp, xs 4dp, sm 8dp, md 12dp, lg 16dp, xl 24dp, xxl 32dp, xxxl 48dp |
| Radius | sm 5dp (checkboxes), md 8dp (buttons, fields, timeline blocks), lg 12dp (cards, dialogs, sheets), squircle 16dp (FAB), indicator 12dp (navigation indicator) |
| Border | thin 1dp, strong 1.5dp, focus 2dp |
| Size | touchMin 48dp, rowMin 56dp, checkbox 22dp, checkboxSm 20dp, dot 10dp, dotSm 6dp, marker 6dp, iconSm 16dp, iconMd 24dp, fab 56dp, bottomBar 64dp, rail 80dp, indicatorW 48dp, indicatorH 32dp |
| Layout | gutter 16dp (compact), 24dp (medium+); maxContent 640dp; dayPane 360dp (expanded), 400dp (large); listsPane 280dp |
| Type | see 13.3 |
| Motion | FAST_MS 100 |

### 13.3 Typography (`ui/theme/Type.kt`, `ui/theme/Fonts.kt`)

Fonts are bundled in `res/font/`: Lora 400/500/600 and Inter 400/500/600 (static TTFs, SIL Open Font Licence, licence texts shipped in assets and shown in Settings › About › Licences). No downloadable fonts, no Play services.

| Token | Family | Size / line height | Weight |
|---|---|---|---|
| titleLg | Lora | 26 / 32 sp | Medium |
| titleMd | Lora | 18 / 24 sp | Medium |
| body | Inter | 16 / 24 sp | Regular |
| bodySm | Inter | 15 / 22 sp | Regular |
| caption | Inter | 13 / 18 sp | Regular |
| label | Inter | 12 / 16 sp | Medium |
| numberLg | Inter | 28 / 34 sp | Medium, tabular figures |

When `showCalories` or `showProtein` is off, that value is hidden everywhere it would appear (rows, totals, Next meal card, alarm summary, widget, History averages); the data is still stored and exported.

Lora is used only for screen titles, card and meal titles, the Next meal card's meal name, the alarm screen's title and the History weight number, and never below 18sp (a serif at small sizes gets muddy on low-density tablet screens). All numbers use Inter with `fontFeatureSettings = "tnum"`. Secondary text = caption in textSecondary; small labels = label in textTertiary.

### 13.4 Colour (`ui/theme/Color.kt`)

**Base palette**

| Role | Light | Dark |
|---|---|---|
| background | #FAF9F5 | #262624 |
| surface | #F0EEE6 | #1F1E1D |
| card | #FFFFFF | #30302E |
| border | #E8E6DC | #3D3D3A |
| track | #E8E6DC | #3D3D3A |
| textPrimary | #141413 | #FAF9F5 |
| textSecondary | #4D4C48 | #C2C0B6 |
| textTertiary | #696862 | #9C9A92 |
| ink (primary buttons, selected states) | #141413 | #FAF9F5 |
| onInk | #FAF9F5 | #141413 |
| accent | from `accentKey` (default clay #C96442) | from `accentKey` (default clay #D97757) |
| onAccentText / onAccentIcon | computed per accent (see Accent options) | computed per accent |
| success (icons, bars, markers) | #15803D | #4ADE80 |
| successText | #166534 | #4ADE80 |
| warning (icons, bars, markers) | #B85C08 | #FBBF24 |
| warningText | #92400E | #FBBF24 |
| danger (icons, bars, markers) | #DC2626 | #F87171 |
| dangerText | #B91C1C | #F87171 |
| star | #B85C08 | #FBBF24 |
| nowLine | #DC2626 | #F87171 |

Text drawn on the accent uses onAccentText; icons on the accent use onAccentIcon. Body text is never drawn in the accent colour on card or surface. Text in a status colour always uses its text role (successText, warningText, dangerText: ≥ 4.5:1 on background, surface and card); icons, bars and markers use the plain role (≥ 3:1). A unit test checks every status role against all three backgrounds in both themes.

**Accent options** (`settings.accentKey`; ignored when wallpaper colours are on). Text and icons on the accent use whichever of #141413 / #FFFFFF has the higher contrast, computed once per accent. A unit test checks every accent at 3:1 against the theme's background, card and surface, and 4.5:1 for its on-accent text; if a value fails, adjust its lightness minimally and record the change in PROGRESS.md.

| Key | Light | Dark |
|---|---|---|
| clay (default) | #C96442 | #D97757 |
| ocean | #2F6FB2 | #6AA6E8 |
| forest | #3F7D4E | #6FBF7F |
| plum | #8A4FA8 | #B98BD6 |
| rose | #B5487A | #E88AB4 |
| amber | #B7791F | #E6B04F |
| graphite | #57534E | #A8A29E |

**Item palette** — used for meals, courses, events and task lists. Stored as a key (`colorKey` column, String), never as hex, so each theme can use a variant that meets contrast. Every value meets 4.5:1 against that theme's background, surface and card (so coloured titles are readable), checked by a unit test.

| Key | Light | Dark |
|---|---|---|
| blue | #1D4ED8 | #60A5FA |
| teal | #0F766E | #2DD4BF |
| orange | #A84A0C | #FB923C |
| violet | #6D28D9 | #A78BFA |
| red | #881337 | #FDA4AF |
| pink | #BE185D | #F472B6 |
| green | #166534 | #4ADE80 |
| gold | #806000 | #FACC15 |

**Not to be mistaken for errors:** every palette colour differs from that theme's danger and dangerText colours by a CIEDE2000 colour difference of at least 12, and any two palette colours of the same theme by at least 10 (unit tests), so a red course or meal never looks like an error or a cancellation. (Danger is still always paired with words or icons — "Cancelled", "Overdue" — 13.6.)
**Check marks and text on a palette colour** (MealCheckbox, checked task boxes, the selected ColorPicker circle) use that key's `onColor`: whichever of #141413 / #FFFFFF has the higher contrast with it, computed per key and theme (in dark mode that is #141413 for every key; white on a light-gold or light-green fill is unreadable). A unit test checks ≥ 3:1.
New meals, courses, events and lists get the next key in this order that is least used among existing rows of that kind (ties: palette order). The user can change it.

**Priority colours** (task flags and checkbox borders): high = danger, medium = warning, low = blue (palette), none = textTertiary.

**Material colour scheme.** Build `lightColorScheme` and `darkColorScheme` setting every role explicitly so no default purple appears anywhere: primary = ink, onPrimary = onInk, primaryContainer = surface, onPrimaryContainer = textPrimary, inversePrimary = onInk, secondary = ink, onSecondary = onInk, secondaryContainer = border, onSecondaryContainer = textPrimary, tertiary = accent, onTertiary = onAccentText, tertiaryContainer = surface, onTertiaryContainer = textPrimary, background = background, onBackground = textPrimary, surface = card, onSurface = textPrimary, surfaceVariant = surface, onSurfaceVariant = textSecondary, surfaceTint = Transparent, inverseSurface = textPrimary, inverseOnSurface = background, error = dangerText, onError = whichever of #141413 / #FFFFFF gives higher contrast with dangerText, errorContainer = danger at 15% alpha over the actual surface, onErrorContainer = dangerText (adjust only its lightness if necessary to reach 4.5:1 on that composited container), outline = border, outlineVariant = border, scrim = #000000, surfaceBright = card, surfaceDim = surface, surfaceContainerLowest = card, surfaceContainerLow = card, surfaceContainer = surface, surfaceContainerHigh = surface, surfaceContainerHighest = surface. Pass `tonalElevation = 0.dp` to Card, Surface, sheets and bars. Material components whose defaults fill with a container role (filled Card, filled TextField, Switch track, SearchBar, Slider inactive track, SegmentedButton, NavigationBar, FilterChip) are never used with default colours: use the 13.6 component specs (OutlinedCard-style cards, `OutlinedTextField`, explicit Switch colours). Error/supporting text always uses dangerText, never the icon-only danger role. Test actual Material error/onError/container pairs and dynamic surfaces for text 4.5:1; if a fixed status role fails on a wallpaper surface, adjust its lightness for that surface and test it. A screenshot test ("Material defaults") renders each of those components in both themes so a grey or tinted fill shows up in review (K40).

**Wallpaper colours (Material You).** When `settings.dynamicColor` is true, use `dynamicLightColorScheme` / `dynamicDarkColorScheme(context)` for every Material role, and map the app roles: background/surface/card/border/text roles from the dynamic scheme's background, surfaceContainer, surfaceContainerLowest, outlineVariant, onSurface, onSurfaceVariant; ink = primary; accent = primary. Status, star, nowLine, priority and item-palette colours stay fixed. Fonts, shapes and layout do not change.

**Theme mode.** `themeMode` SYSTEM follows `isSystemInDarkTheme()`; LIGHT / DARK force it. Status and navigation bar icon colours follow the in-app theme (13.11).

### 13.5 Motion and haptics

M1. `const val FAST_MS = 100`. Every animation uses `tween(FAST_MS, easing = LinearOutSlowInEasing)`. No springs, no bounce, no scale effects, no confetti.
M2. Card expand/collapse: `animateContentSize(tween(FAST_MS))`. Checkbox state changes instantly. No transition between navigation destinations. Pager swipes use the default fling. Bottom sheets and dialogs use Material defaults.
M3. `settings.animations`: NORMAL as M1–M2; REDUCED keeps colour fades but no size or position animation (`animateContentSize` and expand animations become `snap()`); OFF uses `snap()` everywhere. If the system animator duration scale is 0 ("Remove animations"), behave as OFF regardless.
M4. Haptics (when `settings.haptics` is on): only when an item or task becomes checked — `performHapticFeedback(HapticFeedbackType.ToggleOn)` (fallback `TextHandleMove` on older Compose). Nothing else vibrates in the app UI.

### 13.6 Components and shared rules (`ui/components`)

| Component | Spec |
|---|---|
| Card | `OutlinedCard` look: card fill, radius lg, 1dp border, padding lg (COMPACT density: md), no elevation; gap between cards md (COMPACT: sm) |
| Button (Primary) | 48dp tall, radius md, ink fill, onInk label |
| Button (Secondary) | 48dp, 1dp border, textPrimary label |
| Button (Tertiary) | text only |
| Pressed / hover / focus | ripple in textPrimary at 12% (pressed), 8% (hover); focus = 2dp ink outline |
| Disabled | content at 38% alpha; Primary container becomes border colour |
| Text field | `OutlinedTextField`, label always visible; focused 2dp ink border; error 2dp danger border + supporting text (caption, danger); character counter appears within 10 characters of the limit |
| Chip | unselected 1dp border; selected = secondaryContainer fill + 16dp check icon |
| Switch | checked = ink track, onInk thumb; unchecked = surface track with a 2dp textTertiary border and a textTertiary thumb |
| Dialog | card container, radius lg, title titleMd, buttons end-aligned (Tertiary cancel, Primary confirm) |
| Bottom sheet | drag handle, side padding xl, title titleMd top-start, primary action bottom-end; content column has `navigationBarsPadding().imePadding()`; max width 640dp |
| Snackbar | inverseSurface background, inverseOnSurface text, action in SemiBold; 4 s, or `undoSeconds` when it has Undo; one at a time |
| MealCheckbox | 22dp rounded square (radius sm); unchecked 2dp border in the item colour; checked filled with the item colour and a 14dp check in that key's onColor (13.4) |
| ColorDot | 10dp circle in an item colour |
| StatusMarker | 6dp marker that differs by shape: ≥ `markerHighPct` (default 90%) filled circle (success); ≥ `markerMidPct` (default 60%) 1.5dp ring with a 2dp centre dot (warning); from 1% up to that, empty 1.5dp ring (danger); nothing for 0% or future days |
| WeightChart | 13.10.3 |
| ProgressRow | label (caption, textSecondary) and value (bodySm, tnum) on one line, status text under it (caption), then a 6dp bar with rounded ends; colours from 8.3 |
| ConfirmDialog | see C-CONFIRM |
| ScopeSheet | see 13.7.9 |
| CalendarStrip | week strip + expandable month grid shared by Today, Schedule and the Tasks calendar (13.8.2) |
| TimePickerDialog / DatePickerDialog | T6, T7 |
| ColorPicker | row of the 8 item-palette circles (40dp, 48dp touch target), selected one shows a check |
| AlarmHealthCard | AL30 |

**C-CONFIRM — confirmation rule.** Every action that removes something, applies to more than one day, or replaces data shows `ConfirmDialog` first: a title, one sentence that states exactly what will change and from when (for example "Remove Apple from every Tuesday from 6 Oct 2026. Past days stay as they are."), a "Cancel" button and a confirm button labelled with the verb ("Remove", "Add", "Cancel class", "Replace data"). Nothing changes until confirm is tapped. Simple deletions that can be undone (unchecking an item, deleting a task) use a snackbar with Undo instead.
When `confirmSingleDay` is off, reversible actions that affect only one day (a "Just this day" add or remove that deletes no log entries, pin time, skip a meal, cancel a class or event for one day) skip the dialog and show an Undo snackbar instead. Anything that removes logged data, applies to more than one day, deletes a course, semester, list or food, or replaces data is always confirmed.

**Density and text size.** `density` COMPACT reduces card padding and gaps one step and the visual row height from rowMin 56dp to 48dp; touch targets never go below 48dp. `appTextScale` is applied by wrapping the app's content in `CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * appTextScale / 100f))`; the alarm screen and the widget use the system scale only.

**Loading.** Never show spinners. Until a screen's first state arrives, draw only the background (Room answers in well under 100 ms).

**Empty states** (centred, caption in textSecondary, xxl top padding, optional Tertiary button):

| Where | Text | Button |
|---|---|---|
| Today, no meals exist at all | "No meals yet. Add your first meal to start your daily checklist." | "Add a meal" |
| Today, meals exist but none on this day | "No meals planned for this day." | "Add a meal" |
| A meal with no items | "No foods in this meal yet." | "+ Add food" |
| Food library | "Your foods appear here. Add one to reuse it in any meal." | "New food" |
| Schedule day list | "Nothing scheduled." | — |
| Schedule, no semester | "Add a semester to see your classes every week." | "Add semester" |
| Tasks list | "No tasks" | — |
| Trash | "Trash is empty. Deleted tasks stay here for <n> days." (with `trashDays` 0: "Trash is empty. Deleted tasks stay here until you empty it.") | — |
| Recipe with no ingredients | "Add the foods this recipe is made of." | "+ Add ingredient" |
| Weight | "Log your first weigh-in" | "Log weight" |
| History weekly table | "Nothing logged yet." | — |

**Input limits** (error text under the field; Save disabled until valid; typing past a maximum length is blocked, not trimmed afterwards). Per-100 g values: kcal 0–900, protein 0–100 g; grams 1–2000. Names: meal, food, course, event, holiday, semester 1–40 (course and event titles 1–60) — "Enter a name". Serving label 0–30. kcal 0–5000 — "Enter 0 to 5,000". Protein 0–300 g. Servings 0.25–20. Weight 20–300 kg (44–661 lb). Task title 1–200, notes 0–5000, list name 1–30, tag 1–24 of letters, digits or "-". Recipe: makes 0.5–100 servings, 1–50 ingredients, ingredient grams 1–5000. End time after start — "End must be after start". Weekly repeat needs a day — "Pick at least one day". "After N times" 1–999. "Ends on" not before the start — "End date must be on or after the start date". Semester end after start and not overlapping another semester.

**Accessibility**
- Whole rows are the touch target: checklist, task and subtask rows use `Modifier.toggleable(value, role = Role.Checkbox)` with merged semantics, read as "<name>, <portion>, <kcal> kcal, checked" (tasks: "<title>, <due text>, <priority>, not completed"). Long-press actions are exposed with `semantics { onLongClick(label = "More options") }` and the same menu opens on right-click (PL8).
- Card titles and group headers: `semantics { heading() }`.
- ProgressRow: content description "Calories 1,840 of 2,500" and `progressBarRangeInfo`.
- Colour is never the only signal: StatusMarker shapes, priority words in descriptions, the clock icon on a moved meal ("Moved from <ideal time>"), the pin icon ("Pinned time").
- Icons beside a text label are decorative (`contentDescription = null`); every standalone icon button describes its action ("Open lists", "Sort tasks", "Show timeline", "Show cards", "More options", "Add", "Previous month", "Next month", "Expand calendar", "Collapse calendar").
- Timeline and day-list blocks are focusable with descriptions like "Calculus class, 1:00 to 2:30 PM" and "Lunch at 1:30 PM, 2 of 3 eaten". Calendar cells: "Monday 5 October, 92% complete" / "Monday 5 October, 2 classes, 1 event".
- Every tappable element ≥ 48dp (`minimumInteractiveComponentSize()` where the visual is smaller).
- Only the colour tokens above are used; they meet WCAG AA in both themes. No new colours.
- No fixed heights on anything containing text (`heightIn(min = …)`); everything works at font scale 2.0 (Android 14+ non-linear scaling) — long names wrap to 2 lines, then ellipsis.
- TalkBack announcements use `liveRegion`, never `announceForAccessibility`.

### 13.7 Today (route `Today`)

Shows `SelectedDate`. Two views — Cards (default) and Timeline — switched from the top bar; the choice is saved in `settings.todayView`.

13.7.1 **Top bar** — a plain Row, 56dp tall, horizontal padding = gutter, directly under the status-bar inset (no Large/Medium top app bar, no extra spacer). Title (titleLg): "Today" / "Yesterday" / "Tomorrow" / "Mon, 6 Oct". Actions: view toggle (`Icons.Outlined.ViewTimeline` in Cards view, `Icons.Outlined.ViewAgenda` in Timeline view); a "Today" text button when SelectedDate is not today; overflow menu (`MoreVert`) with checkable items "Bare-minimum day" (toggles `day_states.bareMinimum`; while on, a caption line "Bare-minimum day — starred foods only" shows under the title) and, only when a workout is configured, "Rest day" (checked when the day is not a workout day; toggling writes `workoutPlan` REST or TRAIN), plus "Copy from another day…", "Food library" and "Edit meals".

13.7.2 **Calendar strip** (CalendarStrip, 13.8.2) in "completion" mode: 7 columns Monday–Sunday of the week containing SelectedDate (first day of week from `firstDayOfWeek`, 17.4; weekday masks always use Monday = bit 0). Each column: weekday letter (label, textTertiary) above the date number (body). Selected day: 36dp accent circle behind the number, number in onAccentText. Today when not selected: number bold with a 4dp ink dot above the letter. Under the number: StatusMarker for that day's completion (past and today only). Swipe horizontally to change week. A chevron button under the strip ("Expand calendar") expands the month grid in place with the same markers; tapping a date selects it and collapses the grid.

13.7.3 **Alarm health card** (AL30) when meal alarms are on and a required row is red.

13.7.4 **Schedule chip** (only when SelectedDate has classes or events): one AssistChip "3 classes · 1 event" that opens the Day schedule sheet — the same day list as Schedule (13.8.3) for that date, with each class or event row offering "Cancel for this day" / "Restore" (classes) or "Cancel this occurrence" (events), each confirmed (C-CONFIRM).

13.7.5 **Warning cards** — one per plan warning: card with a 3dp start border in warning colour and caption text, e.g. "Lunch moved to 2:00 PM — clashes with Statistics (1:00–2:00 PM)".

13.7.6 **Doesn't-fit cards** — danger border, title "<Meal> doesn't fit today" (bodySm SemiBold), reason line (caption), and a FlowRow of Secondary buttons: "Merge into <previous meal>", "Merge into <next meal>" (each only if that neighbour exists), "Pick a time", "Skip today". Each confirms, then writes a DayOverride (MERGE_INTO / PIN_TIME / SKIP).

13.7.7 **Next meal card** (only when SelectedDate is today and `showNextMealCard` is on; first card): the first slot whose time is later than now − 15 minutes and that is not complete, skipped or merged. 1.5dp ink border, padding 20dp. Row: ColorDot, meal name (Lora 20sp), spacer, time (Inter 20sp tnum). Line "3 items left · 710 kcal" (bodySm, textSecondary). Primary full-width button "Mark all eaten" (checks all unchecked non-optional items; snackbar with Undo) and a Tertiary "Show" that scrolls to that meal's card. If no slot qualifies but visible required items remain unchecked: "No upcoming meals · <n> items left" with no success icon. Show "All meals done today" only when every visible required item is checked and at least one exists; no visible planned items → "No meals planned today" (or "No meals yet" when the meal library is empty). The card's "now" is the minute ticker in the ViewModel (P-rule 15.4), not recomputed per frame.

13.7.8 **Summary card** — title "Today's totals" (or "Bare-minimum target"). Targets come from G0; when a weekday target applies, a caption under the title says "<Weekday> targets". ProgressRow "Calories" ("1,840 / 2,500 kcal", plus " · 1 unknown" when `unknownCount > 0`) with the goal status text (8.3); ProgressRow "Protein" ("96 / 120 g"); caption "Checklist 78%". After `starredNudgeMold` (default 6:00 PM; never when null) on today only, if starred items are unchecked, a warningText line "★ 2 starred items left". On the weigh-in weekday with no weight logged: a line "Weigh-in day" with a Tertiary button "Log weight" (W2).

13.7.9 **Meal cards** (Cards view), in plan time order:
- Header row: ColorDot, meal name (titleMd), spacer, time (bodySm tnum). Moved: `Icons.Outlined.Schedule` 16dp before the time. Pinned: `Icons.Outlined.PushPin` 16dp. Under the name: "2 of 3 · 710 kcal" (caption) or "Skipped".
- Overflow menu: "Add food", "Edit meal…", "Pin time for this day", "Skip this meal this day" (or "Restore" when skipped/pinned), "Remove meal…" (Scope sheet).
- Tapping the header toggles collapse. When `autoCollapseDoneMeals` is on, a card collapses automatically the first time all its non-optional items become checked, and shows a success check next to the time.
- Item rows (min 56dp): MealCheckbox, md gap, column with food name (body) and portion (caption: "2 × 1 medium (118 g)"), spacer, kcal (bodySm tnum; "—" if unknown). Starred items: 12dp star after the name. Optional items: label "optional" under the portion. Checked rows: name and kcal in textTertiary with strikethrough. Rows never change order when checked.
- Tap a row → toggle (taps on the same row within 300 ms are ignored). Long-press (or right-click) → Item sheet.
- Each card lists that day's custom entries for the meal under a small label "What I ate", then Tertiary buttons "+ Add food" and "+ What I ate".
- Merged-in items appear at the end of the target card under "From <meal name>".
- Unchecking or deleting shows a snackbar "Unchecked <name>" / "Deleted <name>" with Undo that restores the exact row.

**Scope sheet** (ModalBottomSheet) titled "Remove <name>" or "Add <name>": four radio rows "Just this day (<Mon, 6 Oct>)", "Every <Monday> from now", "From now on", "For a date range" (shows start and end pickers when selected); a Primary "Continue" opens ConfirmDialog with the exact sentence. Used for items and whole meals.

13.7.10 **Timeline view**: vertical scroll from 6:00 AM (mold 360) to 2:00 AM (mold 1560), extended to include wake and every block or slot outside that range. 64dp per hour. Start gutter 52dp with hour labels (label, textTertiary) and 1dp hour lines (border). Draw, in this order: class and event blocks, the workout block, meal pills, the now line (today only). Blocks: radius md, title label SemiBold + time caption; classes and events use their item colour as a 4dp start bar on a surface fill with a 1dp border; workout uses a 1dp dashed textTertiary border. Overlapping busy blocks split the width (max 3 columns). Meal pills: height max(duration scaled, 40dp), card fill, 1dp border, 4dp start bar in the meal colour, text "Lunch · 1:30 PM · 2/3". Tapping a pill opens a sheet with that meal's rows; tapping a class or event opens its day-list actions (13.7.4). The static grid (hour lines and labels) is drawn in one `drawWithCache` (15.3). Opening the timeline on today scrolls so the now line is at one third of the viewport height.

13.7.11 **FAB** — 56dp squircle, accent fill, "+" in onAccentIcon, no shadow; opens the Add sheet. Bottom content padding = navigation-bar inset + 96dp so the last card scrolls above the FAB.

13.7.12 **Add sheet** — a food-first flow:
1. Meal chips (FilterChip per visible meal; preselected = the card that opened it, or from the FAB the meal whose time is nearest to now).
2. A search field "Search your foods" with results from the library (favourites first, then most recently used, then alphabetical; prefix and word matches, case- and accent-insensitive), each row showing name, serving (or "per 100 g"; recipes show their serving label and a "Recipe" label) and kcal. Tapping a result adds it as a **custom entry for this day** with an amount field: a servings stepper (0.25 steps) for SERVING foods, or a grams field prefilled with `defaultGrams` (with the computed kcal and protein shown live) for PER_100G foods — or, with "Also add to this meal's checklist" turned on, opens the Scope sheet in Add mode to create a meal item.
3. "New food" expands inline fields: Name (required), "Values are" segmented "Per serving" / "Per 100 g", Serving (optional, placeholder "e.g. 1 bowl"; per-100 g foods show "Usual amount (g)" instead), Calories with an "I don't know" checkbox (disables the field and saves null), Protein (g) with its own "I don't know", and "Save to my foods" (default on). Save logs it, adds it to the library when chosen, and applies the checklist option above.
Buttons: "Cancel" and "Add" (Primary, enabled when a food is chosen or the new-food name is filled).

13.7.12a **Copy and repeat.** "Copy from another day…" (Today overflow) opens a date picker (default yesterday) and then a sheet listing that day's meals with their "What I ate" entries, each meal with a checkbox (all on by default) and a "Also copy checked items" switch (off by default). Copy (confirmed: "Copy <n> entries from <date> to <date>?") inserts new custom entries with the same snapshots, and, if chosen, checks the same meal items on this day where they are visible. Each meal card's overflow also has "Repeat <yesterday's / last <weekday>'s> entries", which copies that meal's custom entries from the previous day it had any (within 14 days). Nothing is copied twice: an entry with the same name, portion and meal already on this day is skipped.

13.7.13 **Item sheet** (long-press a row): title = food name. Multiplier chips "×0.5", "×1", "×1.5", "×2" plus "Custom…" (checks the item if unchecked). Fields "Calories this day" and "Protein this day", each with "I don't know" (they edit only that day's log row). Buttons: "Edit food…" (opens the food editor, 13.16; changes apply to the library food and therefore today and future days), "Edit in this meal…" (servings or grams, star, optional), "Remove from checklist…" (Scope sheet) and, for custom entries, "Delete entry".

### 13.8 Schedule (route `Schedule`)

13.8.1 **Layout.** Top bar "Schedule" with a "Today" text button when the selected date is not today, and an overflow with "Show tasks" (checkable, `settings.showTasksInCalendar`; hidden until 1.1) and "Import calendar file…" (14.7). Under it a SingleChoiceSegmentedButtonRow: "Calendar" and "Semester".

13.8.2 **CalendarStrip in schedule mode.** Header row: year (label, textTertiary) above the month name (titleLg) with a chevron button beside it; tapping the month name or chevron expands/collapses the month grid. Week row: seven columns, weekday name (label) above the date number (body); the selected date has a 36dp accent circle; today when not selected is bold with a 4dp ink dot above the weekday. Under each date up to 3 dots (dotSm, 4dp apart): one per class or event that day (and timed task if "Show tasks" is on), in each item's colour, ordered by start time; a 4th or later item adds nothing. Weekends' numbers use textTertiary. Swiping the week row changes week; swiping the month grid changes month. Expanded, the grid shows all weeks of the month (leading/trailing days of other months in textTertiary). Days inside a holiday show a 3dp grey dash under the number instead of class dots (events still show).

13.8.3 **Day list** (under the strip; the selected date): every class session, event and (if enabled) timed task of that date, sorted by start time (all-day events first, then timed items; tasks with no time last under a small "Tasks" label). Each row (min 64dp):
- a 32dp calendar icon (`Icons.Outlined.CalendarMonth`; tasks use `Icons.Outlined.TaskAlt`) tinted in the item's colour;
- line 1: course "<code> – <name>" or event title, in the item's colour (bodySm, up to 2 lines);
- line 2: kind label in textPrimary (body): "Class", "Extra class", "Event", the task's list name, or for a task linked to a course "Due · <course code or name>" (the row then uses the course colour);
- line 3: time range in textSecondary (caption) — "Oct 8 · 10:30 AM–12:20 PM", plus " · <location>" when set; cancelled classes and events show "Cancelled" in danger colour and strikethrough on line 1.
Tapping a row opens the item's sheet: classes → "Cancel for this day" / "Restore", "Edit this date…", "Edit class…"; events → "Cancel this occurrence", "Edit…", "Delete…"; tasks → open TaskDetail.
Holiday days show a card "<Holiday name> — no classes" at the top of the list. Empty: "Nothing scheduled."

13.8.4 **"+" FAB** opens a small menu (DropdownMenu anchored to the FAB): "Class", "Extra class", "Personal event", "Task" (task appears only when "Show tasks" is on). Each opens its form prefilled with the selected date (and, for classes, its weekday).

13.8.5 **Course and class form** (sheet): Course name (required), Code (optional), Location (optional), Colour (ColorPicker), Sessions: a list of rows "Mon · 10:30 AM–12:20 PM" (plus " · Week A" / " · Week B" / " · every 3 weeks" when not weekly) with a × to remove, and "+ Add session" (weekday single-choice chips, Start, End, and "Repeats": "Every week" (default) / "Every 2 weeks" / "Every 3 weeks" / "Every 4 weeks"; when not weekly, "Starts in" chips "Week 1" … "Week n" of the semester, labelled "Week A" / "Week B" for 2 weeks; BB2a). At least one session ("Add at least one session"). Save asks nothing for a new course; editing an existing course's sessions confirms "Change <name> for every week of <semester>?". "Delete course" confirms "Delete <name> and all its classes from <semester>? Past meal logs are not affected." If no semester covers the selected date, the form's top shows "Add a semester first" with a button to the Semester segment and Save is disabled.

13.8.6 **Event form** (sheet): Title (required); Type segmented "Personal" / "Extra class" (Extra class shows a course dropdown and uses its colour); Colour (Personal only); All-day switch; Date; Start and End (hidden when all-day); Location; Repeat dropdown: "Does not repeat", "Every day", "Every week on…", "Every month on day <N>", "Custom…" ("Every week on…" shows 7 weekday chips; "Custom…" shows "Every [number] [days|weeks]" and weekday chips for weeks); Ends (when repeating): radios "Never", "On [date]", "After [N] times"; Note. A caption preview line repeats the summary, e.g. "Every Tue, Thu · 6:00–7:30 PM · until 20 Dec" or "Once · Sat 11 Oct · 5:00–9:00 PM". Saving a repeating event confirms with the summary sentence. Editing or deleting an occurrence of a repeating event or a class session first asks "Only <date>" or "All" (RC1), then confirms.

13.8.7 **Semester segment**: a list of semesters (name, "1 Aug – 5 Dec 2026", "Current" badge on the one containing today). "+ Add semester" opens a sheet: Name, Start date, End date (C-CONFIRM: "Save <name> from <start> to <end>? Your timetable repeats between these dates."). Tapping a semester expands it to show its holidays ("Diwali break · 20–26 Oct") with "+ Add holiday" (sheet: name, start, end within the semester) and long-press/right-click → Delete (confirmed), plus "Copy timetable from <previous semester>" (copies courses and sessions, not exceptions; confirmed) and "Delete semester" (confirmed; explains that its courses and classes are deleted).

### 13.9 Tasks (route `Tasks`, version 1.1)

A task manager in this app's own style. It must not copy another product's name, logo, icons or artwork.

13.9.1 **Lists navigation.** Compact and medium width: a `ModalNavigationDrawer` opened by a menu button at the start of the top bar (`gesturesEnabled = drawerState.isOpen`, plus `BackHandler` to close). Expanded and large: a permanent lists pane (listsPane width). Contents: smart lists with an icon and an open-task count — "Inbox" (`Inbox`), "Today" (`Today`), "Tomorrow" (`WbSunny`), "Next 7 Days" (`DateRange`), "Calendar" (`CalendarMonth`, no count), "Completed" (`CheckCircle`, no count), "Trash" (`Delete`, no count); header "Lists" with each user list (ColorDot + name + count) and "+ New list" (dialog: name + ColorPicker); header "Courses" (only when the current semester has courses) with each course (ColorDot + code or name + count of open linked tasks; tapping lists that course's open deadlines by due date, like Canvas's per-course to-do); header "Tags" with each tag ("#gym" + count). Long-press/right-click a tag: "Rename" (dialog; renaming to an existing tag merges them, confirmed) and "Delete" (confirm: "Delete tag #<name>? It is removed from <n> tasks; the tasks stay."). Long-press/right-click a list: "Rename", "Change colour", "Delete" (confirm: "Delete list <name>? Its tasks move to the Inbox."). The tab opens on "Today" the first time and remembers the last list (rememberSaveable for the session).

13.9.2 **Top bar**: menu button (compact/medium), title = current list name (titleLg), sort button (`Icons.AutoMirrored.Outlined.Sort`) with "Sort by due date" (default), "Sort by priority", "Sort by title".

13.9.3 **Contents** (LazyColumn, keys "h-<group>", "t-<taskId>", "c-<taskId>", "done-header"; contentType "header" / "task" / "completed"):
- Group headers (label SemiBold, textSecondary, lg top padding). Due-date sort: "Overdue" (danger), "Today", "Tomorrow", "Next 7 Days", "Later", "No date". Priority sort: "High", "Medium", "Low", "None". Title sort: no headers. Empty groups are hidden.
- Order within a group (due-date sort): timed tasks by time, then all-day, then priority (high first), then `createdAtMillis`. Priority sort: due (no date last), then title. Title sort: case-insensitive alphabetical.
- Every list and count excludes tasks in the Trash. Smart lists: Inbox = open tasks with no list. Today = open tasks due today plus all overdue (under "Overdue"). Tomorrow = due tomorrow. Next 7 Days = due today..today+6 plus overdue, one header per day ("Today", "Tomorrow", "Wed, 8 Oct", …). Tag = open tasks with that tag. Completed = closed tasks (done, and won't do if `showWontDo`), newest first, headers per completion day. Course = open tasks linked to that course. Trash = deleted tasks, newest first, each row with "Restore" and "Delete forever" in its menu, a caption "Removed for good after <trashDays> days" (with `trashDays` 0: "Kept until you empty the Trash") at the top and an "Empty trash" button in the top bar (confirmed: "Permanently delete <n> tasks? This can't be undone.").
- At the bottom of every list except Completed: "Completed (n)" with a chevron, collapsed by default; expanding shows that list's completed tasks (dimmed, strikethrough, checked). Tapping their checkbox reopens them.
- Calendar smart list: the CalendarStrip in "tasks" mode (dots in the priority colours of the day's first 3 open tasks) with the selected day's tasks listed below. Quick add defaults to the selected day.

13.9.4 **Task row** (min 56dp, no swipe gestures): 22dp checkbox with a 2dp border in the priority colour (checked = filled, with the check in whichever of #141413 / #FFFFFF contrasts more with the fill, as 13.4's onColor); tap toggles completion (repeat rules TK3; 300 ms tap guard), the task leaves the open list and a snackbar "Completed <title>" offers Undo. Title (body, max 2 lines). Meta line (label, shown only when non-empty, items 8dp apart): due text ("Today", "Tomorrow 6:00 PM", "Fri", "15 Oct", "15 Oct 2027"; danger colour when overdue), repeat icon, reminder icon, "2/5" for subtasks, tags ("#gym"), and in smart lists the list's ColorDot + name. Tap the row → TaskDetail. Long-press/right-click → menu "Date…", "Priority" (High/Medium/Low/None), "Move to…", "Course…", "Won't do" (snackbar "Marked <title> as won't do" with Undo; TK4a), "Delete" (moves to the Trash; snackbar "Moved to Trash" with Undo). Won't-do rows in Completed show a circled × instead of the checkbox.

13.9.5 **Quick add** (FAB → ModalBottomSheet above the keyboard): single-line field "Add a task" focused automatically (FocusRequester after a 150 ms delay, then `keyboardController.show()`), ImeAction.Done = save. Under it, parse chips showing what the parser understood (date/time, priority, #tags, ~list); each chip's × disables that part (the words stay in the title). A row of icon buttons: Date (opens Date sheet), Priority (flag in the chosen priority colour, menu), List (menu), Tag (inserts "#"), Send (enabled when not blank). **Suggestions** (when `quickAddSuggestions` is on): typing `*` at the start of a word shows a row of chips above the field — "Today", "Tomorrow", "Next week", "Pick a date…" — choosing one sets the date (as a parse chip) and removes the `*`; typing `~`, `#` or `@` shows matching lists, existing tags or courses (filtered as the user types, max 6); choosing one completes the word. Esc or a space closes the row. Defaults from the current list: user list → that list; Inbox → no list; Today → due today; Tomorrow → due tomorrow; tag → that tag; Next 7 Days / Calendar / Completed → Inbox, due today (Calendar: the selected day). Explicit choices and parsed text override defaults. After saving, the field clears and stays open.

13.9.6 **Date sheet** (quick add, row menu, detail): chips "Today", "Tomorrow", "Next Monday", "No date"; a DatePicker (T6); "Time": "None" or the time (TimePickerDialog) and "Clear time"; "Reminders": up to 3 rows, each an offset dropdown (timed: "On time", "5 min before", "10 min before", "30 min before", "1 hour before", "2 hours before", "1 day before"; all-day: "On the day, 9:00 AM", "1 day before, 9:00 AM") and a segmented "Notification" / "Alarm", with × to remove and "+ Add reminder" hidden at 3; switching between timed and all-day resets invalid offsets to the first valid one; "Repeat": Never / Daily (interval 1–99) / Weekly (interval 1–99 and weekday chips for Due date mode) / Monthly (interval 1 for Due date mode; 1–99 for Completion mode). Repeats have no COUNT/UNTIL end controls in 1.1 because the task model has no such fields (TK5), plus, when repeating, "Repeat from" segmented "Due date" / "Completion date" (preselected from `defaultRepeatFrom`) (TK2a; the weekday chips hide for Completion date). Buttons "Clear" (removes date, time, reminders, repeat) and "Done".

13.9.7 **Task detail** (`TaskDetail(taskId)`; full screen on compact, detail pane on expanded): back button, priority flag menu, overflow "Move to…", "Won't do", "Delete" (moves to the Trash; go back, snackbar with Undo). Body: checkbox + title field (Inter 20sp SemiBold, multi-line, no underline); due chip ("Add date" or the due text with repeat/reminder icons) → Date sheet; list chip (dot + name) → Move to; course chip ("Add course" or the course's dot + code) → a course picker (current semester first, "None"); tags as InputChips with × and an "+ Tag" chip (dialog; stored lowercase without "#"); notes field (body, placeholder "Notes", grows); "Subtasks" header, subtask rows (20dp checkbox in a 48dp target, title, × delete) and an "Add subtask" field (Done adds and keeps focus); caption "Created <date>". Edits save immediately (title and notes from local state with a 500 ms debounce; never bind a text field directly to a Room Flow). A task whose title is blank when leaving is deleted without asking. If any task has a reminder and notifications are not allowed, the Tasks screen shows a one-line card "Reminders need notification permission" with "Fix".

13.9.8 **Course deadlines.** A task with a course is a deadline for it: it uses the course colour in the Schedule day list and dots, shows "Due · <code>" there, and is listed under that course in the drawer. Nothing else changes (it is an ordinary task).
13.9.9 Tasks never appear on Today and never affect meal times.

### 13.10 History (route `History`)

One scrolling screen (two panes on expanded width: calendar + stats on the start side, progress on the end side).

13.10.1 **Month calendar**: header with previous/next buttons and the month name (titleMd); horizontal swipe changes month. 7-column grid (locale first day of week). Day cell ≥ 48dp: a 40dp circle filled with the status colour at 20% alpha by completion (thresholds as StatusMarker) and the StatusMarker under the number; plain if nothing logged; nothing for future days; today has a 1.5dp ink outline; holiday days show a 3dp grey dash. Tapping a day sets SelectedDate and opens Today.
13.10.2 **Month stats card**: days logged, average kcal, average protein, current streak, best streak (W5, W6).
13.10.3 **Weight card**: latest weight (Lora 28sp, tabular figures) with unit, change since start and over the last 4 weeks (W4; coloured success when moving toward the goal, textSecondary otherwise — never danger), and the goal ("Goal 70.0 kg"). Range chips "1M", "3M", "6M", "1Y", "All" (default 3M). Chart drawn with Compose Canvas in one `drawWithCache` pass: x = date, y = weight in the display unit; weekly points (W3) as 6dp ink dots joined by a 1dp textTertiary line; the trend (W8, when on) as a 2dp accent line through the trend value at every entry; the plan line (G4, when on) dashed textTertiary; the goal line dashed success. A one-line legend under the chart names only what is drawn ("● Weigh-ins  — Trend  ┄ Plan  ┄ Goal"). y range = visible data, goal, trend and plan padded by 1 kg (2 lb); x labels as dMMM at sensible intervals (max 6); y labels at round numbers (max 5). Tapping or dragging on the chart shows a tooltip with date and weight for the nearest point (and announces it via liveRegion). 0 points → empty state "Log your first weigh-in". 1 point → a single dot with its label.
13.10.4 **"+ Log weight"** → dialog: weight (one decimal, in the display unit), date (default today), optional note. Duplicate date → W1 confirmation.
13.10.5 **Weight entries**: list newest first (date, weight, change from the previous entry, note); long-press/right-click → Edit / Delete (snackbar with Undo). The Weight card's overflow has "Export as CSV" (14.6).
13.10.6 **Weekly table** (W7), last 8 weeks, as a simple list of rows on compact width and a table on medium+.

### 13.11 Insets and system bars

IN1. `enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { isDark }, navigationBarStyle = same)` in `MainActivity.onCreate` before `setContent`, where `isDark` comes from the in-app theme (13.4); call it again whenever the theme changes so system icons stay visible.
IN2. On API 29+ set `window.isNavigationBarContrastEnforced = false` so no grey scrim appears behind 3-button navigation.
IN3. Insets are applied exactly once per edge: the app-level Scaffold uses `contentWindowInsets = WindowInsets(0)`; each screen's top bar applies `statusBarsPadding()`; the bottom bar draws its background under the navigation bar and pads only its content with `navigationBarsPadding()`; the navigation rail pads with `systemBars.only(Start + Vertical)` and `displayCutout.only(Start)`; when there is no bottom bar (rail layouts, full-screen routes), scrolling content adds the navigation-bar inset to its bottom content padding.
IN4. Content on the end side of landscape screens pads with `displayCutout.only(End)` and `systemBars.only(End)`.
IN5. Every ModalBottomSheet content column uses `navigationBarsPadding().imePadding()`. Forms scroll so the focused field is never under the keyboard.
IN6. The FAB sits above the navigation bar and keyboard (`navigationBarsPadding()`), 16dp from the end edge and the bottom of its pane.
IN7. Test every screen with gesture navigation, 3-button navigation, a display cut-out on the left and right in landscape, the keyboard open, and in split-screen (19.4).

### 13.12 Adaptive layouts

AD1. Read `currentWindowAdaptiveInfo().windowSizeClass`. Width classes: Compact < 600dp, Medium 600–839dp, Expanded 840–1199dp, Large ≥ 1200dp. Height class Compact < 480dp.
AD2. **Navigation.** Compact width → custom bottom bar: surface background extending under the navigation bar, 1dp top divider, five equal items 64dp tall, a 48×32dp indicator (radius indicator) behind a 24dp icon, label (label style) 4dp below. Selected: indicator fill = border colour, icon in accent, label in textPrimary; unselected: textTertiary, no indicator. Medium and wider → custom navigation rail (80dp) with the same indicator, icon and label, items starting 24dp below the top inset, surface background, 1dp end divider. Icons: Today `Outlined.CheckCircle`, Tasks `Outlined.TaskAlt` (from 1.1), Schedule `Outlined.DateRange`, History `Outlined.CalendarMonth`, Settings `Outlined.Settings`. In 1.0 there are four items (Tasks hidden); in 1.1 five, in the order listed. Keyboard focus moves through items with arrow keys.
AD3. **Panes per destination.**

| Destination | Compact | Medium | Expanded / Large |
|---|---|---|---|
| Today | single column | single column, max 640dp, centred | day pane (calendar strip, health card, schedule chip, warnings, doesn't-fit, Next meal, Summary) + meal pane (cards or timeline) |
| Schedule | strip + day list | same, max 640dp centred | month grid always expanded on the start pane (dayPane width) + day list on the end pane |
| Tasks | drawer + list; detail full screen | drawer + list; detail full screen | permanent lists pane + list pane + detail pane (`ListDetailPaneScaffold`; detail shows a "Select a task" placeholder when none) |
| History | single column | single column, max 640dp | calendar + stats \| weight + weekly table |
| Settings | list; category opens full screen | list; category opens full screen | categories list + category detail (`ListDetailPaneScaffold`) |

AD4. Panes respect foldable hinges (the adaptive scaffolds do this; custom two-pane rows use `calculatePaneScaffoldDirective` / `currentWindowAdaptiveInfo().windowPosture.hingeList` to avoid placing content under a separating hinge).
AD5. Bottom sheets, dialogs and menus are the same on every size (sheet max width 640dp). On expanded width, task detail and food editor open in panes instead of full-screen routes where the table above says so.
AD6. Changing size (rotation, fold, window resize) keeps the selected destination, list, task, date and scroll position.
AD7. Roborazzi screenshot tests cover each destination at Compact (411×891dp), Medium (700×1000dp), Expanded (1280×800dp) and a phone in landscape (891×411dp), light and dark, font scale 1.0 and 2.0 (18.2).

### 13.13 Settings and Preferences (route `Settings`)

Every value saves immediately; the plan, alarms, widget and theme update at once. Every row shows its current value as a caption (min 56dp, title in body, value in caption). On expanded width Settings is list-detail (AD3).

13.13.1 **Search.** A search field at the top of Settings ("Search settings"). It matches titles, values and keywords from `PreferenceRegistry` (13.13.5), case- and accent-insensitive, and lists results as "Title — Page › Section". Choosing one opens that page, scrolls to the row and highlights it with a surface fill for 1.5 s.

13.13.2 **Settings root**, in this order:

| Row | Contents |
|---|---|
| Goals | Goal type (Gain / Lose / Maintain), calorie target, protein target, "Different targets on some days" (switch; when on, seven rows Monday–Sunday in `firstDayOfWeek` order, each showing "Same as every day" or its values; tapping one opens a dialog with Calories and Protein fields, each with "Same as every day"), goal weight, weekly change, weight unit |
| Meals and food | "Edit meals" (13.16), "Food library", per-meal alarm switches (`sections.alarmEnabled`) |
| Day and timing | New day starts at (12:00–6:00 AM), wake time, wake before first class (switch + minutes), big-meal gap, snack gap, max automatic move for fixed meals, latest meal time |
| Workout | Workout switch; days; timing (after a meal: meal + minutes / at a fixed time); duration |
| **Preferences** | opens the Preferences hub (13.13.3) |
| Backup | Export backup; Import backup; last export; weekly auto-backup switch; choose folder; last auto-backup; "Export weights as CSV" (14.6); "Import calendar file (.ics)" (14.7); caption "Backups are not encrypted. Keep them somewhere private." |
| About | Version (e.g. "0.3.0-alpha.1 (Alpha)"); Licences; Alarm diagnostics; Crash reports; Engine check; Device help; "Run setup again"; privacy statement |

While the version name contains "-alpha", a card at the top of Settings says "You're testing an Alpha. Things may break — please report them." with "Report a bug" (opens `https://github.com/OafishWither69/OTasks/issues/new?template=bug_report.yml` in the browser with `ACTION_VIEW`) and "Copy device info" (version, Android version, manufacturer, model to the clipboard).

13.13.3 **Preferences hub** — five pages. Each page ends with "Reset to defaults" (confirmed: "Reset <page> to defaults?"); the hub ends with "Reset all preferences" (confirmed: "Reset every preference to its default? Your meals, foods, logs, schedule, tasks and goals are not changed."). Resetting restores the 7.1 defaults for that page's fields only.

| Page | Contents |
|---|---|
| **Alarms & sound** | Alarm health card; "Test alarm"; "Show alarm details on the lock screen"; "Ring after a restart, before unlocking" (AL3); **Default alarm** profile: sound (ringtone picker `RingtoneManager.ACTION_RINGTONE_PICKER` for TYPE_ALARM, with "Default alarm sound" and "Silent"), volume (Phone's alarm volume / Fixed + slider 10–100%), gradual volume (Off / 15 / 30 / 60 s), vibration pattern (Standard / Pulse / Heartbeat / Long / None, each with "Preview" that vibrates once), ring length, snooze length, maximum snoozes, flash screen; **When the phone is…** Normal / On vibrate / On silent / In Do Not Disturb / In a call, each a dropdown (Sound and vibrate / Sound only / Vibrate only / Screen only; "In a call" offers only the last two). Then **Meal alarms**, **Task alarms** and **Weigh-in alarm**, each opening the same form with a "Use default alarm settings" switch at the top (when on, the fields show the default's values, disabled). |
| **Notifications & reminders** | Meal alarms (master switch; asks for permission when turned on); follow-up reminder (Off / 15 / 30 / 45 / 60 / 90 min); "Missed" notification; starred-items nudge (Off / time); "Stop meal alarms after reaching my calorie target"; "Upcoming alarm notice" (Off / 15 / 30 / 60 / 120 min before); "Morning overview" (switch + "At wake time" or a time); daily summary (switch + time); weigh-in reminder (switch, weekday, time, Notification / Alarm); default style for new task reminders (Notification / Alarm); "Notification categories" (opens `ACTION_APP_NOTIFICATION_SETTINGS`) |
| **Display & motion** | Theme (System / Light / Dark); Use wallpaper colours; Accent colour (7 swatches, disabled while wallpaper colours are on); Refresh rate (Highest available / Standard (60 Hz) / Let the system decide) and "Keep highest in battery saver"; Animations (Normal / Reduced / Off); Haptic feedback; Text size in the app (90–130% slider with a live preview line); Density (Comfortable / Compact); Default Today view (Cards / Timeline); **Charts:** "Weight trend line", "Trend smoothing" (Slow 5% / Normal 10% / Fast 20%), "Plan line" |
| **Calendar & time** | Time format (System / 12-hour / 24-hour); First day of the week (Locale default / Monday / Sunday / Saturday); Date format (Locale default / Day-Month-Year / Month-Day-Year / Year-Month-Day — also used by quick add); Open the app on (Today / Tasks / Schedule / History / Last used); Show timed tasks in the schedule; Completion markers ("Complete from" 50–100%, "Partly from" 10% to complete − 5%, with a preview of the three marker shapes) |
| **Behaviour** | Confirm single-day changes; Auto-collapse finished meals; Show calories; Show protein; Show "Next meal" card; Undo time (4 / 10 / 20 s); **Tasks:** "Keep deleted tasks for" (7 / 30 / 90 days / Until I empty the Trash); "Default repeat from" (Due date / Completion date); "Show won't-do tasks in Completed"; "Smart recognition in quick add"; "Suggestions while typing *, ~, # and @"; **Voice assistant** (from 1.2, 13.18): "Let the phone's assistant use OTasks", "Allow changes" |

13.13.3a Until version 1.1, rows and options that only concern tasks are hidden: the "Tasks" choice of "Open the app on", "Default style for new task reminders", "Default repeat from", "Show timed tasks in the schedule", the Task alarms profile page and the Behaviour page's Tasks section.

13.13.4 Turning meal alarms on asks for notification permission first; if denied, the switch turns back off and a snackbar says "Notifications are needed for alarms". An option the device cannot honour stays visible with a caption explaining why (for example "This device can't vibrate" under vibration settings on a tablet without a vibrator; "Needs Android 12 or newer" never appears because minSdk is 31).

13.13.5 **PreferenceRegistry** (`ui/settings/PreferenceRegistry.kt`): one entry per preference with key, page, section, title and value-label string resources, search keywords and default. Settings pages, search and reset are built from it. Each entry also states its type and allowed values (used by PreferenceStore to validate stored values and by backup import). A unit test checks that every `Settings` key and `AlarmProfileEntity` field except ids, device-local and internal keys appears in the registry exactly once, that its registry default equals the entity default, that page reset restores it, and that it round-trips through the backup DTO.

### 13.14 Setup (route `Setup`)

Shown full screen whenever `onboardingDone == false` (first launch, or "Run setup again"). Top: "Step 2 of 5" (label, textTertiary) and a 5-segment progress bar. Bottom: "Back" (Tertiary, from step 2), "Skip" (Tertiary) and "Next" (Primary; "Finish" on step 5). Nothing is required; Skip and Next both move on. Content is max 560dp wide and scrolls.

1. **Welcome** — a Secondary button "Restore from a backup" (14.4; on success setup ends and Today opens), app name (titleLg), one line "Plan your meals around your day and get reminded on time." (1.1 changes it to "Plan your meals around your day, get reminded on time, and keep track of your tasks."), and Units: weight in kg / lb (default U2).
2. **Your goal** — Gain / Lose / Maintain (segmented); Daily calories target and Protein target (optional fields with G5's note); Current weight and Goal weight (optional, in the chosen unit); Weekly change (hidden for Maintain).
3. **Your day** — "My day starts at" (default 4:00 AM, explained as "Meals after midnight still count for the day before until this time"); Wake time; "I'm a student" switch → semester name, start and end dates (optional) and "Wake before my first class" (switch + minutes).
4. **Workout** — "I work out" switch → days, timing and duration (optional).
5. **Alarms** — one line "OTasks can ring like an alarm clock when it's time to eat.", the Meal alarms switch, the Alarm health card, the Device help entry, and "Test alarm".

Finish (and Skip on the last step) saves everything entered, always including `weightUnit` (so the unit never changes later with the locale; U2 is only the initial suggestion) (a weight → WeightEntity for logical today; a semester → SemesterEntity), sets `onboardingDone = true` and opens Today, which shows the "No meals yet" empty state.

### 13.15 Engine check (route `EngineCheck`)

Two sections: "Timing check" (Ex1–Ex8) and, from version 1.1, "Parser check" (Q1–Q16). Each row: id, one-line description, and PASS (success) or FAIL (danger). A FAIL row expands to list expected vs actual for each mismatch. Runs on `Dispatchers.Default` when opened, using only the in-code fixtures (never the user's data or settings).

### 13.16 Food library and meal editor

13.16.1 **Food library** (route `FoodLibrary`; a pane on expanded width): search field; filter chips "All", "Favourites", "Recipes", "Archived"; rows with name, serving, kcal and protein, star toggle; tap → food editor sheet (Name, "Values are" Per serving / Per 100 g, Serving or Usual amount (g), Calories + "I don't know", Protein + "I don't know", Favourite). Changing SERVING ↔ PER_100G is blocked while any meal item or recipe ingredient references the food: "Used in meals or recipes — create a new food for a different basis". The check and save share a transaction. Amount conversion is never guessed; the user can create a separate food and deliberately replace references. Snapshot-only historical logs do not block a basis edit because they do not recalculate. Saving edits shows "Changes apply to today and future days. Past days keep what was logged." when the food is used by any meal item. Long-press/right-click → "Archive" (if used anywhere) or "Delete" (if never used; confirmed). FAB "New food"; the food editor's "Values are" offers "Per serving" / "Per 100 g" / "Recipe", and choosing Recipe (or tapping a recipe) opens the recipe editor (13.16.5).

13.16.2 **Meal editor** (route `MealEditor`): the list of meals active today or later, in sortOrder, each with ColorDot, name, timing summary ("Fixed · 1:30 PM", "Flexible · around 4:00 PM", "30 min after waking", "60 min after Dinner", "45 min after workout") and up/down buttons to reorder (also drag handles with keyboard alternatives). Tap → meal form. "+ New meal" → meal form then Scope sheet (Add). A meal's overflow: "Remove…" (Scope sheet).
13.16.3 **Meal form** (sheet): Name; Size (Snack / Big meal); Colour (ColorPicker); Timing (dropdown: "Fixed time", "Flexible around a time", "After waking", "After another meal", "After workout") with the fields each needs — time; minutes after waking; meal + minutes; minutes after workout plus "On rest days: <meal> + minutes" (or "use <time>"); How long it takes (minutes); Alarm (switch). Anchor choices list only Fixed and After-waking meals (section 7.2 validation). Edits apply to the meal row (S4), no scope needed.
13.16.4 **Foods in a meal**: inside the meal form, a "Foods" list with each item's food, servings stepper, star and optional toggles, reorder, and "+ Add food" (library search or new food → Scope sheet in Add mode). Removing uses the Scope sheet.
13.16.5 **Recipe editor** (route `RecipeEditor(foodId)`; a pane on expanded width): Name; Serving (e.g. "1 bowl"); "Makes [n] servings" (stepper, 0.5 steps); Favourite; an "Ingredients" list — each row the food name, its amount (servings stepper for per-serving foods, grams field for per-100 g foods) and its kcal, with reorder and × — and "+ Add ingredient" (library search excluding recipes, or a new food); a totals card "Whole recipe: 1,240 kcal · 62 g protein" and "Per serving: 620 kcal · 31 g protein" (RX2) with "<n> ingredients have unknown calories" when relevant. Save is enabled with a name and at least one ingredient. Saving a recipe already used in meals shows "Changes apply to today and future days. Past days keep what was logged." Recipes can be logged, starred and added to meals exactly like any per-serving food.

### 13.17 Home-screen widget (Glance)

WG1. `NextMealWidget`, default 3×2 cells, resizable in both directions, `SizeMode.Responsive` with three layouts: small (2×1: next meal name and time), medium (3×2: next meal line, items line, calories progress), large (4×3: medium plus the rest of today's meals with check counts).
WG2. Lines: "Next: <Meal> · <time>" (same rule as 13.7.7; the corresponding completed/no-upcoming/no-plan state from 13.7.7, "No meals yet" when there are no meals); "<n> items · <kcal> kcal"; "Today <kcal> / <target> kcal" with a thin progress bar (no target → "Today <kcal> kcal").
WG3. Colours from `GlanceTheme` mapped to the app palette (and to dynamic colours when `dynamicColor` is on); the meal dot is the only item colour. Text sizes follow the user's font scale; nothing is clipped at 2.0.
WG4. Tap → MainActivity on Today (logical today). Updated by the debounced trigger (A11), the maintenance alarm and every alarm action (`NextMealWidget().updateAll(context)`), plus `updatePeriodMillis = 1800000`.
WG4a. `android:widgetCategory="home_screen"` only (never `keyguard`), so meal names and calories are not offered as a lock-screen widget.
WG5. Widget code imports only `androidx.glance.*` (never `androidx.compose.ui` or material3). Data is read inside `provideGlance` with repository suspend functions before `provideContent`.

### 13.18 Voice control through the phone's assistant (version 1.2, Phase 14)

OTasks exposes a small set of actions to the phone's own AI assistant (for example Gemini) through Android **AppFunctions**, so the user can say "I had two eggs for breakfast, log it" or "skip lunch today" and the assistant runs the action inside OTasks. The assistant runs and calls OTasks **on the device**; OTasks still has no `INTERNET` permission and sends nothing anywhere (P1). What the user *says* goes to the assistant's maker under the assistant's own privacy terms — the Preferences page says so.

VC0. **Gate.** Phase 14 starts only when (1) the AppFunctions Jetpack library (`androidx.appfunctions`) is publicly available to all developers (beta or stable, not partner-only early access), and (2) the owner has at least one device on which the assistant calls third-party AppFunctions. Before that, nothing in this section is built. Claude Code checks both at the start of Phase 14, records the library version and the test device in PROGRESS.md, and stops to tell the owner if either is missing. Library and annotation names below follow the library as announced; use the names of the version actually released and update this section first (0.5).

VC1. **Architecture.** One file per area in `app/voice/` (`MealFunctions`, `TaskFunctions`, `ScheduleFunctions`, `WeightFunctions`), each function annotated as an app function with a plain-English description and typed, documented parameters. Every function only calls existing repository methods (the same ones the UI uses) — no new business logic, no direct DAO access — so voice and touch always behave the same. AppFunctions is declared only on the API levels and devices where the platform supports it; elsewhere nothing is registered and the Preferences rows say "Not available on this phone".

VC2. **Functions** (meal/weight/class dates relative to logical today (T1), task dates relative to calendar today (TK1); meal and course names are matched case- and accent-insensitively, exact name first, then a unique prefix; an ambiguous or unknown name returns an error listing up to 5 valid names):

| Function | Parameters | Does |
|---|---|---|
| `getNextMeal` | — | Returns the next meal (13.7.7 rule): name, time, items left, kcal left |
| `getTodaySummary` | — | Returns today's calories and protein vs targets (G0) and checklist % |
| `logFood` | `food` (text), `amount` (number, optional), `unit` ("servings" \| "grams", optional), `meal` (optional) | Finds the food in the library (same search as 13.7.12); adds it as a custom entry ("What I ate") to the named meal, or the meal nearest now. Unknown food → error "Not in your food library" (it never creates foods with guessed values, P6) |
| `checkMeal` | `meal` | Marks all unchecked non-optional items of that meal today as eaten (like "Mark all eaten") |
| `skipMeal` | `meal` | Skips that meal today (SKIP override) |
| `pinMealTime` | `meal`, `time` | Pins that meal's time for today (PIN_TIME) |
| `pauseMealAlarmsToday` | — | Sets `day_states.alarmsPaused = true` for today: AL12 skips every meal alarm that day (log SKIPPED "paused"); Today shows a caption "Meal alarms paused today" with "Resume" |
| `cancelClassToday` | `course` | Cancels that course's sessions today (CANCELLED exceptions) |
| `logWeight` | `weight` (number), `unit` ("kg" \| "lb", optional = the user's unit) | Logs today's weight; if today already has one, returns an error asking the user to change it in the app (W1 needs a confirmation) |
| `addTask` (1.1+) | `text` | Runs the quick-add parser (10.2); creates only a non-repeating task due calendar today or without a date; future/past dates return "Use the app to choose another date" |
| `completeTask` (1.1+) | `title` | Completes the single matching open non-repeating task due calendar today or without a date; repeating or other-day tasks return "Complete this task in the app"; several matches → error listing them |

VC3. **Only safe, reversible, single-day actions.** No function deletes anything, changes more than one day, edits the library, meals, timetable, goals or settings, or imports/exports data. Those always need the app and its confirmations (C-CONFIRM). Values are validated exactly like the UI (13.6 limits; amounts 0.25–20 servings or 1–2000 g; weight 20–300 kg); invalid input returns an error message, never a partial write.

VC4. **Every change is visible and undoable.** After a function changes data, OTasks posts a notification on a new channel `assistant` (DEFAULT importance, no sound, VISIBILITY_PRIVATE, public version "OTasks updated"): "Logged 2 × Egg to Breakfast" / "Skipped Lunch today" etc., with an **Undo** action (valid for 10 minutes; `setTimeoutAfter`; built with `setAuthenticationRequired(true)`, SP12) that reverses exactly that change. The function's return value contains the same sentence so the assistant can read it back. Each call is written to `alarm_log` with event ACTION, kind OTHER, detail = the function name only (never its arguments, SP6).

VC5. **Data returned.** Read functions return only the fields listed in VC2 — never notes, the food library, history, weights other than the one just logged, or anything not asked for.

VC6. **Security.** Only callers the platform allows (the system's designated assistant, which holds the platform permission for executing app functions) can run them; OTasks adds no exported component of its own for this (SP2 is updated to list the library's service, with the permission that guards it, after the Phase 14 merged-manifest check). Every function first checks VC7 and UserManager.isUserUnlocked() before accessing credential-protected storage. Require KeyguardManager.isDeviceLocked() == false for every function returning or changing personal data; otherwise return "Unlock your phone to do this". Storage-unlocked state is not current device authentication; assistant caller permission does not waive this check. Text parameters are treated as untrusted input: length-limited (200 characters), never used in SQL except as bound parameters (D5), never used to build intents or URIs.

VC7. **Preferences** (new page section under Preferences › Behaviour, "Voice assistant"; hidden until Phase 14 ships):
- `voiceControlEnabled` Boolean, default **false** — "Let the phone's assistant use OTasks". Off by default because spoken requests leave the device through the assistant; turning it on shows a one-time explanation with that sentence and a "Turn on" button.
- `voiceAllowChanges` Boolean, default true — "Allow changes" (off = only `getNextMeal` and `getTodaySummary` work).
- When off, every function returns "Voice control is off in OTasks settings".

VC8. **Tests.** Robolectric tests call each function directly: correct repository effect, validation errors, ambiguous names, the preference switches, the locked state, the notification with Undo, and that Undo restores the exact previous state. A manual check per function on the test device (VC0).

---

## 14. Backup and restore (`app/backup`)

All file access uses the Storage Access Framework. The app requests no storage permissions.

14.1 **Format.** One UTF-8 JSON document:
```json
{ "format": "otasks-backup", "schemaVersion": 1, "appVersion": "1.0.0",
  "exportedAt": "2026-10-09T12:30:00+05:30",
  "preferences": { ... }, "alarmProfiles": [ ... ], "weekdayGoals": [ ... ], "foods": [ ... ],
  "recipeIngredients": [ ... ], "sections": [ ... ], "mealItems": [ ... ],
  "dayOverrides": [ ... ], "logEntries": [ ... ], "dayStates": [ ... ], "weights": [ ... ],
  "historyDays": [ ... ], "historyItems": [ ... ],
  "semesters": [ ... ], "holidays": [ ... ], "courses": [ ... ], "classSessions": [ ... ],
  "classExceptions": [ ... ], "events": [ ... ], "eventExceptions": [ ... ],
  "taskLists": [ ... ], "tasks": [ ... ], "subtasks": [ ... ], "tags": [ ... ], "taskTags": [ ... ],
  "taskReminders": [ ... ] }
```
Each table has its own `@Serializable` DTO (never the Room entities). `Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = false }`. `preferences` is a JSON object of key → typed value for every registry key that is not device-local (13.13.5); device-local keys (7.1) are never exported, and an import keeps this device's current values of those. Trashed tasks are exported with their `deletedAtMillis`. `alarm_log`, `alarm_state`, the generation and device-protected files are not exported. History snapshots and historyStartDay are exported; validate day/item ownership and ranges before replacement. Since no new app backup has shipped, this corrected format still starts at schemaVersion 1.
14.2 **Export.** `CreateDocument("application/json")`, default name `otasks-backup-YYYY-MM-DD.json`. Written on `Dispatchers.IO`; updates `lastExportMillis`; snackbar "Backup saved". The export screen's caption: "Backups are not encrypted. Keep them somewhere private."
14.3 **Weekly auto-backup.** "Choose folder" uses `OpenDocumentTree()`, then `takePersistableUriPermission(uri, READ | WRITE)` and saves the URI string. The check runs on `ON_RESUME` and from the maintenance alarm: if enabled, the URI is set, and `lastAutoBackupMillis` is null or ≥ 7 days old, write `otasks-auto-YYYY-MM-DD.json` into the folder (`DocumentFile.fromTreeUri`), update `lastAutoBackupMillis`, then delete older files starting with `otasks-auto-` so only the newest 4 remain. Any failure (folder deleted, permission lost after reinstall): log it, keep the setting, and on the next app open show a snackbar "Auto-backup failed — choose the folder again" with action "Choose". Never crash.
14.4 **Import.** `OpenDocument(arrayOf("application/json", "application/octet-stream", "text/plain"))`. Read and validate fully before writing anything:
- size ≤ 20 MB, enforced while reading through a counting stream that stops at 20 MB + 1 byte (the size the provider reports is not trusted); `format == "otasks-backup"`; `schemaVersion` ≤ the app's current backup schema version (older versions are upgraded in memory by `BackupCodec` step by step; newer → "This backup is from a newer version of OTasks. Update the app first.");
- every text within its limit (13.6), every number within its range, every enum value known, every foreign key resolvable within the file, ≤ 200,000 rows per table, ≤ 3 reminders per task, ≤ 50 ingredients per recipe, no recipe used as an ingredient, every `colorKey` a known palette key;
- `preferences`: each key must be a known, non-device-local registry key with a value of its type and allowed values; unknown keys are skipped (listed in Diagnostics), invalid values fail the import;
- RECIPE foods' kcal, protein and `isPartial` in the file are ignored and recomputed from their ingredients after validation (RX2), so a backup can never carry inconsistent recipe values;
- `alarmProfiles.soundUri` must be null, "SILENT", or a `content://` URI whose authority is `media` or `settings` (what the system ringtone picker returns); any other value (a `file://` path, another app's provider) is replaced by null and reported in the confirmation ("Alarm sounds were reset to the default");
- on any failure: snackbar "This file is not a valid OTasks backup" (plus the first problem in Diagnostics), nothing changes.
Then a confirmation: "Replace all data with this backup from <exportedAt date>? It has <n> foods, <n> meals, <n> logged days, <n> classes, <n> events, <n> tasks and <n> weigh-ins. Your current data will be deleted." with confirm "Replace data". On confirm, apply AL0 generation rotation/cancellation, then replace exported tables in ONE transaction (ids preserved), clear device-local alarm_state, and call rescheduleAll() and widget update. Preserve this device's local preferences. A failed replacement leaves existing data intact and rebuilds its alarms under the new generation.
14.5 The backup schema version increases whenever the database schema changes in a way that affects exported tables; `BackupCodec` keeps upgrade steps for every older version, each covered by a unit test with a stored sample file in `app/src/test/resources/backups/`.

14.6 **Weight CSV export** (`WeightCsv.kt`). From Settings › Backup or the Weight card: `CreateDocument("text/csv")`, default name `otasks-weights-YYYY-MM-DD.csv`, UTF-8 with a byte-order mark (so Excel reads non-English notes correctly), RFC 4180 (comma separator, CRLF line ends, fields with a comma, quote or line break quoted, quotes doubled). Columns: `date` (ISO `YYYY-MM-DD`), `weight_kg` (2 decimals, "." separator whatever the locale), `weight_lb` (2 decimals), `trend_kg` (W8, 2 decimals; empty when the trend line is off), `note`. **Formula-injection guard:** a `note` that starts with `=`, `+`, `-`, `@`, a tab or a carriage return gets a leading `'`, so spreadsheet apps show it as text instead of running it. Unit tests: commas and quotes in notes, a note "=HYPERLINK(...)", lb rounding.

14.7 **Calendar import (.ics)** — "Import calendar file (.ics)" in Settings › Backup and the Schedule overflow. For example, a Canvas calendar export or a university timetable file the user downloaded.
- `OpenDocument(arrayOf("text/calendar", "application/octet-stream", "text/plain"))`; read through a counting stream that stops at 5 MB + 1 byte ("This file is too large to import").
- Parsed by `IcsParser` in `:domain/ics`: a single-pass line reader (no regular expressions on whole lines, no recursion) handling line unfolding, `\,` `\;` `\n` `\\` escapes, and at most 10,000 characters per **unfolded** line (longer → the file is rejected). Only `VEVENT` (1.0) and, from 1.1, `VTODO` are read; `VALARM`, `ATTACH`, `URL`, `ATTENDEE`, `ORGANIZER` and everything else are ignored. **Nothing in the file is fetched, opened or executed.**
- Fields: `UID`, `SUMMARY` → title, `LOCATION`, `DESCRIPTION` → note, `DTSTART`/`DTEND` or `DURATION`, `VALUE=DATE` → all-day, `TZID=` → that ZoneId if known; unknown TZID rejects that component and is counted in preview (never silently assume device zone), `Z` times → converted from UTC to the device zone, floating times (no TZID, no Z) → the device zone; `RRULE` with `FREQ=DAILY|WEEKLY` (+ `INTERVAL` 1–99, `BYDAY` plain weekdays for weekly), `FREQ=MONTHLY` with interval 1, `COUNT` 1–999 (larger/invalid values are unsupported, never silently clamped), `UNTIL` → the event's repeat fields; `EXDATE` → CANCELLED exceptions; a `RECURRENCE-ID` override on the same date → a MOVED exception (or CANCELLED when `STATUS:CANCELLED`), and one that moves the occurrence to another date → a CANCELLED exception plus a separate one-off event; at most 500 exceptions and overrides per event; `STATUS:CANCELLED` events are skipped. Anything the repeat model cannot express (e.g. `BYDAY=2TU`, `BYMONTHDAY` lists, `FREQ=MONTHLY` with `INTERVAL` > 1, `FREQ=YEARLY`, `WKST` other than MO with `INTERVAL` > 1) imports the first occurrence only and is counted as "simplified". **Zero-length timed events** (`DTEND` = `DTSTART`, typical of assignment due dates) are deadlines, not busy time: in 1.0 they are skipped and counted ("<n> deadlines skipped — import them as tasks in version 1.1"); from 1.1 they are offered as tasks. Events longer than one day import as all-day on their first day and are counted as simplified. Timed recurring sources in UTC or a zone different from the device zone cannot be represented faithfully by local molds across DST: import only the first occurrence and count/explain the simplification in preview. Local-zone and floating recurrences use the supported local recurrence model; later device-zone changes keep their saved wall times. Embedded VTIMEZONE definitions are not executed/interpreted; a recognized TZID uses the system ZoneId rules. Times are converted to molds with T2 (an event ending after midnight continues past 1440).
- Every value is cut to the 13.6 limits (titles 60, location 40, note 500), and at most 5,000 events per file are read.
- **Preview sheet** before writing: "Found <n> events (<n> repeating, <n> simplified) from <date> to <date>"; "Import as" — "Personal events" (default) or "Extra classes for…" (course dropdown, uses its colour); Colour (personal; default the next palette key, 13.4); "Skip events that have already ended" (default on). From 1.1, an "Import as tasks (deadlines)" choice with a course picker turns each item into an all-day or timed task with that course (13.9.8). Then C-CONFIRM: "Import <n> new and update <n> events?".
- **Identity/re-import.** Match `(importUid, importRecurrenceId)`, with empty recurrence id for the master. Canonical RECURRENCE-ID is its original ISO date for all-day, original ISO local date-time for floating, or original instant for zoned/UTC times, never the moved destination. A cross-date move keeps the original-date CANCELLED exception and one detached row with that recurrence id; reimport updates that same row. Reject duplicate conflicting keys in one file before writing. No UID → duplicate detection by title/day/times; do not claim update support for UID-less edits. For a matched imported master, the preview explicitly says its imported fields and exception set will be replaced, including local edits; an omitted component does not delete an existing row. Imported task completion/Trash state and local tags/reminders are retained; new imports start open. Everything writes in one transaction (D6), then A11 reschedules.
- **VTODO/deadlines (1.1).** SUMMARY/title, DESCRIPTION/notes, DUE/calendar dueDay and dueMold, UID and RECURRENCE-ID identity, optional chosen course; DUE absent → use DTSTART, both absent → skip with preview count. VALUE=DATE is all-day; zoned/UTC/floating dates follow the rules above. Ignore file completion, organiser, links and alarms; never silently complete a local task. Supported DUE_DATE repeats use TK2 with no end condition; finite COUNT/UNTIL or unsupported task repeats import as a single deadline and are counted as simplified. An all-day VEVENT deadline uses DTSTART, not its exclusive DTEND. Preview lists every skipped/simplified category and the retained first occurrence before confirmation.
- Unit tests in `:domain/ics` with sample files in `domain/src/test/resources/ics/`: a weekly class with `EXDATE`, a cross-date moved occurrence imported twice then moved again (one detached row), local edits replaced only after preview, VTODO DUE/DTSTART/no-date, an all-day deadline, a UTC time and foreign-zone recurrence across DST with explicit simplification, an unsupported rule, folded lines, escaped commas, and a malformed file (fails cleanly with "This file is not a calendar file OTasks can read").

---

## 15. Performance

The app should feel instant and scroll without dropped frames on 60–144 Hz displays, including budget tablets.

15.1 **Lists.** Every `item`/`items` in a lazy list has a stable unique key and a `contentType` shared by items of the same layout ("section", "task", "header", "event" …). Keys never change when an item is checked or edited.
15.2 **Stable state.** UI state classes are `@Immutable` (A5). Event lambdas passed to rows are remembered (`remember(viewModel) { … }`) or method references, so a row recomposes only when its own data changes. Toggling one item must not recompose other rows (verified with a Compose recomposition-count test on Today, 18.2).
15.3 **No work while drawing.** No sorting, filtering, recurrence expansion, totals, string formatting of numbers/dates or `occursOn` loops inside composables; all of it happens in ViewModels on `Dispatchers.Default` (A4). Static visuals with many parts (timeline hour grid, month grids, the weight chart) are drawn in one `Modifier.drawWithCache` pass; movable children use the lambda forms `Modifier.offset { }` / `graphicsLayer { }`.
15.4 **Time.** Anything that depends on the current time reads a minute ticker held in the ViewModel (emits once per minute, aligned to the minute boundary), never a per-frame clock. The alarm screen clock uses the same.
15.5 **Database.** Indices as in section 7. Range queries (week, month, 8 weeks) are single queries. No query on the main thread (A9).
15.6 **Startup.** Cold start to first frame under 600 ms on a mid-range phone in release builds: no work in `Application.onCreate` beyond creating the container and channels and launching the background volume-restore (AL21) and, when unlocked, the A11 trigger; the database opens lazily on first use. Include `androidx.profileinstaller` and a Baseline Profile module (`:baselineprofile`, generated with the Macrobenchmark plugin) covering cold start and scrolling Today, Schedule and Tasks.
15.7 **Refresh rate.** PL13.
15.8 **Budget.** Release builds are minified (B3). The APK stays under 10 MB.

---

## 16. Security and privacy

Threat model in one paragraph: the data is personal (diet, weight, timetable, tasks) but there is no server, no account and no network, so the realistic risks are (1) other apps on the phone abusing an exported component or an intent, (2) someone holding the locked phone, (3) a malicious or broken file the user imports, (4) personal data leaking into logs, crash reports, exports or system backups, and (5) a compromised build pipeline shipping a tampered APK. Each rule below names which one it covers.

SP1. No `INTERNET` permission and no network code anywhere. (Opening `dontkillmyapp.com` hands a URL to the user's browser; the app itself makes no connection.)
SP2. **(1)** Exported components are exactly: `MainActivity` (launcher and app shortcuts), `BootReceiver` (protected system broadcasts only; it checks the action, AL1) and the widget's `NextMealWidgetReceiver` (must be exported for `APPWIDGET_UPDATE`; it handles only the standard `APPWIDGET_*` actions, and the widget's taps use explicit `actionStartActivity` intents). Everything else is `exported=false`. The Phase 10 audit reads the **merged** manifest (`app/build/intermediates/merged_manifests/release/`) and lists every exported component, including ones added by libraries (expected: only androidx.profileinstaller's `ProfileInstallReceiver`, which is guarded by the `android.permission.DUMP` permission); anything else is removed with `tools:node="remove"` or justified in PROGRESS.md. The app declares no ContentProvider, no `VIEW`/`SEND` intent filters for files or links, and no deep-link scheme (the `otasks://` URIs of 12.3 are only ever used inside explicit intents).
SP3. **(1)** All PendingIntents are immutable and wrap explicit intents (12.3).
SP3a. **App shortcuts** (static, `res/xml/shortcuts.xml`): "Log food" (opens Today on logical today with the Add sheet open), "Next meal" (opens Today scrolled to the Next meal card), "Log weight" (opens History with the log-weight dialog). Version 1.1 adds "Add task". Each shortcut is an explicit intent to MainActivity with an `action` extra from a fixed set; unknown values are ignored.
SP4. **(1)** MainActivity treats every incoming intent as untrusted: accept `epochDay` only within ±3650 days of today, `taskId` only if that task exists and is not in the Trash, the shortcut `action` only from its fixed set; ignore anything else. It never forwards a received intent, never reads a `Parcelable` or `Intent` extra, and never starts another component or opens a URI taken from an intent (no intent redirection). AlarmReceiver reads ids only from its own URI (12.3).
SP5. All database access goes through Room with bound parameters and escaped `LIKE` patterns (D5).
SP6. **(4)** Never write food, weight, task, event, course, preference values or file contents to Logcat, `alarm_log`, Diagnostics or crash reports. Release builds strip `Log.d`/`Log.v`/`Log.i` (R8 `-assumenosideeffects`); `Log.w`/`Log.e` carry only ids, kinds and exception class names.
SP7. **(3)** Every import is size-limited while reading and validated in full before anything is written: backups (14.4), calendar files (14.7). Parsers are single-pass with hard limits on size, line length and row counts; a malformed file can only produce an error message.
SP8. **(2)** Every notification with personal content (alarms, follow-ups, missed, upcoming, morning overview, daily summary, task and weigh-in reminders) is `VISIBILITY_PRIVATE` with a generic public version (AL17, AL26). The widget is home-screen only (WG4a).
SP9. **(4)** Android cloud backup and phone-to-phone transfer are both off (B6); user-controlled disclosure is limited to backup/CSV exports, clipboard/report sharing, and explicitly enabled assistant responses (VC5). Backups and CSV exports are plain files the user chooses to create; the export screens say they are not encrypted (14.2). CSV exports are protected against formula injection (14.6).
SP10. **(5)** Release keystore and passwords never enter the repository; CI reads them from encrypted secrets (18.4), and the release workflow is hardened as in 18.4a.
SP10a. **Crash log (4).** `OTasksApp` installs a `Thread.setDefaultUncaughtExceptionHandler` that writes the exception class names and stack frames of the whole cause chain — **without exception messages**, which can contain user text — plus app version, Android version, manufacturer and model, and the current destination name (never user content) to `files/crash/crash-<timestamp>.txt` (newest 5 kept; skipped while the user is locked, AL3), then calls the previous handler. On the next launch a snackbar says "OTasks closed unexpectedly. Share a report?" with "Share". Settings › About › "Crash reports (<n>)" lists them with "Share" (`ACTION_SEND`, text/plain, the report as `EXTRA_TEXT` — never a file URI, so no FileProvider is needed; the user picks the app) and "Delete all". Nothing is sent automatically.
SP11. A short privacy statement lives in `PRIVACY.md` and in Settings › About: "OTasks stores everything on this device. It has no internet access, no accounts, no analytics and no ads. You control backup and CSV exports and report sharing. Optional assistant access shares only the requested fields with your assistant under its own privacy terms."
SP12. **(2) Lock screen.** The alarm screen is shown over the lock screen but never opens the app without unlocking: "Open app", "Open" and "Log weight" call `requestDismissKeyguard` first (12.7 OPEN). On the alarm screen itself, Mark all eaten / Skip / Complete / Snooze / Dismiss work without unlocking, as in clock apps. In **notifications** (follow-up, missed, upcoming, task and weigh-in reminders), every action that changes data (Mark all eaten, Skip, Skip today, Complete, Won't do) is built with `Notification.Action.Builder.setAuthenticationRequired(true)` (API 31+), so it asks for the PIN when used from the lock screen; Snooze and Dismiss do not. With `alarmDetailsOnLockScreen` off, names stay hidden until unlock (AL35); before the first unlock after a restart, they are never shown (AL3).
SP13. **(1)** URLs the app opens (dontkillmyapp.com, the GitHub issue form) are fixed constants, opened with `ACTION_VIEW` + `CATEGORY_BROWSABLE` in try/catch. No URL or intent target ever comes from user data, a backup or a calendar file.
SP14. **(2)** No app lock and no `FLAG_SECURE` in version 1 (the owner chose not to have one); the Recents thumbnail may show the current screen. This is a deliberate decision, recorded here so it is not "fixed" by accident.

---

## 17. Localization, accessibility and quality gates

17.1 **Strings.** Every user-facing string is in `res/values/strings.xml` (plurals in `<plurals>`), referenced with `stringResource` / `pluralStringResource` / `context.getString`. No string concatenation for sentences: use placeholders (`%1$s`). Warnings from the planner are resource keys plus arguments (9.3). English (`values/`) is the only language at launch; the project MUST build and pass tests with a pseudo-locale (`en-XA`, `ar-XB`) enabled in debug.
17.2 **Exceptions.** The quick-add parser's keywords are English only in version 1 and are not translated; the field's placeholder and chips are.
17.3 **RTL.** `android:supportsRtl="true"`; use start/end, never left/right; directional icons use `Icons.AutoMirrored`; the weight chart's date axis runs in the reading direction (right-to-left in RTL). Verified with the `ar-XB` pseudo-locale screenshot test.
17.4 **Formats.** Dates, times and numbers follow the device locale (T8, T9, N5) unless the Calendar & time preferences override them. The first day of the week in calendars follows `firstDayOfWeek` (LOCALE = `WeekFields.of(locale).firstDayOfWeek`); weekday bit masks always use Monday = bit 0.
17.5 **Lint gates.** Build fails on: `HardcodedText`, `MissingTranslation` (once a second language exists), `UnusedResources` (warning only), `NewApi`, `InlinedApi`, `MissingPermission`, `ExportedReceiver`, `UnspecifiedImmutableFlag`, `ObsoleteSdkInt` (warning), and Compose lint rules (`ComposeModifierMissing`, `ComposeUnstableCollections` — or the equivalents in the chosen lint set).
17.6 **Accessibility gates.** Compose UI tests run `composeTestRule.enableAccessibilityChecks()` (or the ATF-based checks) on every screen; screenshot tests at font scale 2.0 (13.12 AD7); a manual TalkBack pass per phase (19.4).

---

## 18. Testing, CI and releases

### 18.1 Test layers

| Layer | Where | What |
|---|---|---|
| Domain unit tests | `:domain` (JVM) | VisibilityResolver, scopes, totals, goal semantics and day targets (G0), recipes (RX2), recurrence (events and tasks), alternating class weeks (BB2a), MealScheduler Ex1–Ex8, TaskRules (a)–(j), QuickAddParser Q1–Q16, weight series, trend (W8) and streaks, IcsParser (14.7), colour contrast and danger distance of the palette and status roles |
| Data tests | `:app` with Robolectric | DAOs (toggle double-tap safety, transactions, cascade rules), PreferenceStore (defaults for missing and invalid values, unknown keys kept), repositories, scope operations against a real in-memory Room database, recipe recompute on ingredient edit (RX3), Trash purge (TK7), tag rename/merge, backup export → import round trip, import validation failures (including bad preference values and a `file://` sound URI), .ics import update-not-duplicate, weight CSV, every migration (MigrationTestHelper) |
| Alarm logic tests | `:app` with Robolectric | rescheduleAll produces the expected AlarmManager calls (ShadowAlarmManager) for given plans; PendingIntent identity (ids 7 and 1007 stay separate, 12.3); cancel rules; follow-up rules; ringing snoozes use `setAlarmClock`; upcoming notice and morning overview; weigh-in and task reminder scheduling (trashed tasks never scheduled); boot snapshot contents (no names) and the locked-boot path; BootReceiver ignores unknown actions; malformed alarm URIs are ignored; nothing scheduled when exact alarms are denied |
| UI tests | `:app` with Robolectric + Compose UI test | key flows per phase (checklist toggle and undo, add food, scope sheet, event form validation, quick add with chips, settings toggles), accessibility checks, recomposition count on Today |
| Screenshot tests | Roborazzi | every destination and the alarm screen at the sizes in AD7, light/dark, font 1.0/2.0, `ar-XB` |
| Instrumented smoke tests | `androidTest`, emulator | launch, navigate all tabs, ring a TEST alarm and press "It works", rotate during each screen (run in CI on an API 34 emulator nightly and before releases) |
| Manual device tests | real devices | section 19.4 |

### 18.2 Rules

TS1. Every bug fix starts with a failing test that reproduces it, where technically possible.
TS2. The Engine check fixtures and the unit tests share one source of truth (`:domain/check`). Changing an expected result is a spec change (0.5).
TS3. Screenshot baselines are recorded deliberately (`./gradlew recordRoborazziDebug`) and reviewed in the pull request diff; CI verifies them (`verifyRoborazziDebug`).
TS4. No test depends on the real clock or time zone: inject a `Clock` (java.time) through AppContainer; tests use fixed clocks and zones (including a zone with DST changes, e.g. America/New_York, for the day-boundary and alarm-time tests).

### 18.3 Commands (documented in CLAUDE.md)

`./gradlew :domain:test` · `./gradlew :app:testDebugUnitTest` · `./gradlew verifyRoborazziDebug` · `./gradlew lintDebug` · `./gradlew assembleDebug` · `./gradlew assembleRelease` (needs signing env) · `./gradlew :app:connectedDebugAndroidTest` (emulator) · `./gradlew :baselineprofile:generateBaselineProfile` (device/emulator).

### 18.4 GitHub Actions

`.github/workflows/ci.yml` — on every push and pull request: checkout, set up JDK 17 and Gradle (with caching), run `:domain:test`, `:app:testDebugUnitTest`, `verifyRoborazziDebug`, `lintDebug`, `assembleDebug`; upload test reports, lint report and the debug APK as artifacts.
`.github/workflows/nightly.yml` — scheduled daily and on demand: the instrumented smoke tests on an API 34 emulator (`reactivecircus/android-emulator-runner`) and an API 31 emulator.
`.github/workflows/release.yml` — on tags `v*` (e.g. `v0.3.0-alpha.1`, `v1.0.0`): run everything in ci.yml, check that the tag equals `v` + versionName, decode the keystore from the `RELEASE_KEYSTORE_BASE64` secret, build `assembleRelease` with `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`, verify the APK signature (`apksigner verify --print-certs`, certificate SHA-256 must equal the `RELEASE_CERT_SHA256` repository variable), and create a GitHub Release with `OTasks-<versionName>.apk` and `OTasks-<versionName>.apk.sha256`. Tags containing `-alpha` are published as **pre-releases** titled "OTasks <versionName> — Alpha (Phase <N>: <phase name>)"; the body contains the CHANGELOG section for that version, a "What to test" list (the phase's manual checks from §20), "Known issues" from PROGRESS.md, and the install note from R4. Tags without a suffix are normal releases marked "Latest".
Dependabot (or Renovate) is configured for Gradle and GitHub Actions, weekly, grouped.

18.4a **Pipeline security (SP10).**
- Every workflow starts with `permissions: contents: read`; only the release job adds `contents: write`. No workflow uses `pull_request_target` or runs on `issue_comment`; secrets are used only in `release.yml`, which runs only on `v*` tags (pull requests from forks never see them).
- Third-party actions are pinned to a full commit SHA with the version in a comment (`uses: owner/action@<40-char sha> # v4.2.0`); Dependabot updates the pins. First-party `actions/*` may be pinned the same way.
- `gradle/actions/wrapper-validation` runs first in every workflow, so a tampered `gradle-wrapper.jar` fails the build.
- The signing secrets live in a GitHub **environment** named `release`, limited to tags matching `v*`; the release job uses `environment: release`. The job first checks that the tagged commit is on `main` (`git merge-base --is-ancestor $GITHUB_SHA origin/main`) and fails otherwise. `main` is protected: changes only through pull requests with green CI (Claude Code works on branches, CLAUDE.md).
- The release job runs without the Gradle build cache (`cache-disabled: true` in `gradle/actions/setup-gradle`), so nothing a pull-request run cached can end up in a signed APK.
- Optional, owner's choice: add the owner as a required reviewer on the `release` environment; each alpha then waits for one tap in GitHub before it is signed. Recorded in PROGRESS.md either way.
- The keystore is decoded to `$RUNNER_TEMP` and deleted in an `always()` step; passwords are passed as environment variables, never on the command line, and never echoed.
- The release job publishes the APK's SHA-256 and the signing certificate's SHA-256 in the release notes; README tells testers how to check them (`apksigner verify --print-certs`) and that every version must show the same certificate.

### 18.5 Releases

R1. `CHANGELOG.md` (Keep a Changelog format) is updated in every phase.
R2. Tags are `v` + versionName exactly; the release workflow fails if they differ.
R3. **Alpha after every phase.** When a phase's automated checks pass, Claude Code: (1) sets versionName to the next alpha (B2) and updates CHANGELOG.md; (2) merges to `main`; (3) creates and pushes the tag; (4) waits for the release workflow to finish (`gh run watch`); (5) confirms with `gh release view` that the pre-release exists with the APK and checksum; (6) writes the release link in PROGRESS.md and tells the owner. If the workflow fails, it fixes the cause and releases the next alpha number; it never deletes or re-uses a published tag.
R3a. Alpha releases are built with the release build type and the release signing key (applicationId `com.otasks.app`), so each one installs over the previous alpha and over to 1.0.0 without losing data. Settings › About shows the full version name, e.g. "0.3.0-alpha.1 (Alpha)".
R3b. Bugs the owner finds while testing an alpha are fixed on the same phase and shipped as `alpha.<n+1>` before the next phase starts, unless the owner says to move on.
R3c. The first non-alpha release is `v1.0.0` at the end of Phase 10; `v1.1.0` (Tasks) follows at the end of Phase 13, and `v1.2.0` (voice control) at the end of Phase 14.
R3d. **Signing setup (Phase 1, needs the owner).** Claude Code generates the release keystore with `keytool` OUTSIDE the repository (e.g. `~/otasks-release.jks`, RSA 4096, validity 30 years), shows the owner the alias and certificate SHA-256, and, with the owner's approval, stores the secrets with `gh secret set RELEASE_KEYSTORE_BASE64 / RELEASE_KEYSTORE_PASSWORD / RELEASE_KEY_ALIAS / RELEASE_KEY_PASSWORD` and the variable with `gh variable set RELEASE_CERT_SHA256`. The owner keeps a backup copy of the keystore file and passwords somewhere safe: if it is lost, no later version can be installed over the existing app.
R4. The README explains install steps for sideloading (download APK, allow "Install unknown apps" for the browser or file manager, open the APK), how to update (install over the existing app; same signing key), how to back up, and points to the in-app Device help for alarm reliability.

### 18.6 If the app ever goes to Google Play (not in version 1)

`USE_EXACT_ALARM` and `USE_FULL_SCREEN_INTENT` need a policy declaration (alarm-clock-like core functionality), `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` needs a justification or removal, a privacy policy URL and Data safety form are required, and the package name and signing key must be decided before the first upload. Nothing else in the architecture changes.

---

## 19. Known pitfalls and fixes

When a build error, crash or wrong behaviour matches an entry, apply its fix first and mention the entry ID in the commit message.

### 19.1 Build

K1. KSP version must match the Kotlin version; never fall back to kapt or downgrade Kotlin.
K2. Kotlin 2.x needs the Compose compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`) at the Kotlin version; no `composeOptions.kotlinCompilerExtensionVersion`.
K3. kotlinx.serialization needs `org.jetbrains.kotlin.plugin.serialization` at the Kotlin version.
K4. Experimental Material 3 APIs (DatePicker, TimePicker, ExposedDropdownMenuBox, SegmentedButton, ModalBottomSheet, adaptive scaffolds): opt in per file with `@OptIn`.
K5. `Modifier.menuAnchor()` without arguments is deprecated: use `menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)` (name per the current Material 3 version).
K6. `combine` with more than 5 flows: use the `combine(listOf(...))` overload or nest combines.
K7. Glance code that imports `androidx.compose.ui`/material3 crashes or does not compile (WG5).
K8. A Compose-only app has no `Theme.Material3` XML theme: use `@android:style/Theme.Material.Light.NoActionBar` (or a minimal theme in `themes.xml` with `android:windowBackground` set) for activities.

### 19.2 Runtime — layout

K9. Gap above titles or content under bars: insets applied twice or not at all — follow IN3 exactly.
K10. Grey band behind 3-button navigation: IN2.
K11. Status-bar icons invisible when the in-app theme differs from the system: IN1.
K12. Purple or tinted surfaces: set every ColorScheme role and `surfaceTint = Transparent`, `tonalElevation = 0.dp` (13.4).
K13. Dates off by one in date pickers: T6.
K14. TimePicker crash `IllegalArgumentException: initialHour should be in [0..23]`: T7 (`(mold % 1440) / 60`).
K15. "Key was already used" in lazy lists: keys must be unique strings per item type (13.9.3).
K16. Typing lags in task title/notes: local state + debounce (13.9.7).
K17. Quick-add keyboard does not open: FocusRequester after a short delay inside the sheet + `keyboardController.show()` (13.9.5).
K18. Drawer opens on the system back-swipe from the left edge: `gesturesEnabled = drawerState.isOpen` (13.9.1).
K19. Navigation argument crashes: use type-safe routes (A7) instead of string templates.

### 19.3 Runtime — alarms and Android versions

K20. `ForegroundServiceStartNotAllowedException` when an alarm fires: AL15 fallback.
K21. `MissingForegroundServiceTypeException`: the manifest type and the `startForeground` type must both be mediaPlayback (AL1, AL17).
K22. Starting the ring service from `BOOT_COMPLETED` is forbidden on Android 15+ for media-playback services: BootReceiver only reschedules (12.4).
K23. Alarm never rings after the phone was idle for hours: the wake lock (AL11), `setAlarmClock` for anything that rings, and the manufacturer's background settings (AL31).
K24. Silent alarm on vibrate/silent, or no vibration: AL19, AL22; `setAudioAttributes` before `setDataSource`.
K25. `SecurityException` from `setStreamVolume` in Do Not Disturb: AL21.
K26. Swiping the ringing notification leaves sound playing: AL24.
K27. Full-screen intent shows only as a heads-up when unlocked: expected (AL39).
K28. Exact alarms rejected on Android 12–13: `USE_EXACT_ALARM` exists only from API 33; on 31–32 `SCHEDULE_EXACT_ALARM` may be revoked — AL8 and the health card.
K29. Phone's alarm volume left at the app's level after a crash: AL21's restore from the device-protected `saved-alarm-volume` file, launched from `OTasksApp.onCreate`.
K30. The app shows yesterday after midnight: T4.
K31. Persisted folder permission lost after reinstall: expected; 14.3.
K32. Predictive back: never override `onBackPressed`; use `BackHandler` (AL2).
K33. Tablet with no vibrator / no default alarm sound: PL9, AL20, AL22.
K34. Alarm screen status-bar icons invisible on black: AL34.
K35. Two meals (or reminders) never both ring, or the wrong one is cancelled: PendingIntents compared equal and replaced each other — give each its own data URI (12.3); never derive request codes from `id % n`.
K36. A snooze rings late with the screen off: it was scheduled with `setExactAndAllowWhileIdle`, which Doze rate-limits and may defer — ringing snoozes use `setAlarmClock` (12.7). Test with `adb shell dumpsys deviceidle force-idle`.
K37. "Open settings" on the Device help sheet always lands on App info: `resolveActivity` returns null for other apps' activities on Android 11+ (package visibility) — just try `startActivity` and catch (AL31).
K38. Alarms missed after an overnight restart until the phone is unlocked: the direct-boot path (AL3) is missing or a component lacks `directBootAware`; or code touched the database before unlock and crashed.
K39. The alarm is silent when the phone is in use and the ring service could not start: the fallback must use an alarm-sound channel with `FLAG_INSISTENT` (AL15), not the silent `alarms` channel.
K40. Grey or beige fills on cards, text fields, switches or search bars: a Material component was used with default colours that read `surfaceContainerHighest`/other container roles — use the 13.6 specs (13.4).
K41. Data appears on a new phone without an import (and maybe crashes with a Room downgrade error): Android backup or transfer was left on — B6 excludes every domain from both.
K42. Battery drain on phones with LTPO/adaptive refresh: a display mode was pinned — PL13 leaves the mode to the system on `hasArrSupport()` displays.

### 19.4 Real-device test matrix

The emulator cannot reliably show full-screen alarms over the lock screen, alarms after reboot, vibration on silent, or manufacturer battery behaviour. Before each release, test the features implemented in that phase and regressions in earlier phases; future features are marked not applicable, never passed. The full alarm/device matrix is required for Phases 6, 7, 10 and 13; earlier phases require launch/navigation/layout/font/TalkBack checks on a phone and tablet. For the full matrix, test on at least: one OnePlus/OPPO phone on Android 15+, one Samsung phone, one Pixel or stock-Android phone or emulator image with Play, one Android tablet (no vibrator), and one Xiaomi/Redmi phone if available. For each: alarm over the lock screen; alarm with the screen on; vibrate mode; Do Not Disturb; during a call; after reboot (unlocked); **after a reboot left locked** (the alarm must ring before unlocking, with no names shown); after 8+ hours idle overnight; snooze ×3, including one snooze in forced Doze (`adb shell dumpsys deviceidle force-idle`); "Open app" from the locked alarm screen asks to unlock; follow-up; task alarm; widget update; split-screen; rotation; gesture and 3-button navigation; font size largest; TalkBack on Today and the alarm screen. Record results in PROGRESS.md's device table, with each device's Android version and its battery-optimisation state for OTasks (Unrestricted / Optimised / Restricted), because alarm results depend on both.

---

## 20. Build phases

### 20.0 How every phase runs

1. **Plan** — Claude Code (Sonnet 5.5) re-reads the phase and the spec sections it names and writes a short plan in PROGRESS.md.
2. **Build** — in small, compiling, tested steps on a branch `codex/phase-<n>-<name>`.
3. **Automated checks** — all tests, screenshot verification and lint pass locally and in CI.
4. **Review** — a fresh Claude Code session using **Opus 5.5** (not the session that built it) reviews the phase's pull request against this spec: every numbered item of the phase, the rules it cites, the hard rules in CLAUDE.md, test coverage, and anything that looks like drift (renamed things, invented behaviour, missing strings, hard-coded values). Findings go into the pull request as a checklist; the builder fixes them; the reviewer confirms. Only then is it merged.
5. **Alpha release** (R3) — versionName per B2, CHANGELOG.md updated, tag pushed, GitHub pre-release verified, link recorded in PROGRESS.md and sent to the owner with the "What to test" list.
6. **Owner testing** — the owner (and friends) install the alpha and tick the manual checks. Bugs → fixed on the same phase and released as the next alpha (R3b), each fix reviewed the same way.
7. **Done** — the next phase starts only when the owner says so.

Each phase adds its own strings, Settings/Preferences rows, unit tests and screenshot tests; nothing is left for "later" unless the phase says so.

| Phase | Version | Adds | Risk |
|---|---|---|---|
| 1 | 0.1.0 | Prototype review and removal, project, hardened CI, signing, theme, navigation shell, complete database (key/value preferences), backup/transfer rules, meal domain rules | Medium |
| 2 | 0.2.0 | Setup, Today checklist, custom entries, weekday targets, display/calendar/behaviour preferences | Medium |
| 3 | 0.3.0 | Food library (per serving, per 100 g and recipes), meal editor, scopes, bare-minimum day, copy and repeat | Medium |
| 4 | 0.4.0 | Schedule: semesters, holidays, courses (weekly and A/B weeks), events, repeats, Canvas-style calendar | Medium |
| 5 | 0.5.0 | Timing engine, Engine check, timeline, warnings, doesn't-fit | High (logic) |
| 6 | 0.6.0 | Meal alarms core: scheduling, PendingIntent identity, ring service and audible fallback, alarm screen and lock-screen rules, actions and snoozes, restart before unlock, health card, test alarm | High (devices) |
| 7 | 0.7.0 | Alarm profiles and per-mode behaviour, device help, diagnostics with simulated restart, notifications page, daily summary, upcoming notice, morning overview | High (devices) |
| 8 | 0.8.0 | History, streaks, weight tracking and chart (trend and plan lines), weight CSV, weigh-in reminder | Low |
| 9 | 0.9.0 | Settings completed, search and reset, backup and auto-backup, calendar (.ics) import, wallpaper colours, app shortcuts, crash log | Medium |
| 10 | 1.0.0 | Widget, performance, accessibility, localisation, security and device audit, **1.0 release** | Medium |
| 11 | 1.1.0 | Tasks A: lists, smart lists, Trash, won't do, task rows, quick add (plain), sorting and groups | Medium |
| 12 | 1.1.0 | Tasks B: task detail, subtasks, tags (rename/delete), repeats (from due date or completion), course deadlines, Date sheet | Medium |
| 13 | 1.1.0 | Tasks C: task reminders, smart quick-add parser with suggestions, task calendar, tasks and deadlines in the schedule, .ics deadlines as tasks, **1.1 release** | Medium |
| 14 | 1.2.0 | Voice control through the phone's assistant (AppFunctions), **only once the VC0 gate is met**, **1.2 release** | Medium |

Versions: phases 1–9 release `0.<phase>.0-alpha.<n>`. Phase 10 releases `1.0.0-alpha.<n>`, then `1.0.0`. Phases 11–13 release `1.1.0-alpha.<n>` with n continuing across the three phases, and Phase 13 ends with `1.1.0`. Phase 14 releases `1.2.0-alpha.<n>` and ends with `1.2.0`. Release titles always name the phase.

### Phase 1 — Foundation and data

1. **Prototype review and removal** (0.7). Read the prototype's code and `docs/SPEC.md`. Write a short "Prototype review" in PROGRESS.md: what worked, what broke, and questions for the owner about anything this spec does not cover. Tag the last prototype commit `prototype-final`, push the tag, then delete every prototype file in one commit. Do not continue until the owner has seen the review.
2. Gradle project exactly as section 4 (modules `:app`, `:domain`, `:baselineprofile`), version catalog, B1–B7, `.gitignore`, `.editorconfig`, README skeleton, LICENSE (GPL-3.0-or-later; every Kotlin file starts with `// SPDX-License-Identifier: GPL-3.0-or-later`), PRIVACY.md, CHANGELOG.md, `.github/ISSUE_TEMPLATE/bug_report.yml` (fields: what happened, steps, expected, app version, device and Android version, "Copy device info" paste box, screenshots).
3. Theme: tokens, colours (light/dark), bundled fonts with licences, type, motion (13.2–13.5).
4. MainActivity: edge-to-edge (IN1–IN2), refresh rate (PL13, fixed to HIGHEST until the preference exists in Phase 2), no orientation lock, StrictMode in debug.
5. Adaptive navigation shell: bottom bar on compact, rail on medium+ (AD2), four destinations (Today, Schedule, History, Settings) each showing its title and "Coming in a later phase".
6. CI (18.4 ci.yml), Robolectric, Roborazzi and lint green; Dependabot. Release signing (R3d) and release.yml. Pipeline hardening (18.4a) from the first workflow: permissions, SHA-pinned actions, wrapper validation, the `release` environment and the tag ruleset (the last two need the owner's approval, like the secrets).
7. Every entity, DAO and the database from section 7 (version 1, schema exported; includes history_days, history_items and device-local alarm_state) — including the tables first used by later phases (weekday_goals, recipe_ingredients, task tables, tags, task_tags) — Converters, mappers, AppContainer (A1), PreferenceStore and the `Settings` class with every key of 7.1, repositories for meals and preferences, the A11 trigger (logging only for now), and the four alarm-profile rows.
7a. Backup and transfer rules (B6 `data_extraction_rules.xml`, everything excluded).
8. `:domain`: LogicalDay and Mold (T1–T3, T7), VisibilityResolver (8.1), Totals and Completion (8.2), Goals (8.3), Scopes (8.4), amount factor (N2) — with full unit tests, and Robolectric repository tests for toggleItem double-tap safety and every scope operation, H1/H2 materialization/correction, immutable past targets/denominators, UNIQUE log enforcement and scope log/override remapping.

Checks: [ ] Alpha `v0.1.0-alpha.1` is on GitHub Releases as a pre-release and installs on a phone and a tablet. [ ] Prototype review written; tag `prototype-final` exists; no prototype application/source/build/resource file remains (historical references in specification/evidence documents are allowed). [ ] App launches on API 31 and the newest API; light/dark follow the system. [ ] Bottom bar on a phone, rail on a tablet and on a landscape phone; rotating keeps the selected tab. [ ] Nothing under the status bar or navigation bar with gesture and 3-button navigation. [ ] "Show refresh rate" in Developer options shows the highest rate inside the app while scrolling (on adaptive-refresh panels it may drop when the screen is still; PL13). [ ] `app/schemas/1.json` committed; no seed content (only the four alarm-profile rows; the preferences table starts empty). [ ] The Actions tab shows every workflow with read-only default permissions and pinned actions.

### Phase 2 — Setup and Today

1. Setup (13.14) steps 1–3 (steps 4 and 5 arrive in Phases 5 and 6; until then they are skipped).
2. Today in Cards view: 13.7.1–13.7.2, 13.7.7–13.7.9, 13.7.11, the Add sheet's new-food/custom-entry path (13.7.12, with new foods saved to the library) and the Item sheet's per-day fields (13.7.13). The checklist switch creates a daily item from logical today onward (S2 From now on), after confirmation; full scope selection/library search waits for Phase 3. Meal times = `idealMold` for FIXED/FLEXIBLE and `wakeMold + offset` for AFTER_WAKE; AFTER_* meals use their idealMold until Phase 5. Meals can be created from the empty state with the meal form (13.16.3) in its simple form (name, size, colour, fixed or flexible time).
3. Check, uncheck, Undo snackbars, goal semantics on the summary card.
4. Settings root (13.13.2) with Goals, including "Different targets on some days" and G0 with its unit tests (every target shown in Phase 2 comes from `DayTargets`); the Preferences hub with Display & motion, Calendar & time and Behaviour pages fully working (theme, accent, refresh rate — replacing Phase 1's fixed behaviour — animations, haptics, text size, density, time and date formats, first day of week, start destination, markers, confirmations, show/hide calories and protein, Next meal card, undo time) and their per-page reset.

Checks: [ ] Fresh install shows setup; finishing it shows "No meals yet". [ ] Create Breakfast, Lunch and Snack; log foods with and without known kcal; totals and "· 1 unknown" are right. [ ] Check, uncheck with Undo, double-tap quickly: never two log rows. [ ] Gain / Lose / Maintain change the progress colours and status text as 8.3. [ ] Give Saturday its own calorie target: Saturday's summary card uses it and says "Saturday targets"; Friday uses the everyday target; turning the switch off restores the everyday target everywhere. [ ] Every Display & motion, Calendar & time and Behaviour preference changes the app immediately (refresh rate with "Show refresh rate", accent, text size, 24-hour time, Sunday-first week, hiding calories). [ ] Tablet: Today shows the two-pane layout.

### Phase 3 — Foods, meals and scopes

1. Food library (13.16.1) with both bases (N2), food editor, archive/delete rules (D4). Recipes: `recipe_ingredients`, RX1–RX3 with unit tests, the recipe editor (13.16.5) and the "Recipes" filter.
2. Meal editor and full meal form (13.16.2–13.16.4) with all five timing types (AFTER_MEAL and AFTER_WORKOUT are stored and shown, and placed by the engine in Phase 5), foods in a meal with servings or grams.
3. The Add sheet's library search and "Also add to this meal's checklist" with the Scope sheet; Scope sheet and C-CONFIRM for every add/remove; meal-card overflow actions (pin, skip, restore, remove).
4. Bare-minimum day; Copy from another day and Repeat entries (13.7.12a).

Checks: [ ] A per-100 g food (e.g. oats, 389 kcal / 100 g) added as 60 g shows 233 kcal; a per-serving food ×1.5 multiplies correctly. [ ] Remove a food "Every <today's weekday> from now": next week's same day has no food, tomorrow does, today's earlier log stays. [ ] "Just this day" removal is back tomorrow. [ ] Bare-minimum day: only starred foods show, targets become their totals, tomorrow is normal. [ ] Editing a food updates today; past days keep their snapshot. [ ] Copy yesterday's entries into today: copied once, and copying again adds nothing. [ ] A recipe "Overnight oats" (oats 60 g, milk 1 serving, whey 1 serving) that makes 2 servings shows the right whole and per-serving totals; logging 1 serving adds half; editing the milk's calories updates the recipe; yesterday's log keeps its old value.

### Phase 4 — Schedule

1. Recurrence (9.2) with unit tests: NONE on its day and the day after; WEEKLY Tue+Thu; WEEKLY Mon every 2 weeks (true, false, true on three consecutive Mondays); MONTHLY on the 31st skips 30-day months and February; DAILY COUNT 3 (true days 1–3, false day 4); UNTIL inclusive; a CANCELLED exception; a MOVED exception.
2. ScheduleRepository and busy blocks (9.1) with tests (holidays, semester bounds, cancelled and moved sessions, alternating weeks BB2a, all-day events excluded).
3. Schedule screen (13.8) with CalendarStrip, day list (classes and events; tasks come in 1.1), "+" menu (Class, Extra class, Personal event), course/class form, event form, Only this date / All, Semester segment with holidays and "Copy timetable".
4. Today's calendar strip switches to the shared CalendarStrip (completion mode) with month expansion.

Checks: [ ] Semester "Fall 2026" 1 Aug – 5 Dec with a holiday 20–26 Oct; a course with Mon + Wed sessions shows dots only on Mondays and Wednesdays inside the semester and outside the holiday. [ ] Event "Club" every Tue and Thu 6–7 PM, after 4 times: the preview text is right and exactly 4 occurrences appear. [ ] "Only this date" moves one occurrence; "All" changes every one. [ ] Cancel a class for one day: it shows Cancelled. [ ] A lab "Every 2 weeks · Week B" appears only in the semester's 2nd, 4th, 6th … weeks, and its row says "Week B". [ ] A red course and a cancelled class look clearly different. [ ] Month grid expands from the chevron and the month name; dots use each item's colour. [ ] Tablet: month grid and day list side by side.

### Phase 5 — Timing engine

1. MealScheduler exactly as 9.4, including the workout (Settings › Workout and Setup step 4) and AFTER_MEAL / AFTER_WORKOUT, with unit tests Ex1–Ex8 (9.5).
2. Engine check screen (13.15, Timing check) in Settings › About.
3. Today uses the DayPlan: order, times, moved/pinned icons, schedule chip and Day schedule sheet, warning cards, doesn't-fit cards with their actions, Rest day toggle; Settings › Day and timing.
4. Timeline view (13.7.10).

Checks: [ ] Engine check: Ex1–Ex8 PASS. [ ] Recreating Ex1's classes on a real Monday gives the same times on Today; cancelling the 2:00 PM class moves the afternoon snack back to 4:00 PM immediately. [ ] Ex4's doesn't-fit card appears and "Merge into Lunch" moves those foods under Lunch for that day only. [ ] Timeline shows classes, workout, meal pills and the now line; pills never overlap classes. [ ] Rest day toggle moves the post-workout meal as Ex6.

### Phase 6 — Meal alarms core

1. Section 12 except: per-type profiles and per-mode choices (use the DEFAULT profile's defaults, fixed), Device help, Diagnostics screen, daily summary, upcoming notice, morning overview, weigh-in (12.9) and task reminders (12.8). Included: manifest, channels (all of 12.2), PendingIntent identity (12.3), rescheduleAll (meals + maintenance), receiver with wake lock, ring service (sound, volume with the background restore, vibration, timeout, swipe), the audible fallback (AL15), actions and `setAlarmClock` snoozes (12.7), alarm screen (12.11) with the lock-screen rules (AL35, SP12) and `alarmDetailsOnLockScreen`, restart before unlock (AL3) with `ringBeforeUnlock`, health card, test alarm, `alarm_log`.
2. Preferences › Notifications & reminders (meal alarm master switch, follow-up, missed notification, starred nudge, stop after target); Preferences › Alarms & sound with the health card, Test alarm, "Show alarm details on the lock screen" and "Ring after a restart, before unlocking" (the profile settings arrive in Phase 7); per-meal alarm switches; Setup step 5; health card on Today.
3. Robolectric alarm logic tests (18.1).

Checks (real devices, 19.4): [ ] Enabling alarms asks for notification permission; denying turns the switch off. [ ] Health card all green after the Fix buttons. [ ] Test alarm rings over the lock screen in 10 s. [ ] Pin a meal 2 minutes ahead without restarting the app: it rings on time. [ ] Vibrate/silent/DND/during a call: vibration only; a tablet without a vibrator shows a silent full-screen alarm. [ ] Snooze 3 times, then no Snooze button; Mark all eaten checks everything; Skip marks the meal skipped. [ ] Ignored alarm stops after 60 s → "Missed" → follow-up later. [ ] After reboot and after an overnight idle, alarms still ring. [ ] Pin a meal 10 minutes ahead, restart the phone and do NOT unlock: it rings, showing only "Meal time"; after unlocking, normal alarms are back and nothing rings twice. [ ] Snooze with the screen off in forced Doze (`adb shell dumpsys deviceidle force-idle`): it rings on time. [ ] "Open app" on the locked alarm screen asks for the PIN first; cancelling keeps the alarm screen. [ ] With "Show alarm details on the lock screen" off, the locked alarm screen says "Meal time" and shows the meal after unlocking. [ ] Alarm screen buttons fully above gesture and 3-button navigation in portrait, landscape and split-screen.

### Phase 7 — Alarm customisation and reliability tools

1. Alarm profiles (7.1) with "Use default", per-mode behaviour (AL19), gradual volume (AL21a), vibration patterns (AL22), flash screen, sound picker; Preferences › Alarms & sound with the Meal alarms profile page.
2. Device help (AL31, DeviceQuirks; no `resolveActivity`), Diagnostics screen with "Simulate restart" (AL33).
3. Daily summary (AL6a), upcoming notice (AL6b), morning overview (AL6c) and their Notifications & reminders rows.

Checks: [ ] Changing each "When the phone is…" option changes the behaviour in that mode. [ ] Gradual volume, each vibration pattern and flash screen work; the Meal alarms profile with its own sound overrides the default. [ ] Fixed volume rings at the chosen level and restores the phone's alarm volume afterwards. [ ] Device help shows the right steps for the phone's manufacturer and "Open settings" lands somewhere useful. [ ] Diagnostics lists upcoming alarms and recent events; "Copy report" contains no food or meal names. [ ] Daily summary arrives at the chosen time with the right numbers. [ ] Upcoming notice set to 30 min: "Lunch at 1:30 PM" appears at 1:00 PM, "Skip today" skips lunch (Restore brings it back), and the notice disappears when the alarm rings. [ ] Morning overview arrives at the planned wake time with the right count. [ ] "Simulate restart" reports the number of upcoming alarms. [ ] On a Xiaomi/vivo/Huawei phone "Open settings" opens the manufacturer's screen, not App info.

### Phase 8 — History and weight

1. History (13.10) with month grid, stats and weekly table.
2. Weight logging, chart, entries, W1–W8 with unit tests (trend line, plan line G4, legend), the Charts preferences, and weight CSV export (14.6).
3. Weigh-in reminder (12.9), its rows in Notifications & reminders and the Weigh-in alarm profile page; Today's "Weigh-in day" line.

Checks: [ ] Completed days turn green in the month grid and the week strip; holidays show the dash. [ ] Tapping a past date opens it on Today with the "Today" button. [ ] Two weights a week apart: two points; the goal and plan lines follow the goal type; kg/lb changes labels, not data. [ ] Fifteen daily weights spanning 14 days that go up and down around a slow drop: the trend line is smooth and the card shows "Trend … /week"; with only ten daily weights the rate is omitted (W4); the trend and plan switches hide their lines. [ ] Exported CSV opens in a spreadsheet with the right numbers; a note starting with "=" shows as text. [ ] Range chips change the chart. [ ] The weigh-in reminder works as a notification and as an alarm; "Log weight" opens the dialog.

### Phase 9 — Settings, backup and finishing touches

1. Settings and Preferences complete (13.13): Day and timing, Workout, every Preferences page, wallpaper colours, settings search, "Reset all preferences", About, the Alpha card, and the PreferenceRegistry completeness test (13.13.5).
2. Backup export, import and weekly auto-backup (14.1–14.5) with round-trip and validation tests.
2a. Calendar import (14.7, events only) with the `:domain/ics` tests and the preview sheet.
3. App shortcuts (SP3a) and the crash log (SP10a, without exception messages).

Checks: [ ] Changing the calorie target updates Today at once; changing the big-meal gap changes a real day's plan; Engine check still passes. [ ] Export, change things, import: everything is back exactly; alarms are rescheduled. [ ] A random JSON file and a truncated backup change nothing and show the error. [ ] Auto-backup writes a file on the next app open; only 4 are kept. [ ] Wallpaper colours: the app follows the wallpaper; status and item colours stay readable. [ ] Searching "vibrate", "120", "volume" and "24" finds the right rows. [ ] Page reset and "Reset all preferences" restore defaults without touching meals or goals. [ ] Each app shortcut opens the right place. [ ] A deliberate test crash (debug menu) produces a report that can be shared. [ ] "Report a bug" opens the GitHub issue form. [ ] Export a Canvas (or any) calendar as .ics and import it: the preview counts are right, classes land on the right days and times, importing the same file again adds nothing new. [ ] A backup edited to set the alarm sound to a `file://` path imports with the sound reset to default.

### Phase 10 — Widget, audits and 1.0

1. Widget (13.17).
2. Performance (section 15): baseline profile generated and committed, recomposition test, cold-start measurement in PROGRESS.md, release build smoke-tested on a real device.
3. Accessibility (13.6, 17.6), localisation (pseudo-locales, RTL) and adaptive (AD7) passes.
4. Full real-device matrix (19.4) in PROGRESS.md.
4a. Security audit (section 16): merged-manifest exported-component list (SP2), every notification's lock-screen version, Logcat of a full session contains no personal data, crash report contains no messages, CI workflows checked against 18.4a. Findings and fixes recorded in PROGRESS.md.
5. README (install, update, backup, alarms help, privacy) and CHANGELOG; `v1.0.0-alpha.<n>` for the final device round, then `v1.0.0` as a normal release once the owner signs off.

Checks: [ ] Widget shows the next meal, updates within seconds of checking a food, resizes cleanly, opens Today. [ ] Smooth scrolling on Today, Schedule and History on a 120 Hz device. [ ] Release APK installs over the last alpha on a phone and a tablet and keeps all data. [ ] `v1.0.0` is published as the latest release with APK and checksum.

### Phase 11 — Tasks A (1.1)

1. Tasks destination appears in navigation (AD2). TaskRepository and TaskRules (10.1) with tests (a)–(f) and (j).
2. 13.9.1–13.9.5 without the parse chips, suggestions, the Calendar smart list, the Courses header and reminders: drawer / permanent lists pane, smart lists including Trash (TK6–TK7, purge in AL7), user lists (create, rename, recolour, delete), tags list, sorting and groups, task rows, Completed section with won't-do rows (TK4a), row menu (without "Course…"), quick add with title and the Priority and List buttons (due date via the "Today"/"Tomorrow" list defaults only), the Behaviour page's Tasks rows that apply (Trash days, won't-do display).

Checks: [ ] Quick add "Buy oats" in Today: appears under Today. [ ] A task due yesterday shows under "Overdue". [ ] New list "College" with a colour; its tasks show the list dot in smart lists. [ ] Deleting the list moves its tasks to the Inbox after confirmation. [ ] Deleting a task puts it in Trash; Restore brings it back; "Empty trash" asks first. [ ] "Won't do" moves a task to Completed with a ×; hiding won't-do tasks in Behaviour hides it. [ ] Tablet: lists pane and task list side by side.

### Phase 12 — Tasks B (1.1)

1. Date sheet (13.9.6) without Reminders; repeats with completion and Undo (TK3–TK5), "Repeat from" (TK2a) with tests (g)–(i).
2. Task detail (13.9.7) with subtasks, tags (`tags`/`task_tags`, rename and delete from the drawer), notes, course chip and the row menu's "Course…" (13.9.8), the drawer's Courses header, list-detail panes on expanded width; the "Add task" app shortcut.

Checks: [ ] Three subtasks, tick one: the row shows "1/3". [ ] A weekly repeating task: ticking it creates the next one; Undo removes it. [ ] Tags filter the Tags smart list; renaming a tag changes it on every task. [ ] "Water plants" every 3 days from completion, ticked 2 days late: the next one is due 3 days after today. [ ] A task linked to ECON101 shows under Courses › ECON101. [ ] Tablet: list and detail side by side.

### Phase 13 — Tasks C and 1.1 (1.1)

1. Task reminders (12.8, AL6, AL26–AL28), the Reminders row of the Date sheet, the Task alarms profile page, the "Reminders need notification permission" card.
2. QuickAddParser (10.2) with tests Q1–Q16, parse chips, `*` `~` `#` `@` suggestions, the smart-recognition and suggestions preferences, Parser check on the Engine check screen.
3. Calendar smart list; timed tasks and course deadlines in the Schedule day list and dots; "Show tasks" preference; "Task" in the schedule's "+" menu; .ics import "as tasks (deadlines)" (14.7).
4. Re-run the Phase 10 audits on everything added in 1.1, then release `v1.1.0`.

Checks: [ ] Engine check: Q1–Q16 PASS. [ ] "gym tomorrow 6pm !high #fitness" shows chips; cancelling the date chip keeps "tomorrow 6pm" in the title. [ ] A notification reminder 2 minutes ahead arrives; "Complete" ticks the task; the lock screen shows only "Task reminder". [ ] An alarm reminder rings with Complete / Snooze / Open. [ ] Turning meal alarms off does not stop task reminders. [ ] Timed tasks appear in the Schedule day list in their list colour, and course deadlines in the course colour with "Due · <code>"; the toggle hides them. [ ] Typing "*" offers Today/Tomorrow/Next week/Pick a date; "@econ" offers ECON101. [ ] Importing a Canvas .ics as deadlines for a course creates tasks once; importing again updates them.

### Phase 14 — Voice control (1.2)

0. **Gate (VC0).** Check that `androidx.appfunctions` is open to all developers and that the owner's test device lets the assistant call third-party app functions. Record both in PROGRESS.md. If either is missing, stop and tell the owner; do not build anything.
1. 13.18 VC1–VC8: the functions, `day_states.alarmsPaused` with Today's "Meal alarms paused today · Resume" caption, the `assistant` channel and Undo notifications, the Voice assistant preferences with the one-time explanation, and the tests. Update SP2 after the merged-manifest check.
2. Re-run the Phase 10 security audit (4a) on the new entry points, then release `v1.2.0`.

Checks: [ ] With voice control off, asking the assistant to log food gets "Voice control is off in OTasks settings". [ ] "Log two eggs for breakfast" adds them to Breakfast and shows "Logged 2 × Egg to Breakfast" with Undo; Undo removes exactly that. [ ] "Skip lunch today" skips lunch; its alarm does not ring. [ ] "Pause my meal alarms today": no meal alarm rings today, Today shows the caption, Resume brings them back. [ ] "What's my next meal?" answers correctly. [ ] A food not in the library is refused, not invented. [ ] With "Allow changes" off, only the two questions work. [ ] On the locked phone, a change asks to unlock.

---

## 21. Glossary

| Term | Meaning |
|---|---|
| Logical day | A calendar date whose day starts at `dayStartHour` (T1) |
| Mold | Minute of logical day (T2) |
| Meal / section | A named time slot with foods (table `sections`) |
| Food | A reusable entry in the personal food library (`foods`) |
| Meal item | A food placed in a meal, with servings and flags (`meal_items`) |
| Custom entry | Something eaten that is not on the checklist ("What I ate") |
| Override | A per-day change to a meal or item (`day_overrides`) |
| Scope | How far a change reaches: this day, every weekday, from now on, date range |
| Busy block | A class, event or workout that meals must avoid |
| Day plan | The planner's output for one day: wake time, meal times, workout, warnings |
| Starred / priority | A food that still shows on a Bare-minimum day |
| Health card | The alarm permission and battery checklist (AL30) |
| Engine check | The in-app screen that runs the timing and parser examples |
| Recipe | A library food made of other foods; its values per serving are computed (RX2) |
| Weekday targets | Optional per-weekday calorie/protein targets (G0) |
| Trend line | Smoothed moving average of all weigh-ins (W8) |
| Plan line | Straight line from the first weigh-in at the planned weekly change (G4) |
| Boot snapshot | Times and kinds of the next 48 hours of ringing alarms, kept so they ring after a restart before unlock (AL3) |
| AppFunctions | Android's way for an app to offer actions that the phone's assistant can run on the device (13.18) |
| Trash | Where deleted tasks wait `trashDays` days before being removed (TK7) |
| A/B weeks | Class sessions that repeat every 2 (or 3–4) weeks (BB2a) |
| Device-local preference | A preference that belongs to one phone (folder permission, last tab…) and is never exported (7.1) |
