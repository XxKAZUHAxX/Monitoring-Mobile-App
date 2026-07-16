package com.example.lessonmonitor.data.local.entity

import kotlinx.serialization.Serializable

/** Per-student attendance outcome for a single lesson. */
@Serializable
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}
