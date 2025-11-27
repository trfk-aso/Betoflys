package org.betofly.app.ui.screens.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.favorite_blue
import betofly.composeapp.generated.resources.favorite_dark
import betofly.composeapp.generated.resources.favorite_gold
import betofly.composeapp.generated.resources.favorite_light
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.ui.screens.home.TripImage
import org.betofly.app.ui.screens.home.buttonBackgroundColor
import org.jetbrains.compose.resources.painterResource

@Composable
fun EntryCard(
    entry: EntryModel,
    tripTitle: String,
    isFavorite: Boolean,
    currentThemeId: String,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    val cardBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

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
            Column(modifier = Modifier.padding(12.dp)) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.type == EntryType.PHOTO && entry.mediaIds.isNotEmpty()) {
                        val mediaId = entry.mediaIds.first()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            TripImage(
                                mediaId,
                            )
                        }
                    } else {
                        val icon = when (entry.type) {
                            EntryType.NOTE -> Icons.Default.Description
                            EntryType.PLACE -> Icons.Default.Place
                            EntryType.ROUTE_POINT -> Icons.Default.Tour
                            else -> Icons.Default.Photo
                        }
                        Icon(
                            icon,
                            contentDescription = entry.type.name,
                            tint = Color.White,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                entry.title?.let {
                    Text(it, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                entry.text?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.LightGray
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(tripTitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                Spacer(Modifier.height(8.dp))

            }

            if (isFavorite) {
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
                        .clickable { onFavoriteToggle() }
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(cardBackgroundColor, shape = RoundedCornerShape(8.dp))
        ) {
            val actions = mutableListOf<String>()
            if (onEdit != null) actions.add("Edit")
            actions.add("Favorite")
            if (onDelete != null) actions.add("Delete")

            actions.forEach { action ->
                DropdownMenuItem(
                    onClick = {
                        when (action) {
                            "Edit" -> onEdit?.invoke()
                            "Favorite" -> onFavoriteToggle()
                            "Delete" -> onDelete?.invoke()
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

@Composable
fun EntryCardForDetails(
    entry: EntryModel,
    tripTitle: String,
    isFavorite: Boolean,
    currentThemeId: String,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    val cardBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp)) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.type == EntryType.PHOTO && entry.mediaIds.isNotEmpty()) {
                        val mediaId = entry.mediaIds.first()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            TripImage(
                                mediaId,
                            )
                        }
                    } else {
                        val icon = when (entry.type) {
                            EntryType.NOTE -> Icons.Default.Description
                            EntryType.PLACE -> Icons.Default.Place
                            EntryType.ROUTE_POINT -> Icons.Default.Tour
                            else -> Icons.Default.Photo
                        }
                        Icon(
                            icon,
                            contentDescription = entry.type.name,
                            tint = Color.White,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                entry.title?.let {
                    Text(it, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                entry.text?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.LightGray
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(tripTitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                Spacer(Modifier.height(8.dp))

            }

            if (isFavorite) {
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
                        .clickable { onFavoriteToggle() }
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(cardBackgroundColor, shape = RoundedCornerShape(8.dp))
        ) {
            val actions = mutableListOf<String>()
            if (onEdit != null) actions.add("Edit")
            actions.add("Favorite")
            if (onDelete != null) actions.add("Delete")

            actions.forEach { action ->
                DropdownMenuItem(
                    onClick = {
                        when (action) {
                            "Edit" -> onEdit?.invoke()
                            "Favorite" -> onFavoriteToggle()
                            "Delete" -> onDelete?.invoke()
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
