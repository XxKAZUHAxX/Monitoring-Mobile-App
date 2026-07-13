# Prompt: Kotlin Android App — Lesson & Attendance Monitoring System

## Role & Context

You are an Android development agent. Build a **native Android application** in **Kotlin** using **Android Studio** as the IDE. This is a **monitoring app for lessons/classes**, tracking categories of lessons, individual lessons within each category, and attendance records for students enrolled in those lessons.

**Phase 1 scope is strictly local-first**: all data must be stored on-device only (no backend, no internet dependency). Design the architecture so that cloud sync (e.g., Firebase, custom REST API) can be added in a future phase **without requiring a rewrite** — use a repository abstraction layer so local storage can later be swapped for, or paired with, a remote data source.

---

## Mandatory Development Workflow (Read Before Starting)

Follow this process strictly. Do not skip or reorder these steps.

1. **Plan thoroughly before implementing anything.** Before opening Android Studio to write code, produce a complete implementation plan: the full data model, entity relationships, screen list, navigation flow, and a breakdown of every feature in this prompt. Explicitly think through likely roadblocks (e.g., cascade deletes across Category → Lesson → Attendance, recurring lesson session generation, notification scheduling, local credential security) and decide your approach to each one on paper first. Do not begin implementation until this plan is complete and internally consistent.

2. **Commit after every milestone/feature — no exceptions.** Break the work into discrete milestones: initial project setup, Room schema, navigation skeleton, then each feature listed under "Phase 1 Feature Requirements" (user account, category/lesson CRUD, attendance tracking, student profiles, recurring lessons, calendar view, search & filter, statistics dashboard, export/backup, notifications, dark mode). After a milestone is implemented and verified working, commit it to git with a clear, descriptive commit message **before** starting the next one. Never leave a milestone partially done while starting another, and never bundle multiple unrelated features into a single commit.

3. **Maintain a living app-logic document.** Create a markdown file named `APP_LOGIC.md` that documents, in plain language, the logical process of how the app works: how data flows from Category → Lesson → Student → Attendance, how each screen connects to the next, how each feature (recurring lessons, statistics, export, notifications, etc.) reads/writes data, and any important business rules or edge-case decisions (e.g., what happens on cascade delete). Start this file during the planning stage in step 1, and update it every time a new feature is committed so it always reflects the current, actual state of the app — not just the original plan.

---

## Tech Stack

- **Language:** Kotlin
- **IDE:** Android Studio
- **UI:** Jetpack Compose with Material Design 3 (preferred for a modern, maintainable UI). If you have a strong reason to use XML + ViewBinding instead, state the tradeoff before proceeding.
- **Architecture:** MVVM (Model-View-ViewModel) with a Repository pattern
- **Local persistence:** Room (SQLite abstraction)
- **Async:** Kotlin Coroutines + Flow
- **DI:** Hilt (or manual DI if you prefer lighter weight — justify the choice)
- **Local session/preferences:** Jetpack DataStore (not SharedPreferences, which is deprecated for this use case)
- **Navigation:** Jetpack Navigation Component (Compose Navigation if using Compose)

---

## Data Hierarchy & Core Flow

The app follows a strict drill-down hierarchy:

```
Dashboard
 └─ Categories (e.g., "Math", "Piano", "Driving Lessons")
     └─ Lessons (within a category, e.g., "Algebra Basics", "Week 3 Recital Prep")
         └─ Lesson Details / Attendance Session
             ├─ Facilitator name
             ├─ Place / Room
             ├─ Enrolled students
             └─ Per-student attendance record (status, reason if absent)
```

### 1. Dashboard (Categories)
- List all categories.
- Add / edit / delete a category (name, optional description/color/icon).
- Tapping a category opens its list of lessons.

### 2. Category → Lessons
- List all lessons under the selected category.
- Add / edit / delete a lesson (title, optional date/time, optional description).
- Tapping a lesson opens its detail/attendance view.

