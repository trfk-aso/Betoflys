package org.betofly.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.btn_create_trip_blue
import betofly.composeapp.generated.resources.btn_create_trip_gold
import betofly.composeapp.generated.resources.btn_create_trip_light
import betofly.composeapp.generated.resources.btn_favorites_blue
import betofly.composeapp.generated.resources.btn_favorites_gold
import betofly.composeapp.generated.resources.btn_favorites_light
import betofly.composeapp.generated.resources.btn_journal_blue
import betofly.composeapp.generated.resources.btn_journal_gold
import betofly.composeapp.generated.resources.btn_journal_light
import betofly.composeapp.generated.resources.btn_stats_blue
import betofly.composeapp.generated.resources.btn_stats_gold
import betofly.composeapp.generated.resources.btn_stats_light
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeNavigationRow(
    currentThemeId: String,
    onCreateTrip: () -> Unit,
    onJournal: () -> Unit,
    onFavorites: () -> Unit,
    onStatistics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickAccessCard(
            icon = getNavigationButtonImage(currentThemeId, "create_trip"),
            backgroundColor = tabBackgroundColor,
            onClick = onCreateTrip
        )
        QuickAccessCard(
            icon = getNavigationButtonImage(currentThemeId, "journal"),
            backgroundColor = tabBackgroundColor,
            onClick = onJournal
        )
        QuickAccessCard(
            icon = getNavigationButtonImage(currentThemeId, "favorites"),
            backgroundColor = tabBackgroundColor,
            onClick = onFavorites
        )
        QuickAccessCard(
            icon = getNavigationButtonImage(currentThemeId, "stats"),
            backgroundColor = tabBackgroundColor,
            onClick = onStatistics
        )
    }
}

@Composable
fun QuickAccessCard(
    icon: DrawableResource,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(60.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
fun getNavigationButtonImage(themeId: String, buttonType: String): DrawableResource {
    return when (themeId) {
        "theme_light", "theme_dark" -> when (buttonType) {
            "create_trip" -> Res.drawable.btn_create_trip_light
            "journal" -> Res.drawable.btn_journal_light
            "favorites" -> Res.drawable.btn_favorites_light
            "stats" -> Res.drawable.btn_stats_light
            else -> Res.drawable.btn_create_trip_light

        }
        "theme_blue" -> when (buttonType) {
            "create_trip" -> Res.drawable.btn_create_trip_blue
            "journal" -> Res.drawable.btn_journal_blue
            "favorites" -> Res.drawable.btn_favorites_blue
            "stats" -> Res.drawable.btn_stats_blue
            else -> Res.drawable.btn_create_trip_blue
        }
        "theme_gold" -> when (buttonType) {
            "create_trip" -> Res.drawable.btn_create_trip_gold
            "journal" -> Res.drawable.btn_journal_gold
            "favorites" -> Res.drawable.btn_favorites_gold
            "stats" -> Res.drawable.btn_stats_gold
            else -> Res.drawable.btn_create_trip_gold
        }
        else -> Res.drawable.btn_create_trip_light
    }
}
