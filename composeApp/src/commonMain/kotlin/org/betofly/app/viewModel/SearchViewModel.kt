package org.betofly.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.Trip
import org.betofly.app.model.TripCategory
import org.betofly.app.repository.SearchRepository
import org.betofly.app.ui.screens.search.SortOption

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    data class SuccessTrips(val trips: List<Trip>) : SearchUiState()
    data class SuccessEntries(val entries: List<EntryModel>, val trips: List<Trip>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

data class SearchFilters(
    val query: String? = null,
    val type: SearchType = SearchType.TRIPS,
    val entryType: EntryType? = null,
    val category: TripCategory? = null,
    val dateRange: ClosedRange<LocalDate>? = null,
    val hasRoute: Boolean? = null,
    val hasMedia: Boolean? = null,
    val isFavorite: Boolean? = null,
    val sortOption: SortOption = SortOption.DATE
)

enum class SearchType { TRIPS, ENTRIES }

class SearchViewModel(
    private val repo: SearchRepository,
    private val tripDetailsViewModel: TripDetailsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters

    private val _recent = MutableStateFlow<List<String>>(emptyList())
    val recent: StateFlow<List<String>> = _recent

    init {
        viewModelScope.launch {
            _recent.value = repo.getRecentQueries()
        }
    }

    fun updateFilters(newFilters: SearchFilters) {
        _filters.value = newFilters
        search()
    }

    fun resetFilters() {
        _filters.value = SearchFilters()
        _uiState.value = SearchUiState.Idle
    }

    fun applyFilters() {
        search()
    }

    fun onTripSelected(tripId: Long) {
        tripDetailsViewModel.setTripId(tripId)
    }

    fun applySort(sortOption: SortOption) {
        _filters.value = _filters.value.copy(sortOption = sortOption)
        val currentState = _uiState.value
        if (currentState is SearchUiState.SuccessEntries) {
            val sortedEntries = when (sortOption) {
                SortOption.DATE -> currentState.entries.sortedBy { it.timestamp }
                SortOption.TITLE -> currentState.entries.sortedBy { it.title }
                SortOption.MOST_MEDIA -> currentState.entries.sortedByDescending { it.mediaIds.size }
                SortOption.PROGRESS -> currentState.entries.sortedBy { entry ->
                    val trip = currentState.trips.find { it.id == entry.tripId }
                    trip?.progress ?: 0f
                }
            }
            _uiState.value = currentState.copy(entries = sortedEntries)
        }
    }

    fun search() {
        val f = _filters.value

        if (f.query.isNullOrBlank() &&
            f.entryType == null &&
            f.category == null &&
            f.dateRange == null &&
            f.hasRoute == null &&
            f.hasMedia == null &&
            f.isFavorite == null
        ) {
            _uiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                f.query?.let {
                    repo.saveQuery(it)
                    _recent.value = repo.getRecentQueries()
                }

                if (f.type == SearchType.TRIPS) {
                    val trips = repo.searchTrips(
                        query = f.query,
                        category = f.category,
                        hasRoute = f.hasRoute,
                        hasMedia = f.hasMedia,
                        isFavorite = f.isFavorite,
                        dateRange = f.dateRange
                    )
                    _uiState.value =
                        if (trips.isEmpty()) SearchUiState.Empty else SearchUiState.SuccessTrips(trips)
                } else {
                    val entries = repo.searchEntries(
                        query = f.query,
                        type = f.entryType,
                        tripCategory = f.category,
                        hasMedia = f.hasMedia,
                        isFavorite = f.isFavorite,
                        dateRange = f.dateRange
                    )
                    val trips = repo.searchTrips(
                        query = f.query,
                        category = f.category,
                        hasRoute = f.hasRoute,
                        hasMedia = f.hasMedia,
                        isFavorite = f.isFavorite,
                        dateRange = f.dateRange
                    )
                    _uiState.value =
                        if (entries.isEmpty()) SearchUiState.Empty
                        else SearchUiState.SuccessEntries(entries, trips)
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
