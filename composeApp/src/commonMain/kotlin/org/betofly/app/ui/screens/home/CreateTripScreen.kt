package org.betofly.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.di.ImagePicker
import org.betofly.app.model.Trip
import org.betofly.app.model.TripCategory
import org.betofly.app.viewModel.HomeViewModel
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.bg_dark
import betofly.composeapp.generated.resources.bg_graphite_gold
import betofly.composeapp.generated.resources.bg_light
import betofly.composeapp.generated.resources.bg_royal_blue
import betofly.composeapp.generated.resources.ic_back_blue
import betofly.composeapp.generated.resources.ic_back_dark
import betofly.composeapp.generated.resources.ic_back_gold
import betofly.composeapp.generated.resources.ic_back_light
import betofly.composeapp.generated.resources.ic_photo_blue
import betofly.composeapp.generated.resources.ic_photo_dark
import betofly.composeapp.generated.resources.ic_photo_gold
import org.betofly.app.repository.ThemeRepository
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = koinInject(),
    themeRepository: ThemeRepository = koinInject(),
    currentThemeId: String
) {
    var title by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf(today.plus(3, DateTimeUnit.DAY)) }
    var category by remember { mutableStateOf(TripCategory.CITY_BREAK) }

    var coverImageUri by remember { mutableStateOf<Any?>(null) }
    var note by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var newTag by remember { mutableStateOf("") }

    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.toEpochMillis())
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.toEpochMillis())

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var showImageOptions by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val backgroundRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.bg_light
        "theme_dark" -> Res.drawable.bg_dark
        "theme_blue" -> Res.drawable.bg_royal_blue
        "theme_gold" -> Res.drawable.bg_graphite_gold
        else -> Res.drawable.bg_light
    }

    val tabBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    val backButtonRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.ic_back_light
        "theme_dark" -> Res.drawable.ic_back_dark
        "theme_blue" -> Res.drawable.ic_back_blue
        "theme_gold" -> Res.drawable.ic_back_gold
        else -> Res.drawable.ic_back_light
    }

    val addImageIconRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.ic_photo_dark
        "theme_dark" -> Res.drawable.ic_photo_dark
        "theme_blue" -> Res.drawable.ic_photo_blue
        "theme_gold" -> Res.drawable.ic_photo_gold
        else -> Res.drawable.ic_photo_dark
    }


    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White.copy(alpha = 0.5f),
        errorTextColor = Color.Red,
        focusedContainerColor = tabBackgroundColor,
        unfocusedContainerColor = tabBackgroundColor,
        disabledContainerColor = tabBackgroundColor,
        errorContainerColor = tabBackgroundColor,
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

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        startDate = Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        endDate = Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = endDatePickerState) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Image(
                                painter = painterResource(backButtonRes),
                                contentDescription = "Back",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = (-20).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Trip",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Enter title") },
                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tabBackgroundColor, RoundedCornerShape(8.dp)),
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp)
                )

                ImagePicker(
                    onImagePicked = { pickedImageUri ->
                        coverImageUri = pickedImageUri
                    },
                    themeRepository = themeRepository
                )

                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tabBackgroundColor, RoundedCornerShape(8.dp))
                ) { Text("Start: $startDate", color = Color.White, fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tabBackgroundColor, RoundedCornerShape(8.dp))
                ) { Text("End: $endDate", color = Color.White, fontWeight = FontWeight.Bold) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tabBackgroundColor, RoundedCornerShape(8.dp))
                ) {
                    OutlinedTextField(
                        value = category.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(tabBackgroundColor)
                    ) {
                        TripCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name.replace("_", " "), color = Color.White) },
                                onClick = { category = cat; expanded = false },
                                modifier = Modifier.background(tabBackgroundColor)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text("Add Tag") },
                        placeholder = { Text("New tag") },
                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .weight(1f)
                            .background(tabBackgroundColor, RoundedCornerShape(8.dp)),
                        singleLine = true,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (newTag.isNotBlank()) {
                                tags = tags + newTag.trim()
                                newTag = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tabBackgroundColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) { Text("Add", color = Color.White, fontWeight = FontWeight.Bold) }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { tags = tags.filterNot { it == tag } },
                            label = { Text(tag, color = Color.White, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.background(tabBackgroundColor, RoundedCornerShape(8.dp))
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description") },
                    placeholder = { Text("Enter note") },
                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .background(tabBackgroundColor, RoundedCornerShape(8.dp)),
                    singleLine = false,
                    maxLines = 6,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        val trip = Trip(
                            id = 0,
                            title = title,
                            startDate = startDate,
                            endDate = endDate,
                            category = category,
                            coverImageId = coverImageUri?.toString(),
                            description = note.ifBlank { null },
                            tags = tags,
                            createdAt = now,
                            updatedAt = now
                        )
                        viewModel.createTrip(trip)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tabBackgroundColor, RoundedCornerShape(8.dp)),
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tabBackgroundColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun LocalDate.toEpochMillis(): Long =
    this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
