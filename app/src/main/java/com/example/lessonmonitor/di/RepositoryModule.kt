package com.example.lessonmonitor.di

import com.example.lessonmonitor.data.repository.AttendanceRepositoryImpl
import com.example.lessonmonitor.data.repository.AuthRepositoryImpl
import com.example.lessonmonitor.data.repository.BackupRepositoryImpl
import com.example.lessonmonitor.data.repository.CategoryRepositoryImpl
import com.example.lessonmonitor.data.repository.EnrollmentRepositoryImpl
import com.example.lessonmonitor.data.repository.LessonRepositoryImpl
import com.example.lessonmonitor.data.repository.StudentRepositoryImpl
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.AuthRepository
import com.example.lessonmonitor.domain.repository.BackupRepository
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.StudentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds domain repository interfaces to their Phase-1 local-only
 * implementations. Additional `@Binds` are added here as each feature
 * milestone introduces its own repository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(impl: StudentRepositoryImpl): StudentRepository

    @Binds
    @Singleton
    abstract fun bindEnrollmentRepository(impl: EnrollmentRepositoryImpl): EnrollmentRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
