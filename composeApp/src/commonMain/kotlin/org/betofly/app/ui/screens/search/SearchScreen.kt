package org.betofly.app.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.bg_dark
import betofly.composeapp.generated.resources.bg_graphite_gold
import betofly.composeapp.generated.resources.bg_light
import betofly.composeapp.generated.resources.bg_royal_blue
import betofly.composeapp.generated.resources.favorite_blue
import betofly.composeapp.generated.resources.favorite_dark
import betofly.composeapp.generated.resources.favorite_gold
import betofly.composeapp.generated.resources.favorite_light
import betofly.composeapp.generated.resources.ic_back_blue
import betofly.composeapp.generated.resources.ic_back_dark
import betofly.composeapp.generated.resources.ic_back_gold
import betofly.composeapp.generated.resources.ic_back_light
import betofly.composeapp.generated.resources.ic_empty_state_blue
import betofly.composeapp.generated.resources.ic_empty_state_dark
import betofly.composeapp.generated.resources.ic_empty_state_gold
import betofly.composeapp.generated.resources.ic_empty_state_light
import betofly.composeapp.generated.resources.ic_search_blue
import betofly.composeapp.generated.resources.ic_search_dark
import betofly.composeapp.generated.resources.ic_search_gold
import betofly.composeapp.generated.resources.ic_search_light
import betofly.composeapp.generated.resources.ic_settings_blue
import betofly.composeapp.generated.resources.ic_settings_dark
import betofly.composeapp.generated.resources.ic_settings_gold
import betofly.composeapp.generated.resources.ic_settings_light
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.model.EntryType
import org.betofly.app.model.Trip
import org.betofly.app.model.TripCategory
import org.betofly.app.model.TripUiModel
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.home.EmptyTripsState
import org.betofly.app.ui.screens.home.QuickAccessRow
import org.betofly.app.ui.screens.home.toEpochMillis
import org.betofly.app.viewModel.SearchType
import org.betofly.app.viewModel.SearchUiState
import org.betofly.app.viewModel.SearchViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    themeRepository: ThemeRepository,
    viewModel: SearchViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val recent by viewModel.recent.collectAsState()
    val currentSort = filters.sortOption
    val currentThemeId by themeRepository.currentThemeId.collectAsState()

    var showEntryType by remember { mutableStateOf(false) }
    var showTripCategory by remember { mutableStateOf(false) }
    var showFlags by remember { mutableStateOf(false) }
    var showDateRange by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val backgroundRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.bg_light
        "theme_dark" -> Res.drawable.bg_dark
        "theme_blue" -> Res.drawable.bg_royal_blue
        "theme_gold" -> Res.drawable.bg_graphite_gold
        else -> Res.drawable.bg_light
    }

    val searchFieldColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFF3FBB27)
        "theme_blue" -> Color(0xFF2BA7FF)
        "theme_gold" -> Color(0xFFFC8600)
        else -> Color(0xFF3FBB27)
    }

    val topBarBackgroundColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    val screenBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF005533)
        "theme_dark" -> Color(0xFF004433)
        "theme_blue" -> Color(0xFF1A2A5D)
        "theme_gold" -> Color(0xFF935022)
        else -> Color(0xFF005533)
    }

    val inputBackgroundColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    val textColor = Color.White

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Search",
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

            Column(Modifier.fillMaxSize().padding(16.dp)) {

                CustomSearchBar(
                    query = filters.query ?: "",
                    onQueryChange = { viewModel.updateFilters(filters.copy(query = it)) },
                    repository = themeRepository,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemedChip(
                        text = "Trips",
                        selected = filters.type == SearchType.TRIPS,
                        onClick = { viewModel.updateFilters(filters.copy(type = SearchType.TRIPS)) },
                        background = searchFieldColor
                    )
                    ThemedChip(
                        text = "Entries",
                        selected = filters.type == SearchType.ENTRIES,
                        onClick = { viewModel.updateFilters(filters.copy(type = SearchType.ENTRIES)) },
                        background = searchFieldColor
                    )
                    ThemedChip(
                        text = "Entry Type",
                        selected = showEntryType,
                        onClick = { showEntryType = !showEntryType },
                        background = searchFieldColor
                    )
                    ThemedChip(
                        text = "Trip Category",
                        selected = showTripCategory,
                        onClick = { showTripCategory = !showTripCategory },
                        background = searchFieldColor
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val favoritesIconRes = when (currentThemeId) {
                        "theme_light" -> Res.drawable.favorite_light
                        "theme_dark" -> Res.drawable.favorite_dark
                        "theme_blue" -> Res.drawable.favorite_blue
                        "theme_gold" -> Res.drawable.favorite_gold
                        else -> Res.drawable.favorite_light
                    }

                    IconButton(
                        onClick = {
                            viewModel.updateFilters(
                                filters.copy(isFavorite = if (filters.isFavorite == true) null else true)
                            )
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(favoritesIconRes),
                            contentDescription = "Favorites",
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    ThemedChip(
                        text = "Date Range",
                        selected = showDateRange,
                        onClick = { showDateRange = !showDateRange },
                        background = searchFieldColor
                    )
                }

                if (showEntryType) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EntryType.values().forEach { type ->
                            ThemedChip(
                                text = type.name,
                                selected = filters.entryType == type,
                                onClick = { viewModel.updateFilters(filters.copy(entryType = type)) },
                                background = inputBackgroundColor
                            )
                        }
                    }
                }

                if (showTripCategory) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TripCategory.values().forEach { cat ->
                            ThemedChip(
                                text = cat.name,
                                selected = filters.category == cat,
                                onClick = { viewModel.updateFilters(filters.copy(category = cat)) },
                                background = inputBackgroundColor
                            )
                        }
                    }
                }

                if (showDateRange) {
                    Spacer(Modifier.height(8.dp))

                    val startDatePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = filters.dateRange?.start?.toEpochMillis()
                    )
                    val endDatePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = filters.dateRange?.endInclusive?.toEpochMillis()
                    )

                    var showStartPicker by remember { mutableStateOf(false) }
                    var showEndPicker by remember { mutableStateOf(false) }

                    if (showStartPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showStartPicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        startDatePickerState.selectedDateMillis?.let {
                                            val picked = Instant.fromEpochMilliseconds(it)
                                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                            val currentEnd = filters.dateRange?.endInclusive
                                            viewModel.updateFilters(
                                                filters.copy(dateRange = picked..(currentEnd ?: picked))
                                            )
                                        }
                                        showStartPicker = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showStartPicker = false },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = startDatePickerState)
                        }
                    }

                    if (showEndPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showEndPicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        endDatePickerState.selectedDateMillis?.let {
                                            val picked = Instant.fromEpochMilliseconds(it)
                                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                            val currentStart = filters.dateRange?.start ?: picked
                                            viewModel.updateFilters(
                                                filters.copy(dateRange = currentStart..picked)
                                            )
                                        }
                                        showEndPicker = false
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showEndPicker = false },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = endDatePickerState)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = inputBackgroundColor)
                        ) {
                            Text(filters.dateRange?.start?.toString() ?: "Start Date", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = inputBackgroundColor)
                        ) {
                            Text(filters.dateRange?.endInclusive?.toString() ?: "End Date", color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (recent.isNotEmpty() && filters.query.isNullOrBlank()) {
                    Text("Recent:", Modifier.padding(bottom = 4.dp), color = Color.White)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recent.take(5)) { q ->
                            ThemedAssistChip(
                                text = q,
                                onClick = { viewModel.updateFilters(filters.copy(query = q)) },
                                background = searchFieldColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                when (val state = uiState) {
                    is SearchUiState.Idle -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Start typing to search…", color = Color.White)
                    }
                    is SearchUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    is SearchUiState.Empty -> SearchEmptyState(currentThemeId ?: "theme_light")
                    is SearchUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.White)
                    }
                    is SearchUiState.SuccessTrips -> {
                        val tripUiModels = state.trips.map { it.toUiModel() }
                        TripGrid(
                            trips = tripUiModels,
                            currentThemeId = currentThemeId ?: "theme_light",
                            onClick = {
                                viewModel.onTripSelected(it.trip.id)
                                navController.navigate(Screen.TripDetails.route)
                            }
                        )
                    }
                    is SearchUiState.SuccessEntries -> {
                        EntryList(
                            entries = state.entries,
                            trips = state.trips,
                            onClick = { tripId ->
                                viewModel.onTripSelected(tripId)
                                navController.navigate(Screen.TripDetails.route)
                            },
                            onSortChange = { sortOption -> viewModel.applySort(sortOption) },
                            currentSort = currentSort,
                            themeRepository = themeRepository
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemedChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    background: Color
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, color = Color.White) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = background,
            selectedContainerColor = background.copy(alpha = 0.9f),
            labelColor = Color.White,
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(40.dp)
    )
}

@Composable
fun ThemedAssistChip(
    text: String,
    onClick: () -> Unit,
    background: Color
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, color = Color.White) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = background,
            labelColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(40.dp)
    )
}

