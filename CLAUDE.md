# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**Lesson Monitor** — a native Android (Kotlin, Jetpack Compose, Material 3) app for tracking lesson categories, individual lessons, and per-student attendance records. Fully local/offline-first with no backend dependency (Phase 1). Architecture is designed so a future cloud-sync backend (Phase 2) can be bolted onto the repository abstraction layer without rewriting ViewModels or UI.

## Build / Test Commands

The Gradle wrapper (`gradlew`) is **not** committed — open the project in Android Studio and it will offer to regenerate it, or run `gradle wrapper` once if Gradle is installed locally.

```bash
# Run unit tests (JUnit + MockK + Turbine; these run on the JVM, no device/emulator needed)
./gradlew :app:testDebugUnitTest

# Run a single unit test class
./gradlew :app:testDebugUnitTest --tests "com.example.lessonmonitor.ui.dashboard.DashboardViewModelTest"

# Run instrumented tests (require a device/emulator)
./gradlew :app:connectedAndroidTest

# Build a debug APK
./gradlew :app:assembleDebug
```

## Architecture

```
UI (Compose screens + ViewModels)
  └─ domain/repository/*  (interfaces — the Phase 2 swap point)
       └─ data/repository/*Impl  (wrap Room DAOs, local-only for now)
            └─ data/local/*  (Room entities, DAOs, AppDatabase, TypeConverters)
```

**MVVM + Repository**: ViewModels depend only on `domain/repository/*` interfaces (injected via Hilt). The `data/repository/` implementations wrap Room DAOs. A future `RemoteDataSource` is designed to sit alongside the existing local one inside these Impls without touching ViewModels or UI.

**No separate domain model layer**: Room entities are reused as domain types everywhere — there is no `domain/model/` package. This was a deliberate scope decision to avoid parallel DTOs; entities gained `@Serializable` directly for JSON backup export.

**Navigation**: Two-level Compose Navigation graph. The outer `LessonMonitorNavHost` holds `auth_graph` (Splash → CreateCredential/Login, no bottom nav) and a single `main` destination. `main` (`MainScreen.kt`) owns its own inner `NavHost` with a Material 3 `NavigationBar` and four nested tab graphs (Dashboard, Calendar, Statistics, Settings), each maintaining its own back stack via `saveState`/`restoreState`.

**DI**: Hilt. `DatabaseModule` provides Room + DAOs. `DataStoreModule` provides encrypted credential storage (Keystore-backed `EncryptedSharedPreferences`) and plain DataStore for the `isLoggedIn` session flag. `RepositoryModule` binds domain interfaces to their Impls.

## Key Design Decisions

- **Credentials**: PBKDF2WithHmacSHA256 (120k iterations, random salt) stored in encrypted DataStore, never in Room. The `UserEntity` Room table holds only non-sensitive state (`biometricEnabled`, `createdAt`). Plain DataStore holds only the `isLoggedIn` boolean.
- **Recurring lessons**: Sessions are lazily generated into a rolling 60-day lookahead window, idempotent via a unique `(lessonId, sessionDate)` index. Triggers on app open and Calendar screen open.
- **Cascade deletes**: Hard delete at the FK level (`ON DELETE CASCADE`), but every destructive UI action first runs count queries and shows exact impact counts in a confirmation dialog.
- **Student unenrollment ≠ history loss**: `AttendanceRecord` references `studentId` directly (not `enrollmentId`), so unenrolling a student never touches their historical records.
- **Attendance default**: A student on the roster with no record yet defaults to `PRESENT` (attendance is opt-out, not opt-in).
- **Backup/Restore**: JSON snapshot via kotlinx.serialization (not raw `.db` copy) — safer across schema version changes. Restore is destructive (clears all tables, re-inserts parents before children). Explicitly not wrapped in `withTransaction` to keep it plainly unit-testable.
- **File sharing**: CSV/JSON exports written to app-private cache `exports/` and shared via `FileProvider` + `ACTION_SEND`. Backup restore uses SAF (`OpenDocument`) — no storage permissions required.
- **Charts**: Hand-rolled Compose `Canvas`/`Box`-based percentage bars rather than a charting library — keeps dependencies minimal for Phase 1.
- **Activity base class**: `MainActivity` extends `FragmentActivity` (not plain `ComponentActivity`) because `BiometricPrompt` requires it.
- **Room schema snapshots**: Committed to `app/schemas/` (`room.schemaLocation` in build.gradle.kts) so future migrations can be written against known-good prior schemas.

## Key Files

| File | Purpose |
|---|---|
| `PLAN.md` | Frozen upfront implementation plan (data model, screens, milestones, assumptions) |
| `APP_LOGIC.md` | Living document updated after every milestone — tracks how the app actually works and any deviations from PLAN.md |
| `app/src/main/java/.../data/local/entity/` | Room entities (7 tables + 2 enums) |
| `app/src/main/java/.../domain/repository/` | Repository interfaces (7 total: Auth, Category, Lesson, Student, Enrollment, Attendance, Backup) |
| `app/src/main/java/.../navigation/Routes.kt` | Central route registry with helper functions |
| `app/src/main/java/.../navigation/LessonMonitorNavHost.kt` | Root navigation host |
| `app/src/main/java/.../navigation/MainScreen.kt` | Bottom-nav shell hosting the 4 tab graphs |
| `app/src/main/java/.../di/` | Hilt modules (Database, DataStore, Repository) |
| `data/worker/` | Notification system: AlarmScheduler, worker, receiver, helper (Milestone 14) |
| `gradle/libs.versions.toml` | Version catalog — all dependency versions in one place |

## Milestone History

Each feature was committed as a separate milestone (see git log), in this order: project setup → Room schema → navigation skeleton → user account → category/lesson CRUD → attendance tracking → student profiles → recurring lessons → calendar view → search & filter → statistics dashboard → export/backup → notifications/reminders → dark mode → final docs pass. All 16 planned milestones are complete.
