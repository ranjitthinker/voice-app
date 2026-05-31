package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [Recording::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voice_vault_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.recordingDao())
                }
            }
        }

        suspend fun populateDatabase(dao: RecordingDao) {
            // Helper to generate a random mock waveform CSV
            fun makeWaveform(count: Int): String {
                return (1..count).map { (15..95).random() }.joinToString(",")
            }

            val cal = Calendar.getInstance()

            // Weekly Sync Meeting (Oct 24, 2023 10:30 AM)
            cal.set(2023, Calendar.OCTOBER, 24, 10, 30, 0)
            dao.insertRecording(
                Recording(
                    title = "Weekly Sync Meeting",
                    dateMillis = cal.timeInMillis,
                    durationSeconds = 2712, // 45:12
                    tag = "MEETING",
                    filePath = "/simulated/weekly_sync.mp3",
                    isFavorite = true,
                    fileSize = 43200000, // ~43 MB
                    waveformCSV = makeWaveform(60)
                )
            )

            // Project Alpha Interview (Oct 23, 2023 2:15 PM)
            cal.set(2023, Calendar.OCTOBER, 23, 14, 15, 0)
            dao.insertRecording(
                Recording(
                    title = "Project Alpha Interview",
                    dateMillis = cal.timeInMillis,
                    durationSeconds = 725, // 12:05
                    tag = "INTERVIEW",
                    filePath = "/simulated/project_alpha.mp3",
                    isFavorite = false,
                    fileSize = 12500000, // ~12.5 MB
                    waveformCSV = makeWaveform(60)
                )
            )

            // Guitar Melody Draft (Oct 22, 2023 11:45 PM)
            cal.set(2023, Calendar.OCTOBER, 22, 23, 45, 0)
            dao.insertRecording(
                Recording(
                    title = "Guitar Melody Draft",
                    dateMillis = cal.timeInMillis,
                    durationSeconds = 154, // 02:34
                    tag = "MEMO",
                    filePath = "/simulated/guitar_melody.mp3",
                    isFavorite = true,
                    fileSize = 2500000, // ~2.5 MB
                    waveformCSV = makeWaveform(60)
                )
            )

            // Quarterly Strategy Session (Oct 24, 2023) - as shown in Playback Screen
            cal.set(2023, Calendar.OCTOBER, 24, 9, 15, 0)
            dao.insertRecording(
                Recording(
                    title = "Quarterly Strategy Session",
                    dateMillis = cal.timeInMillis,
                    durationSeconds = 2535, // 42:15
                    tag = "MEETING",
                    filePath = "/simulated/quarterly_strategy.mp3",
                    isFavorite = false,
                    fileSize = 39800000, // ~39.8 MB
                    waveformCSV = makeWaveform(80)
                )
            )

            // Add an Urgent Memo
            cal.set(2023, Calendar.OCTOBER, 21, 16, 20, 0)
            dao.insertRecording(
                Recording(
                    title = "Urgent Product Briefing",
                    dateMillis = cal.timeInMillis,
                    durationSeconds = 305, // 5:05
                    tag = "URGENT",
                    filePath = "/simulated/urgent_brief.mp3",
                    isFavorite = true,
                    fileSize = 5100000,
                    waveformCSV = makeWaveform(60)
                )
            )
        }
    }
}
