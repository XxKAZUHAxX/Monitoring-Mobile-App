# APP_LOGIC.md — Living App Logic Document

> This file is updated after **every** milestone commit. It describes how the app actually works (or, at this stage, how it is currently planned to work before any code exists). See `PLAN.md` for the frozen upfront plan and rationale; this file tracks current reality and any deviations from that plan as implementation proceeds.

Last updated: Milestone 8 — Feature: Student profiles (profile CRUD, photo, cross-lesson history).

## Milestone 8 notes (Student profiles)

- **`StudentRepository` expanded** from Milestone 7's minimal (name-only) surface to full CRUD: `create(...)` grew optional `phone`/`email`/`notes`/`photoPath` parameters (defaulted to `null`, so the Milestone 7 roster "quick add" call site — `create(name)` via the interface type — kept working unchanged), plus `update`, `delete`, and `getDeleteImpact` (→ `StudentDeleteImpact(enrollmentCount, recordCount)`, mirroring PLAN.md §1 assumption #9's Student cascade-delete matrix, built from the existing `EnrollmentDao.countForStudent`/`AttendanceRecordDao.countForStudent` queries).
- **`AttendanceRepository` gained `getHistoryForStudent(studentId)`**, joining `AttendanceRecordDao.getForStudent` with a new `AttendanceSessionDao.getAll()` query and the existing `LessonDao.getAll()` in-memory (same "combine + manual lookup map" approach as `EnrollmentRepositoryImpl`'s roster join, no `@Relation` POJO) to produce `StudentAttendanceHistoryEntry(record, session, lessonTitle)`, sorted newest-first, for the Student Detail screen.
- **`StudentFormScreen`/`StudentFormViewModel`** is now real: name (required)/phone/email/notes, plus a photo picker using `ActivityResultContracts.PickVisualMedia()` (the modern Android Photo Picker — **no storage permission needed**, unlike the older `GetContent`/`ACTION_OPEN_DOCUMENT` approaches). A picked photo is immediately copied into app-private storage (`util/PhotoStorage.kt`'s `copyUriToInternalStorage`, writing to `filesDir/student_photos/<uuid>.jpg`) since the picker's `content://` URI grant isn't guaranteed to outlive the call — `StudentEntity.photoPath` stores that absolute file path, exactly as PLAN.md §2 specifies. Displayed via `BitmapFactory.decodeFile(...).asImageBitmap()` (no image-loading library dependency added, since this is always a local file).
- **`StudentDetailScreen`/`StudentDetailViewModel`** is now real: profile card (photo/phone/email/notes) + a delete action (top bar icon) that fetches the exact impact counts before showing a confirmation `AlertDialog`, then a cross-lesson attendance history list (lesson title, session date, status, reason). On successful delete, the ViewModel's `deleted` flag flips and the screen calls a new `onDeleted` callback to pop the back stack — `DashboardNavGraph.kt`'s `StudentDetail` composable was updated to pass `{ navController.popBackStack() }` (the only navigation-graph change this milestone needed).
- `StudentPickerScreen`'s "create a full student profile instead" secondary action (from Milestone 7) now actually lands on a working form instead of a placeholder.
- **Tests added/updated**: `StudentRepositoryImplTest` (now takes `EnrollmentDao`/`AttendanceRecordDao` too; new create-with-all-fields/create-defaults/update/getDeleteImpact cases), `AttendanceRepositoryImplTest` (now takes `LessonDao` too; new `getHistoryForStudent` join + orphan-filtering cases), `StudentFormViewModelTest`, `StudentDetailViewModelTest` (load/requestDelete/confirmDelete/cancelDelete).

## Milestone 7 notes (Attendance tracking)

- **Three new repositories**, all bound in `di/RepositoryModule.kt`:
  - `StudentRepository`/`StudentRepositoryImpl` — deliberately minimal (`getAll`/`search`/`getById`/`create(name)` only). Full profile fields (photo/phone/email/notes) plus update/delete (with the Student cascade-delete confirmation from PLAN.md §1 assumption #9) are left for the Student Profiles milestone (#8); this milestone only needed enough to look students up and quick-add one by name onto a roster.
  - `EnrollmentRepository`/`EnrollmentRepositoryImpl` — `getRosterForLesson(lessonId)` joins the active `Enrollment` rows with their `Student` profiles in-memory (`combine` + a manual lookup, no `@Relation` POJO added) into a `RosterEntry`, sorted by name. `unenroll` deactivates (`active = false`) rather than deleting the row, per `EnrollmentEntity`'s existing doc comment — a student's attendance history for that lesson is never lost this way (PLAN.md §3 roadblock #5).
  - `AttendanceRepository`/`AttendanceRepositoryImpl` — `createSession(lessonId, sessionDate)` is idempotent: it inserts (`OnConflictStrategy.IGNORE`) and, on a conflict against the unique `(lessonId, sessionDate)` index, looks up and returns the existing row's id instead of failing. This is the same idempotent shape the rolling-window generator in the Recurring/Scheduled Lessons milestone (#9) will reuse. `markAttendance` upserts one `AttendanceRecord` per `(sessionId, studentId)`.
- **`LessonDetailScreen`/`LessonDetailViewModel`** is now real: shows the lesson's facilitator/place, and a `TabRow` with **Roster** (enrolled students, tap-to-open `StudentDetail`, delete icon to unenroll, FAB to open `StudentPicker`) and **Sessions** (manual "add a session for this date" text field + button, list of existing sessions newest-first, tap to open `AttendanceSession`) tabs. Each of the three data sources (lesson/roster/sessions) is collected independently and kept live, so roster/session changes made from other screens reflect immediately.
- **`StudentPickerScreen`/`StudentPickerViewModel`**: a searchable list of all students with a checkbox reflecting whether they're on *this* lesson's roster (tap the row to toggle enroll/unenroll), plus an inline "quick add" name field that creates a minimal `Student` row and enrolls them in one step. The existing "create a full student profile instead" secondary action still navigates to `StudentForm` (still a placeholder, milestone #8) for anyone who wants to add phone/email/notes/photo up front.
- **`AttendanceSessionScreen`/`AttendanceSessionViewModel`**: loads the roster + any existing records for that session **once** (not a continuous live collection) — edits are local until an explicit "Save attendance" button persists all rows in one pass. This intentionally mirrors the load-once/edit/submit pattern already used by `CategoryFormViewModel`/`LessonFormViewModel`, rather than a live `combine` of 3 Flows that could otherwise silently clobber in-progress edits if an unrelated Room write invalidated one of them mid-edit. Each roster row shows 4 `FilterChip`s (Present/Absent/Late/Excused per PLAN.md §1 assumption #8) and a conditional reason field for Absent/Excused. **Assumption**: a student with no record yet defaults to `PRESENT` (attendance is opt-out, not opt-in) — noted here since PLAN.md didn't specify a default.
- No navigation graph changes were needed — all four routes (`LessonDetail`, `StudentPicker`, `AttendanceSession`, plus the existing `StudentForm`/`StudentDetail` placeholders) already existed with matching callback signatures from Milestone 4.
- **Tests added**: `StudentRepositoryImplTest`, `EnrollmentRepositoryImplTest` (roster join/sort/orphan-filtering, enroll/unenroll), `AttendanceRepositoryImplTest` (idempotent `createSession`, `markAttendance`), `LessonDetailViewModelTest`, `StudentPickerViewModelTest`, `AttendanceSessionViewModelTest` (default-to-PRESENT, status-change clears reason, submit persists per-row with correct null-vs-set reason).

## Milestone 6 notes (Category & Lesson CRUD)

- **New repositories**: `domain/repository/CategoryRepository.kt` + `data/repository/CategoryRepositoryImpl.kt`, and `domain/repository/LessonRepository.kt` + `data/repository/LessonRepositoryImpl.kt`, both bound via `di/RepositoryModule.kt`. Each interface exposes a `getDeleteImpact(id)` method returning a small data class (`CategoryDeleteImpact`/`LessonDeleteImpact`) built from the `countX()` DAO queries added in Milestone 3 — this is the one place the exact pre-delete counts required by PLAN.md §1 assumption #3 are assembled, so every delete confirmation dialog reads from a single source of truth.
  - `CategoryDeleteImpact` = lessons + attendance sessions + attendance records under that category (mirrors PLAN.md §2's example dialog text verbatim: "This will delete 4 lessons, 12 sessions, 96 attendance records").
  - `LessonDeleteImpact` = roster enrollments + attendance sessions + attendance records under that lesson.
- `DashboardScreen`/`DashboardViewModel` now show a real, live (`Flow`-backed) list of categories with add (FAB), tap-to-open-lessons, and delete-with-confirmation (impact counts fetched on demand, not kept continuously live, since they're only needed right before a delete decision).
- `CategoryFormScreen`/`CategoryFormViewModel` is a real add/edit form: name (required), description, a single-emoji icon field, and a fixed 7-swatch color palette (a full color picker was judged out of scope for Phase 1 — `CategoryEntity.color` just needs *a* value, not an infinite one). Editing preserves `createdAt`/`id` by copying the originally-loaded entity rather than reconstructing it.
- `LessonsListScreen`/`LessonsListViewModel` follows the same list/add/delete-with-confirmation pattern, scoped to one `categoryId` (re-subscribes only when the id actually changes, via a cancel-and-restart collection job).
- `LessonFormScreen`/`LessonFormViewModel` is the most involved screen: title/description/facilitator/place, a recurring-lesson toggle, a recurrence-type dropdown (Daily/Weekly/Custom days), day-of-week `FilterChip`s (ISO 1=Monday..7=Sunday, matching `LessonEntity.recurrenceDaysOfWeek`'s documented CSV format), and start/end date + start/end time fields.
  - **Deliberate simplification**: dates (`yyyy-MM-dd`) and times (`HH:mm`) are plain validated text fields, not Material3 `DatePicker`/`TimePicker` widgets. Given this environment has no local Kotlin/Gradle compiler to catch mistakes in less-familiar experimental Material3 APIs, plain text input was chosen to keep this milestone reliable; a nicer picker UI can be swapped in later without changing the ViewModel contract.
  - This screen only persists the Lesson *template* (including its recurrence rule). Actually generating `AttendanceSessionEntity` rows from that rule into the rolling 60-day window is explicitly deferred to the Recurring/Scheduled Lessons milestone (#9), per PLAN.md §1 assumption #4 — `isRecurring`/`recurrenceType` are stored now but nothing yet reads them to create sessions.
- **Tests added**: `CategoryRepositoryImplTest`, `LessonRepositoryImplTest` (create/update-preserves-createdAt/getDeleteImpact/delete, MockK-based), `DashboardViewModelTest`, `CategoryFormViewModelTest`, `LessonsListViewModelTest`, `LessonFormViewModelTest` (validation paths — blank title, missing recurrence days, malformed date — plus create/update happy paths and CSV day-of-week encoding), all using the shared `MainDispatcherRule`.
- No navigation graph changes were needed — `DashboardNavGraph.kt`'s existing routes/callbacks already matched the screens' public signatures; only the screens' internal implementations and their (now real) default `hiltViewModel()` parameters changed.

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
