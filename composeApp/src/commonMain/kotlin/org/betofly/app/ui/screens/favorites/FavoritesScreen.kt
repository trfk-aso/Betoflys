package org.betofly.app.ui.screens.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.bg_dark
import betofly.composeapp.generated.resources.bg_graphite_gold
import betofly.composeapp.generated.resources.bg_light
import betofly.composeapp.generated.resources.bg_royal_blue
import betofly.composeapp.generated.resources.ic_add_blue
import betofly.composeapp.generated.resources.ic_add_dark
import betofly.composeapp.generated.resources.ic_add_gold
import betofly.composeapp.generated.resources.ic_add_light
import betofly.composeapp.generated.resources.ic_back_blue
import betofly.composeapp.generated.resources.ic_back_dark
import betofly.composeapp.generated.resources.ic_back_gold
import betofly.composeapp.generated.resources.ic_back_light
import betofly.composeapp.generated.resources.ic_settings_blue
import betofly.composeapp.generated.resources.ic_settings_dark
import betofly.composeapp.generated.resources.ic_settings_gold
import betofly.composeapp.generated.resources.ic_settings_light
import org.betofly.app.model.EntryModel
import org.betofly.app.model.TripUiModel
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.home.EmptyTripsState
import org.betofly.app.ui.screens.home.QuickAccessRow
import org.betofly.app.ui.screens.home.TripCard
import org.betofly.app.viewModel.FavoritesUiState
import org.betofly.app.viewModel.FavoritesViewModel
import org.betofly.app.viewModel.JournalViewModel
import org.betofly.app.viewModel.SearchUiState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    viewModelFavorites: FavoritesViewModel = koinInject(),
    themeRepository: ThemeRepository,
    viewModelJournal: JournalViewModel = koinInject()
) {
    val uiState by viewModelFavorites.uiState.collectAsState()
    val selectedTab by viewModelFavorites.selectedTab.collectAsState()

    var currentPage by remember { mutableStateOf(0) }
    val maxItems = if (selectedTab == "Trips") 2 else 2

    val currentThemeId by themeRepository.currentThemeId.collectAsState()

    val backgroundRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.bg_light
        "theme_dark" -> Res.drawable.bg_dark
        "theme_blue" -> Res.drawable.bg_royal_blue
        "theme_gold" -> Res.drawable.bg_graphite_gold
        else -> Res.drawable.bg_light
    }

    val topBarBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF00110D)
        "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Favorites",
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
                        containerColor = topBarBackgroundColor
                    )
                )

                val tabBackgroundColor = when (currentThemeId) {
                    "theme_light" -> Color(0xFF003322)
                    "theme_dark" -> Color(0xFF003322)
                    "theme_blue" -> Color(0xFF0A1A3D)
                    "theme_gold" -> Color(0xFF814011)
                    else -> Color(0xFF003322)
                }

                val tabs = listOf("Trips", "Entries")
                val selectedIndex = tabs.indexOf(selectedTab)

                TabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = topBarBackgroundColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            color = tabBackgroundColor
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = index == selectedIndex,
                            onClick = {
                                viewModelFavorites.onTabSelected(tabs[index])
                                currentPage = 0
                            },
                            text = {
                                Text(
                                    text = title,
                                    color = if (index == selectedIndex) Color.White else Color.LightGray
                                )
                            },
                            modifier = Modifier.background(tabBackgroundColor)
                        )
                    }
                }
            }
        },
        bottomBar = {
            val paginationButtonColor = when (currentThemeId) {
                "theme_light" -> Color(0xFF003322)
                "theme_dark" -> Color(0xFF003322)
                "theme_blue" -> Color(0xFF0A1A3D)
                "theme_gold" -> Color(0xFF814011)
                else -> Color(0xFF003322)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(paginationButtonColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(paginationButtonColor),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    val paginationButtonColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF004433)
                        "theme_dark" -> Color(0xFF004433)
                        "theme_blue" -> Color(0xFF0C1C4F)
                        "theme_gold" -> Color(0xFF92521A)
                        else -> Color(0xFF004433)
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(paginationButtonColor, shape = RoundedCornerShape(8.dp)) // фон как у табов
                            .clickable(
                                enabled = currentPage > 0,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { if (currentPage > 0) currentPage-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    val canGoForward = when (uiState) {
                        is FavoritesUiState.Success -> {
                            val itemsList = if (selectedTab == "Trips") {
                                (uiState as FavoritesUiState.Success).trips
                            } else {
                                (uiState as FavoritesUiState.Success).entries
                            }
                            (currentPage + 1) * maxItems < itemsList.size
                        }
                        else -> false
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(paginationButtonColor, shape = RoundedCornerShape(8.dp))
                            .clickable(
                                enabled = canGoForward,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { if (canGoForward) currentPage++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Forward",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                QuickAccessRow(
                    currentThemeId = currentThemeId ?: "theme_light",
                    onNewTrip = { navController.navigate(Screen.Home.route) },
                    onJournal = { navController.navigate(Screen.Journal.route) },
                    onSearch = { navController.navigate(Screen.Search.route) },
                    onFavorites = { navController.navigate(Screen.Favorites.route) },
                    onStatistics = { navController.navigate(Screen.Statistics.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            when (val state = uiState) {
                is FavoritesUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is FavoritesUiState.Empty -> EmptyTripsState(
                    currentThemeId = currentThemeId ?: "theme_light",
                    onCreateTrip = { navController.navigate(Screen.CreateTrip.route) }
                )

                is FavoritesUiState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Error: ${state.message}") }

                is FavoritesUiState.Success -> {
                    val itemsList = if (selectedTab == "Trips") state.trips else state.entries
                    val startIndex = currentPage * maxItems
                    val endIndex = (startIndex + maxItems).coerceAtMost(itemsList.size)
                    val paginatedItems = itemsList.subList(startIndex, endIndex)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(paginatedItems) { item ->
                            if (selectedTab == "Trips") {
                                val tripUi = item as TripUiModel
                                SwipeToDismissCustom(
                                    onDismiss = { viewModelFavorites.toggleTripFavorite(tripUi.trip.id) }
                                ) {
                                    TripCard(
                                        tripUi = tripUi,
                                        currentThemeId = currentThemeId ?: "theme_light",
                                        onClick = {
                                            viewModelFavorites.onTripSelected(tripUi.trip.id)
                                            navController.navigate(Screen.TripDetails.route)
                                        },
                                        onEdit = {},
                                        onExport = {},
                                        onFavorite = { viewModelFavorites.toggleTripFavorite(tripUi.trip.id) },
                                        onDelete = {}
                                    )
                                }
                            } else {
                                val entry = item as EntryModel
                                SwipeToDismissCustom(
                                    onDismiss = { viewModelFavorites.toggleEntryFavorite(entry.id) }
                                ) {
                                    EntryCard(
                                        entry = entry,
                                        tripTitle = entry.title ?: "",
                                        currentThemeId = currentThemeId ?: "theme_light",
                                        isFavorite = viewModelFavorites.favoriteEntryIds.collectAsState().value.contains(entry.id),
                                        onClick = {
                                            viewModelFavorites.onTripSelected(entry.tripId)
                                            navController.navigate(Screen.TripDetails.route)
                                        },
                                        onFavoriteToggle = {
                                            viewModelFavorites.toggleEntryFavorite(entry.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToDismissCustom(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        Modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        offsetX += dragAmount.x
                        change.consume()
                    },
                    onDragEnd = {
                        if (abs(offsetX) > 200f) {
                            onDismiss()
                        } else {
                            offsetX = 0f
                        }
                    }
                )
            }
    ) {
        content()
    }
}
