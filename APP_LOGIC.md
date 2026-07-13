# APP_LOGIC.md — Living App Logic Document

> This file is updated after **every** milestone commit. It describes how the app actually works (or, at this stage, how it is currently planned to work before any code exists). See `PLAN.md` for the frozen upfront plan and rationale; this file tracks current reality and any deviations from that plan as implementation proceeds.

Last updated: Milestone 1 — Planning (no code written yet).

---

## 1. Overall Data Flow

```
User logs in (or unlocks via biometric)
  → Dashboard shows all Categories
    → Tap a Category → Lessons list for that Category
      → Tap a Lesson → Lesson Detail
          - Roster tab: enrolled Students (add/remove via Student Picker)
          - Sessions tab: list of AttendanceSessions (auto-generated for recurring lessons)
            → Tap a Session → Attendance Session screen
                - One row per enrolled Student
                - Set status: Present / Absent / Late / Excused
                - If Absent or Excused, capture a free-text reason
```

Every occurrence of a recurring lesson is its own `AttendanceSession` row, so marking attendance on one date never overwrites another date's history. Non-recurring lessons get exactly one `AttendanceSession` tied to their `startDate`.

---

## 2. How Each Screen Connects

- **Splash** → reads the `isLoggedIn` flag from DataStore → routes to `Login` (or `CreateCredential` on first run) or straight to `Dashboard`.
- **Dashboard** → lists Categories (Room `Flow` query) → tapping one navigates to `LessonsList(categoryId)`. Search icon in the top bar opens the global `Search` screen.
- **LessonsList** → lists Lessons for `categoryId`, with filter chips (date range / has-absences). Tapping a lesson opens `LessonDetail(lessonId)`.
- **LessonDetail** → two tabs: Roster (enrolled Students, add via `StudentPicker`) and Sessions (auto-generated/managed list, tap one → `AttendanceSession(sessionId)`).
- **StudentPicker** → search existing Students or jump to `StudentForm` to create a new one, then create an `Enrollment` row linking Student ↔ Lesson.
- **StudentDetail** → shown from Roster, Search, or Statistics — displays the Student's profile plus every `AttendanceRecord` across all lessons they've ever been enrolled in (query is by `studentId`, independent of current enrollment status).
- **AttendanceSession** → one `AttendanceRecord` row per enrolled Student for that date; edits are saved per-row immediately (no separate "save all" step planned, to avoid partial-save data loss).
- **Calendar** → month/week grid pulls `AttendanceSession.sessionDate` values inside the visible range; tapping a date opens `DayAgenda` (sessions occurring that day) which links into `LessonDetail`/`AttendanceSession`.
- **Search** → queries Category.name, Lesson.title, Student.name in parallel; results grouped by type, tapping a result navigates to the matching detail screen.
- **Statistics** → per-student attendance % = `PRESENT count / total AttendanceRecord count` for that student; per-lesson % = same ratio aggregated across all sessions of that lesson.
- **Settings** → dark mode toggle (persisted in DataStore, defaults to system theme), logout (clears `isLoggedIn` flag only — credential hash stays), biometric toggle, links to `Export` and `BackupRestore`.
- **Export** → user picks a Category or Lesson scope → builds a CSV from `AttendanceRecord`/`AttendanceSession` joins → shared via the Android share sheet.
- **BackupRestore** → serializes every entity table to one JSON file (export) or reads/validates + replaces the Room DB contents from a chosen JSON file (import).

---

## 3. Business Rules & Edge-Case Decisions

- **Cascade delete**: deleting a Category deletes its Lessons, which deletes their Enrollments/AttendanceSessions, which deletes their AttendanceRecords. Deleting a Lesson does the same minus the Category. Deleting a Student deletes their Enrollments and AttendanceRecords, but leaves Lessons/Sessions intact for other students. Every delete action first runs a count query and shows the exact impact in the confirmation dialog before proceeding — nothing is deleted silently.
- **Unenrolling ≠ losing history**: removing a Student from a Lesson's roster sets `Enrollment.active = false` (or deletes the Enrollment row) but never touches their existing `AttendanceRecord` rows for that lesson, because records reference `studentId` directly rather than the enrollment relationship.
- **Recurring lesson sessions**: generated lazily into a rolling 60-day-ahead window, recomputed idempotently (unique `(lessonId, sessionDate)` index) whenever the app opens or the Calendar is scrolled past the current window edge — never generated indefinitely upfront.
- **Facilitator/place**: each Lesson has a default facilitator name and place; any individual `AttendanceSession` may override either for that specific occurrence (e.g., a substitute facilitator on one date).
- **Credentials**: never stored in plaintext. PBKDF2-derived hash + random salt live in an encrypted DataStore backed by an Android Keystore master key. The `isLoggedIn` flag is a separate, non-sensitive DataStore boolean cleared on logout; the credential itself is untouched by logout.
- **Notifications**: scheduled via `AlarmManager` for each session inside the lookahead window, deep-linking to that session's `AttendanceSession` screen when tapped; a daily housekeeping job keeps the window and alarms up to date.

---

## 4. Deviations From `PLAN.md`

_None yet — implementation has not started._
