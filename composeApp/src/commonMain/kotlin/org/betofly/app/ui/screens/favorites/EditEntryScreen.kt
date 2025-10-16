package org.betofly.app.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.viewModel.JournalViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    navController: NavHostController,
    viewModel: JournalViewModel = koinInject(),
    themeRepository: ThemeRepository
) {
    val entry by viewModel.editingEntry.collectAsState()
    if (entry == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    var title by remember { mutableStateOf(entry!!.title ?: "") }
    var text by remember { mutableStateOf(entry!!.text ?: "") }
    var showDialog by remember { mutableStateOf(false) }

    val currentThemeId by themeRepository.currentThemeId.collectAsState()

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
        errorTextColor = Color.Red,
        focusedContainerColor = inputBackgroundColor,
        unfocusedContainerColor = inputBackgroundColor,
        disabledContainerColor = inputBackgroundColor,
        errorContainerColor = inputBackgroundColor,
        cursorColor = Color.White,
        errorCursorColor = Color.Red,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        errorBorderColor = Color.Transparent,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White,
        disabledLabelColor = Color.White.copy(alpha = 0.5f),
        errorLabelColor = Color.Red
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .background(screenBackgroundColor)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    colors = fieldColors
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text") },
                    modifier = Modifier.height(150.dp),
                    colors = fieldColors
                )

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = inputBackgroundColor)
                ) {
                    Text("Edit in Dialog", color = Color.White)
                }

                if (showDialog) {
                    var dialogTitle by remember { mutableStateOf(title) }
                    var dialogText by remember { mutableStateOf(text) }

                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        containerColor = screenBackgroundColor,
                        title = { Text("Edit Entry", color = Color.White) },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = dialogTitle,
                                    onValueChange = { dialogTitle = it },
                                    label = { Text("Title") },
                                    colors = fieldColors
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = dialogText,
                                    onValueChange = { dialogText = it },
                                    label = { Text("Text") },
                                    modifier = Modifier.height(150.dp),
                                    colors = fieldColors
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                title = dialogTitle
                                text = dialogText
                                viewModel.saveEditedEntry(
                                    entry!!.copy(
                                        title = title,
                                        text = text,
                                        updatedAt = Clock.System.now()
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                    )
                                )
                                showDialog = false
                            }) { Text("Save", color = Color.White) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    )
                }
            }
        }
    )
}
