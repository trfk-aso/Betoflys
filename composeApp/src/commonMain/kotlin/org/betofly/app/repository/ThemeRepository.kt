package org.betofly.app.repository

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import org.betofly.app.data.Betofly
import org.betofly.app.model.Theme
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ThemeRepository {
    suspend fun initializeThemes()
    suspend fun getAllThemes(): List<Theme>
    suspend fun setCurrentTheme(themeId: String)
    suspend fun getCurrentThemeId(): String?
    suspend fun markThemePurchased(themeId: String)

    val currentThemeId: StateFlow<String?>
}

class ThemeRepositoryImpl(
    private val db: Betofly
) : ThemeRepository {

    private val queries = db.betoflyQueries

    private val _currentThemeId = MutableStateFlow<String?>(null)
    override val currentThemeId: StateFlow<String?> = _currentThemeId.asStateFlow()

    override suspend fun initializeThemes() {
        val existing = queries.getThemes().executeAsList()
        if (existing.isEmpty()) {
            queries.insertTheme(
                id = "theme_light",
                name = "Light",
                isPurchased = 1,
                type = "free",
                previewRes = "bg_light",
                primaryColor = 0xFFFFFFFF,
                splashText = "Shine bright!"
            )
            queries.insertTheme(
                id = "theme_dark",
                name = "Dark",
                isPurchased = 1,
                type = "free",
                previewRes = "bg_dark",
                primaryColor = 0xFF000000,
                splashText = "Stay focused!"
            )
            queries.insertTheme(
                id = "theme_blue",
                name = "Royal Blue",
                isPurchased = 0,
                type = "paid",
                previewRes = "bg_royal_blue",
                primaryColor = 0xFF4285F5,
                splashText = "Stay sharp!"
            )
            queries.insertTheme(
                id = "theme_gold",
                name = "Graphite Gold",
                isPurchased = 0,
                type = "paid",
                previewRes = "bg_graphite_gold",
                primaryColor = 0xFFFFD700,
                splashText = "Shine on!"
            )
        }

        if (_currentThemeId.value == null) {
            _currentThemeId.value = queries.getCurrentThemeId().executeAsOneOrNull()
        }
    }

    override suspend fun getAllThemes(): List<Theme> {
        return queries.getThemes().executeAsList().map {
            Theme(
                id = it.id,
                name = it.name,
                isPurchased = it.isPurchased == 1L,
                type = it.type!!,
                previewRes = it.previewRes ?: "",
                primaryColor = it.primaryColor ?: 0L,
                splashText = it.splashText ?: ""
            )
        }
    }

    override suspend fun setCurrentTheme(themeId: String) {
        queries.insertCurrentTheme(themeId)
        _currentThemeId.value = themeId
    }

    override suspend fun getCurrentThemeId(): String? {
        return _currentThemeId.value
    }

    override suspend fun markThemePurchased(themeId: String) {
        queries.purchaseTheme(themeId)
    }
}

