package org.betofly.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.btn_favorites_blue
import betofly.composeapp.generated.resources.btn_favorites_gold
import betofly.composeapp.generated.resources.btn_favorites_light
import betofly.composeapp.generated.resources.btn_home_blue
import betofly.composeapp.generated.resources.btn_home_gold
import betofly.composeapp.generated.resources.btn_home_light
import betofly.composeapp.generated.resources.btn_journal_blue
import betofly.composeapp.generated.resources.btn_journal_gold
import betofly.composeapp.generated.resources.btn_journal_light
import betofly.composeapp.generated.resources.btn_search_blue
import betofly.composeapp.generated.resources.btn_search_gold
import betofly.composeapp.generated.resources.btn_search_light
import betofly.composeapp.generated.resources.btn_stats_blue
import betofly.composeapp.generated.resources.btn_stats_gold
import betofly.composeapp.generated.resources.btn_stats_light
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun QuickAccessRow(
    currentThemeId: String,
    onNewTrip: () -> Unit,
    onJournal: () -> Unit,
    onSearch: () -> Unit,
    onFavorites: () -> Unit,
    onStatistics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF00110D)
        "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickAccessButton(getButtonImage(currentThemeId, "home"), onNewTrip)
            QuickAccessButton(getButtonImage(currentThemeId, "journal"), onJournal)
            QuickAccessButton(getButtonImage(currentThemeId, "search"), onSearch)
            QuickAccessButton(getButtonImage(currentThemeId, "favorites"), onFavorites)
            QuickAccessButton(getButtonImage(currentThemeId, "stats"), onStatistics)
        }
    }
}

@Composable
fun QuickAccessButton(iconRes: DrawableResource, onClick: () -> Unit) {
    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
fun getButtonImage(themeId: String, buttonType: String): DrawableResource {
    return when (themeId) {
        "theme_light", "theme_dark" -> when (buttonType) {
            "home" -> Res.drawable.btn_home_light
            "journal" -> Res.drawable.btn_journal_light
            "search" -> Res.drawable.btn_search_light
            "favorites" -> Res.drawable.btn_favorites_light
            "stats" -> Res.drawable.btn_stats_light
            else -> Res.drawable.btn_home_light
        }
        "theme_blue" -> when (buttonType) {
            "home" -> Res.drawable.btn_home_blue
            "journal" -> Res.drawable.btn_journal_blue
            "search" -> Res.drawable.btn_search_blue
            "favorites" -> Res.drawable.btn_favorites_blue
            "stats" -> Res.drawable.btn_stats_blue
            else -> Res.drawable.btn_home_blue
        }
        "theme_gold" -> when (buttonType) {
            "home" -> Res.drawable.btn_home_gold
            "journal" -> Res.drawable.btn_journal_gold
            "search" -> Res.drawable.btn_search_gold
            "favorites" -> Res.drawable.btn_favorites_gold
            "stats" -> Res.drawable.btn_stats_gold
            else -> Res.drawable.btn_home_gold
        }
        else -> Res.drawable.btn_home_light
    }
}