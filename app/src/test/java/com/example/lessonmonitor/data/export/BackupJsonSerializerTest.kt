package com.example.lessonmonitor.data.export

import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.BackupSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupJsonSerializerTest {

    @Test
    fun `encode then decode round-trips a snapshot`() {
        val snapshot = BackupSnapshot(
            schemaVersion = 1,
            exportedAt = 12345L,
            users = emptyList(),
            categories = listOf(CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)),
            lessons = emptyList(),
            students = emptyList(),
            enrollments = emptyList(),
            attendanceSessions = emptyList(),
            attendanceRecords = emptyList()
        )

        val json = BackupJsonSerializer.encode(snapshot)
        val decoded = BackupJsonSerializer.decode(json)

        assertEquals(snapshot, decoded)
    }

    @Test
    fun `decode ignores unknown keys so older code can open a newer export`() {
        val json = """
            {
              "schemaVersion": 1,
              "exportedAt": 1,
              "users": [],
              "categories": [],
              "lessons": [],
              "students": [],
              "enrollments": [],
              "attendanceSessions": [],
              "attendanceRecords": [],
              "aFutureFieldThisVersionDoesNotKnowAbout": "ignored"
            }
        """.trimIndent()

        val decoded = BackupJsonSerializer.decode(json)

        assertEquals(1, decoded.schemaVersion)
    }
}
