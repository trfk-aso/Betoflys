package org.betofly.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.bg_dark
import betofly.composeapp.generated.resources.bg_graphite_gold
import betofly.composeapp.generated.resources.bg_light
import betofly.composeapp.generated.resources.bg_royal_blue
import betofly.composeapp.generated.resources.ic_back_blue
import betofly.composeapp.generated.resources.ic_back_dark
import betofly.composeapp.generated.resources.ic_back_gold
import betofly.composeapp.generated.resources.ic_back_light
import betofly.composeapp.generated.resources.ic_settings_blue
import betofly.composeapp.generated.resources.ic_settings_dark
import betofly.composeapp.generated.resources.ic_settings_gold
import betofly.composeapp.generated.resources.ic_settings_light
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.RoutePoint
import org.betofly.app.model.Trip
import org.betofly.app.model.TripUiModel
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.repository.TripRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.favorites.EntryCard
import org.betofly.app.ui.screens.favorites.EntryCardForDetails
import org.betofly.app.viewModel.FavoritesViewModel
import org.betofly.app.viewModel.HomeViewModel
import org.betofly.app.viewModel.RecordingViewModel
import org.betofly.app.viewModel.TripDetailsUiState
import org.betofly.app.viewModel.TripDetailsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    navController: NavHostController,
    viewModel: TripDetailsViewModel = koinInject(),
    homeViewModel: HomeViewModel = koinInject(),
    themeRepository: ThemeRepository = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recordingViewModel: RecordingViewModel = koinInject()
    val currentThemeId by themeRepository.currentThemeId.collectAsState()

    val backgroundRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.bg_light
        "theme_dark" -> Res.drawable.bg_dark
        "theme_blue" -> Res.drawable.bg_royal_blue
        "theme_gold" -> Res.drawable.bg_graphite_gold
        else -> Res.drawable.bg_light
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
                            text = (uiState as? TripDetailsUiState.Success)?.trip?.title ?: "Trip Details",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            key(currentThemeId) {
                Image(
                    painter = painterResource(backgroundRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            when (uiState) {
                is TripDetailsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TripDetailsUiState.Error -> {
                    Text(
                        (uiState as TripDetailsUiState.Error).message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is TripDetailsUiState.Success -> {
                    TripDetailsContent(
                        tripDetails = uiState as TripDetailsUiState.Success,
                        navController = navController,
                        viewModel = viewModel,
                        homeViewModel = homeViewModel,
                        recordingViewModel = recordingViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsContent(
    tripDetails: TripDetailsUiState.Success,
    navController: NavHostController,
    viewModel: TripDetailsViewModel,
    homeViewModel: HomeViewModel,
    recordingViewModel: RecordingViewModel,
    modifier: Modifier = Modifier,
    themeRepository: ThemeRepository = koinInject()
) {
    val trip = tripDetails.trip
    val entries = tripDetails.entries
    val route = tripDetails.routePoints
    val currentThemeId by themeRepository.currentThemeId.collectAsState()

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

    Box(modifier = modifier.fillMaxSize()) {
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
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                TripImage(trip.coverImageId)
                Spacer(Modifier.height(8.dp))
                Text("${trip.startDate} – ${trip.endDate}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Spacer(Modifier.height(8.dp))

                val progressColor = when (currentThemeId) {
                    "theme_light" -> Color(0xFF00FF00)
                    "theme_dark" -> Color(0xFF4CAF50)
                    "theme_blue" -> Color(0xFF2196F3)
                    "theme_gold" -> Color(0xFFFFC107)
                    else -> Color(0xFF00FF00)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(progressColor.copy(alpha = 0.2f))
                ) {
                    LinearProgressIndicator(
                        progress = trip.progress,
                        color = progressColor,
                        trackColor = Color.Transparent,
                        modifier = Modifier
                            .fillMaxHeight()
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (trip.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        trip.tags.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tag, color = Color.White) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = progressColor,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Text("Entries", style = MaterialTheme.typography.titleLarge, color = Color.White)
                EntriesTab(
                    entries,
                    navController,
                    currentThemeId = currentThemeId ?: "theme_light",
                    modifier = Modifier.heightIn(max = 600.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text("Info", style = MaterialTheme.typography.titleLarge, color = Color.White)
                InfoTab(trip, entries, viewModel)

                Spacer(Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (editColor, exportColor, favoriteColor, deleteColor, recordColor) = when (currentThemeId) {
                        "theme_light", "theme_dark" -> listOf(
                            Color(0xFF3C6B1E),
                            Color(0xFF3FBB27),
                            Color(0xFFA9BD12),
                            Color(0xFFBD1212),
                            Color(0xFFFF6D00)
                        )
                        "theme_blue" -> listOf(
                            Color(0xFF0D1B38),
                            Color(0xFF2BA7FF),
                            Color(0xFFF3A30F),
                            Color(0xFFBD1212),
                            Color(0xFFFF8C42)
                        )
                        "theme_gold" -> listOf(
                            Color(0xFF2F1C11),
                            Color(0xFFFC8600),
                            Color(0xFFF3A30F),
                            Color(0xFFBD1212),
                            Color(0xFFFFA726)
                        )
                        else -> listOf(
                            Color.Gray, Color.Gray, Color.Gray, Color.Gray, Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                homeViewModel.onTripSelected(trip.id)
                                navController.navigate(Screen.Edit.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = editColor)
                        ) { Text("Edit") }

                        Spacer(Modifier.width(16.dp))

                        Button(
                            onClick = { homeViewModel.exportTrip(trip.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = exportColor)
                        ) { Text("Export") }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { homeViewModel.toggleFavorite(trip.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = favoriteColor)
                        ) { Text("Favorite") }

                        Spacer(Modifier.width(16.dp))

                        Button(
                            onClick = {
                                homeViewModel.deleteTrip(trip.id)
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = deleteColor)
                        ) { Text("Delete") }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                recordingViewModel.startTripRecording(trip.id, trip.title)
                                navController.navigate(Screen.Recording.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = recordColor)
                        ) { Text("Record Trip") }
                    }
                }
            }
        }
    }
}

@Composable
fun EntriesTab(
    entries: List<EntryModel>,
    navController: NavHostController,
    currentThemeId: String,
    tripRepository: TripRepository = koinInject(),
    modifier: Modifier = Modifier
) {
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var selectedNote by remember { mutableStateOf<EntryModel?>(null) }
    var selectedPlace by remember { mutableStateOf<EntryModel?>(null) }

    val favoritesViewModel: FavoritesViewModel = koinInject()

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(entries) { entry ->
            val favoriteIds by favoritesViewModel.favoriteEntryIds.collectAsState()
            val isFavorite = favoriteIds.contains(entry.id)

            val tripTitleState = produceState<String?>(initialValue = null, entry.tripId) {
                value = tripRepository.getTripById(entry.tripId)?.title
            }
            val tripTitle = tripTitleState.value ?: ""

            Box(
                modifier = Modifier
                    .width(180.dp)
                    .wrapContentHeight()
                    .height(220.dp)
            ) {
                EntryCardForDetails(
                    entry = entry,
                    tripTitle = tripTitle,
                    isFavorite = isFavorite,
                    currentThemeId = currentThemeId,
                    onClick = {
                        when (entry.type) {
                            EntryType.PHOTO -> if (entry.mediaIds.isNotEmpty()) selectedImage = entry.mediaIds.first()
                            EntryType.NOTE -> selectedNote = entry
                            EntryType.PLACE -> selectedPlace = entry
                            else -> navController.navigate("entryDetails/${entry.id}")
                        }
                    },
                    onFavoriteToggle = { favoritesViewModel.toggleEntryFavorite(entry.id) },
                )
            }
        }
    }

    if (selectedImage != null) {
        Dialog(onDismissRequest = { selectedImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedImage = null },
                contentAlignment = Alignment.Center
            ) {
                selectedImage?.let { img ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        TripImage(img)
                    }
                }
            }
        }
    }

    if (selectedNote != null) {
        NoteDialog(
            note = selectedNote!!,
            onDismiss = { selectedNote = null },
            currentThemeId = currentThemeId
        )
    }

    if (selectedPlace != null) {
        PlaceDialog(
            place = selectedPlace!!,
            onDismiss = { selectedPlace = null },
            currentThemeId = currentThemeId
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RouteTab(route: List<RoutePoint>, currentThemeId: String) {
    val cardBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    if (route.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
        ) {
            Text(
                "No route points available",
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
        return
    }

    val totalMeters = remember(route) { calculateTotalDistanceMeters(route) }
    val duration = remember(route) { calculateDuration(route) }
    val totalKmText = formatMetersAsKm(totalMeters)
    val durationText = formatDuration(duration)

    var scale by remember { mutableStateOf(1f) }

    val (graphBgColor, graphLineColor) = when (currentThemeId) {
        "theme_light" -> Color(0xFF336131) to Color(0xFF3FBB27)
        "theme_dark" -> Color(0xFF002818) to Color(0xFF3FBB27)
        "theme_blue" -> Color(0xFF32577F) to Color(0xFF2BA7FF)
        "theme_gold" -> Color(0xFF764E2E) to Color(0xFFFC8600)
        else -> Color.Gray to Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Length: $totalKmText",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
                Text(
                    "Duration: $durationText",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(16.dp)
                            .background(Color.Green, shape = CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Start", color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(16.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("End", color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                        }
                    }
            ) {
                RouteCanvas(
                    route = route,
                    backgroundColor = graphBgColor,
                    lineColor = graphLineColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackgroundColor)
                    .padding(12.dp)
            ) {
                route.take(10).forEachIndexed { idx, p ->
                    Text(
                        "• ${idx + 1}: ${p.coords.latitude.formatDecimals(6)}, ${p.coords.longitude.formatDecimals(6)}" +
                                (if (p.altitude != null || p.speed != null)
                                    " h:${p.altitude?.toInt() ?: 0}m s:${p.speed?.toInt() ?: 0}km/h"
                                else "") +
                                " at ${p.timestamp}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (route.size > 10) {
                    Text(
                        "...and ${route.size - 10} more points",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun InfoTab(
    trip: Trip,
    entries: List<EntryModel>,
    viewModel: TripDetailsViewModel
) {
    val places by viewModel.places.collectAsState(initial = emptyList())
    LaunchedEffect(trip.id) { viewModel.loadPlaces(trip.id) }

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            "Description:",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            trip.description ?: "No description",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(Modifier.height(16.dp))

        Text(
            "Statistics:",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            "Entries: ${entries.size}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            "Places: ${places.size}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(Modifier.height(16.dp))

        if (places.isNotEmpty()) {
            Text(
                "Places:",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(8.dp))
            places.forEach { place ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF1E88E5))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        place.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
