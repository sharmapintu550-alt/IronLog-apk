package com.example.data.entity

import androidx.room.TypeConverter
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromUuid(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUuid(uuidString: String?): UUID? {
        return uuidString?.let {
            try {
                UUID.fromString(it)
            } catch (e: Exception) {
                UUID.nameUUIDFromBytes(it.toByteArray(Charsets.UTF_8))
            }
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return ""
        return list.joinToString("|||")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return if (data.contains("|||")) {
            data.split("|||").filter { it.isNotBlank() }
        } else if (data.startsWith("[") && data.endsWith("]")) {
            data.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } else {
            data.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }
}
