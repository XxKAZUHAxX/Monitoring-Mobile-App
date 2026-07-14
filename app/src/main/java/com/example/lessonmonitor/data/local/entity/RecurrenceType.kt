package com.example.lessonmonitor.data.local.entity

import kotlinx.serialization.Serializable

/**
 * Simple recurrence rules for a [LessonEntity], per PLAN.md §2/§5 — no RRULE
 * parsing, just daily/weekly/custom-weekday patterns.
 */
@Serializable
enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    CUSTOM_DAYS
}
