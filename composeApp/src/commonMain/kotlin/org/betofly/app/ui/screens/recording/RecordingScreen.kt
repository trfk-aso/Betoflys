package org.betofly.app.ui.screens.recording

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.betofly.app.model.Coordinates
import org.betofly.app.viewModel.RecordingViewModel
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import betofly.composeapp.generated.resources.ic_car_blue
import betofly.composeapp.generated.resources.ic_car_dark
import betofly.composeapp.generated.resources.ic_car_gold
import betofly.composeapp.generated.resources.ic_car_light
import betofly.composeapp.generated.resources.ic_note_blue
import betofly.composeapp.generated.resources.ic_note_dark
import betofly.composeapp.generated.resources.ic_note_gold
import betofly.composeapp.generated.resources.ic_note_light
import betofly.composeapp.generated.resources.ic_photo_blue
import betofly.composeapp.generated.resources.ic_photo_dark
import betofly.composeapp.generated.resources.ic_photo_gold
import betofly.composeapp.generated.resources.ic_photo_light
import betofly.composeapp.generated.resources.ic_place_blue
import betofly.composeapp.generated.resources.ic_place_dark
import betofly.composeapp.generated.resources.ic_place_gold
import betofly.composeapp.generated.resources.ic_place_light
import betofly.composeapp.generated.resources.ic_settings_blue
import betofly.composeapp.generated.resources.ic_settings_dark
import betofly.composeapp.generated.resources.ic_settings_gold
import betofly.composeapp.generated.resources.ic_settings_light
import betofly.composeapp.generated.resources.ic_start_blue
import betofly.composeapp.generated.resources.ic_start_dark
import betofly.composeapp.generated.resources.ic_start_gold
import betofly.composeapp.generated.resources.ic_start_light
import betofly.composeapp.generated.resources.ic_stop
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.di.ImagePicker
import org.betofly.app.model.EntryType
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.home.QuickAccessRow
import org.betofly.app.ui.screens.home.TripImage
import org.jetbrains.compose.resources.painterResource
import kotlin.math.round
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel = koinInject(),
    navController: NavHostController,
    themeRepository: ThemeRepository = koinInject()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val duration by viewModel.recordingDuration.collectAsState()
    val currentTripName by viewModel.currentTripTitle.collectAsState()
    val currentThemeId by themeRepository.currentThemeId.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    var showPlaceDialog by remember { mutableStateOf(false) }
    var placeName by remember { mutableStateOf("") }
    var placeLat by remember { mutableStateOf("") }
    var placeLon by remember { mutableStateOf("") }

    var showPhotoPicker by remember { mutableStateOf(false) }

    val totalDistance = remember(routePoints) {
        routePoints.zipWithNext().sumOf { (p1, p2) ->
            haversineDistance(p1.coords, p2.coords)
        }
    }

    val backgroundRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.bg_light
        "theme_dark" -> Res.drawable.bg_dark
        "theme_blue" -> Res.drawable.bg_royal_blue
        "theme_gold" -> Res.drawable.bg_graphite_gold
        else -> Res.drawable.bg_light
    }

    val backgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF00110D)
        "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    val photoIconRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.ic_photo_light
        "theme_dark" -> Res.drawable.ic_photo_dark
        "theme_blue" -> Res.drawable.ic_photo_blue
        "theme_gold" -> Res.drawable.ic_photo_gold
        else -> Res.drawable.ic_photo_light
    }


    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Recording",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isRecording) {
                            showExitDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        val backIconRes = when (currentThemeId) {
                            "theme_light" -> Res.drawable.ic_back_light
                            "theme_dark" -> Res.drawable.ic_back_dark
                            "theme_blue" -> Res.drawable.ic_back_blue
                            "theme_gold" -> Res.drawable.ic_back_gold
                            else -> Res.drawable.ic_back_light
                        }
                        Image(
                            painter = painterResource(backIconRes),
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        val settingsIconRes = when (currentThemeId) {
                            "theme_light" -> Res.drawable.ic_settings_light
                            "theme_dark" -> Res.drawable.ic_settings_dark
                            "theme_blue" -> Res.drawable.ic_settings_blue
                            "theme_gold" -> Res.drawable.ic_settings_gold
                            else -> Res.drawable.ic_settings_light
                        }
                        Image(
                            painter = painterResource(settingsIconRes),
                            contentDescription = "Settings",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF00110D)
                        "theme_dark" -> Color(0xFF00110D)
                        "theme_blue" -> Color(0xFF060F22)
                        "theme_gold" -> Color(0xFF673001)
                        else -> Color(0xFF00110D)
                    }
                )
            )
        },
        bottomBar = {
            QuickAccessRow(
                currentThemeId = currentThemeId ?: "theme_light",
                onNewTrip = { navController.navigate(Screen.Home.route) },
                onJournal = { navController.navigate(Screen.Journal.route) },
                onSearch = { navController.navigate(Screen.Search.route) },
                onFavorites = { navController.navigate(Screen.Favorites.route) },
                onStatistics = { navController.navigate(Screen.Statistics.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            key(currentThemeId) {
                Image(
                    painter = painterResource(backgroundRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentTripName ?: "Trip: Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        val indicatorColor = if (isRecording) Color.Red else Color.Gray
                        Box(
                            Modifier
                                .size(16.dp)
                                .background(indicatorColor, CircleShape)
                        )
                    }

                    val roundedDistance = round(totalDistance * 100) / 100.0
                    Text(
                        "Duration: ${duration}s",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        "Points recorded: ${routePoints.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        "Approx. distance: $roundedDistance km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(2.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(2.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(photoIconRes),
                                    contentDescription = "Photo",
                                    modifier = Modifier.size(128.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Box(
                                    modifier = Modifier
                                        .size(128.dp)
                                        .clickable { showPhotoPicker = true }
                                )
                            }

                            if (showPhotoPicker) {
                                ImagePicker(
                                    onImagePicked = { uri ->
                                        viewModel.addPhotoEntry("Photo", uri.toString())
                                        showPhotoPicker = false
                                    },
                                    themeRepository = themeRepository
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val noteBackgroundColor = when (currentThemeId) {
                            "theme_light" -> Color(0xFF336131)
                            "theme_dark"  -> Color(0xFF002818)
                            "theme_blue"  -> Color(0xFF32577F)
                            "theme_gold"  -> Color(0xFF764E2E)
                            else -> Color(0xFF336131)
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(60.dp)
                                .background(noteBackgroundColor, shape = RoundedCornerShape(12.dp))
                                .clickable { showNoteDialog = true }
                        ) {
                            Text(
                                text = "+Add Note",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val placeBackgroundColor = when (currentThemeId) {
                            "theme_light" -> Color(0xFF336131)
                            "theme_dark"  -> Color(0xFF002818)
                            "theme_blue"  -> Color(0xFF32577F)
                            "theme_gold"  -> Color(0xFF764E2E)
                            else -> Color(0xFF336131)
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(60.dp)
                                .background(placeBackgroundColor, shape = RoundedCornerShape(12.dp))
                                .clickable { showPlaceDialog = true }
                        ) {
                            Text(
                                text = "+Add Place",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val recordBackgroundColor = when (currentThemeId) {
                            "theme_light" -> Color(0xFF336131)
                            "theme_dark"  -> Color(0xFF002818)
                            "theme_blue"  -> Color(0xFF32577F)
                            "theme_gold"  -> Color(0xFF764E2E)
                            else -> Color(0xFF336131)
                        }

                        val carIconRes = when (currentThemeId) {
                            "theme_light" -> Res.drawable.ic_car_light
                            "theme_dark"  -> Res.drawable.ic_car_dark
                            "theme_blue"  -> Res.drawable.ic_car_blue
                            "theme_gold"  -> Res.drawable.ic_car_gold
                            else -> Res.drawable.ic_car_light
                        }

                        val startIconRes = when (currentThemeId) {
                            "theme_light" -> Res.drawable.ic_start_light
                            "theme_dark"  -> Res.drawable.ic_start_dark
                            "theme_blue"  -> Res.drawable.ic_start_blue
                            "theme_gold"  -> Res.drawable.ic_start_gold
                            else -> Res.drawable.ic_start_light
                        }

                        val stopIconRes = Res.drawable.ic_stop

                        val actionIconRes = if (isRecording) stopIconRes else startIconRes

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(70.dp)
                                .shadow(6.dp, RoundedCornerShape(14.dp))
                                .background(recordBackgroundColor, shape = RoundedCornerShape(14.dp))
                                .clickable {
                                    when {
                                        isPaused -> viewModel.resumeRecording(Coordinates(50.0, 30.0))
                                        else -> viewModel.toggleRecording(Coordinates(50.0, 30.0))
                                    }
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Image(
                                    painter = painterResource(carIconRes),
                                    contentDescription = "Car",
                                    modifier = Modifier
                                        .height(70.dp)
                                        .aspectRatio(2f),
                                    contentScale = ContentScale.Fit
                                )

                                Image(
                                    painter = painterResource(actionIconRes),
                                    contentDescription = if (isRecording) "Stop" else "Start",
                                    modifier = Modifier
                                        .height(70.dp)
                                        .aspectRatio(2f),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(30.dp))

                    val cardBackgroundColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF003322)
                        "theme_dark" -> Color(0xFF003322)
                        "theme_blue" -> Color(0xFF0A1A3D)
                        "theme_gold" -> Color(0xFF814011)
                        else -> Color(0xFF003322)
                    }

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White.copy(alpha = 0.5f),
                        errorTextColor = Color.Red,
                        focusedContainerColor = cardBackgroundColor,
                        unfocusedContainerColor = cardBackgroundColor,
                        disabledContainerColor = cardBackgroundColor,
                        errorContainerColor = cardBackgroundColor,
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

                    if (showNoteDialog) {
                        AlertDialog(
                            onDismissRequest = { showNoteDialog = false },
                            containerColor = cardBackgroundColor,
                            title = {
                                Text(
                                    "Add Note",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Column {
                                    Text(
                                        "Title:",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextField(
                                        value = noteTitle,
                                        onValueChange = { noteTitle = it },
                                        placeholder = { Text("Enter title", color = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        colors = fieldColors
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        "Text:",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        placeholder = { Text("Enter note text", color = Color.White.copy(alpha = 0.5f)) },
                                        modifier = Modifier.height(100.dp),
                                        colors = fieldColors
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (noteTitle.isNotBlank() || noteText.isNotBlank()) {
                                        viewModel.addNoteEntry(noteTitle, noteText)
                                        noteTitle = ""
                                        noteText = ""
                                        showNoteDialog = false
                                    }
                                }) {
                                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    noteTitle = ""
                                    noteText = ""
                                    showNoteDialog = false
                                }) {
                                    Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    if (showPlaceDialog) {
                        AlertDialog(
                            onDismissRequest = { showPlaceDialog = false },
                            containerColor = cardBackgroundColor,
                            title = {
                                Text(
                                    "Add Place",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Column {
                                    Text("Place Name:", color = Color.White, fontWeight = FontWeight.Bold)
                                    TextField(
                                        value = placeName,
                                        onValueChange = { placeName = it },
                                        placeholder = { Text("Enter place name", color = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        colors = fieldColors
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text("Latitude:", color = Color.White, fontWeight = FontWeight.Bold)
                                    TextField(
                                        value = placeLat,
                                        onValueChange = { placeLat = it },
                                        placeholder = { Text("e.g., 50.4501", color = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = fieldColors
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text("Longitude:", color = Color.White, fontWeight = FontWeight.Bold)
                                    TextField(
                                        value = placeLon,
                                        onValueChange = { placeLon = it },
                                        placeholder = { Text("e.g., 30.5234", color = Color.White.copy(alpha = 0.5f)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = fieldColors
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val lat = placeLat.toDoubleOrNull()
                                    val lon = placeLon.toDoubleOrNull()
                                    if (placeName.isNotBlank() && lat != null && lon != null) {
                                        viewModel.addPlaceEntry(placeName, Coordinates(lat, lon))
                                        placeName = ""
                                        placeLat = ""
                                        placeLon = ""
                                        showPlaceDialog = false
                                    }
                                }) {
                                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    placeName = ""
                                    placeLat = ""
                                    placeLon = ""
                                    showPlaceDialog = false
                                }) {
                                    Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    val buttonColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF3FBB27)
                        "theme_dark"  -> Color(0xFF3FBB27)
                        "theme_blue"  -> Color(0xFF2BA7FF)
                        "theme_gold"  -> Color(0xFFFC8600)
                        else -> Color(0xFF3FBB27)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveAndExit()
                                navController.navigate(Screen.Home.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Text("Done")
                        }

                        Button(
                            onClick = {
                                navController.navigate(Screen.TripDetails.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Text("Next")
                        }
                    }
                }
            }

            val cardBackgroundColor = when (currentThemeId) {
                "theme_light" -> Color(0xFF003322)
                "theme_dark" -> Color(0xFF003322)
                "theme_blue" -> Color(0xFF0A1A3D)
                "theme_gold" -> Color(0xFF814011)
                else -> Color(0xFF003322)
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    containerColor = cardBackgroundColor,
                    title = {
                        Text(
                            "Are you sure?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            "Route recording is in progress. If you exit, the recording will be paused.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.pauseRecording()
                            showExitDialog = false
                            navController.popBackStack()
                        }) {
                            Text("Exit", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onPrimary,
    background: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(1.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

fun haversineDistance(c1: Coordinates, c2: Coordinates): Double {
    val R = 6371.0
    val lat1 = c1.latitude.toRadians()
    val lon1 = c1.longitude.toRadians()
    val lat2 = c2.latitude.toRadians()
    val lon2 = c2.longitude.toRadians()
    val dLat = lat2 - lat1
    val dLon = lon2 - lon1
    val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

fun Double.toRadians() = this * PI / 180.0