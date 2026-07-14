package com.example.lessonmonitor.data.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.example.lessonmonitor.domain.schedule.RecurringSessionGenerator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SessionWindowWorker.doWork] orchestration.
 *
 * Both dependencies are mocked; the worker is constructed directly (no Hilt).
 * Tests run on the JVM via [runTest] since the worker's dependencies are all
 * plain Kotlin suspend functions — no Android framework calls happen inside
 * [doWork] (the [AlarmScheduler] is mocked away).
 */
class SessionWindowWorkerTest {

    private val appContext: Context = mockk(relaxed = true)
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val recurringSessionGenerator: RecurringSessionGenerator = mockk()
    private val alarmScheduler: AlarmScheduler = mockk()

    private lateinit var worker: SessionWindowWorker

    @Before
    fun setUp() {
        worker = SessionWindowWorker(appContext, workerParams, recurringSessionGenerator, alarmScheduler)
    }

    @Test
    fun `doWork calls generator then scheduler and returns success`() = runTest {
        coEvery { recurringSessionGenerator.generateUpcomingSessions() } returns Unit
        coEvery { alarmScheduler.syncAlarms() } returns Unit

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        coVerify(exactly = 1) { recurringSessionGenerator.generateUpcomingSessions() }
        coVerify(exactly = 1) { alarmScheduler.syncAlarms() }
    }

    @Test
    fun `doWork returns retry when generator throws`() = runTest {
        coEvery { recurringSessionGenerator.generateUpcomingSessions() } throws RuntimeException("DB closed")

        val result = worker.doWork()

        assertEquals(Result.retry(), result)
        coVerify(exactly = 0) { alarmScheduler.syncAlarms() }
    }

    @Test
    fun `doWork returns retry when scheduler throws`() = runTest {
        coEvery { recurringSessionGenerator.generateUpcomingSessions() } returns Unit
        coEvery { alarmScheduler.syncAlarms() } throws RuntimeException("AlarmManager error")

        val result = worker.doWork()

        assertEquals(Result.retry(), result)
        coVerify(exactly = 1) { recurringSessionGenerator.generateUpcomingSessions() }
        coVerify(exactly = 1) { alarmScheduler.syncAlarms() }
    }
}
