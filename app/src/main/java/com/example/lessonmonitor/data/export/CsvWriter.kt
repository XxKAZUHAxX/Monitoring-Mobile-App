package com.example.lessonmonitor.data.export

import com.example.lessonmonitor.domain.repository.LessonExportRow
import java.time.LocalDate

/**
 * Manual CSV building (PLAN.md §6 tech decision: "CSV built manually, no
 * library needed"). Plain-JVM/pure-function so it's unit-testable without
 * Robolectric, same rationale as `util/PasswordHasher.kt`.
 */
object CsvWriter {

    private const val HEADER = "Session Date,Student,Status,Reason"

    fun writeLessonAttendanceCsv(rows: List<LessonExportRow>): String {
        val lines = rows.map { row ->
            listOf(
                LocalDate.ofEpochDay(row.sessionDate).toString(),
                row.studentName,
                row.status.name,
                row.absenceReason.orEmpty()
            ).joinToString(",") { escapeField(it) }
        }
        return (listOf(HEADER) + lines).joinToString("\n")
    }

    /** Quotes a field (doubling any embedded quotes) if it contains a comma, quote, or newline. */
    private fun escapeField(value: String): String =
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
}
