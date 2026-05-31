package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ThemeSettings
import com.example.db.AppDatabase
import com.example.db.Recording
import com.example.db.RecordingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

enum class VaultScreen {
    SPLASH,
    DASHBOARD,
    RECORDING,
    PLAYBACK
}

enum class RepeatMode {
    OFF, ONE, STARRED, ALL
}

class VoiceVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordingRepository

    // Search and display results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recordingsList = MutableStateFlow<List<Recording>>(emptyList())
    val recordingsList: StateFlow<List<Recording>> = _recordingsList.asStateFlow()

    // Screen navigation
    private val _currentScreen = MutableStateFlow(VaultScreen.SPLASH)
    val currentScreen: StateFlow<VaultScreen> = _currentScreen.asStateFlow()

    private val screenBackstack = mutableListOf<VaultScreen>()

    // Recording State
    enum class RecordStatus { IDLE, RECORDING, PAUSED }
    private val _recordStatus = MutableStateFlow(RecordStatus.IDLE)
    val recordStatus: StateFlow<RecordStatus> = _recordStatus.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _inputLevels = MutableStateFlow<List<Float>>(listOf(0.2f, 0.4f, 0.6f, 0.2f, 0.1f))
    val inputLevels: StateFlow<List<Float>> = _inputLevels.asStateFlow()

    private val _isHdQuality = MutableStateFlow(true)
    val isHdQuality: StateFlow<Boolean> = _isHdQuality.asStateFlow()

    // Playback State
    private val _activePlaybackRecording = MutableStateFlow<Recording?>(null)
    val activePlaybackRecording: StateFlow<Recording?> = _activePlaybackRecording.asStateFlow()

    private val _isPlaybackPlaying = MutableStateFlow(false)
    val isPlaybackPlaying: StateFlow<Boolean> = _isPlaybackPlaying.asStateFlow()

    private val _playbackSeconds = MutableStateFlow(0)
    val playbackSeconds: StateFlow<Int> = _playbackSeconds.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.5f) // Default "1.5x SPEED"
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    fun cycleRepeatMode() {
        val modes = RepeatMode.values()
        val currentIdx = modes.indexOf(_repeatMode.value)
        val nextIdx = (currentIdx + 1) % modes.size
        _repeatMode.value = modes[nextIdx]
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    // Simulated Storage Info
    val totalStorageLimitGb = 50.0
    private val _storageUsedGb = MutableStateFlow(12.4)
    val storageUsedGb: StateFlow<Double> = _storageUsedGb.asStateFlow()

    // Coroutine loops
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    // Theme Settings
    val isDarkMode: StateFlow<Boolean> = ThemeSettings.isDarkMode

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = RecordingRepository(database.recordingDao())

        ThemeSettings.init(application)

        // Observe search query
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                repository.searchRecordings(query)
            }.collectLatest { list ->
                _recordingsList.value = list

                // Calculate estimated storage used
                val totalBytes = list.sumOf { it.fileSize }
                val gb = 12.4 + (totalBytes / (1024.0 * 1024.0 * 1024.0))
                _storageUsedGb.value = Math.min(totalStorageLimitGb, Math.round(gb * 10.0) / 10.0)
            }
        }

        // Navigate past splash automatically after a delay
        viewModelScope.launch {
            delay(3000)
            setScreen(VaultScreen.DASHBOARD)
        }
    }

    fun setScreen(screen: VaultScreen) {
        if (_currentScreen.value != screen) {
            screenBackstack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun goBack(): Boolean {
        if (screenBackstack.isNotEmpty()) {
            _currentScreen.value = screenBackstack.removeAt(screenBackstack.size - 1)
            if (_currentScreen.value == VaultScreen.DASHBOARD) {
                stopPlaying()
            }
            return true
        }
        return false
    }

    fun toggleTheme() {
        val nextMode = !isDarkMode.value
        ThemeSettings.setDarkMode(getApplication(), nextMode)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(recording: Recording) {
        viewModelScope.launch {
            val updated = recording.copy(isFavorite = !recording.isFavorite)
            repository.update(updated)
            if (_activePlaybackRecording.value?.id == recording.id) {
                _activePlaybackRecording.value = updated
            }
        }
    }

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            repository.delete(recording)
            if (_activePlaybackRecording.value?.id == recording.id) {
                _activePlaybackRecording.value = null
                stopPlaying()
            }
            setScreen(VaultScreen.DASHBOARD)
        }
    }

    fun renameRecording(recording: Recording, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val updated = recording.copy(title = newTitle)
            repository.update(updated)
            if (_activePlaybackRecording.value?.id == recording.id) {
                _activePlaybackRecording.value = updated
            }
        }
    }

    fun toggleHdQuality() {
        _isHdQuality.value = !_isHdQuality.value
    }

    // Playback Controllers
    fun selectRecordingForPlayback(recordingId: Int) {
        viewModelScope.launch {
            val rec = repository.getRecordingById(recordingId)
            if (rec != null) {
                stopPlaying()
                _activePlaybackRecording.value = rec
                _playbackSeconds.value = 0
                _isPlaybackPlaying.value = false
                setScreen(VaultScreen.PLAYBACK)
            }
        }
    }

    fun startPlaying() {
        _isPlaybackPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_isPlaybackPlaying.value) {
                val currentRec = _activePlaybackRecording.value ?: break
                val maxSeconds = currentRec.durationSeconds.toInt()
                delay((1000 / _playbackSpeed.value).toLong())
                if (_playbackSeconds.value < maxSeconds) {
                    _playbackSeconds.value += 1
                } else {
                    handlePlaybackEnded()
                }
            }
        }
    }

    private fun handlePlaybackEnded() {
        val currentRec = _activePlaybackRecording.value ?: return
        val list = _recordingsList.value
        when (_repeatMode.value) {
            RepeatMode.OFF -> {
                _playbackSeconds.value = 0
                _isPlaybackPlaying.value = false
            }
            RepeatMode.ONE -> {
                _playbackSeconds.value = 0
            }
            RepeatMode.STARRED -> {
                val starredList = list.filter { it.isFavorite }
                if (starredList.isNotEmpty()) {
                    val currentIdx = starredList.indexOfFirst { it.id == currentRec.id }
                    val nextIdx = if (currentIdx != -1) (currentIdx + 1) % starredList.size else 0
                    _activePlaybackRecording.value = starredList[nextIdx]
                    _playbackSeconds.value = 0
                } else {
                    _playbackSeconds.value = 0
                    _isPlaybackPlaying.value = false
                }
            }
            RepeatMode.ALL -> {
                if (list.isNotEmpty()) {
                    val currentIdx = list.indexOfFirst { it.id == currentRec.id }
                    val nextIdx = if (currentIdx != -1) (currentIdx + 1) % list.size else 0
                    _activePlaybackRecording.value = list[nextIdx]
                    _playbackSeconds.value = 0
                } else {
                    _playbackSeconds.value = 0
                    _isPlaybackPlaying.value = false
                }
            }
        }
    }

    fun playNext() {
        val currentRec = _activePlaybackRecording.value ?: return
        val list = _recordingsList.value
        if (list.isNotEmpty()) {
            val currentIdx = list.indexOfFirst { it.id == currentRec.id }
            val nextIdx = if (currentIdx != -1) (currentIdx + 1) % list.size else 0
            _activePlaybackRecording.value = list[nextIdx]
            _playbackSeconds.value = 0
            if (_isPlaybackPlaying.value) {
                startPlaying()
            }
        }
    }

    fun playPrevious() {
        val currentRec = _activePlaybackRecording.value ?: return
        val list = _recordingsList.value
        if (list.isNotEmpty()) {
            val currentIdx = list.indexOfFirst { it.id == currentRec.id }
            val prevIdx = if (currentIdx != -1) (currentIdx - 1 + list.size) % list.size else 0
            _activePlaybackRecording.value = list[prevIdx]
            _playbackSeconds.value = 0
            if (_isPlaybackPlaying.value) {
                startPlaying()
            }
        }
    }

    fun pausePlaying() {
        _isPlaybackPlaying.value = false
        playbackJob?.cancel()
    }

    fun togglePlayback() {
        if (_isPlaybackPlaying.value) {
            pausePlaying()
        } else {
            startPlaying()
        }
    }

    fun stopPlaying() {
        _isPlaybackPlaying.value = false
        playbackJob?.cancel()
        _playbackSeconds.value = 0
    }

    fun skipPlayback(seconds: Int) {
        val rec = _activePlaybackRecording.value ?: return
        val updated = _playbackSeconds.value + seconds
        _playbackSeconds.value = Math.max(0, Math.min(rec.durationSeconds.toInt(), updated))
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
        val currentIdx = speeds.indexOf(_playbackSpeed.value)
        val nextIdx = (currentIdx + 1) % speeds.size
        _playbackSpeed.value = speeds[nextIdx]

        if (_isPlaybackPlaying.value) {
            startPlaying()
        }
    }

    // Recording Controllers
    fun triggerStartRecording() {
        setScreen(VaultScreen.RECORDING)
        _recordStatus.value = RecordStatus.RECORDING
        _recordingSeconds.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (_recordStatus.value == RecordStatus.RECORDING) {
                delay(1000)
                _recordingSeconds.value += 1
                _inputLevels.value = List(5) { (2..10).random() / 10f }
            }
        }
    }

    fun pauseResumeRecording() {
        if (_recordStatus.value == RecordStatus.RECORDING) {
            _recordStatus.value = RecordStatus.PAUSED
            recordingJob?.cancel()
        } else if (_recordStatus.value == RecordStatus.PAUSED) {
            _recordStatus.value = RecordStatus.RECORDING
            recordingJob = viewModelScope.launch {
                while (_recordStatus.value == RecordStatus.RECORDING) {
                    delay(1000)
                    _recordingSeconds.value += 1
                    _inputLevels.value = List(5) { (2..10).random() / 10f }
                }
            }
        }
    }

    fun cancelRecording() {
        _recordStatus.value = RecordStatus.IDLE
        recordingJob?.cancel()
        _recordingSeconds.value = 0
        setScreen(VaultScreen.DASHBOARD)
    }

    fun stopAndSaveRecording(customTitle: String? = null, customTag: String? = null) {
        val secondsRecorded = _recordingSeconds.value
        if (secondsRecorded <= 0) {
            cancelRecording()
            return
        }

        viewModelScope.launch {
            _recordStatus.value = RecordStatus.IDLE
            recordingJob?.cancel()

            val nextNumber = _recordingsList.value.size + 1
            val title = if (!customTitle.isNullOrBlank()) customTitle else "Recording Session #$nextNumber"
            val fileTag = if (!customTag.isNullOrBlank()) customTag else "MEMO"

            val cal = Calendar.getInstance()
            val generatedWaveform = (1..60).map { (15..95).random() }.joinToString(",")
            val fileSize = secondsRecorded * 16000L

            val newRec = Recording(
                title = title,
                dateMillis = cal.timeInMillis,
                durationSeconds = secondsRecorded.toLong(),
                tag = fileTag,
                filePath = "/simulated/session_$nextNumber.mp3",
                isFavorite = false,
                fileSize = fileSize,
                waveformCSV = generatedWaveform
            )

            val id = repository.insert(newRec)
            _recordingSeconds.value = 0

            selectRecordingForPlayback(id.toInt())
        }
    }

    // --- Gemini AI Speech-To-Text / Transcription Integration ---
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiResultText = MutableStateFlow("")
    val aiResultText: StateFlow<String> = _aiResultText.asStateFlow()

    private val _aiTranscriptLoadingSubtext = MutableStateFlow("")
    val aiTranscriptLoadingSubtext: StateFlow<String> = _aiTranscriptLoadingSubtext.asStateFlow()

    fun runGeminiVoiceAnalysis() {
        val rec = _activePlaybackRecording.value ?: return
        _isAiLoading.value = true
        _aiResultText.value = ""

        viewModelScope.launch {
            val sequences = listOf(
                "Accessing encrypted Voice Vault audio channel...",
                "Decoding audio signals with neural networks...",
                "Sending secure context vectors to Gemini 3.5 Flash...",
                "Formatting meeting summaries, Action Items & transcript flow..."
            )

            // Dynamic progression updates
            val progressJob = viewModelScope.launch {
                for (step in sequences) {
                    if (_isAiLoading.value) {
                        _aiTranscriptLoadingSubtext.value = step
                        delay(1200)
                    }
                }
            }

            val formatSeconds = rec.durationSeconds
            val durationText = "${formatSeconds / 60}:${String.format("%02d", formatSeconds % 60)}"

            val result = com.example.api.GeminiManager.transcribeOrSummarizeAudio(
                title = rec.title,
                tag = rec.tag,
                durationString = durationText
            )

            progressJob.cancel()
            _aiResultText.value = result
            _isAiLoading.value = false
        }
    }
}
