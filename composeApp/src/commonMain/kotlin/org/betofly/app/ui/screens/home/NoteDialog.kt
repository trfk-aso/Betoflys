package org.betofly.app.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.betofly.app.model.EntryModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    note: EntryModel,
    onDismiss: () -> Unit,
    currentThemeId: String
) {
    val inputBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    val screenBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF005533)
        "theme_dark" -> Color(0xFF004433)
        "theme_blue" -> Color(0xFF1A2A5D)
        "theme_gold" -> Color(0xFF935022)
        else -> Color(0xFF005533)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White.copy(alpha = 0.5f),
        focusedContainerColor = inputBackgroundColor,
        unfocusedContainerColor = inputBackgroundColor,
        disabledContainerColor = inputBackgroundColor,
        cursorColor = Color.White,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White,
        disabledLabelColor = Color.White.copy(alpha = 0.5f)
    )

    var title by remember { mutableStateOf(note.title ?: "") }
    var text by remember { mutableStateOf(note.text ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = screenBackgroundColor,
        title = { Text("Note", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = fieldColors
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text") },
                    modifier = Modifier.height(100.dp),
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        }
    )
}