package com.example.lessonmonitor.domain.repository

import com.example.lessonmonitor.data.local.entity.AttendanceStatus

/**
 * Flat denormalized row used for CSV export of lesson attendance.
 *
 * @property sessionDate Epoch day (LocalDate.toEpochDay()) of the session.
 * @property studentName Display name of the student.
 * @property status Attendance outcome for that student.
 * @property absenceReason Optional reason when status is not PRESENT.
 */
data class LessonExportRow(
    val sessionDate: Long,
    val studentName: String,
    val status: AttendanceStatus,
    val absenceReason: String?
)
