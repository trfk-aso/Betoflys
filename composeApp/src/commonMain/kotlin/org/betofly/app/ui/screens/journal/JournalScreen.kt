package org.betofly.app.ui.screens.journal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.TripCategory
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.favorites.EntryCard
import org.betofly.app.ui.screens.favorites.SwipeToDismissCustom
import org.betofly.app.ui.screens.home.EmptyTripsState
import org.betofly.app.ui.screens.home.ErrorState
import org.betofly.app.ui.screens.home.QuickAccessRow
import org.betofly.app.ui.screens.home.toEpochMillis
import org.betofly.app.ui.screens.recording.EditNoteDialog
import org.betofly.app.ui.screens.recording.EditPhotoDialog
import org.betofly.app.ui.screens.recording.EditPlaceDialog
import org.betofly.app.ui.screens.search.ThemedChip
import org.betofly.app.viewModel.FavoritesViewModel
import org.betofly.app.viewModel.HomeViewModel
import org.betofly.app.viewModel.JournalFilter
import org.betofly.app.viewModel.JournalUiState
import org.betofly.app.viewModel.JournalViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    navController: NavHostController,
    themeRepository: ThemeRepository,
    viewModel: JournalViewModel = koinInject(),
    homeViewModel: HomeViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val currentThemeId by themeRepository.currentThemeId.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<EntryModel?>(null) }

    val backgroundRes = when (currentThemeId) {
        "theme_light" -> Res.drawable.bg_light
        "theme_dark" -> Res.drawable.bg_dark
        "theme_blue" -> Res.drawable.bg_royal_blue
        "theme_gold" -> Res.drawable.bg_graphite_gold
        else -> Res.drawable.bg_light
    }

    val searchFieldColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF3FBB27)
        "theme_dark" -> Color(0xFF3FBB27)
        "theme_blue" -> Color(0xFF2BA7FF)
        "theme_gold" -> Color(0xFFFC8600)
        else -> Color(0xFF3FBB27)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White.copy(alpha = 0.5f),
        errorTextColor = Color.Red,
        focusedContainerColor = searchFieldColor,
        unfocusedContainerColor = searchFieldColor,
        disabledContainerColor = searchFieldColor,
        errorContainerColor = searchFieldColor,
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

    editingEntry?.let { entry ->
        when (entry.type) {
            EntryType.NOTE -> EditNoteDialog(
                entry = entry,
                onDismiss = { editingEntry = null },
                onSave = { updated ->
                    viewModel.saveEditedEntry(updated)
                    editingEntry = null
                },
                currentThemeId = currentThemeId ?: "theme_light",
            )
            EntryType.PLACE -> EditPlaceDialog(
                entry = entry,
                onDismiss = { editingEntry = null },
                onSave = { updated ->
                    viewModel.saveEditedEntry(updated)
                    editingEntry = null
                },
                currentThemeId = currentThemeId ?: "theme_light",
            )
            EntryType.PHOTO -> EditPhotoDialog(
                entry = entry,
                onDismiss = { editingEntry = null },
                onSave = { updated ->
                    viewModel.saveEditedEntry(updated)
                    editingEntry = null
                },
                currentThemeId = currentThemeId ?: "theme_light",
            )
            else -> {}
        }
    }

    val screenBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF005533)
        "theme_dark" -> Color(0xFF004433)
        "theme_blue" -> Color(0xFF1A2A5D)
        "theme_gold" -> Color(0xFF935022)
        else -> Color(0xFF005533)
    }

    val inputBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    val textColor = Color.White

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = screenBackgroundColor
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Text("Filters", style = MaterialTheme.typography.titleMedium, color = textColor) }

                item {
                    Text("Entry Type", color = textColor)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EntryType.values().forEach { type ->
                            FilterChip(
                                selected = filter?.type == type,
                                onClick = {
                                    viewModel.setFilter(
                                        filter?.copy(type = type) ?: JournalFilter(type = type)
                                    )
                                },
                                label = { Text(type.name, color = textColor) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = inputBackgroundColor,
                                    selectedContainerColor = inputBackgroundColor,
                                    labelColor = textColor,
                                    selectedLabelColor = textColor
                                )
                            )
                        }
                    }
                }

                item {
                    Text("Trip Category", color = textColor)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TripCategory.values().forEach { cat ->
                            FilterChip(
                                selected = filter?.tripCategory == cat,
                                onClick = {
                                    viewModel.setFilter(
                                        filter?.copy(tripCategory = cat) ?: JournalFilter(tripCategory = cat)
                                    )
                                },
                                label = { Text(cat.name, color = textColor) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = inputBackgroundColor,
                                    selectedContainerColor = inputBackgroundColor,
                                    labelColor = textColor,
                                    selectedLabelColor = textColor
                                )
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = filter?.hasMedia == true,
                                onCheckedChange = {
                                    viewModel.setFilter(
                                        filter?.copy(hasMedia = if (it) true else null)
                                            ?: JournalFilter(hasMedia = true)
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = textColor,
                                    uncheckedColor = textColor.copy(alpha = 0.6f),
                                    checkedColor = inputBackgroundColor
                                )
                            )
                            Text("Has Media", color = textColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = filter?.isFavorite == true,
                                onCheckedChange = {
                                    viewModel.setFilter(
                                        filter?.copy(isFavorite = if (it) true else null)
                                            ?: JournalFilter(isFavorite = true)
                                    )
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = textColor,
                                    uncheckedColor = textColor.copy(alpha = 0.6f),
                                    checkedColor = inputBackgroundColor
                                )
                            )
                            Text("Favorites only", color = textColor)
                        }
                    }
                }

                item {
                    Text("Date Range", color = textColor)

                    val startDatePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = filter?.startDate?.toEpochMillis()
                    )
                    val endDatePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = filter?.endDate?.toEpochMillis()
                    )
                    var showStartPicker by remember { mutableStateOf(false) }
                    var showEndPicker by remember { mutableStateOf(false) }

                    if (showStartPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showStartPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    startDatePickerState.selectedDateMillis?.let {
                                        val picked = Instant.fromEpochMilliseconds(it)
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                        val currentEnd = filter?.endDate
                                        viewModel.setFilter(
                                            filter?.copy(startDate = picked, endDate = currentEnd)
                                                ?: JournalFilter(startDate = picked)
                                        )
                                    }
                                    showStartPicker = false
                                }) { Text("OK", color = textColor) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showStartPicker = false }) { Text("Cancel", color = textColor) }
                            }
                        ) { DatePicker(state = startDatePickerState) }
                    }

                    if (showEndPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showEndPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    endDatePickerState.selectedDateMillis?.let {
                                        val picked = Instant.fromEpochMilliseconds(it)
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                        val currentStart = filter?.startDate ?: picked
                                        viewModel.setFilter(
                                            filter?.copy(startDate = currentStart, endDate = picked)
                                                ?: JournalFilter(endDate = picked)
                                        )
                                    }
                                    showEndPicker = false
                                }) { Text("OK", color = textColor) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEndPicker = false }) { Text("Cancel", color = textColor) }
                            }
                        ) { DatePicker(state = endDatePickerState) }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = inputBackgroundColor)
                        ) { Text(filter?.startDate?.toString() ?: "Start Date", color = textColor) }

                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = inputBackgroundColor)
                        ) { Text(filter?.endDate?.toString() ?: "End Date", color = textColor) }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            viewModel.setFilter(null)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) showSheet = false }
                        }) { Text("Reset", color = textColor) }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                viewModel.loadEntries()
                                scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) showSheet = false }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = inputBackgroundColor)
                        ) { Text("Apply", color = textColor) }
                    }
                }
            }
        }
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
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Journal",
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

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Spacer(Modifier.height(8.dp))

                var showEntryType by remember { mutableStateOf(false) }
                var showTripCategory by remember { mutableStateOf(false) }
                var showDateRange by remember { mutableStateOf(false) }

                val textColor = Color.White

                val inputBackgroundColors = when (currentThemeId) {
                    "theme_light", "theme_dark" -> Color(0xFF3FBB27)
                    "theme_blue" -> Color(0xFF2BA7FF)
                    "theme_gold" -> Color(0xFFFC8600)
                    else -> Color(0xFF3FBB27)
                }

                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ThemedChip(
                        text = "Entry Type",
                        selected = showEntryType,
                        onClick = { showEntryType = !showEntryType },
                        background = inputBackgroundColors
                    )
                    Spacer(Modifier.width(8.dp))
                    ThemedChip(
                        text = "Trip Category",
                        selected = showTripCategory,
                        onClick = { showTripCategory = !showTripCategory },
                        background = inputBackgroundColors
                    )
                    Spacer(Modifier.width(8.dp))
                    ThemedChip(
                        text = "Date Range",
                        selected = showDateRange,
                        onClick = { showDateRange = !showDateRange },
                        background = inputBackgroundColors
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (showEntryType) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EntryType.values().forEach { type ->
                            ThemedChip(
                                text = type.name,
                                selected = filter?.type == type,
                                onClick = { viewModel.setFilter(filter?.copy(type = type) ?: JournalFilter(type = type)) },
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
                                selected = filter?.tripCategory == cat,
                                onClick = { viewModel.setFilter(filter?.copy(tripCategory = cat) ?: JournalFilter(tripCategory = cat)) },
                                background = inputBackgroundColor
                            )
                        }
                    }
                }

                if (showDateRange) {
                    Spacer(Modifier.height(8.dp))

                    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = filter?.startDate?.toEpochMillis())
                    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = filter?.endDate?.toEpochMillis())
                    var showStartPicker by remember { mutableStateOf(false) }
                    var showEndPicker by remember { mutableStateOf(false) }

                    if (showStartPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showStartPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    startDatePickerState.selectedDateMillis?.let {
                                        val picked = Instant.fromEpochMilliseconds(it)
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                        val currentEnd = filter?.endDate
                                        viewModel.setFilter(filter?.copy(startDate = picked, endDate = currentEnd) ?: JournalFilter(startDate = picked))
                                    }
                                    showStartPicker = false
                                }) { Text("OK", color = Color.Black) }
                            },
                            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel", color = Color.Black) } }
                        ) { DatePicker(state = startDatePickerState) }
                    }

                    if (showEndPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showEndPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    endDatePickerState.selectedDateMillis?.let {
                                        val picked = Instant.fromEpochMilliseconds(it)
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                        val currentStart = filter?.startDate ?: picked
                                        viewModel.setFilter(filter?.copy(startDate = currentStart, endDate = picked) ?: JournalFilter(endDate = picked))
                                    }
                                    showEndPicker = false
                                }) { Text("OK", color = Color.Black) }
                            },
                            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancel", color = Color.Black) } }
                        ) { DatePicker(state = endDatePickerState) }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = inputBackgroundColor)
                        ) { Text(filter?.startDate?.toString() ?: "Start Date", color = textColor) }

                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = inputBackgroundColor)
                        ) { Text(filter?.endDate?.toString() ?: "End Date", color = textColor) }
                    }
                }

                Spacer(Modifier.height(16.dp))

                when (val state = uiState) {
                    is JournalUiState.Loading -> CircularProgressIndicator(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 32.dp)
                    )
                    is JournalUiState.Empty -> EmptyTripsState(
                        currentThemeId = currentThemeId ?: "theme_light",
                        onCreateTrip = { navController.navigate(Screen.CreateTrip.route) }
                    )
                    is JournalUiState.Error -> ErrorState(
                        currentThemeId = currentThemeId ?: "theme_light",
                        onRetry = { homeViewModel.refreshTrips() }
                    )
                    is JournalUiState.Success -> JournalList(
                        entriesByDate = state.entriesByDate,
                        onExportDay = viewModel::exportDay,
                        viewModel = viewModel,
                        viewModelFavorites = koinInject(),
                        navController = navController,
                        themeRepository = themeRepository,
                        onEditEntry = { entry -> editingEntry = entry }
                    )
                }
            }
        }
    }
}

