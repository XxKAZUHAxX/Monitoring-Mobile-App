package com.example.lessonmonitor.data.export

import com.example.lessonmonitor.data.local.entity.AttendanceStatus
import com.example.lessonmonitor.domain.repository.LessonExportRow
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvWriterTest {

    @Test
    fun `writeLessonAttendanceCsv writes a header row plus one row per record`() {
        val rows = listOf(
            LessonExportRow(sessionDate = 0L, studentName = "Ada", status = AttendanceStatus.PRESENT, absenceReason = null),
            LessonExportRow(sessionDate = 1L, studentName = "Bea", status = AttendanceStatus.ABSENT, absenceReason = "Sick")
        )

        val csv = CsvWriter.writeLessonAttendanceCsv(rows)
        val lines = csv.split("\n")

        assertEquals("Session Date,Student,Status,Reason", lines[0])
        assertEquals("1970-01-01,Ada,PRESENT,", lines[1])
        assertEquals("1970-01-02,Bea,ABSENT,Sick", lines[2])
    }

    @Test
    fun `writeLessonAttendanceCsv quotes fields containing a comma`() {
        val rows = listOf(
            LessonExportRow(sessionDate = 0L, studentName = "Doe, Jane", status = AttendanceStatus.LATE, absenceReason = null)
        )

        val csv = CsvWriter.writeLessonAttendanceCsv(rows)

        assertEquals("Session Date,Student,Status,Reason\n1970-01-01,\"Doe, Jane\",LATE,", csv)
    }

    @Test
    fun `writeLessonAttendanceCsv doubles embedded quotes`() {
        val rows = listOf(
            LessonExportRow(sessionDate = 0L, studentName = "Ada", status = AttendanceStatus.EXCUSED, absenceReason = "Said \"sick\"")
        )

        val csv = CsvWriter.writeLessonAttendanceCsv(rows)

        assertEquals("Session Date,Student,Status,Reason\n1970-01-01,Ada,EXCUSED,\"Said \"\"sick\"\"\"", csv)
    }

    @Test
    fun `writeLessonAttendanceCsv with no rows writes only the header`() {
        val csv = CsvWriter.writeLessonAttendanceCsv(emptyList())

        assertEquals("Session Date,Student,Status,Reason", csv)
    }
}
