package org.betofly.app.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.model.Trip
import org.betofly.app.model.TripUiModel
import org.betofly.app.ui.screens.home.TripCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripGrid(
    trips: List<TripUiModel>,
    currentThemeId: String,
    onClick: (TripUiModel) -> Unit,
    onEdit: (TripUiModel) -> Unit = {},
    onExport: (TripUiModel) -> Unit = {},
    onFavorite: (TripUiModel) -> Unit = {},
    onDelete: (TripUiModel) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(trips) { tripUi ->
            // создаем копию TripUiModel с обрезанными тегами
            val trimmedTrip = tripUi.copy(
                trip = tripUi.trip.copy(
                    tags = tripUi.trip.tags.take(2) // максимум 2 тега
                )
            )

            TripCard(
                tripUi = trimmedTrip,
                currentThemeId = currentThemeId,
                onClick = { onClick(tripUi) },
                onEdit = { onEdit(tripUi) },
                onExport = { onExport(tripUi) },
                onFavorite = { onFavorite(tripUi) },
                onDelete = { onDelete(tripUi) }
            )
        }
    }
}