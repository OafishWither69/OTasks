# CLAUDE.md — working rules for OTasks

OTasks is an offline Android app (Kotlin, Jetpack Compose, Room) for meal planning with smart timing, full-screen meal alarms, a student schedule, tasks, history and weight tracking. **`SPEC.md` is the source of truth.** This file tells you how to work on it.

## The prototype (read once, then delete)

This repository starts with an earlier prototype of the app. It is **reference only** (SPEC §0.7):

- In Phase 1 step 1, read it to learn what worked and what broke, and write a short review in PROGRESS.md. Ask the owner about anything in it that the spec doesn't cover; do not adopt it on your own.
- **Copy nothing.** Not code, not resources, not Gradle files, not file or class names. Every file is written fresh from SPEC.md.
- Tag the last prototype commit `prototype-final` and push the tag, then delete all prototype files in a single commit. After that, look at old code only through `git show prototype-final:<path>`, and only for reference.
- The prototype's `docs/SPEC.md` is obsolete. Only the root `SPEC.md` counts. Preserve `docs/SPEC-CORRECTIONS.md` when removing prototype files; it belongs to the rebuild contract.

## Before you start any session

1. Read `PROGRESS.md` to find the current phase, open issues and recent decisions.
2. Read the parts of `SPEC.md` the current phase needs. Its phase section (§20) lists them. At the start of a new phase, re-read §1–§9 and the phase's sections in full.
3. If the spec and the code disagree, the spec wins, unless PROGRESS.md records an approved deviation.

## How to work

- **One phase at a time.** Build only the current phase (§20). Do not start later phases, even partially. If you notice something a later phase needs, write it in PROGRESS.md under "Notes for later phases".
- **Plan, then build.** At the start of a phase, write a short plan in PROGRESS.md (files, tests, order). Then work in small, compiling steps.
- **Exact values.** Where the spec gives a number, name, colour, duration, text or file name, use it exactly. User-facing text goes in `strings.xml`.
- **Gaps.** If something affects what the user sees or how their data behaves and the spec does not say, **stop and ask the owner one short question**. If it is purely internal, choose the simplest option consistent with the spec and log it under "Decisions" in PROGRESS.md.
- **Spec changes go first.** Any change in behaviour, data model or text is written into SPEC.md (and its version line bumped) before the code, with the owner's approval.
- **Known pitfalls.** Before inventing a fix for a build error, crash or odd behaviour, check SPEC.md §19. If an entry matches, apply it and cite its ID (e.g. "K14") in the commit message.
- **Never** rename, move or delete existing files, classes, tables or columns unless the phase requires it. No TODOs, placeholder logic or demo data in committed code.

## Hard rules (never break these)

- No `INTERNET` permission, no networking, analytics, ads, Firebase or Google Play services (SP1, PL12).
- No `fallbackToDestructiveMigration`, ever. Every schema change gets a `Migration` with SQL and a `MigrationTestHelper` test; commit `app/schemas/` (D1–D3).
- No Hilt/Dagger/Koin, DataStore, SharedPreferences, WorkManager, chart or image libraries (§4.1). Preferences are key/value rows in Room via `PreferenceStore` (§7.1); a new preference needs a registry entry, not a migration.
- Preserve elapsed plans/targets under H1/H2 before edits; never recompute old completion denominators from live rows.
- Alarm cancellation uses persisted identities/generations (AL0); diagnostics are never functional state. Notification navigation uses activity PendingIntents directly.
- `:domain` stays pure Kotlin: no `android.*` imports.
- No database, file or ContentResolver work on the main thread (A9).
- `java.time` only; inject a `Clock`; never hard-code a time zone (T5, TS4).
- No orientation locks; layouts branch on window size class, never on "is tablet" (PL4, PL5).
- Manufacturer-specific code lives only in `platform/DeviceQuirks.kt` (PL11).
- Alarm code follows §12 literally: `setAlarmClock` for anything that rings (including snoozes), wake lock from receiver to service, BootReceiver only reschedules, restore the alarm volume in `finally`.
- Every PendingIntent is explicit, immutable and identified by its own data URI (§12.3). Never derive request codes or notification ids from `id % n`.
- Code that can run before the first unlock after a restart (BootReceiver, AlarmReceiver, AlarmRingService, AlarmActivity, `OTasksApp.onCreate`) checks `UserManager.isUserUnlocked()` before touching Room or credential-protected files (AL3). The boot snapshot never contains names or amounts.
- Never call `resolveActivity`/`queryIntentActivities` to decide whether to launch another app's screen; try `startActivity` and catch (AL31). No `<queries>` block, no `QUERY_ALL_PACKAGES`.
- Never log food, weight, task, event, course or preference values, and never put exception messages in crash reports (SP6, SP10a).
- Security rules in §16 are hard rules: no new exported components, intent filters, ContentProviders or deep links; never forward a received intent; every file import is size-limited while reading and validated before writing; `LIKE` patterns escape user text (D5).
- GitHub Actions: read-only default permissions, third-party actions pinned to a commit SHA, wrapper validation first, secrets only in the `release` environment (§18.4a). Never add `pull_request_target`.
- Every destructive or multi-day action shows the confirmation in §13.6 (C-CONFIRM).

