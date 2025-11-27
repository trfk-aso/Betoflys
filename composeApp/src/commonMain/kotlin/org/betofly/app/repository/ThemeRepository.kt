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
        println("initializeThemes() called")
        val existing = queries.getThemes().executeAsList()
        println("Existing themes: ${existing.map { it.id }}")

        if (existing.isEmpty()) {
            println("Inserting default themes")
            queries.insertTheme(
                id = "theme_light",
                name = "Light",
                isPurchased = 1,
                type = "free",
                previewRes = "bg_light",
                primaryColor = 0xFFFFFFFF,
                splashText = "Shine bright!",
                price = null
            )
            queries.insertTheme(
                id = "theme_dark",
                name = "Dark",
                isPurchased = 1,
                type = "free",
                previewRes = "bg_dark",
                primaryColor = 0xFF000000,
                splashText = "Stay focused!",
                price = null
            )
            queries.insertTheme(
                id = "theme_blue",
                name = "Royal Blue",
                isPurchased = 0,
                type = "paid",
                previewRes = "bg_royal_blue",
                primaryColor = 0xFF4285F5,
                splashText = "Stay sharp!",
                price = 1.99
            )
            queries.insertTheme(
                id = "theme_gold",
                name = "Graphite Gold",
                isPurchased = 0,
                type = "paid",
                previewRes = "bg_graphite_gold",
                primaryColor = 0xFFFFD700,
                splashText = "Shine on!",
                price = 1.99
            )
        }

        var current = queries.getCurrentThemeId().executeAsOneOrNull()
        println("Current theme from DB before check: $current")

        if (current == null) {
            current = "theme_light"
            queries.insertCurrentTheme(current)
            println("No current theme found, setting default: $current")
        } else {
            println("Found current theme in DB: $current")
        }

        _currentThemeId.value = current
        println("Current theme in StateFlow: ${_currentThemeId.value}")
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
        println("setCurrentTheme() called with themeId: $themeId")
        queries.insertCurrentTheme(themeId)
        _currentThemeId.value = themeId
        println("Current theme updated in StateFlow: ${_currentThemeId.value}")
    }

    override suspend fun getCurrentThemeId(): String? {
        if (_currentThemeId.value == null) {
            _currentThemeId.value = queries.getCurrentThemeId().executeAsOneOrNull()
                ?: "theme_light"
            println("getCurrentThemeId() fetched from DB: ${_currentThemeId.value}")
        } else {
            println("getCurrentThemeId() from StateFlow: ${_currentThemeId.value}")
        }
        return _currentThemeId.value
    }

    override suspend fun markThemePurchased(themeId: String) {
        queries.purchaseTheme(themeId)
    }
}

