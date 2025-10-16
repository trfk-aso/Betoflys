package org.betofly.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Place
import org.betofly.app.model.RoutePoint
import org.betofly.app.model.Trip
import org.betofly.app.repository.TripRepository

class TripDetailsViewModel(
    private val repository: TripRepository
) : ViewModel() {

    var selectedTripId: Long? = null
        private set

    private val _uiState = MutableStateFlow<TripDetailsUiState>(TripDetailsUiState.Loading)
    val uiState: StateFlow<TripDetailsUiState> = _uiState

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    fun setTripId(tripId: Long) {
        selectedTripId = tripId
        loadTrip(tripId)
    }

    private fun loadTrip(tripId: Long) {
        viewModelScope.launch {
            try {
                val trip = repository.getTripById(tripId)
                if (trip != null) {
                    val entries = repository.getEntriesForTrip(tripId)
                    val routePoints = repository.getRoutePointsForTrip(tripId)
                    _uiState.value = TripDetailsUiState.Success(trip, entries, routePoints)
                } else {
                    _uiState.value = TripDetailsUiState.Error("Trip not found")
                }
            } catch (e: Exception) {
                _uiState.value = TripDetailsUiState.Error("Failed to load trip")
            }
        }
    }

    fun loadPlaces(tripId: Long) {
        viewModelScope.launch {
            _places.value = repository.getPlacesForTrip(tripId)
        }
    }
}

sealed class TripDetailsUiState {
    object Loading : TripDetailsUiState()
    data class Success(
        val trip: Trip,
        val entries: List<EntryModel>,
        val routePoints: List<RoutePoint>
    ) : TripDetailsUiState()
    data class Error(val message: String) : TripDetailsUiState()
}

