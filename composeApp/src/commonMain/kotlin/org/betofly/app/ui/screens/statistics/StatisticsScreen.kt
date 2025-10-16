package org.betofly.app.ui.screens.statistics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import org.betofly.app.model.Trip
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.home.EmptyTripsState
import org.betofly.app.ui.screens.home.HomeScreen
import org.betofly.app.ui.screens.home.QuickAccessRow
import org.betofly.app.viewModel.HomeViewModel
import org.betofly.app.viewModel.SearchUiState
import org.betofly.app.viewModel.StatisticsPeriod
import org.betofly.app.viewModel.StatisticsUiState
import org.betofly.app.viewModel.StatisticsViewModel
import org.betofly.app.viewModel.TripStatistics
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavHostController,
    viewModel: StatisticsViewModel = koinInject(),
    themeRepository: ThemeRepository,
    homeViewModel: HomeViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val graphData by viewModel.graphData.collectAsState()
    val tripStats by viewModel.tripStats.collectAsState()
    val period by viewModel.period.collectAsState()
    val selectedTripId by viewModel.selectedTripId.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
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
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Statistics",
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
        },
        modifier = Modifier.shadow(0.dp),
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
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val periodBackgroundColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF00110D)
                        "theme_dark" -> Color(0xFF00110D)
                        "theme_blue" -> Color(0xFF060F22)
                        "theme_gold" -> Color(0xFF673001)
                        else -> Color(0xFF00110D)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(periodBackgroundColor, shape = RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatisticsPeriod.values().forEach { p ->
                                val selected = p == period
                                TextButton(
                                    onClick = { viewModel.setPeriod(p) },
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = p.name.replace("_", " "),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                item {
                    when (uiState) {
                        is StatisticsUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is StatisticsUiState.NoData -> EmptyTripsState(
                            currentThemeId = currentThemeId ?: "theme_light",
                            onCreateTrip = { navController.navigate(Screen.CreateTrip.route) }
                        )

                        is StatisticsUiState.Error -> {
                            Text(
                                "Error: ${(uiState as StatisticsUiState.Error).message}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is StatisticsUiState.Success -> {
                            val data = uiState as StatisticsUiState.Success
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricItem("Trips", data.tripsCount, currentThemeId ?: "theme_light")
                                MetricItem("Total Days", data.totalDays, currentThemeId ?: "theme_light")
                                MetricItem("Entries", data.entriesCount, currentThemeId ?: "theme_light")
                                MetricItem("Photos", data.photosCount, currentThemeId ?: "theme_light")
                                MetricItem("Places", data.placesCount, currentThemeId ?: "theme_light")
                                MetricItem("Avg Trip Progress", "${(data.avgTripProgress * 100).toInt()}%", currentThemeId ?: "theme_light")
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))

                    val backgroundColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF00110D)
                        "theme_dark" -> Color(0xFF00110D)
                        "theme_blue" -> Color(0xFF060F22)
                        "theme_gold" -> Color(0xFF673001)
                        else -> Color(0xFF00110D)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (allTrips.isNotEmpty()) {
                                var expanded by remember { mutableStateOf(false) }

                                Box {
                                    Button(
                                        onClick = { expanded = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = selectedTripId?.let { id ->
                                                allTrips.find { it.id == id }?.title
                                            } ?: "Select Trip",
                                            color = Color.White
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier
                                            .background(
                                                color = backgroundColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        allTrips.forEach { trip ->
                                            DropdownMenuItem(
                                                text = { Text(trip.title, color = Color.White) },
                                                onClick = {
                                                    viewModel.selectTrip(trip.id)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                selectedTripId?.let { id ->
                                    val tripTitle = allTrips.find { it.id == id }?.title ?: ""
                                    TextButton(
                                        onClick = {
                                            homeViewModel.onTripSelected(id)
                                            navController.navigate(Screen.TripDetails.route)
                                        }
                                    ) {
                                        Text(
                                            "View Trip: $tripTitle",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))

                    val backgroundColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF00110D)
                        "theme_dark" -> Color(0xFF00110D)
                        "theme_blue" -> Color(0xFF060F22)
                        "theme_gold" -> Color(0xFF673001)
                        else -> Color(0xFF00110D)
                    }

                    tripStats?.toUiModel()?.let { t ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Trip: ${t.tripTitle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                MetricItem("Route Length (km)", t.routeLength, currentThemeId ?: "theme_light")
                                MetricItem("Recording Duration", t.recordingDuration, currentThemeId ?: "theme_light")
                                MetricItem("Days Covered", t.daysCovered, currentThemeId ?: "theme_light")
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }

                item {
                    val backgroundColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF00110D)
                        "theme_dark" -> Color(0xFF00110D)
                        "theme_blue" -> Color(0xFF060F22)
                        "theme_gold" -> Color(0xFF673001)
                        else -> Color(0xFF00110D)
                    }

                    graphData?.let { g ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Statistics Chart",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )

                                val combinedData = linkedMapOf<String, Int>()
                                g.entriesPerDay.forEach { (date, count) ->
                                    combinedData["Day ${date.dayOfMonth}"] = count
                                }
                                g.topTags.forEach { (tag, count) ->
                                    combinedData["Tag: $tag"] = count
                                }
                                g.topCategories.forEach { (cat, count) ->
                                    combinedData["Cat: $cat"] = count
                                }

                                UnifiedBarChart(
                                    dataMap = combinedData,
                                    modifier = Modifier.fillMaxWidth(),
                                    textColor = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun UnifiedBarChart(
    dataMap: Map<String, Int>,
    modifier: Modifier = Modifier,
    maxBarHeight: Dp = 180.dp,
    textColor: Color = Color.White
) {
    if (dataMap.isEmpty()) {
        Text("No data available", modifier = Modifier.padding(16.dp), color = textColor)
        return
    }

    val maxValue = dataMap.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(maxBarHeight + 70.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dataMap.entries.toList()) { entry ->
            val label = entry.key
            val value = entry.value
            val barHeight = (value.toFloat() / maxValue) * maxBarHeight.value

            val barColor = when {
                label.startsWith("Day") -> MaterialTheme.colorScheme.primary
                label.startsWith("Tag") -> MaterialTheme.colorScheme.secondary
                label.startsWith("Cat") -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "$value",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(60.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .height(barHeight.dp)
                        .width(32.dp)
                        .shadow(4.dp, RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(barColor.copy(alpha = 0.8f), barColor)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}

@Composable
fun MetricItem(title: String, value: Any, currentThemeId: String) {
    // Цвет фона по теме
    val backgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF00110D)
        "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp) // фиксированная высота для табличного вида
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp), // внутренние отступы слева и справа
        verticalAlignment = Alignment.CenterVertically, // текст по центру по вертикали
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            value.toString(),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

data class TripStatisticsUi(
    val tripTitle: String,
    val routeLength: String,
    val recordingDuration: String,
    val daysCovered: String
)

fun TripStatistics.toUiModel(): TripStatisticsUi {
    return TripStatisticsUi(
        tripTitle = trip.title,
        routeLength = ((routeLength * 100).toInt() / 100f).toString(),
        recordingDuration = "${recordingDuration / 60} min",
        daysCovered = daysWithEntries.toString()
    )
}
