package com.example.lessonmonitor.data.export

import com.example.lessonmonitor.domain.repository.BackupSnapshot
import kotlinx.serialization.json.Json

/**
 * JSON encode/decode for [BackupSnapshot] (PLAN.md §6 tech decision: "JSON
 * snapshot ... via kotlinx.serialization"). Uses a dedicated `Json`
 * instance — `prettyPrint` keeps exported files human-inspectable per that
 * same decision, `ignoreUnknownKeys` lets an older app version still open a
 * snapshot exported by a newer one that added fields.
 */
object BackupJsonSerializer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun encode(snapshot: BackupSnapshot): String = json.encodeToString(BackupSnapshot.serializer(), snapshot)

    fun decode(text: String): BackupSnapshot = json.decodeFromString(BackupSnapshot.serializer(), text)
}
