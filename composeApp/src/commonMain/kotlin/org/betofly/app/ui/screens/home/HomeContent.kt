package org.betofly.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.betofly.app.model.Trip
import org.betofly.app.model.TripCategory
import org.betofly.app.model.TripUiModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.betofly.app.ui.screens.Screen

@Composable
fun HomeContent(
    navController: NavHostController,
    trips: List<TripUiModel>,
    recentlyEdited: List<TripUiModel>,
    recentlyExported: List<TripUiModel>,
    selectedCategory: TripCategory?,
    onCategorySelected: (TripCategory?) -> Unit,
    onTripClick: (Long) -> Unit,
    onEdit: (TripUiModel) -> Unit,
    onExport: (TripUiModel) -> Unit,
    onFavorite: (TripUiModel) -> Unit,
    onDelete: (TripUiModel) -> Unit,
    currentThemeId: String
) {
    val backgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF00110D)
        "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    val tabBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    val categories = TripCategory.values()

    val filteredTrips = selectedCategory?.let { category ->
        trips.filter { it.trip.category == category }
    } ?: trips

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (filteredTrips.isNotEmpty()) {
            item {
                Text(
                    text = "My Trips",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(filteredTrips) { tripUi ->
                TripCard(
                    tripUi = tripUi,
                    currentThemeId = currentThemeId,
                    onClick = { onTripClick(tripUi.trip.id) },
                    onEdit = { onEdit(tripUi) },
                    onExport = { onExport(tripUi) },
                    onFavorite = { onFavorite(tripUi) },
                    onDelete = { onDelete(tripUi) }
                )
            }
        }

        item {
            Text(
                text = "Quick Access",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        item {
            HomeNavigationRow(
                currentThemeId = currentThemeId,
                onCreateTrip = { navController.navigate(Screen.CreateTrip.route) },
                onJournal = { navController.navigate(Screen.Journal.route) },
                onFavorites = { navController.navigate(Screen.Favorites.route) },
                onStatistics = { navController.navigate(Screen.Statistics.route) },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Trip Categories",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
        }
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Color.White.copy(alpha = 0.2f) else tabBackgroundColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = category.name.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (recentlyEdited.isNotEmpty()) {
            item {
                Text(
                    text = "Recently Edited",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(recentlyEdited) { tripUi ->
                TripCard(
                    tripUi = tripUi,
                    currentThemeId = currentThemeId,
                    onClick = { onTripClick(tripUi.trip.id) },
                    onEdit = { onEdit(tripUi) },
                    onExport = { onExport(tripUi) },
                    onFavorite = { onFavorite(tripUi) },
                    onDelete = { onDelete(tripUi) }
                )
            }
        }

        if (recentlyExported.isNotEmpty()) {
            item {
                Text(
                    text = "Recently Exported",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(recentlyExported) { tripUi ->
                TripCard(
                    tripUi = tripUi,
                    currentThemeId = currentThemeId,
                    onClick = { onTripClick(tripUi.trip.id) },
                    onEdit = { onEdit(tripUi) },
                    onExport = { onExport(tripUi) },
                    onFavorite = { onFavorite(tripUi) },
                    onDelete = { onDelete(tripUi) }
                )
            }
        }
    }
}