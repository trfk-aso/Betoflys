package org.betofly.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.favorite_blue
import betofly.composeapp.generated.resources.favorite_dark
import betofly.composeapp.generated.resources.favorite_gold
import betofly.composeapp.generated.resources.favorite_light
import betofly.composeapp.generated.resources.note_blue
import betofly.composeapp.generated.resources.note_dark
import betofly.composeapp.generated.resources.note_gold
import betofly.composeapp.generated.resources.note_light
import betofly.composeapp.generated.resources.photo_blue
import betofly.composeapp.generated.resources.photo_dark
import betofly.composeapp.generated.resources.photo_gold
import betofly.composeapp.generated.resources.photo_light
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayAt
import org.betofly.app.model.Trip
import org.betofly.app.model.TripUiModel
import org.betofly.app.viewModel.TripDetailsUiState
import org.betofly.app.viewModel.TripDetailsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun TripCard(
    tripUi: TripUiModel,
    currentThemeId: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onExport: (TripUiModel) -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val cardBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    val progressColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFF00FF7F)
        "theme_blue" -> Color(0xFF4DD0E1)
        "theme_gold" -> Color(0xFFFFD700)
        else -> Color(0xFF00FF7F)
    }
    val trackColor = cardBackgroundColor.copy(alpha = 0.4f)

    val tagBackgroundColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    val progress = tripUi.trip.progress

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            Column {
                TripImage(tripUi.trip.coverImageId)

                Column(modifier = Modifier.padding(12.dp)) {
                    Text(tripUi.trip.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(
                        "${tripUi.trip.startDate} – ${tripUi.trip.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )

                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = progressColor,
                        trackColor = trackColor
                    )

                    Spacer(Modifier.height(8.dp))

                    val filteredTags = tripUi.trip.tags.filter { it.isNotBlank() }
                    if (filteredTags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            filteredTags.forEach { tag ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(tag, color = Color.White) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = tagBackgroundColor)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (tripUi.photoCount > 0) {
                            Image(
                                painter = painterResource(
                                     when (currentThemeId) {
                                        "theme_light" -> Res.drawable.photo_light
                                        "theme_dark" -> Res.drawable.photo_dark
                                        "theme_blue" -> Res.drawable.photo_blue
                                        "theme_gold" -> Res.drawable.photo_gold
                                        else -> Res.drawable.photo_light
                                    }
                                ),
                                contentDescription = "Photos",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(tripUi.photoCount.toString(), color = Color.White)
                        }

                        if (tripUi.noteCount > 0) {
                            Image(
                                painter = painterResource(
                                     when (currentThemeId) {
                                        "theme_light" -> Res.drawable.note_light
                                        "theme_dark" -> Res.drawable.note_dark
                                        "theme_blue" -> Res.drawable.note_blue
                                        "theme_gold" -> Res.drawable.note_gold
                                        else -> Res.drawable.note_light
                                    }
                                ),
                                contentDescription = "Notes",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(tripUi.noteCount.toString(), color = Color.White)
                        }

                        if (tripUi.hasRoute) {
                            Icon(Icons.Default.Route, contentDescription = "Route", tint = Color.White)
                        }
                    }

                    tripUi.lastExportedAt?.let { exportedAt ->
                        Text(
                            text = "Exported: $exportedAt",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (tripUi.isFavorite) {
                Image(
                    painter = painterResource(
                         when (currentThemeId) {
                            "theme_light" -> Res.drawable.favorite_light
                            "theme_dark" -> Res.drawable.favorite_dark
                            "theme_blue" -> Res.drawable.favorite_blue
                            "theme_gold" -> Res.drawable.favorite_gold
                            else -> Res.drawable.favorite_light
                        }
                    ),
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(cardBackgroundColor, shape = RoundedCornerShape(8.dp))
        ) {
            listOf("Edit", "Export", "Favorite", "Delete").forEach { action ->
                DropdownMenuItem(
                    onClick = {
                        when (action) {
                            "Edit" -> onEdit()
                            "Export" -> scope.launch { onExport(tripUi) }
                            "Favorite" -> onFavorite()
                            "Delete" -> onDelete()
                        }
                        expanded = false
                    },
                    text = {
                        Box(
                            modifier = Modifier
                                .background(buttonBackgroundColor(currentThemeId, action), shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(action, color = Color.White)
                        }
                    }
                )
            }
        }
    }
}

fun buttonBackgroundColor(themeId: String, action: String): Color {
    return when (themeId) {
        "theme_light", "theme_dark" -> when (action) {
            "Edit" -> Color(0xFF3FBB27)
            "Export" -> Color(0xFFA9BD12)
            "Favorite" -> Color(0xFF3C6B1E)
            "Delete" -> Color(0xFFBD1212)
            else -> Color.Gray
        }
        "theme_blue" -> when (action) {
            "Edit" -> Color(0xFF2BA7FF)
            "Export" -> Color(0xFFF3A30F)
            "Favorite" -> Color(0xFF0D1B38)
            "Delete" -> Color(0xFFBD1212)
            else -> Color.Gray
        }
        "theme_gold" -> when (action) {
            "Edit" -> Color(0xFFFC8600)
            "Export" -> Color(0xFFF3A30F)
            "Favorite" -> Color(0xFF2F1C11)
            "Delete" -> Color(0xFFBD1212)
            else -> Color.Gray
        }
        else -> Color.Gray
    }
}

fun LocalDate.daysUntil(other: LocalDate): Int =
    (other.toEpochDays() - this.toEpochDays()).toInt()
