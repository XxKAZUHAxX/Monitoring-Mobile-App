# Lesson Monitor

A native Android app for tracking lesson categories, individual lessons, and per-student attendance records. Fully local/offline-first — no backend dependency.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM with Repository pattern
- **Local persistence:** Room (SQLite)
- **Async:** Kotlin Coroutines + Flow
- **DI:** Hilt
- **Preferences:** Jetpack DataStore + EncryptedSharedPreferences (Android Keystore)
- **Background work:** WorkManager + AlarmManager
- **Build:** Gradle (Kotlin DSL), version catalog

## Build & Run

Open the project in **Android Studio**. The Gradle wrapper is not committed — Android Studio will offer to regenerate it automatically.

```bash
# Unit tests (JVM, no device needed)
./gradlew :app:testDebugUnitTest

# Single test class
./gradlew :app:testDebugUnitTest --tests "com.example.lessonmonitor.ui.dashboard.DashboardViewModelTest"

# Instrumented tests (device/emulator required)
./gradlew :app:connectedAndroidTest

# Debug APK
./gradlew :app:assembleDebug
```

## Features

- **Local authentication** — PIN/password stored with PBKDF2 hashing + biometric unlock fallback
- **Category & Lesson CRUD** — hierarchical drill-down with cascade-delete confirmation
- **Attendance tracking** — per-student per-session (Present/Absent/Late/Excused) with optional absence reason
- **Student profiles** — reusable across lessons, with photo, contact info, and cross-lesson attendance history
- **Recurring lessons** — daily/weekly/custom-day patterns with auto-generated session window
- **Calendar view** — month grid + day agenda, tap-through to attendance screens
- **Search & filter** — global search across categories/lessons/students, inline filter chips
- **Statistics dashboard** — per-student and per-lesson attendance rate cards with percentage bars
- **Export/backup** — CSV export per lesson, full JSON backup/restore via Android share sheet
- **Notifications** — AlarmManager reminders for upcoming lesson sessions with deep-link to attendance
- **Dark mode** — system-default with manual light/dark override in Settings

## Architecture

```
UI (Compose screens + ViewModels)
  └─ domain/repository/*  (interfaces — Phase 2 cloud-sync swap point)
       └─ data/repository/*Impl  (wrap Room DAOs)
            └─ data/local/*  (Room entities, DAOs, AppDatabase)
```

- **MVVM + Repository**: ViewModels depend only on domain interfaces (Hilt-injected). Repository implementations wrap Room DAOs. A future `RemoteDataSource` can be added alongside existing local ones without touching ViewModels or UI.
- **Two-level navigation**: outer `auth_graph` (Splash → Login) → `main` with 4 bottom-nav tab graphs (Dashboard, Calendar, Statistics, Settings), each preserving its own back stack.
- **Room entities reused as domain types** — no separate `domain/model/` layer (entities are `@Serializable` directly).

## Key Design Decisions

| Decision | Rationale |
|---|---|
| Credentials in encrypted DataStore, not Room | Keystore-backed AES-256; Room holds only non-sensitive fields |
| Rolling 60-day session window | Recurring session rows generated lazily, idempotent via unique index |
| Hard cascade deletes with confirmation | FK-level `CASCADE` but UI shows exact impact counts first |
| JSON backup (not raw `.db`) | Safe across schema version changes, human-inspectable |
| Hand-rolled charts | Avoids heavy dependency for Phase 1 |
| `FragmentActivity` base class | Required by `BiometricPrompt` |

## Assumptions

1. Exactly one local user profile (no multi-user switching).
2. Facilitator/place stored as defaults on `Lesson`, overridable per `AttendanceSession`.
3. Attendance default is Present (opt-out, not opt-in) — a student with no record yet is counted as present.
4. Notification alarms are resynced daily via WorkManager; no boot-time receiver (alarms restored within 24h of reboot).
5. Student unenrollment does not delete historical attendance records (`AttendanceRecord` references `studentId` directly, not `enrollmentId`).

## Project Documents

- `PLAN.md` — frozen upfront implementation plan (data model, screens, milestones)
- `APP_LOGIC.md` — living document tracking actual implementation and deviations
- `ARCHITECTURE.md` — Phase 2 cloud-sync extension points
- `CLAUDE.md` — guidance for Claude Code when working in this repo