fun Trip.toUiModel(
    hasRoute: Boolean = false,
    isFavorite: Boolean = false,
    photoCount: Int = 0,
    noteCount: Int = 0
): TripUiModel {
    return TripUiModel(
        trip = this,
        progress = progress,
        photoCount = photoCount,
        noteCount = noteCount,
        hasRoute = hasRoute,
        isFavorite = isFavorite,
        lastExportedAt = lastExportedAt?.toString(),
    )
}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "titles/notes/tags",
    repository: ThemeRepository,
    modifier: Modifier = Modifier
) {
    val currentThemeId by repository.currentThemeId.collectAsState()

    val backgroundColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFF3FBB27)
        "theme_blue" -> Color(0xFF2BA7FF)
        "theme_gold" -> Color(0xFFFC8600)
        else -> Color(0xFF3FBB27)
    }

    val searchIconRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.ic_search_light
        "theme_dark" -> Res.drawable.ic_search_dark
        "theme_blue" -> Res.drawable.ic_search_blue
        "theme_gold" -> Res.drawable.ic_search_gold
        else -> Res.drawable.ic_search_light
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(searchIconRes),
                contentDescription = "Search",
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    fontSize = 18.sp
                ),
                cursorBrush = SolidColor(Color.Black),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SearchEmptyState(
    currentThemeId: String,
    modifier: Modifier = Modifier
) {
    val emptyIconRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.ic_empty_state_light
        "theme_dark" -> Res.drawable.ic_empty_state_dark
        "theme_blue" -> Res.drawable.ic_empty_state_blue
        "theme_gold" -> Res.drawable.ic_empty_state_gold
        else -> Res.drawable.ic_empty_state_light
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(25.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(emptyIconRes),
            contentDescription = null,
            modifier = Modifier.size(250.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedDatePickerDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    initialDate: Instant?,
    onDateSelected: (LocalDate) -> Unit,
    currentThemeId: String
) {
    if (!show) return

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate?.toEpochMilliseconds())

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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = screenBackgroundColor,
        title = { Text("Select Date", color = Color.White) },
        text = {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = inputBackgroundColor,
                    titleContentColor = Color.White,
                    weekdayContentColor = Color.White,
                    selectedDayContainerColor = Color(0xFF007700),
                    selectedDayContentColor = Color.White,
                    todayContentColor = Color.White,
                    todayDateBorderColor = Color.White,
                    dayContentColor = Color.White,
                    disabledDayContentColor = Color.White.copy(alpha = 0.5f)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val picked = Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    onDateSelected(picked)
                }
                onDismiss()
            }) {
                Text("OK", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

