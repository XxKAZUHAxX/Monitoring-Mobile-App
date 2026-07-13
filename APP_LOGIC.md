# APP_LOGIC.md — Living App Logic Document

> This file is updated after **every** milestone commit. It describes how the app actually works (or, at this stage, how it is currently planned to work before any code exists). See `PLAN.md` for the frozen upfront plan and rationale; this file tracks current reality and any deviations from that plan as implementation proceeds.

Last updated: Milestone 5 — Feature: User Account (local-only).

## Milestone 5 notes (User Account)

- **Storage split, exactly per PLAN.md §6**: `data/datastore/CredentialStore.kt` wraps an `EncryptedSharedPreferences` instance (Keystore-backed `MasterKey`, provided by `di/DataStoreModule.kt`) holding only the PBKDF2 hash + salt — never the plaintext password. `data/datastore/SessionPreferences.kt` wraps a plain (unencrypted) Jetpack DataStore holding only the transient `isLoggedIn` boolean. Logging out clears just that flag; the credential itself is untouched.
- `util/PasswordHasher.kt` implements PBKDF2WithHmacSHA256 (120,000 iterations, random 16-byte salt) with a constant-time hash comparison in `verify()` to avoid leaking timing information. Uses `java.util.Base64` (not `android.util.Base64`) — safe since minSdk is 26, and it lets this class be unit-tested on the plain JVM without Robolectric.
- `domain/repository/AuthRepository.kt` + `data/repository/AuthRepositoryImpl.kt` tie the two stores and `UserDao` together: `createCredential` hashes + saves + creates the singleton `UserEntity` row + logs in; `verifyPassword` re-hashes with the stored salt and compares; `setBiometricEnabled`/`isBiometricEnabled` read-modify-write the `UserEntity.biometricEnabled` flag. Bound to the interface via `di/RepositoryModule.kt` (`@Binds`).
- Real logic now lives in `SplashScreen`/`SplashViewModel` (auto-routes based on `hasCredential()`/`isLoggedIn()`, no more demo buttons), `CreateCredentialScreen`/`CreateCredentialViewModel` (password + confirm fields, 4-char minimum — assumption, this is a local PIN gate not a web login), and `LoginScreen`/`LoginViewModel` (password field + optional biometric button, shown only when the repository's `biometricEnabled` flag is set).
- **Biometric unlock**: `ui/auth/BiometricAuth.kt` wraps `androidx.biometric.BiometricPrompt`, which requires a `FragmentActivity` — so `MainActivity` now extends `FragmentActivity` instead of plain `ComponentActivity` (still fully Compose-compatible, since `FragmentActivity` is itself a `ComponentActivity`). The Settings screen's biometric `Switch` is disabled with an explanatory note if `BiometricManager` reports no usable hardware/enrollment on the device.
- Settings screen now performs a real logout (`SettingsViewModel.logout()` clears the session flag via the repository) before the nav graph pops back to Login — previously this was nav-only.
- **First real unit tests landed** (spec's testing bar): `PasswordHasherTest`, `AuthRepositoryImplTest` (MockK-based, covers create/verify/logout/biometric-toggle paths), and `CreateCredentialViewModelTest`/`LoginViewModelTest` (validation + success/failure paths), using a shared `MainDispatcherRule` (`UnconfinedTestDispatcher`) so `viewModelScope` coroutines run synchronously in tests.
- Added `androidx.fragment:fragment-ktx` dependency for `FragmentActivity`.

## Milestone 4 notes (Navigation skeleton)

- Replaced the single-route placeholder from Milestone 2 with the full route set from PLAN.md §4: `navigation/Routes.kt` (route patterns + concrete-route builder functions, using `0L` as the "create new" sentinel instead of nullable query params) and `navigation/BottomNavItem.kt`.
- Two-level graph, matching PLAN.md exactly: the outer `NavHost` (`LessonMonitorNavHost.kt`) holds the `auth_graph` (Splash/CreateCredential/Login, no bottom nav) and a single `main` destination; `main` (`MainScreen.kt`) owns its own inner `NavHost` + Material 3 `NavigationBar`, with one nested graph per bottom-nav tab (`DashboardNavGraph.kt`, `CalendarNavGraph.kt`, `StatisticsNavGraph.kt`, `SettingsNavGraph.kt`) so each tab keeps its own back stack/state on tab switches (`saveState`/`restoreState` on the bottom nav clicks).
- All 19 screens from PLAN.md §4 now exist as real (if placeholder) Composables, wired with working navigation callbacks — e.g. Dashboard → Lessons List → Lesson Detail → Student Picker/Attendance Session, Calendar → Day Agenda → Attendance Session, Statistics/Search → Student Detail. `StudentDetail` is declared once (in the Dashboard tab graph) and navigated to from both the Dashboard and Statistics tabs, since routes are unique across the whole flattened graph regardless of which nested block declares them.
- Added `ui/components/PlaceholderScreen.kt`, a shared shell (title + description + optional action buttons) used by every screen that doesn't have its real feature milestone yet — each placeholder screen states in its description which upcoming milestone will replace it. Screens are migrated off this one-by-one as features land; it is not meant to survive to the final app.
- **Deferred to the Notifications milestone**: the `attendance_session/{lessonId}/{sessionId}` destination does not yet declare a `navDeepLink` / handle `Activity` intents for the `lessonmonitor://lesson/{lessonId}/session/{sessionId}` notification deep link from PLAN.md §4. Wiring that up needs `MainActivity`'s intent handling threaded through to the *inner* NavHost inside `MainScreen`, which is naturally built alongside the actual notification code rather than speculatively now.
- Settings' "Log out" button now navigates back to `Login` — this is nav-only; it doesn't yet clear anything since no session state exists until the User Account milestone.

## Milestone 3 notes (Room schema)

- All 7 tables from PLAN.md §2 are implemented under `data/local/`: `entity/` (`UserEntity`, `CategoryEntity`, `LessonEntity`, `StudentEntity`, `EnrollmentEntity`, `AttendanceSessionEntity`, `AttendanceRecordEntity`, plus the `AttendanceStatus`/`RecurrenceType` enums), `converter/Converters.kt` (enum ⇄ String), `dao/` (one DAO per entity), and `AppDatabase.kt` (version 1, `exportSchema = true`, schema JSON snapshots written to `app/schemas/` for future migrations).
- **Resolved the `User` table tension flagged in PLAN.md**: `UserEntity` only stores non-sensitive state (`biometricEnabled`, `createdAt`) as a singleton row (`id = 1`). The password hash + salt are *not* Room columns — they'll live in the encrypted DataStore built in the "User account" milestone, per the security decision in PLAN.md §6. This satisfies the deliverable list's "Room schema covering User" literally while keeping the credential out of a plain SQLite table.
- Cascade deletes are configured at the FK level (`onDelete = CASCADE`) exactly per the matrix in PLAN.md §2. Every DAO likely to sit behind a destructive UI action also exposes `countX(...)` queries (e.g. `LessonDao.countByCategory`, `AttendanceSessionDao.countForCategory`, `AttendanceRecordDao.countForCategory/countForLesson/countForStudent`) so the repository layer (built in the CRUD milestones) can assemble the pre-delete "this will remove N lessons, M sessions, K records" confirmation counts without any raw SQL living outside the DAOs.
- `EnrollmentEntity` has a unique `(lessonId, studentId)` index and `upsert` (REPLACE-on-conflict) so re-adding a previously removed student reuses the row instead of duplicating it; `AttendanceRecordEntity` has a unique `(sessionId, studentId)` index for the same reason (re-marking attendance overwrites, never duplicates).
- `AttendanceSessionDao.insert` uses `OnConflictStrategy.IGNORE` against the unique `(lessonId, sessionDate)` index — this is what makes the future rolling-window session generator idempotent (PLAN.md §3 roadblock #2): it can call `insert` for every date in the window every time without checking existence first.
- `di/DatabaseModule.kt` provides `AppDatabase` and all 7 DAOs as Hilt singletons — this is the "local data source" the Phase-2 remote data source will sit alongside later (PLAN.md §3 roadblock #6).
- Repository interfaces/impls, domain models, and any `@Relation` POJOs (e.g. a session-with-its-records shape for the Attendance screen) are intentionally deferred to the milestones that actually need them, rather than speculatively added now.
- Not yet tested with an instrumented Room test (would need the Android test runner); the spec's testing bar (ViewModel/Repository unit tests) will be met starting with the first feature milestone once there's a Repository to test.

## Milestone 2 notes (Initial project setup)

- Gradle project scaffolded: version catalog (`gradle/libs.versions.toml`), root/app `build.gradle.kts`, `AndroidManifest.xml`, and the `com.example.lessonmonitor` package skeleton (`data/`, `domain/`, `ui/`, `navigation/`, `di/`, `util/` folders will be added as each milestone actually needs them, rather than committed empty).
- Dependencies wired per the tech stack in the prompt/`PLAN.md`: Compose + Material 3, Hilt (incl. `hilt-work` for future `@HiltWorker`s), Room, DataStore Preferences, `androidx.security-crypto`, kotlinx.serialization (JSON, for backup/export), WorkManager, Biometric, and JUnit/MockK/Turbine for tests.
- `LessonMonitorApp` (`@HiltAndroidApp`) → `MainActivity` (`@AndroidEntryPoint`) → `LessonMonitorTheme` (M3, dynamic color, dark-mode-aware) → `LessonMonitorNavHost` (single placeholder `dashboard` route) → `DashboardScreen` (placeholder text) — this proves the app→theme→nav→screen chain wires up end to end before any real feature code is added.
- **Known environment limitation**: this dev container has no JDK/Android SDK/Gradle installed, so the build could not be compiled or run here. The Gradle wrapper's binary `gradle-wrapper.jar` (and the `gradlew`/`gradlew.bat` launcher scripts, which are useless without it) were **not** committed for that reason — open the project in Android Studio, which will offer to regenerate the wrapper automatically, or run `gradle wrapper` once if Gradle is installed locally.

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

_None so far. One refinement (not a deviation): the `data/`, `domain/`, `di/`, `util/` package folders from PLAN.md §5 are being created incrementally as each milestone needs them, rather than all committed empty in Milestone 2._
