package org.betofly.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import backup.BackupManager
import backup.BackupStorage
import io.ktor.http.ContentType.Application.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Theme
import org.betofly.app.model.Trip
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
                // запускаем покупку
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
                // просто меняем
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

                val backupBytes = BackupManager.exportAll(trips, allEntries)
                println("exportAll: backup created, size=${backupBytes.size} bytes")

                backupStorage.saveBackup(backupBytes)
                println("exportAll: backup saved successfully")

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
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isImporting = true) }
            println("importZip: started")
            try {
                val backupBytes = backupStorage.loadBackup()
                if (backupBytes == null) {
                    println("importZip: no backup found")
                    return@launch
                }
                println("importZip: loaded backup, size=${backupBytes.size} bytes")

                val (trips, entries) = BackupManager.importAll(backupBytes)
                println("importZip: parsed backup: ${trips.size} trips, ${entries.size} entries")

                trips.forEach { tripRepository.insertTrip(it) }
                entries.forEach { entryRepository.insertEntry(it) }
                println("importZip: inserted trips and entries")

            } catch (e: Exception) {
                println("importZip: failed with exception: ${e.message}")
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isImporting = false) }
                println("importZip: finished")
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


//class SettingsViewModel(
//    private val settingsRepository: SettingsRepository,
//    private val tripRepository: TripRepository,
//    private val entryRepository: EntryRepository,
//    private val backupStorage: BackupStorage,
//    private val themeRepository: ThemeRepository
//) : ViewModel() { // убрали BillingRepository
//
//    private val _uiState = MutableStateFlow(SettingsUiState())
//    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            themeRepository.initializeThemes() // делаем все темы бесплатными
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
//            // полностью убираем логику покупки
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
