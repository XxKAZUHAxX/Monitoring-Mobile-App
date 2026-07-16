package com.example.lessonmonitor.data.local.converter

import androidx.room.TypeConverter
import com.example.lessonmonitor.data.local.entity.AttendanceStatus

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)
}
