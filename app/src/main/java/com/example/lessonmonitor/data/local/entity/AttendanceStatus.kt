package com.example.lessonmonitor.data.local.entity

/** Per-student attendance outcome for a single [AttendanceSessionEntity]. */
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}