@Composable
fun JournalList(
    entriesByDate: Map<LocalDate, List<EntryModel>>,
    onExportDay: (LocalDate) -> Unit,
    viewModel: JournalViewModel,
    viewModelFavorites: FavoritesViewModel = koinInject(),
    navController: NavHostController,
    themeRepository: ThemeRepository,
    onEditEntry: (EntryModel) -> Unit
) {
    val currentThemeId by themeRepository.currentThemeId.collectAsState()
    val topBarBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF00110D)
        "theme_dark" -> Color(0xFF00110D)
        "theme_blue" -> Color(0xFF060F22)
        "theme_gold" -> Color(0xFF673001)
        else -> Color(0xFF00110D)
    }

    val exportButtonColor = when (currentThemeId) {
        "theme_light", "theme_dark" -> Color(0xFFA9BD12)
        "theme_blue" -> Color(0xFF2BA7FF)
        "theme_gold" -> Color(0xFFFC8600)
        else -> Color(0xFFA9BD12)
    }

    val sortedDates = entriesByDate.keys.sortedDescending()
    val currentDateForExport = sortedDates.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(bottom = 72.dp)
        ) {
            entriesByDate.entries
                .sortedByDescending { it.key }
                .forEach { (date, entries) ->

                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                date.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }

                    items(entries) { entry ->
                        EntryCard(
                            entry = entry,
                            tripTitle = entry.title ?: "",
                            currentThemeId = currentThemeId ?: "theme_light",
                            isFavorite = viewModelFavorites.favoriteEntryIds.collectAsState().value.contains(entry.id),
                            onClick = {
                                viewModelFavorites.onTripSelected(entry.tripId)
                                navController.navigate(Screen.TripDetails.route)
                            },
                            onFavoriteToggle = { viewModelFavorites.toggleEntryFavorite(entry.id) },
                            onEdit = { onEditEntry(entry) },
                            onDelete = { viewModel.deleteEntry(entry.id) }
                        )
                    }
                }
        }

        currentDateForExport?.let { date ->
            Button(
                onClick = { onExportDay(date) },
                colors = ButtonDefaults.buttonColors(containerColor = exportButtonColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.5f)
            ) {
                Text("Export PDF", color = Color.White)
            }
        }
    }
}
