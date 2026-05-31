package com.example.db

import kotlinx.coroutines.flow.Flow

class RecordingRepository(private val recordingDao: RecordingDao) {

    val allRecordings: Flow<List<Recording>> = recordingDao.getAllRecordings()

    fun searchRecordings(query: String): Flow<List<Recording>> {
        return if (query.isBlank()) {
            recordingDao.getAllRecordings()
        } else {
            recordingDao.searchRecordings(query)
        }
    }

    suspend fun getRecordingById(id: Int): Recording? {
        return recordingDao.getRecordingById(id)
    }

    suspend fun insert(recording: Recording): Long {
        return recordingDao.insertRecording(recording)
    }

    suspend fun update(recording: Recording) {
        recordingDao.updateRecording(recording)
    }

    suspend fun delete(recording: Recording) {
        recordingDao.deleteRecording(recording)
    }

    suspend fun deleteById(id: Int) {
        recordingDao.deleteRecordingById(id)
    }
}
