package com.example.lessonmonitor.data.local.entity

/**
 * Simple recurrence rules for a [LessonEntity], per PLAN.md §2/§5 — no RRULE
 * parsing, just daily/weekly/custom-weekday patterns.
 */
enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    CUSTOM_DAYS
}
