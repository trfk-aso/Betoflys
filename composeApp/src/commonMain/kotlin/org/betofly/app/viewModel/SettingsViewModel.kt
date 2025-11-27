package org.betofly.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import backup.BackupManager
import backup.BackupStorage
import backup.DataImporter
import export.PdfSharer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.betofly.app.model.EntryModel
import org.betofly.app.data.Trip as DataTrip
import org.betofly.app.model.Trip as ModelTrip
import org.betofly.app.model.Theme
import org.betofly.app.model.TripCategory
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.EntryRepository
import org.betofly.app.repository.PurchaseResult
import org.betofly.app.repository.SettingsRepository
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.repository.TripRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val tripRepository: TripRepository,
    private val entryRepository: EntryRepository,
    private val backupStorage: BackupStorage,
    private val themeRepository: ThemeRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.initializeThemes()
            refreshThemes()
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(currentThemeId = theme.id) }
            themeRepository.setCurrentTheme(theme.id)
        }
    }

    fun selectTheme(theme: Theme) {
        viewModelScope.launch {
            if (theme.type == "paid" && !theme.isPurchased) {
                val result = billingRepository.purchaseTheme(theme.id)
                when (result) {
                    is PurchaseResult.Success -> {
                        refreshThemes()
                    }
                    is PurchaseResult.Error -> {
                        println("Purchase error: ${result.message}")
                    }
                    else -> Unit
                }
            } else {
                themeRepository.setCurrentTheme(theme.id)
                _uiState.update { it.copy(currentThemeId = theme.id) }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            val result = billingRepository.restorePurchases()
            if (result is PurchaseResult.Success) {
                refreshThemes()
            }
        }
    }

    private suspend fun refreshThemes() {
        val all = themeRepository.getAllThemes()
        val current = themeRepository.getCurrentThemeId()
        _uiState.update { it.copy(themes = all, currentThemeId = current) }
    }

    fun exportAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isExporting = true) }
            println("exportAll: started")
            try {
                val trips = tripRepository.getAllTrips()
                println("exportAll: fetched ${trips.size} trips")

                val allEntries = trips.flatMap { entryRepository.getEntriesForTrip(it.id) }
                println("exportAll: fetched ${allEntries.size} entries")

                val pdfBytes = BackupManager.exportAll(trips, allEntries)
                println("exportAll: PDF created, size=${pdfBytes.size} bytes")

                backupStorage.saveBackup(pdfBytes)
                println("exportAll: backup saved successfully")

                withContext(Dispatchers.Main) {
                    PdfSharer.share(pdfBytes, "full_export.pdf")
                }

            } catch (e: Exception) {
                println("exportAll: failed with exception: ${e.message}")
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isExporting = false) }
                println("exportAll: finished")
            }
        }
    }

    fun importZip() {
        val importer = DataImporter()

        _uiState.update { it.copy(isImporting = true) }

        importer.openImportDialog { dataTrips, entries ->
            onImportedData(
                trips = dataTrips.map { it.toModel() },
                entries = entries
            )
        }
    }

    private fun onImportedData(
        trips: List<ModelTrip>,
        entries: List<EntryModel>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                trips.forEach { tripRepository.insertTrip(it) }
                entries.forEach { entryRepository.insertEntry(it) }

                println("IMPORT: added ${trips.size} trips and ${entries.size} entries")

            } catch (e: Exception) {
                println("IMPORT FAILED: ${e.message}")
            } finally {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    fun showResetConfirm() {
        _uiState.update { it.copy(showResetConfirmDialog = true) }
    }

    fun dismissResetConfirm() {
        _uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    fun resetAll() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trips = tripRepository.getAllTrips()
                trips.forEach { tripRepository.deleteTrip(it.id) }
                trips.forEach { trip ->
                    entryRepository.getEntriesForTrip(trip.id).forEach { entryRepository.deleteEntry(it.id) }
                }
                settingsRepository.setFirstLaunch(true)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(showResetConfirmDialog = false) }
            }
        }
    }
}
enum class AppTheme { LIGHT, DARK, SYSTEM, BLUE_PAID, ORANGE_PAID }

