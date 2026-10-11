# PROGRESS.md — OTasks build tracker

This file tracks the build. It is not part of the app. Claude Code updates it at the start and end of every phase; the owner ticks the manual checks after testing on real devices.

**Spec version:** 1.3 · **Current phase:** preparation — specification corrections complete; Phase 1 not started · **Last updated:** 10 October 2026 (Asia/Calcutta)

## Phase status

| Phase | Name | Target version | Status | Latest alpha | Release link | Reviewed (Opus) | Started | Finished |
|---|---|---|---|---|---|---|---|---|
| 1 | Foundation and data | 0.1.0 | Not started | — | — | — | | |
| 2 | Setup and Today | 0.2.0 | Not started | — | — | — | | |
| 3 | Foods, meals and scopes | 0.3.0 | Not started | — | — | — | | |
| 4 | Schedule | 0.4.0 | Not started | — | — | — | | |
| 5 | Timing engine | 0.5.0 | Not started | — | — | — | | |
| 6 | Meal alarms core | 0.6.0 | Not started | — | — | — | | |
| 7 | Alarm customisation and reliability tools | 0.7.0 | Not started | — | — | — | | |
| 8 | History and weight | 0.8.0 | Not started | — | — | — | | |
| 9 | Settings, backup, finishing touches | 0.9.0 | Not started | — | — | — | | |
| 10 | Widget, audits, 1.0 release | 1.0.0 | Not started | — | — | — | | |
| 11 | Tasks A | 1.1.0 | Not started | — | — | — | | |
| 12 | Tasks B | 1.1.0 | Not started | — | — | — | | |
| 13 | Tasks C, 1.1 release | 1.1.0 | Not started | — | — | — | | |
| 14 | Voice control (only after the VC0 gate) | 1.2.0 | Not started | — | — | — | | |

Status values: Not started · Planning · In progress · Alpha released (owner testing) · Done.

## Alpha releases

One row per published alpha. Newest first.

| Version | Phase | Date | Link | What changed | Reviewed | Owner tested? |
|---|---|---|---|---|---|---|
| | | | | | | |

## Release signing