## Commands

```bash
./gradlew :domain:test                       # pure Kotlin unit tests (fast)
./gradlew :app:testDebugUnitTest             # Robolectric: data, alarms, UI tests
./gradlew recordRoborazziDebug               # record screenshot baselines (deliberately)
./gradlew verifyRoborazziDebug               # verify screenshots (CI runs this)
./gradlew lintDebug                          # lint gates (§17.5)
./gradlew assembleDebug                      # debug APK (com.otasks.app.debug)
./gradlew assembleRelease                    # needs signing env vars (§18.4)
./gradlew :app:connectedDebugAndroidTest     # instrumented smoke tests (emulator)
./gradlew :baselineprofile:generateBaselineProfile
gh run watch                                 # follow the release workflow after pushing a tag
gh release view v<versionName>               # confirm the alpha release and its assets
```

Run `:domain:test`, `:app:testDebugUnitTest`, `verifyRoborazziDebug` and `lintDebug` before every commit that touches code. All four must pass.

## Definition of done for a phase

1. Every item in the phase's section of §20 is built.
2. All automated tests pass locally and in CI. New logic has tests: domain rules in `:domain`, data and alarm logic with Robolectric, UI flows with Compose tests, screenshots for every new or changed screen at the sizes in AD7.
3. The manual checks for the phase are listed in PROGRESS.md for the owner to tick on real devices. Do not tick manual checks yourself unless you actually ran them.
4. PROGRESS.md updated: phase status, what was built, decisions, deviations (with reasons), known issues.
5. **Opus review passed** (SPEC 20.0 step 4, see "Review" below).
6. Merged to `main` with a clear message (`phase 3: foods, meals and scopes`).
7. **Alpha release (SPEC R3), every phase, no exceptions:**
   - set `otasks.versionName` in `gradle.properties` to the next alpha (SPEC B2 and 20.0: `0.<phase>.0-alpha.<n>` for phases 1–9, `1.0.0-alpha.<n>` for phase 10, `1.1.0-alpha.<n>` for phases 11–13, `1.2.0-alpha.<n>` for phase 14) and add the CHANGELOG.md section;
   - `git tag v<versionName> && git push origin v<versionName>`;
   - `gh run watch` the release workflow until it finishes; if it fails, fix the cause and release the next alpha number (never delete or reuse a tag);
   - `gh release view v<versionName>` to confirm the pre-release has `OTasks-<versionName>.apk` and its `.sha256`;
   - put the release link in PROGRESS.md and tell the owner: the link, a 3–5 line summary of what changed, and the "What to test" list.
8. Wait. The owner installs the alpha and ticks the manual checks. Bugs they report are fixed in this phase and shipped as the next alpha. Start the next phase only when the owner says so.

Never release from a branch other than `main`, never with failing tests, and never with a debug-signed APK.

## Review (who builds, who checks)

- **Builder:** Claude Code with Sonnet 5.5 builds the phase on its branch and opens a pull request when the automated checks pass.
- **Reviewer:** a separate, fresh Claude Code session with **Opus 5.5** — never the session that built it. The owner starts it with: "Review the open pull request for phase <n> against SPEC.md and CLAUDE.md."
- The reviewer reads the phase in SPEC §20 and every rule it cites, then the diff. It checks: every numbered item is done; values, names and texts match the spec exactly; nothing invented, renamed or left as a placeholder; hard rules respected; strings in `strings.xml`; tests cover the new logic; screenshots look right at every size. It posts findings as a checklist comment on the pull request (`gh pr review --comment`), each with the spec rule ID.
- The builder fixes every finding (or explains why it is wrong); the reviewer re-checks and approves. Only then merge and release.
- Bug-fix alphas get the same review, scoped to the fix.

## Code style

- Kotlin official style, enforced by ktlint/Spotless. Explicit types on public APIs.
- One public class or top-level composable per file in `ui/components`; screen files `XScreen.kt` + `XViewModel.kt`.
- Composables take state and lambdas; ViewModels own logic (A3, A4). UI state classes are `@Immutable` with read-only lists (A5).
- Every lazy-list item has a stable key and a `contentType` (§15.1).
- Only `Tokens.*` for dp/sp/ms outside `ui/theme`. Only theme colours; never add a colour.
- Cite rule IDs in short comments where the code implements a non-obvious rule (`// T7: mold may be ≥ 1440`).

## Git

- Work on branches named `codex/phase-N-short-name` (documentation preparation uses `codex/spec-corrections`); open a pull request into `main`; CI must be green before merging.
- Never commit keystores, passwords, `local.properties` or generated APKs.
- Commit messages: imperative, lower case, cite rule or pitfall IDs where relevant.

## Working style

- Small steps: write a piece, build it, test it, then move on. Do not write a whole phase before compiling.
- Read the exact spec rule before implementing it, rather than relying on memory of an earlier read.
- After each step, check your work against the spec text, not just whether it compiles.

## When you are unsure

Ask one short question rather than guess. Good questions name the options and the trade-off, for example: "The spec doesn't say whether archived foods show up in the Add sheet's search. Hide them, or show them at the end in grey?"
