package org.betofly.app.viewModel

import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.style.LineBreak.Companion.Paragraph
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import export.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.TripCategory
import org.betofly.app.repository.EntryRepository
import org.betofly.app.repository.TripRepository

class JournalViewModel(
    private val entryRepository: EntryRepository,
    private val tripRepository: TripRepository,
    private val tripDetailsViewModel: TripDetailsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<JournalUiState>(JournalUiState.Loading)
    val uiState: StateFlow<JournalUiState> = _uiState

    private val _editingEntry = MutableStateFlow<EntryModel?>(null)
    val editingEntry: StateFlow<EntryModel?> = _editingEntry

    private val _filter = MutableStateFlow<JournalFilter?>(null)
    val filter: StateFlow<JournalFilter?> = _filter

    init { loadEntries() }

    fun setFilter(filter: JournalFilter?) {
        _filter.value = filter
        loadEntries()
    }

    fun loadEntries() = viewModelScope.launch {
        _uiState.value = JournalUiState.Loading
        try {
            val f = _filter.value
            val entries = entryRepository.searchEntries(
                query = f?.query,
                type = f?.type,
                tripCategory = f?.tripCategory,
                hasMedia = f?.hasMedia,
                isFavorite = f?.isFavorite,
                startDate = f?.startDate,
                endDate = f?.endDate
            ).sortedByDescending { it.timestamp }

            if (entries.isEmpty()) {
                _uiState.value = JournalUiState.Empty
            } else {
                _uiState.value = JournalUiState.Success(entries.groupBy { it.timestamp.date })
            }
        } catch (e: Exception) {
            _uiState.value = JournalUiState.Error(e.message ?: "Unknown error")
        }
    }

    fun loadTripsOnly() = viewModelScope.launch {
        _uiState.value = JournalUiState.Loading
        try {
            val trips = tripRepository.getAllTrips()

            if (trips.isEmpty()) {
                _uiState.value = JournalUiState.Empty
            } else {
                val tripEntries = trips.map { trip ->
                    EntryModel(
                        id = trip.id,
                        tripId = trip.id,
                        type = EntryType.TRIP,
                        title = trip.title,
                        text = trip.description,
                        timestamp = trip.createdAt,
                        tags = trip.tags,
                        mediaIds = emptyList(),
                        coords = null,
                        createdAt = trip.createdAt,
                        updatedAt = trip.updatedAt
                    )
                }

                _uiState.value = JournalUiState.Success(tripEntries.groupBy { it.timestamp.date })
            }
        } catch (e: Exception) {
            _uiState.value = JournalUiState.Error(e.message ?: "Failed to load trips")
        }
    }


    fun deleteEntry(entryId: Long) = viewModelScope.launch {
        try {
            entryRepository.deleteEntry(entryId)
            loadEntries()
        } catch (e: Exception) {
            println("Error deleting entry: ${e.message}")
        }
    }

    fun startEditingEntry(entry: EntryModel) {
        _editingEntry.value = entry
    }

    fun saveEditedEntry(updatedEntry: EntryModel) = viewModelScope.launch {
        entryRepository.updateEntry(updatedEntry)
        _editingEntry.value = null
        loadEntries()
    }


    fun exportDay(date: LocalDate) = viewModelScope.launch(Dispatchers.Default) {
        val entries = entryRepository.searchEntries(
            startDate = date,
            endDate = date
        )

        val pdfBytes = PdfExporter.exportDay(date, entries)

        println("PDF generated, size: ${pdfBytes.size} bytes")
    }

}

sealed class JournalUiState {
    object Loading : JournalUiState()
    object Empty : JournalUiState()
    data class Success(val entriesByDate: Map<LocalDate, List<EntryModel>>) : JournalUiState()
    data class Error(val message: String) : JournalUiState()
}

data class JournalFilter(
    val query: String? = null,
    val type: EntryType? = null,
    val tripCategory: TripCategory? = null,
    val hasMedia: Boolean? = null,
    val isFavorite: Boolean? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val searchQuery: String? = null
)