### 3. Lesson Detail
- Add/edit facilitator name.
- Add/edit place/room.
- Manage enrolled students (add/remove students to this lesson).
- For each enrolled student, record attendance status: **Present / Absent** (consider also **Late / Excused** as optional statuses — confirm with product owner if unsure, but implement at least Present/Absent/Late/Excused since it's low additional effort and commonly needed).
- If status is Absent, capture a reason (free text field).
- Support multiple attendance sessions per lesson over time (e.g., a recurring lesson held weekly should let the user log attendance per occurrence/date, not just once) — see recurring lessons feature below.

---

## Phase 1 Feature Requirements (Build Now)

### A. User Account (local-only)
- Single local user profile authentication — this is **not** multi-tenant/cloud auth, just a local gate to the app (password or PIN, stored securely/hashed on-device, e.g., via Android Keystore or hashed credential in Room/DataStore).
- Optional: allow biometric unlock (fingerprint/face) as a convenience layer on top of the local credential.
- Once logged in, the session persists across app restarts/backgrounding indefinitely until the user explicitly logs out (use DataStore to persist a "logged in" flag/session token).
- Provide a logout action (e.g., in a settings/profile screen) that clears the session.

### B. Category & Lesson Management (CRUD)
- Full create/read/update/delete for categories and lessons as described above.
- Confirmation dialogs before delete (especially since deleting a category should cascade-delete its lessons and related attendance data — warn the user clearly about this before confirming).

### C. Attendance Tracking
- Add students to a lesson's roster (name, optional contact info — see Student Profiles below).
- Mark attendance per student per session with status + optional absence reason.

### D. Student Profiles
- Each student has a profile: name, photo (stored locally, e.g., internal storage with a file path reference in Room), contact info (phone/email, optional), and free-text notes.
- A student can be enrolled in multiple lessons/categories; their profile should be reusable across lessons rather than re-created each time.
- A student detail screen should show their attendance history across all lessons they're enrolled in.

### E. Recurring / Scheduled Lessons
- Allow a lesson to be marked as recurring (e.g., weekly on a given weekday, or a custom pattern).
- Each occurrence of a recurring lesson should generate its own attendance session so history isn't overwritten.
- Simple recurrence rules are sufficient for Phase 1 (daily/weekly/custom day-of-week); no need for complex RRULE parsing.

### F. Calendar / Schedule View
- A calendar screen showing lessons/sessions by date (month or week view).
- Tapping a date shows lessons scheduled/occurring that day, linking into the relevant lesson/attendance screen.

### G. Search & Filter
- Global or scoped search across categories, lessons, and students (by name).
- Filter lessons by category, date range, or attendance status (e.g., "show lessons with any absences this week").

### H. Attendance Statistics Dashboard
- Per-student attendance percentage (present vs. total sessions).
- Per-lesson attendance summary (e.g., average attendance rate over time).
- Simple visualizations (bar chart or percentage cards) — a lightweight charting library (e.g., Vico, or a hand-rolled Compose Canvas chart) is fine; avoid heavy dependencies if not needed.

### I. Export / Backup (local)
- Export attendance data to CSV (e.g., per lesson or per category) to local device storage, shareable via Android's share sheet.
- A local backup/restore feature (e.g., export the Room database or a JSON snapshot to a file the user can save and later re-import) to protect against data loss on device reset/reinstall.

### J. Notifications / Reminders
- Local notifications (via WorkManager or AlarmManager) reminding the user of upcoming scheduled lessons.
- Notification should deep-link into the relevant lesson's attendance screen when tapped.

### K. Dark Mode
- Full support for light/dark theme, ideally following system theme by default with a manual override option in settings.

---

## Phase 2 (Future — Do Not Build Yet, But Design For It)

- Cloud sync of local data (e.g., Firebase Firestore or a custom backend) — the Repository layer should already abstract "data source" so a `RemoteDataSource` can be introduced later alongside the existing `LocalDataSource` without changing ViewModels/UI.
- Multi-user / multi-device accounts (true authentication with a backend, not just a local gate).
- Conflict resolution strategy for offline-first sync (mention this is a known future concern; no need to solve it now).

Please leave clear code comments or a short `ARCHITECTURE.md` note wherever a decision was made specifically to keep Phase 2 easy to bolt on.

---

## Non-Functional Requirements

- **Offline-first:** the app must be fully functional with zero network connectivity.
- **Data integrity:** use Room relationships/foreign keys correctly (Category 1—N Lesson, Lesson N—N Student via a join table for enrollment, Lesson 1—N AttendanceSession, AttendanceSession 1—N AttendanceRecord per student).
- **Security:** local credentials must never be stored in plain text.
- **Testing:** include unit tests for ViewModels/Repository logic at minimum; UI tests are a bonus, not required for Phase 1.
- **Code quality:** follow standard Kotlin/Android conventions, use meaningful package structure (e.g., `data/`, `domain/`, `ui/`, `di/`), and keep Composables/screens reasonably small and reusable.

---

## Expected Deliverables

1. A complete, buildable Android Studio project (Kotlin, Gradle).
2. Room database schema covering: `User` (local profile), `Category`, `Lesson`, `Student`, `Enrollment` (join table), `AttendanceSession`, `AttendanceRecord`.
3. All Phase 1 screens/flows wired end-to-end (login → dashboard → category → lesson → attendance, plus calendar, stats, search, export, settings/dark-mode, notifications).
4. A short `README.md` explaining the architecture, how to build/run the project, and any assumptions made.
5. A brief `ARCHITECTURE.md` (or section in the README) noting where Phase 2 cloud-sync hooks were left for future extension.
6. `APP_LOGIC.md` — the living document described in the Mandatory Development Workflow section, kept current through the end of the project.
7. A git commit history showing one milestone/feature committed at a time, per the Mandatory Development Workflow section — not a single bulk commit.

If any requirement above is ambiguous or you need to make an assumption to proceed, state the assumption clearly in your response or in code comments rather than blocking on it.
