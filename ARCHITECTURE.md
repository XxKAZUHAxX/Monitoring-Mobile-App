# ARCHITECTURE.md — Phase 2 Cloud-Sync Extension Points

This document describes where and how a cloud-sync backend (e.g., Firebase Firestore, custom REST API) can be bolted onto the Phase 1 local-only architecture **without rewriting ViewModels or UI**.

## The Swap Point: `domain/repository/*`

Every data operation flows through a domain interface:

```
ViewModel  ──►  domain/repository/AuthRepository (interface)
                       ▲
                       │  @Binds in RepositoryModule
                       │
               data/repository/AuthRepositoryImpl (wraps Room DAOs + DataStore)
```

There are 7 repository interfaces:

| Interface | Current Impl | Phase 2 change |
|---|---|---|
| `AuthRepository` | `AuthRepositoryImpl` | Add remote auth (token refresh, OAuth) |
| `CategoryRepository` | `CategoryRepositoryImpl` | Add cloud push/pull for categories |
| `LessonRepository` | `LessonRepositoryImpl` | Add cloud push/pull for lessons |
| `StudentRepository` | `StudentRepositoryImpl` | Add cloud push/pull for students |
| `EnrollmentRepository` | `EnrollmentRepositoryImpl` | Sync roster changes |
| `AttendanceRepository` | `AttendanceRepositoryImpl` | Sync attendance records |
| `BackupRepository` | `BackupRepositoryImpl` | Cloud backup target (alternative to JSON file) |

## How to Add a Remote Data Source

For each repository, the pattern is:

```kotlin
// Before (Phase 1)
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val lessonDao: LessonDao,
    // ...
) : CategoryRepository { ... }

// After (Phase 2)
class CategoryRepositoryImpl @Inject constructor(
    private val localSource: CategoryDao,          // existing Room DAOs
    private val remoteSource: CategoryRemoteSource, // NEW — Retrofit/Firebase/etc.
    private val syncStrategy: SyncStrategy           // NEW — offline-first merge logic
) : CategoryRepository {
    // getAll(): merge local + remote, resolve conflicts
    // create(): write locally, enqueue remote push
    // delete(): mark locally, enqueue remote push
    // ...
}
```

The ViewModel sees no change — it still injects `CategoryRepository` (the interface), and Hilt still binds `CategoryRepositoryImpl` to it. Only the Impl constructor and internal logic change.

## Hilt Module Changes

In `di/RepositoryModule.kt`, each `@Binds` stays the same. Add a new `@Module` for remote dependencies:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideCategoryRemoteSource(firestore: FirebaseFirestore): CategoryRemoteSource =
        FirestoreCategoryRemoteSource(firestore)
}
```

## Room Schema Considerations

All entities already carry audit timestamps (`createdAt` / `updatedAt`) — these serve as the basis for "last-write-wins" or "modified-since" sync markers. No schema migration is needed to add sync support.

The `EnrollmentEntity.active` boolean (set to `false` on unenroll rather than deleting the row) means unenrollment events can be synced as updates rather than deletes, simplifying conflict resolution.

## Data Flow for Offline-First Sync

```
1. User creates a Category
2. CategoryRepositoryImpl.create(name, ...)
   ├─ categoryDao.insert(entity)          // local write (immediate)
   └─ syncQueue.enqueue(CreateCategoryOp)  // remote write (eventual)
3. WorkManager periodic sync job:
   ├─ syncQueue.drain()                   // push local changes
   ├─ remoteSource.getChanges(since)      // pull remote changes
   └─ mergeStrategy.apply(remoteChanges)  // resolve conflicts, update Room
```

## What Should NOT Change

- **ViewModels** — they depend on interfaces, not implementations
- **UI (Compose screens)** — no data-layer concern leaks into them
- **Room schema** — `@Entity` classes are already `@Serializable` for JSON backup, so they're ready for network serialization too
- **Navigation** — route structure is independent of data source

## Non-Goals for Phase 2

- Real-time sync (Firebase listeners / WebSockets) — Phase 1 architecture targets pull/push, not streaming
- Multi-user accounts — the local auth gate is single-profile; true multi-user auth needs a backend identity system
- Conflict resolution UI — merge conflicts are resolved automatically (last-write-wins or CRDT-based); user-facing conflict pickers are out of scope
