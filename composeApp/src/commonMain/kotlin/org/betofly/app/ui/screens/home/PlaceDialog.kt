package org.betofly.app.ui.screens.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.betofly.app.model.EntryModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDialog(
    place: EntryModel,
    onDismiss: () -> Unit,
    currentThemeId: String
) {
    val screenBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF005533)
        "theme_dark" -> Color(0xFF004433)
        "theme_blue" -> Color(0xFF1A2A5D)
        "theme_gold" -> Color(0xFF935022)
        else -> Color(0xFF005533)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = screenBackgroundColor,
        title = { Text(place.title ?: "Untitled", color = Color.White) },
        text = {
            Text(
                "Coordinates: ${place.coords?.latitude}, ${place.coords?.longitude}",
                color = Color.White
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        }
    )
}