# Specification corrections, 10 October 2026

SPEC.md v1.3 is the implementation contract. This record explains changes from the supplied v1.2 documents; it does not certify that the prototype implements them.

The owner authorized the proposed correction pass and explicitly selected: preserve past meal plans and targets while allowing corrections to intake logs and weight. Other choices below are implementation defaults made within that correction pass, rather than individually confirmed product choices. The original ZIP and byte-identical extracted reference files remain outside the clone. No prototype migration is added.

## Decisions and regression cases

Every row is **specified; implementation and tests pending**. Use these cases in the indicated phase alongside existing phase checks, rather than treating document checks as application tests.

| Review finding | Corrected contract | Required regression case / phase |
|---|---|---|
| S1: history changes after editing current rows | H1/H2 and section 7.2 add history_days/history_items and log section-name snapshots. Materialize elapsed plans before mutations; historical intake remains correctable. | Phase 1: names, amounts, star/optional, goals and holiday edits leave yesterday's plan/target/denominator fixed; several unopened days; failed writes roll back; actual-log correction changes actual totals only. |
| S2: split identity and stale overrides | S1 remaps on/after-boundary logs, overrides, merge targets and anchors transactionally; V6 applies lifecycle bounds before overrides. | Phase 1: checked item survives split once; earlier logs/history keep old ids; ended rows cannot reappear through ADD/PIN_TIME; custom logs retain meal ownership and names. |
| S3: incompatible food basis/amounts | 13.16.1 blocks SERVING/PER_100G basis changes while meal items or recipe ingredients reference the food. No guessed conversion. | Phase 3: save fails atomically when referenced, succeeds when unreferenced; snapshot-only logs stay unchanged. |
| S4: recurrence cutoffs | 9.2 uses exact occurrence ordinals/candidate generation, without arbitrary day horizons. Cancellations consume their original COUNT position. | Phase 4: interval 99/count 20 rejects occurrence 21 at offset 1980; long weekly gaps; partial first week; leap/month-end cases; exhausted COUNT. |
| S5: collisions and inconsistent bounds | 9.4 prevents automatic meal interval overlap; latestMealMold means latest START, with duration inside the logical day. Fixed/anchor gap warnings remain advisory; flexible gaps constrain search. Explicit pins and immovable workouts warn on collisions. | Phase 5: all Ex1–Ex8 unchanged; identical fixed times; duration near day boundary; fixed ideal beyond latest; deterministic tie order; pinned/workout collisions. |
| S6: task calendar dates | TK1, TK2a, parser and T10 consistently use calendar dates for tasks and explicit DST resolution for instants. Meal logical dates stay separate. | Phases 12–13: 01:00 with a 04:00 meal boundary; time-only quick add; overdue before/after midnight; DST gap/overlap. |
| S7: locked boot eligibility/snoozes | AL0/AL0a/AL3 use shared eligibility and persist future snooze counts/triggers plus consumed snapshot state. Invalidated snapshots cannot revive paused/disabled meals. | Phase 6: pause→locked reboot; snooze→reboot; max-snooze count retained; repeated locked delivery rings once; no Room access before first unlock. |
| S8: obsolete alarms/restored ids | AL0 and alarm_state enumerate old identities. Backup replacement rotates a device-local generation, cancels existing alarms/actions/notifications and regenerates under the new generation. | Phases 6/9: source deletion, schedule/zone edit, restore reusing ids, stale action after restore, failed import. |
| S9: navigation/follow-up checks | 12.7 navigation uses direct activity PendingIntents; AL13 reuses full AL0a policy. | Phases 6–7: notification tap opens intended destination; locked access asks to unlock; disabled/paused/empty/merged meals produce no follow-up. |
| S10: diagnostic log used as state | alarm_state.noticeSent owns notification deduplication; alarm_log remains rotating diagnostics. | Phase 7: post notice, dismiss, insert more than 500 diagnostics, reschedule: notice does not reappear that day. |
| S11: Phase 2 checklist creation | Phase 2 includes new-food entry with checklist switch and a confirmed daily-from-today item. Full library search/scopes remain Phase 3. | Phase 2: blank install→meal→new food/checklist item→check/uncheck/Undo→restart. No demo data. |
| S12: release/version collisions | B2 uses bounded major/minor/patch/sequence encoding and a published-code monotonicity check. Phase 10 uses 1.0.0 alphas. | Phase 1: 1.0.10 and 1.1.0 differ; every phase/alpha/final transition increases; invalid components rejected. |
| S13: repeat copy/Undo/model | TK3 copies tags and links one successor via unique previousOccurrenceId; Undo preserves a modified successor. Task repeat UI exposes only supported infinite repeats. | Phase 12: tag/subtask/reminder inheritance; double completion; normal Undo; successor edits; reopening cannot create duplicate active successors. |
| S14: calendar identity/time zones | 14.7 keys imports by UID + original RECURRENCE-ID; preview discloses replacement of imported fields/exception sets. VTODO field mapping is explicit. Foreign-zone/UTC timed repeats simplify to one occurrence with disclosure. | Phases 9/13: cross-date moved occurrence reimported twice and moved again; unknown TZID; foreign-zone recurrence across DST; VTODO DUE/DTSTART; user edits and imported task status preserved as specified. |
| S15: voice lock/single-day contract | VC6 distinguishes storage unlock from current device authentication. Voice task creation/completion is restricted to non-repeating today/undated tasks; other dates/repeats require the app. VC0 remains a gate. | Phase 14 only: device relocked after first unlock; read and write access blocked; future/repeating tasks refused; Undo preserves exact change. |
| S16: contrast and false completion | Material errors use text roles and computed on-error contrast; validate actual composited/dynamic surfaces. Next-meal/widget states distinguish incomplete overdue meals, completed meals and empty plans. | Phases 1/2/10: contrast of every actual pair; overdue unchecked item never reports success; no required items is not a completed day. |

