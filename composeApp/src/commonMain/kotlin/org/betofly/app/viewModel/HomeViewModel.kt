package org.betofly.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.Trip
import org.betofly.app.model.TripCategory
import org.betofly.app.model.TripUiModel
import org.betofly.app.repository.EntryRepository
import org.betofly.app.repository.TripRepository
import org.betofly.app.ui.screens.home.daysUntil
import kotlin.math.log

class HomeViewModel(
    private val repository: TripRepository,
    private val tripDetailsViewModel: TripDetailsViewModel
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<TripCategory?>(null)
    val selectedCategory: StateFlow<TripCategory?> = _selectedCategory.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeTrips()
    }

    private fun observeTrips() {
        viewModelScope.launch {
            repository.observeAllTrips().collect { trips ->
                val filtered = _selectedCategory.value?.let { category ->
                    trips.filter { it.category == category }
                } ?: trips

                if (filtered.isEmpty()) {
                    _uiState.value = HomeUiState.Empty
                } else {
                    val tripsUiModels = filtered.map { enrichTrip(it) }
                    val recentlyEdited = repository.getRecentlyEditedTrips().map { enrichTrip(it) }
                    val recentlyExported = repository.getRecentlyExportedTrips().map { enrichTrip(it) }

                    _uiState.value = HomeUiState.Success(
                        trips = tripsUiModels,
                        recentlyEdited = recentlyEdited,
                        recentlyExported = recentlyExported
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: TripCategory?) {
        _selectedCategory.value = category
    }

    fun onTripSelected(tripId: Long) {
        tripDetailsViewModel.setTripId(tripId)
    }

    fun createTrip(trip: Trip) = viewModelScope.launch { repository.insertTrip(trip) }
    fun editTrip(trip: Trip) = viewModelScope.launch { repository.updateTrip(trip) }
    fun exportTrip(tripId: Long) = viewModelScope.launch { repository.markTripAsExported(tripId) }
    fun toggleFavorite(tripId: Long) = viewModelScope.launch {
        if (repository.isFavorite(tripId)) repository.removeFavorite(tripId)
        else repository.addFavorite(tripId)

        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(
                trips = currentState.trips.map { tripUi ->
                    if (tripUi.trip.id == tripId) tripUi.copy(isFavorite = !tripUi.isFavorite)
                    else tripUi
                },
                recentlyEdited = currentState.recentlyEdited,
                recentlyExported = currentState.recentlyExported
            )
        }
    }
    fun deleteTrip(tripId: Long) = viewModelScope.launch { repository.deleteTrip(tripId) }

    private suspend fun enrichTrip(trip: Trip): TripUiModel {
        val entries = repository.getEntriesForTrip(trip.id)
        val photoCount = entries.count { it.type == EntryType.PHOTO }
        val noteCount = entries.count { it.type == EntryType.NOTE }
        val hasRoute = repository.hasRoute(trip.id)
        val progress = calculateTripProgress(trip, entries)
        val isFavorite = repository.isFavorite(trip.id)
        val lastExportedAt = trip.lastExportedAt?.toString()

        return TripUiModel(
            trip = trip,
            progress = progress,
            photoCount = photoCount,
            noteCount = noteCount,
            hasRoute = hasRoute,
            isFavorite = isFavorite,
            lastExportedAt = lastExportedAt
        )
    }

    fun refreshTrips() {
        observeTrips()
    }

    fun calculateTripProgress(trip: Trip, entries: List<EntryModel>): Float {
        val totalDays = trip.startDate.daysUntil(trip.endDate).coerceAtLeast(1)

        val daysWithEntries = entries
            .map { it.timestamp.date }
            .filter { it in trip.startDate..trip.endDate }
            .distinct()
            .count()

        return (daysWithEntries.toFloat() / totalDays).coerceIn(0f, 1f)
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(
        val trips: List<TripUiModel>,
        val recentlyEdited: List<TripUiModel>,
        val recentlyExported: List<TripUiModel>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}