- Keystore created: no (Phase 1, SPEC R3d)
- Certificate SHA-256: —
- GitHub secrets set: no
- Owner confirmed keystore backup: no
- `release` environment (tags `v*` only) created: no (SPEC 18.4a)
- `main` branch protection (PRs + green CI): no
- Required reviewer on `release` environment (optional, owner's choice): not decided

## Prototype review (Phase 1 step 1)

Reviewed prototype commit `b76f7b8efa7d1dde3cdceb7797093833bb088e53` (9 October 2026). Static review only; existing code has not been compiled or device-tested.

- Present: Today/logging/scopes, basic timetable/events, notification/ring-service paths. The active planner ignores busy blocks; History/Tasks are placeholders.
- Broken/incomplete: wrapper scripts/JAR absent and custom debug keystore missing; bulk logging ignores visibility and is not atomic; alarm identity collisions; snoozes can ring after alarms are disabled; notification navigation uses a receiver trampoline; scope splits are not transactional; no direct-boot snapshot. Details remain in the external `OTasks-review.md` review delivered to the owner.
- Review seen: owner asked what to do next and authorized starting specification corrections on 10 October 2026. Owner explicitly chose preserving past plans/targets while allowing intake/weight corrections. The existing no-prototype-data-migration rule is unchanged; no claim of an approved or completed Phase 1 review/release.
- No prototype source/build/resource files changed during preparation.

- Tag `prototype-final`: not created yet
- Prototype removed: no

## Current phase plan

Preparation complete on branch `codex/spec-corrections`: root SPEC.md v1.3, aligned CLAUDE.md, this tracker, correction decisions/regression cases in docs/SPEC-CORRECTIONS.md. Original ZIP/reference copies are intact outside the clone.

Next: Phase 1, in small compiling steps:
1. Preserve the exact prototype commit with prototype-final; remove prototype files only after preservation, retaining the new contract/evidence documents.
2. Establish a complete pinned Gradle wrapper, JDK/toolchain and three modules; verify wrapper, debug build, domain tests and lint before growing the app.
3. Add all section 7 entities/schema, repositories and meal-domain rules, including historical snapshots, UNIQUE log enforcement and transactional scope remapping. Add meaningful unit/Room tests.
4. Add bundled fonts/licences, theme and adaptive navigation shell; screenshot/layout checks.
5. Configure CI/release pipeline and deliberate release-key custody. Signing secrets/settings and independent reviewer are still outstanding; never mark them done without evidence.
6. Run phase checks, obtain independent review, then release/test alpha. No implementation, release, model-specific review or real-device checks have occurred in preparation.

## Decisions

Internal choices the spec left open (SPEC §0.4). Newest first.

| Date | Phase | Decision | Why |
|---|---|---|---|
| 2026-10-10 | Preparation | Preserve past plans/targets; allow food-log and weight corrections (H1/H2) | Explicit owner answer during this session |
| 2026-10-10 | Preparation | Apply correction defaults and regression cases S1–S16, SPEC v1.3 | Owner authorized the proposed specification correction pass; individual technical decisions recorded in docs/SPEC-CORRECTIONS.md |

## Deviations from the spec

Anything built differently from SPEC.md, with the owner's approval. Each one should also be reflected in SPEC.md, or explain why not.

| Date | Spec rule | What was done instead | Approved by | Spec updated? |
|---|---|---|---|---|
| | | | | |

## Known issues

| ID | Found in | Description | Severity | Status |
|---|---|---|---|---|
| BUILD-1 | Prototype | Complete wrapper and custom debug keystore absent | Blocking build | Open; foundation will replace setup |
| REVIEW-1 | Preparation | Independent phase review and owner-device checks not run | Release gate | Open |
| VOICE-1 | Phase 14 | VC0 gate must be rechecked at that phase; not implemented | Deferred | Open |

## Notes for later phases

- Follow the correction regression cases by phase in docs/SPEC-CORRECTIONS.md; textual resolution is not implementation validation.
- No source changes, tags, commits, remote pushes, settings changes, signing secrets or releases were made during preparation.

## Manual checks

Based on SPEC §20. Future-feature device checks are not applicable until their phase; they must never be ticked as passed. The owner (and testers) tick these after installing the alpha; automated checks live in CI.

### Phase 1 — Foundation and data

- [ ] Alpha `v0.1.0-alpha.1` is on GitHub Releases as a pre-release and installs on a phone and a tablet.
- [ ] Prototype review written; tag `prototype-final` exists; no prototype application/source/build/resource file remains (historical references in specification/evidence documents are allowed).
- [ ] App launches on API 31 and the newest API; light/dark follow the system.
- [ ] Bottom bar on a phone, rail on a tablet and on a landscape phone; rotating keeps the selected tab.
- [ ] Nothing under the status bar or navigation bar with gesture and 3-button navigation.
- [ ] "Show refresh rate" in Developer options shows the highest rate inside the app while scrolling (on adaptive-refresh panels it may drop when the screen is still; PL13).
- [ ] `app/schemas/1.json` committed; no seed content (only the four alarm-profile rows; the preferences table starts empty).
- [ ] The Actions tab shows every workflow with read-only default permissions and pinned actions.

### Phase 2 — Setup and Today

- [ ] Fresh install shows setup; finishing it shows "No meals yet".
- [ ] Create Breakfast, Lunch and Snack; log foods with and without known kcal; totals and "· 1 unknown" are right.
- [ ] Check, uncheck with Undo, double-tap quickly: never two log rows.
- [ ] Gain / Lose / Maintain change the progress colours and status text as 8.3.
- [ ] Give Saturday its own calorie target: Saturday's summary card uses it and says "Saturday targets"; Friday uses the everyday target; turning the switch off restores the everyday target everywhere.
- [ ] Every Display & motion, Calendar & time and Behaviour preference changes the app immediately (refresh rate with "Show refresh rate", accent, text size, 24-hour time, Sunday-first week, hiding calories).
- [ ] Tablet: Today shows the two-pane layout.

### Phase 3 — Foods, meals and scopes

- [ ] A per-100 g food (e.g. oats, 389 kcal / 100 g) added as 60 g shows 233 kcal; a per-serving food ×1.5 multiplies correctly.
- [ ] Remove a food "Every <today's weekday> from now": next week's same day has no food, tomorrow does, today's earlier log stays.
- [ ] "Just this day" removal is back tomorrow.
- [ ] Bare-minimum day: only starred foods show, targets become their totals, tomorrow is normal.
- [ ] Editing a food updates today; past days keep their plan, targets and logged snapshots; correcting a past log changes actual totals only.
- [ ] Copy yesterday's entries into today: copied once, and copying again adds nothing.
- [ ] A recipe "Overnight oats" (oats 60 g, milk 1 serving, whey 1 serving) that makes 2 servings shows the right whole and per-serving totals; logging 1 serving adds half; editing the milk's calories updates the recipe; yesterday's log keeps its old value.

### Phase 4 — Schedule

- [ ] Semester "Fall 2026" 1 Aug – 5 Dec with a holiday 20–26 Oct; a course with Mon + Wed sessions shows dots only on Mondays and Wednesdays inside the semester and outside the holiday.
- [ ] Event "Club" every Tue and Thu 6–7 PM, after 4 times: the preview text is right and exactly 4 occurrences appear.
- [ ] "Only this date" moves one occurrence; "All" changes every one.
- [ ] Cancel a class for one day: it shows Cancelled.
- [ ] A lab "Every 2 weeks · Week B" appears only in the semester's 2nd, 4th, 6th … weeks, and its row says "Week B".
- [ ] A red course and a cancelled class look clearly different.
- [ ] Month grid expands from the chevron and the month name; dots use each item's colour.
- [ ] Tablet: month grid and day list side by side.

### Phase 5 — Timing engine

- [ ] Engine check: Ex1–Ex8 PASS.
- [ ] Recreating Ex1's classes on a real Monday gives the same times on Today; cancelling the 2:00 PM class moves the afternoon snack back to 4:00 PM immediately.
- [ ] Ex4's doesn't-fit card appears and "Merge into Lunch" moves those foods under Lunch for that day only.
- [ ] Timeline shows classes, workout, meal pills and the now line; pills never overlap classes.
- [ ] Rest day toggle moves the post-workout meal as Ex6.

### Phase 6 — Meal alarms core

- [ ] Enabling alarms asks for notification permission; denying turns the switch off.
- [ ] Health card all green after the Fix buttons.
- [ ] Test alarm rings over the lock screen in 10 s.
- [ ] Pin a meal 2 minutes ahead without restarting the app: it rings on time.
- [ ] Vibrate/silent/DND/during a call: vibration only; a tablet without a vibrator shows a silent full-screen alarm.
- [ ] Snooze 3 times, then no Snooze button; Mark all eaten checks everything; Skip marks the meal skipped.
- [ ] Ignored alarm stops after 60 s → "Missed" → follow-up later.
- [ ] After reboot and after an overnight idle, alarms still ring.
- [ ] Pin a meal 10 minutes ahead, restart the phone and do NOT unlock: it rings, showing only "Meal time"; after unlocking, normal alarms are back and nothing rings twice.
- [ ] Snooze with the screen off in forced Doze (`adb shell dumpsys deviceidle force-idle`): it rings on time.
- [ ] "Open app" on the locked alarm screen asks for the PIN first; cancelling keeps the alarm screen.
- [ ] With "Show alarm details on the lock screen" off, the locked alarm screen says "Meal time" and shows the meal after unlocking.
- [ ] Alarm screen buttons fully above gesture and 3-button navigation in portrait, landscape and split-screen.

### Phase 7 — Alarm customisation and reliability tools

- [ ] Changing each "When the phone is…" option changes the behaviour in that mode.
- [ ] Gradual volume, each vibration pattern and flash screen work; the Meal alarms profile with its own sound overrides the default.
- [ ] Fixed volume rings at the chosen level and restores the phone's alarm volume afterwards.
- [ ] Device help shows the right steps for the phone's manufacturer and "Open settings" lands somewhere useful.
- [ ] Diagnostics lists upcoming alarms and recent events; "Copy report" contains no food or meal names.
- [ ] Daily summary arrives at the chosen time with the right numbers.
- [ ] Upcoming notice set to 30 min: "Lunch at 1:30 PM" appears at 1:00 PM, "Skip today" skips lunch (Restore brings it back), and the notice disappears when the alarm rings.
- [ ] Morning overview arrives at the planned wake time with the right count.
- [ ] "Simulate restart" reports the number of upcoming alarms.
- [ ] On a Xiaomi/vivo/Huawei phone "Open settings" opens the manufacturer's screen, not App info.

### Phase 8 — History and weight

- [ ] Completed days turn green in the month grid and the week strip; holidays show the dash.
- [ ] Tapping a past date opens it on Today with the "Today" button.
- [ ] Two weights a week apart: two points; the goal and plan lines follow the goal type; kg/lb changes labels, not data.
- [ ] Fifteen daily weights spanning 14 days that go up and down around a slow drop: the trend line is smooth and the card shows "Trend … /week"; ten daily weights omit the rate; the trend and plan switches hide their lines.
- [ ] Exported CSV opens in a spreadsheet with the right numbers; a note starting with "=" shows as text.
- [ ] Range chips change the chart.
- [ ] The weigh-in reminder works as a notification and as an alarm; "Log weight" opens the dialog.

### Phase 9 — Settings, backup and finishing touches

- [ ] Changing the calorie target updates Today at once; changing the big-meal gap changes a real day's plan; Engine check still passes.
- [ ] Export, change things, import: everything is back exactly; alarms are rescheduled.
- [ ] A random JSON file and a truncated backup change nothing and show the error.
- [ ] Auto-backup writes a file on the next app open; only 4 are kept.
- [ ] Wallpaper colours: the app follows the wallpaper; status and item colours stay readable.
- [ ] Searching "vibrate", "120", "volume" and "24" finds the right rows.
- [ ] Page reset and "Reset all preferences" restore defaults without touching meals or goals.
- [ ] Each app shortcut opens the right place.
- [ ] A deliberate test crash (debug menu) produces a report that can be shared.
- [ ] "Report a bug" opens the GitHub issue form.
- [ ] Export a Canvas (or any) calendar as .ics and import it: the preview counts are right, classes land on the right days and times, importing the same file again adds nothing new.
- [ ] A backup edited to set the alarm sound to a `file://` path imports with the sound reset to default.

### Phase 10 — Widget, audits and 1.0

- [ ] Widget shows the next meal, updates within seconds of checking a food, resizes cleanly, opens Today.
- [ ] Smooth scrolling on Today, Schedule and History on a 120 Hz device.
- [ ] Release APK installs over the last alpha on a phone and a tablet and keeps all data.
- [ ] `v1.0.0` is published as the latest release with APK and checksum.

### Phase 11 — Tasks A (1.1)

- [ ] Quick add "Buy oats" in Today: appears under Today.
- [ ] A task due yesterday shows under "Overdue".
- [ ] New list "College" with a colour; its tasks show the list dot in smart lists.
- [ ] Deleting the list moves its tasks to the Inbox after confirmation.
- [ ] Deleting a task puts it in Trash; Restore brings it back; "Empty trash" asks first.
- [ ] "Won't do" moves a task to Completed with a ×; hiding won't-do tasks in Behaviour hides it.
- [ ] Tablet: lists pane and task list side by side.

### Phase 12 — Tasks B (1.1)

- [ ] Three subtasks, tick one: the row shows "1/3".
- [ ] A weekly repeating task: ticking it creates the next one; Undo removes it.
- [ ] Tags filter the Tags smart list; renaming a tag changes it on every task.
- [ ] "Water plants" every 3 days from completion, ticked 2 days late: the next one is due 3 days after today.
- [ ] A task linked to ECON101 shows under Courses › ECON101.
- [ ] Tablet: list and detail side by side.

### Phase 13 — Tasks C and 1.1 (1.1)

- [ ] Engine check: Q1–Q16 PASS.
- [ ] "gym tomorrow 6pm !high #fitness" shows chips; cancelling the date chip keeps "tomorrow 6pm" in the title.
- [ ] A notification reminder 2 minutes ahead arrives; "Complete" ticks the task; the lock screen shows only "Task reminder".
- [ ] An alarm reminder rings with Complete / Snooze / Open.
- [ ] Turning meal alarms off does not stop task reminders.
- [ ] Timed tasks appear in the Schedule day list in their list colour, and course deadlines in the course colour with "Due · <code>"; the toggle hides them.
- [ ] Typing "*" offers Today/Tomorrow/Next week/Pick a date; "@econ" offers ECON101.
- [ ] Importing a Canvas .ics as deadlines for a course creates tasks once; importing again updates them.

### Phase 14 — Voice control (1.2)

- [ ] With voice control off, asking the assistant to log food gets "Voice control is off in OTasks settings".
- [ ] "Log two eggs for breakfast" adds them to Breakfast and shows "Logged 2 × Egg to Breakfast" with Undo; Undo removes exactly that.
- [ ] "Skip lunch today" skips lunch; its alarm does not ring.
- [ ] "Pause my meal alarms today": no meal alarm rings today, Today shows the caption, Resume brings them back.
- [ ] "What's my next meal?" answers correctly.
- [ ] A food not in the library is refused, not invented.
- [ ] With "Allow changes" off, only the two questions work.
- [ ] On the locked phone, a change asks to unlock.

---

## Device test matrix (SPEC §19.4)

Fill one row per device per release candidate. "Battery setting" = Unrestricted / Optimised / Restricted, as shown in the phone's app battery settings. ✓ pass · ✗ fail (add a known issue) · — not applicable.

| Device | Tester | Android version / skin | Battery setting for OTasks | Lock-screen alarm | Screen-on alarm | Vibrate | DND | In call | After reboot | Reboot left locked | Overnight idle | Snooze ×3 | Snooze in Doze | Locked "Open app" asks to unlock | Follow-up | Task alarm | Widget | Split-screen | Rotation | Gesture nav | 3-button nav | Largest font | TalkBack |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| OnePlus phone |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| OnePlus Pad Go 2 (tablet) |  |  |  |  |  | — |  | — |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| Samsung phone |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| Pixel / stock Android |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| Xiaomi / Redmi (if available) |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |

## Measurements

| Date | Build | Device | Cold start (ms) | Notes |
|---|---|---|---|---|
| | | | | |