Additional alignment: Phase 8's rate-label example now spans 14 days; early release device checks exclude unimplemented features; privacy wording includes CSV/report/clipboard disclosure and optional assistant responses.

## Release mapping

| Phases | Alpha versionName | Final |
|---|---|---|
| 1–9 | 0.phase.0-alpha.n | None |
| 10 | 1.0.0-alpha.n | 1.0.0 |
| 11–13 | 1.1.0-alpha.n, sequence continues across phases | 1.1.0 after Phase 13 |
| 14, when VC0 passes | 1.2.0-alpha.n | 1.2.0 |

versionCode = major × 10,000,000 + minor × 100,000 + patch × 100 + sequence. Alpha sequence 1–98; final 99. Bounds: major 0–199, minor 0–99, patch 0–999. Publishing also checks that the code exceeds every previous published code. This is the new package's first release scheme, not a migration of prototype codes.

## Platform references

Notification taps must target an activity directly instead of starting one through a notification receiver/service. See [Android 12 notification trampoline restrictions](https://developer.android.com/about/versions/12/behavior-changes-12#notification-trampolines).

Current device authentication requires a keyguard/device-lock check; storage unlocked since boot is a different state. See [KeyguardManager.isDeviceLocked](https://developer.android.com/reference/android/app/KeyguardManager#isDeviceLocked()) and [UserManager.isUserUnlocked](https://developer.android.com/reference/android/os/UserManager#isUserUnlocked()).

Calendar UID, original RECURRENCE-ID, DUE and exclusive DTEND semantics follow [RFC 5545](https://www.rfc-editor.org/rfc/rfc5545). Supporting every recurrence/time-zone rule is outside the specified importer; preview must disclose simplification rather than imply faithful recurring import.

## Preparation verification

- All 22 numbered top-level specification sections and all 14 phases preserved.
- Companion version/phase numbering and original checklist alignment checked.
- All 28 table declarations are unique: the original 25 plus history_days, history_items and alarm_state.
- Independent arithmetic checks passed for phase/alpha/final version codes, the former patch/minor collision, COUNT occurrence 20/21, and the midnight task-date comparison. These calculations do not execute Kotlin application logic.
- An in-memory SQLite check confirmed that UNIQUE(day, nullable item) rejects a repeated checklist item while allowing multiple custom entries with a null item.
- Required regression cases listed; none represented as passing application tests.
- Original archive/reference hashes and prototype tracked files verified unchanged.
- No compilation, app tests, device checks, independent phase review or release performed.

## Limits to address during implementation

Room and AlarmManager/device-protected files cannot participate in one atomic transaction. AL0 invalidates the old snapshot before relevant mutations and regenerates it after commit; a crash in that gap can miss a before-unlock alarm. The implementation must expose missing snapshot coverage and test recovery rather than claiming guaranteed delivery.

The importer deliberately simplifies timed recurring foreign-zone/UTC sources to their first occurrence with an explicit preview warning. Full time-zone-aware series would require a wider recurrence model and a separate scope decision.