data class SettingsUiState(
    val themes: List<Theme> = emptyList(),
    val currentThemeId: String? = null,
    val showResetConfirmDialog: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false
)

fun DataTrip.toModel(): ModelTrip {
    return ModelTrip(
        id = id,
        title = title,

        startDate = LocalDate.parse(start_date),
        endDate = LocalDate.parse(end_date),

        category = TripCategory.valueOf(category),

        coverImageId = cover_image_id,
        description = description,

        tags = tags?.split(",")?.map { it.trim() }.orEmpty(),

        createdAt = LocalDateTime.parse(created_at),
        updatedAt = LocalDateTime.parse(updated_at),

        lastExportedAt = last_exported_at?.let { LocalDateTime.parse(it) },

        progress = (progress ?: 0.0).toFloat(),

        duration = duration ?: 0L
    )
}


//class SettingsViewModel(
//    private val settingsRepository: SettingsRepository,
//    private val tripRepository: TripRepository,
//    private val entryRepository: EntryRepository,
//    private val backupStorage: BackupStorage,
//    private val themeRepository: ThemeRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(SettingsUiState())
//    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            themeRepository.initializeThemes()
//            refreshThemes()
//        }
//    }
//
//    fun setTheme(theme: Theme) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _uiState.update { it.copy(currentThemeId = theme.id) }
//            themeRepository.setCurrentTheme(theme.id)
//        }
//    }
//
//    fun selectTheme(theme: Theme) {
//        viewModelScope.launch {
//            themeRepository.setCurrentTheme(theme.id)
//            _uiState.update { it.copy(currentThemeId = theme.id) }
//        }
//    }
//
//    private suspend fun refreshThemes() {
//        val all = themeRepository.getAllThemes().map { theme ->
//            if (theme.type == "paid") theme.copy(type = "free", isPurchased = true)
//            else theme
//        }
//        val current = themeRepository.getCurrentThemeId()
//        _uiState.update { it.copy(themes = all, currentThemeId = current) }
//    }
//
//
//    fun exportAll() {
//        viewModelScope.launch(Dispatchers.IO) {
//            _uiState.update { it.copy(isExporting = true) }
//            try {
//                val trips = tripRepository.getAllTrips()
//                val allEntries = trips.flatMap { entryRepository.getEntriesForTrip(it.id) }
//                val backupBytes = BackupManager.exportAll(trips, allEntries)
//                backupStorage.saveBackup(backupBytes)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _uiState.update { it.copy(isExporting = false) }
//            }
//        }
//    }
//
//    fun importZip() {
//        viewModelScope.launch(Dispatchers.IO) {
//            _uiState.update { it.copy(isImporting = true) }
//            try {
//                val backupBytes = backupStorage.loadBackup() ?: return@launch
//                val (trips, entries) = BackupManager.importAll(backupBytes)
//                trips.forEach { tripRepository.insertTrip(it) }
//                entries.forEach { entryRepository.insertEntry(it) }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _uiState.update { it.copy(isImporting = false) }
//            }
//        }
//    }
//
//    fun showResetConfirm() {
//        _uiState.update { it.copy(showResetConfirmDialog = true) }
//    }
//
//    fun dismissResetConfirm() {
//        _uiState.update { it.copy(showResetConfirmDialog = false) }
//    }
//
//    fun resetAll() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val trips = tripRepository.getAllTrips()
//                trips.forEach { tripRepository.deleteTrip(it.id) }
//                trips.forEach { trip ->
//                    entryRepository.getEntriesForTrip(trip.id).forEach { entryRepository.deleteEntry(it.id) }
//                }
//                settingsRepository.setFirstLaunch(true)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _uiState.update { it.copy(showResetConfirmDialog = false) }
//            }
//        }
//    }
//}
