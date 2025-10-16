package org.betofly.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.model.Coordinates
import org.betofly.app.model.EntryType
import org.betofly.app.model.RoutePoint
import org.betofly.app.model.Trip
import org.betofly.app.repository.EntryRepository
import org.betofly.app.repository.TripRepository
import org.betofly.app.repository.daysUntil
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class TripStatistics(
    val trip: Trip,
    val routeLength: Float,
    val recordingDuration: Long,
    val daysWithEntries: Int
)

data class GraphData(
    val entriesPerDay: Map<LocalDate, Int>,
    val topTags: Map<String, Int>,
    val topCategories: Map<String, Int>
)

enum class StatisticsPeriod(val value: String) {
    WEEK("7"),
    MONTH("30"),
    MONTH_3("90"),
    ALL_TIME("All")
}


sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    object NoData : StatisticsUiState()
    data class Success(
        val tripsCount: Int,
        val totalDays: Long,
        val entriesCount: Int,
        val photosCount: Int,
        val placesCount: Int,
        val avgTripProgress: Float
    ) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(
    private val tripRepository: TripRepository,
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val _period = MutableStateFlow(StatisticsPeriod.ALL_TIME)
    val period: StateFlow<StatisticsPeriod> = _period.asStateFlow()

    private val _selectedTripId = MutableStateFlow<Long?>(null)
    val selectedTripId: StateFlow<Long?> = _selectedTripId.asStateFlow()

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _tripStats = MutableStateFlow<TripStatistics?>(null)
    val tripStats: StateFlow<TripStatistics?> = _tripStats.asStateFlow()

    private val _graphData = MutableStateFlow<GraphData?>(null)
    val graphData: StateFlow<GraphData?> = _graphData.asStateFlow()

    private val _allTrips = MutableStateFlow<List<Trip>>(emptyList())
    val allTrips: StateFlow<List<Trip>> = _allTrips.asStateFlow()

    init {
        observeTrips()
        refreshStatistics(initial = true)
    }

    fun setPeriod(period: StatisticsPeriod) {
        _period.value = period
        refreshStatistics(initial = true)
    }

    fun selectTrip(tripId: Long?) {
        _selectedTripId.value = tripId
        if (tripId != null) {
            loadTripStats(tripId)
        } else {
            _tripStats.value = null
        }
    }


    private fun loadTripStats(tripId: Long) = viewModelScope.launch {
        try {
            val trip = _allTrips.value.find { it.id == tripId } ?: return@launch
            val entries = entryRepository.getEntriesForTrip(trip.id)
            val routePoints = tripRepository.getRoutePointsForTrip(trip.id)
            val routeLength = calculateRouteLength(routePoints)
            val recordingDuration = trip.duration
            val daysWithEntries = entries.map { it.timestamp.date }
                .filter { it in trip.startDate..trip.endDate }
                .distinct()
                .count()

            _tripStats.value = TripStatistics(
                trip = trip,
                routeLength = routeLength,
                recordingDuration = recordingDuration,
                daysWithEntries = daysWithEntries
            )
        } catch (e: Exception) {
            _tripStats.value = null
        }
    }

    fun refreshStatistics(initial: Boolean = false) = viewModelScope.launch {
        if (initial) {
            _uiState.value = StatisticsUiState.Loading
        }
        try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val startDate = when (_period.value) {
                StatisticsPeriod.WEEK -> now.minus(7, DateTimeUnit.DAY)
                StatisticsPeriod.MONTH -> now.minus(30, DateTimeUnit.DAY)
                StatisticsPeriod.MONTH_3 -> now.minus(90, DateTimeUnit.DAY)
                StatisticsPeriod.ALL_TIME -> LocalDate(1970, 1, 1)
            }

            val trips = tripRepository.getAllTrips().filter { it.startDate >= startDate }
            _allTrips.value = trips

            val allEntries = entryRepository.searchEntries(startDate = startDate)

            if (trips.isEmpty() && allEntries.isEmpty()) {
                _uiState.value = StatisticsUiState.NoData
                return@launch
            }

            val totalDays = trips.sumOf { it.startDate.daysUntil(it.endDate).toLong() }
            val photoCount = allEntries.count { it.type == EntryType.PHOTO }
            val placeCount = allEntries.count { it.type == EntryType.PLACE }
            val avgProgress = if (trips.isNotEmpty()) {
                trips.map { trip ->
                    val tripEntries = entryRepository.getEntriesForTrip(trip.id)
                    val days = trip.startDate.daysUntil(trip.endDate).coerceAtLeast(1)
                    val daysWithEntries = tripEntries.map { it.timestamp.date }
                        .filter { it in trip.startDate..trip.endDate }
                        .distinct()
                        .count()
                    (daysWithEntries.toFloat() / days).coerceIn(0f, 1f)
                }.average().toFloat()
            } else 0f

            _uiState.value = StatisticsUiState.Success(
                tripsCount = trips.size,
                totalDays = totalDays,
                entriesCount = allEntries.size,
                photosCount = photoCount,
                placesCount = placeCount,
                avgTripProgress = avgProgress
            )

            val entriesPerDay = allEntries.groupBy { it.timestamp.date }
                .mapValues { it.value.size }
            val topTags = allEntries.flatMap { it.tags }
                .groupingBy { it }.eachCount()
            val topCategories = trips.groupingBy { it.category.name }.eachCount()

            _graphData.value = GraphData(entriesPerDay, topTags, topCategories)

        } catch (e: Exception) {
            _uiState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
        }
    }

    private fun calculateRouteLength(routePoints: List<RoutePoint>): Float {
        if (routePoints.size < 2) return 0f
        return routePoints.zipWithNext { a, b ->
            distanceBetween(a.coords, b.coords)
        }.sum()
    }

    private fun observeTrips() = viewModelScope.launch {
        tripRepository.observeAllTrips().collect { trips ->
            _allTrips.value = trips
            _selectedTripId.value?.let { loadTripStats(it) }
            refreshStatistics()
        }
    }

    private fun distanceBetween(a: Coordinates, b: Coordinates): Float {
        val earthRadius = 6371.0
        val dLat = ((b.latitude - a.latitude) * PI / 180)
        val dLon = ((b.longitude - a.longitude) * PI / 180)
        val lat1 = a.latitude * PI / 180
        val lat2 = b.latitude * PI / 180

        val haversine = sin(dLat / 2).pow(2.0) +
                sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)

        return (2 * earthRadius * asin(sqrt(haversine))).toFloat()
    }
}