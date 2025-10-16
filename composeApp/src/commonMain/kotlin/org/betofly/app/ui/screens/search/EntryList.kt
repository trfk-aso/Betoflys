package org.betofly.app.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.Trip
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.favorites.EntryCard
import org.betofly.app.viewModel.FavoritesViewModel
import org.koin.compose.koinInject

@Composable
fun EntryList(
    entries: List<EntryModel>,
    trips: List<Trip>,
    onClick: (Long) -> Unit,
    onSortChange: (SortOption) -> Unit,
    currentSort: SortOption,
    onFavoriteToggle: (EntryModel) -> Unit = {},
    viewModel: FavoritesViewModel = koinInject(),
    themeRepository: ThemeRepository
) {
    val currentThemeId by themeRepository.currentThemeId.collectAsState()

    val chipBackgroundColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFF3FBB27)
        "theme_blue" -> Color(0xFF2BA7FF)
        "theme_gold" -> Color(0xFFFC8600)
        else -> Color(0xFF3FBB27)
    }

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            SortOption.values().forEach { option ->
                FilterChip(
                    selected = currentSort == option,
                    onClick = { onSortChange(option) },
                    label = {
                        Text(
                            text = option.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = chipBackgroundColor,
                        labelColor = Color.White,
                        selectedContainerColor = chipBackgroundColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                val tripTitle = trips.find { it.id == entry.tripId }?.title ?: "Unknown Trip"

                EntryCard(
                    entry = entry,
                    tripTitle = tripTitle,
                    currentThemeId = currentThemeId ?: "theme_light",
                    isFavorite = viewModel.favoriteEntryIds.collectAsState().value.contains(entry.id),
                    onClick = { onClick(entry.tripId) },
                    onFavoriteToggle = { onFavoriteToggle(entry) }
                )
            }
        }
    }
}

enum class SortOption(val label: String) {
    DATE("By Date"),
    TITLE("By Title"),
    MOST_MEDIA("Most Media"),
    PROGRESS("Progress")
}
