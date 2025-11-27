package org.betofly.app.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
    val backgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = backgroundColor,

        title = { Text("Note", color = Color.White) },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Title", color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.height(2.dp))
                Text(
                    note.title ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))

                Text("Text", color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.height(2.dp))
                Text(
                    note.text ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
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