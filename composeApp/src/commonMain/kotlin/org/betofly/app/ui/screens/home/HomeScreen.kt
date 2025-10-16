package org.betofly.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
import betofly.composeapp.generated.resources.ic_settings_blue
import betofly.composeapp.generated.resources.ic_settings_dark
import betofly.composeapp.generated.resources.ic_settings_gold
import betofly.composeapp.generated.resources.ic_settings_light
import betofly.composeapp.generated.resources.ic_title
import org.betofly.app.model.TripCategory
import org.betofly.app.model.TripUiModel
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.viewModel.HomeUiState
import org.betofly.app.viewModel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    themeRepository: ThemeRepository,
    viewModel: HomeViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentThemeId by themeRepository.currentThemeId.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

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

    val tabBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(Res.drawable.ic_title),
                                contentDescription = null,
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Screen.CreateTrip.route) }) {
                            val addIconRes = when (currentThemeId) {
                                "theme_light" -> Res.drawable.ic_add_light
                                "theme_dark" -> Res.drawable.ic_add_dark
                                "theme_blue" -> Res.drawable.ic_add_blue
                                "theme_gold" -> Res.drawable.ic_add_gold
                                else -> Res.drawable.ic_add_light
                            }
                            Image(
                                painter = painterResource(addIconRes),
                                contentDescription = "Create Trip",
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
            }
        },
        bottomBar = {
            QuickAccessRow(
                currentThemeId = currentThemeId ?: "theme_light",
                onNewTrip = { navController.navigate(Screen.CreateTrip.route) },
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
                is HomeUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is HomeUiState.Empty -> EmptyTripsState(
                    currentThemeId = currentThemeId ?: "theme_light",
                    onCreateTrip = { navController.navigate(Screen.CreateTrip.route) }
                )
                is HomeUiState.Error -> {
                    val error = uiState as HomeUiState.Error
                    ErrorState(
                        currentThemeId = currentThemeId ?: "theme_light",
                        onRetry = { viewModel.refreshTrips() }
                    )
                }
                is HomeUiState.Success -> {
                    val state = uiState as HomeUiState.Success

                    HomeContent(
                        trips = state.trips,
                        recentlyEdited = state.recentlyEdited,
                        recentlyExported = state.recentlyExported,
                        navController = navController,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category -> viewModel.onCategorySelected(category) },
                        onTripClick = { tripId ->
                            viewModel.onTripSelected(tripId)
                            navController.navigate(Screen.TripDetails.route)
                        },
                        onEdit = { tripUi ->
                            viewModel.onTripSelected(tripUi.trip.id)
                            navController.navigate(Screen.Edit.route)
                        },
                        onExport = { tripUi -> viewModel.exportTrip(tripUi.trip.id) },
                        onFavorite = { tripUi -> viewModel.toggleFavorite(tripUi.trip.id) },
                        onDelete = { tripUi -> viewModel.deleteTrip(tripUi.trip.id) },
                        currentThemeId = currentThemeId ?: "theme_light"
                    )
                }
            }
        }
    }
}