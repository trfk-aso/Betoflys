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
import org.betofly.app.model.TripUiModel
import org.betofly.app.repository.EntryRepository
import org.betofly.app.repository.TripRepository
import org.betofly.app.repository.daysUntil

class FavoritesViewModel(
    private val tripRepository: TripRepository,
    private val entryRepository: EntryRepository,
    private val homeViewModel: HomeViewModel,
    private val tripDetailsViewModel: TripDetailsViewModel
) : ViewModel() {

    private val _selectedTab = MutableStateFlow("Trips")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _favoriteEntryIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteEntryIds: StateFlow<Set<Long>> = _favoriteEntryIds.asStateFlow()

    init {
        refreshFavorites()
    }

    fun onTabSelected(tab: String) {
        _selectedTab.value = tab
        refreshFavorites()
    }

    fun onTripSelected(tripId: Long) {
        tripDetailsViewModel.setTripId(tripId)
    }

    fun toggleTripFavorite(tripId: Long) {
        homeViewModel.toggleFavorite(tripId)
        refreshFavorites()
    }

    fun toggleEntryFavorite(entryId: Long) = viewModelScope.launch {
        val favorite = entryRepository.isFavorite(entryId)
        if (favorite) entryRepository.removeFavorite(entryId)
        else entryRepository.addFavorite(entryId)
        refreshFavorites()
    }

    fun refreshFavorites() = viewModelScope.launch {
        _uiState.value = FavoritesUiState.Loading
        try {
            val trips = tripRepository.getAllTrips().filter { tripRepository.isFavorite(it.id) }
            val entries = entryRepository.searchEntries(isFavorite = true)

            _favoriteEntryIds.value = entries.map { it.id }.toSet()

            if (trips.isEmpty() && entries.isEmpty()) {
                _uiState.value = FavoritesUiState.Empty
            } else {
                val tripsUi = trips.map { enrichTrip(it) }
                _uiState.value = FavoritesUiState.Success(
                    trips = tripsUi,
                    entries = entries
                )
            }
        } catch (e: Exception) {
            _uiState.value = FavoritesUiState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun enrichTrip(trip: Trip): TripUiModel {
        val entries = entryRepository.getEntriesForTrip(trip.id)
        val photoCount = entries.count { it.type == EntryType.PHOTO }
        val noteCount = entries.count { it.type == EntryType.NOTE }
        val hasRoute = tripRepository.hasRoute(trip.id)
        val progress = homeViewModel.calculateTripProgress(trip, entries)
        val isFavorite = tripRepository.isFavorite(trip.id)
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
}

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    object Empty : FavoritesUiState()
    data class Success(
        val trips: List<TripUiModel>,
        val entries: List<EntryModel>
    ) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}
