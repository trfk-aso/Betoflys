package org.betofly.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.model.Coordinates
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.Place
import org.betofly.app.model.RoutePoint
import org.betofly.app.repository.TripRepository

class RecordingViewModel(
    private val repository: TripRepository,
    private val tripDetailsViewModel: TripDetailsViewModel
) : ViewModel() {

    var currentTripId: Long? = null
        private set

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _routePoints = MutableStateFlow<List<RoutePoint>>(emptyList())
    val routePoints: StateFlow<List<RoutePoint>> = _routePoints.asStateFlow()

    private val _entries = MutableStateFlow<List<EntryModel>>(emptyList())
    val entries: StateFlow<List<EntryModel>> = _entries.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private val _currentTripTitle = MutableStateFlow<String?>(null)
    val currentTripTitle: StateFlow<String?> = _currentTripTitle.asStateFlow()

    private var _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var timerJob: Job? = null
    private var gpsJob: Job? = null

    private val tripDurations = mutableMapOf<Long, Long>()
    private val tripRoutePoints = mutableMapOf<Long, List<RoutePoint>>()
    private val tripEntries = mutableMapOf<Long, List<EntryModel>>()

    val now: LocalDateTime
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    fun setCurrentTripName(name: String) {
        _currentTripTitle.value = name
    }

    fun startTripRecording(
        tripId: Long,
        tripTitle: String,
        isNewTrip: Boolean = false,
        startCoords: Coordinates? = null
    ) {
        currentTripId = tripId
        _currentTripTitle.value = tripTitle
        tripDetailsViewModel.setTripId(tripId)

        _isRecording.value = true
        _isPaused.value = false

        viewModelScope.launch {
            if (isNewTrip) {
                tripDurations[tripId] = 0L
                tripRoutePoints[tripId] = emptyList()
                tripEntries[tripId] = emptyList()
            } else {
                val trip = repository.getTripById(tripId)
                tripDurations[tripId] = trip?.duration ?: 0L
                tripRoutePoints[tripId] = repository.getRoutePointsForTrip(tripId)
                tripEntries[tripId] = repository.getEntriesForTrip(tripId)
            }

            _recordingDuration.value = tripDurations[tripId] ?: 0L
            _routePoints.value = tripRoutePoints[tripId] ?: emptyList()
            _entries.value = tripEntries[tripId] ?: emptyList()
        }

        startTimer()
        startGps(startCoords)

        startCoords?.let { toggleRouteRecording(it) }
    }

    fun stopTripRecording() {
        _isRecording.value = false
        timerJob?.cancel()
        gpsJob?.cancel()
        timerJob = null
        gpsJob = null

        currentTripId?.let { tripId ->
            val duration = _recordingDuration.value
            viewModelScope.launch {
                repository.updateTripDuration(tripId, duration)
            }
        }
    }

    fun pauseRecording() {
        if (_isRecording.value) {
            _isRecording.value = false
            _isPaused.value = true
            timerJob?.cancel()
            gpsJob?.cancel()
            timerJob = null
            gpsJob = null

            currentTripId?.let { tripId ->
                tripDurations[tripId] = _recordingDuration.value
                tripRoutePoints[tripId] = _routePoints.value
                tripEntries[tripId] = _entries.value
            }
        }
    }

    fun resumeRecording(coords: Coordinates? = null) {
        if (_isPaused.value) {
            _isRecording.value = true
            _isPaused.value = false
            startTimer()
            startGps(coords)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000L)
                _recordingDuration.value += 1
                currentTripId?.let { tripDurations[it] = _recordingDuration.value }
            }
        }
    }

    private fun startGps(coords: Coordinates?) {
        gpsJob?.cancel()
        gpsJob = viewModelScope.launch {
            var lat = coords?.latitude ?: _routePoints.value.lastOrNull()?.coords?.latitude ?: 50.0
            var lon = coords?.longitude ?: _routePoints.value.lastOrNull()?.coords?.longitude ?: 30.0

            while (_isRecording.value) {
                delay(3000L)
                toggleRouteRecording(Coordinates(lat, lon))
                lat += 0.0001
                lon += 0.0001
            }
        }
    }

    fun toggleRecording(coords: Coordinates? = null) {
        when {
            _isRecording.value -> stopTripRecording()
            _isPaused.value -> resumeRecording(coords)
            else -> {
                val tripId = currentTripId ?: return
                val title = _currentTripTitle.value ?: "Unknown Trip"
                startTripRecording(
                    tripId,
                    title,
                    isNewTrip = _routePoints.value.isEmpty(),
                    startCoords = coords
                )
            }
        }
    }

    fun toggleRouteRecording(coords: Coordinates) {
        if (!_isRecording.value) return
        val tripId = currentTripId ?: return

        val point = RoutePoint(
            id = 0L,
            tripId = tripId,
            coords = coords,
            timestamp = now,
            altitude = null,
            speed = null
        )

        viewModelScope.launch {
            repository.insertRoutePoint(point)
            _routePoints.value = _routePoints.value + point
            tripRoutePoints[tripId] = _routePoints.value
        }
    }

    fun addPhotoEntry(title: String, mediaId: String) = addEntry(EntryType.PHOTO, title, null, listOf(mediaId), null)
    fun addNoteEntry(title: String, text: String) = addEntry(EntryType.NOTE, title, text, emptyList(), null)
    fun addPlaceEntry(name: String, coords: Coordinates, note: String? = null) =
        addEntry(EntryType.PLACE, name, note, emptyList(), coords)

    private fun addEntry(
        type: EntryType,
        title: String,
        text: String?,
        mediaIds: List<String>,
        coords: Coordinates?
    ) = viewModelScope.launch {
        val tripId = currentTripId ?: return@launch
        val now = now
        val entry = EntryModel(
            id = 0L,
            tripId = tripId,
            type = type,
            title = title,
            text = text,
            mediaIds = mediaIds,
            coords = coords,
            timestamp = now,
            tags = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        repository.insertEntry(entry)
        _entries.value = _entries.value + entry
        tripEntries[tripId] = _entries.value

        if (type == EntryType.PLACE) {
            val place = Place(
                id = 0L,
                tripId = tripId,
                name = title,
                coords = coords!!,
                note = text,
                photoId = null
            )
            repository.insertPlace(place)
        }
    }

    fun saveAndExit() {
        stopTripRecording()
        currentTripId = null
    }
}
