package com.example.lessonmonitor.data.local.entity

import kotlinx.serialization.Serializable

/** Per-student attendance outcome for a single [AttendanceSessionEntity]. */
@Serializable
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}
