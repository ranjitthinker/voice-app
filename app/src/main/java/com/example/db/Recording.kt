package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class Recording(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateMillis: Long,
    val durationSeconds: Long,
    val tag: String, // MEETING, INTERVIEW, MEMO, URGENT
    val filePath: String,
    val isFavorite: Boolean = false,
    val fileSize: Long = 0,
    val waveformCSV: String // CSV of float values for drawing correct custom audio waveforms
)
