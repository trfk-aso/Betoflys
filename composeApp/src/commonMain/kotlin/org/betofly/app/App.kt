package org.betofly.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.about.AboutScreen
import org.betofly.app.ui.screens.favorites.EditEntryScreen
import org.betofly.app.ui.screens.favorites.FavoritesScreen
import org.betofly.app.ui.screens.home.CreateTripScreen
import org.betofly.app.ui.screens.home.EditTripScreen
import org.betofly.app.ui.screens.home.HomeScreen
import org.betofly.app.ui.screens.home.TripDetailsScreen
import org.betofly.app.ui.screens.journal.JournalScreen
import org.betofly.app.ui.screens.onboarding.OnboardingScreen
import org.betofly.app.ui.screens.recording.RecordingScreen
import org.betofly.app.ui.screens.search.SearchScreen
import org.betofly.app.ui.screens.settings.SettingsScreen
import org.betofly.app.ui.screens.splash.SplashScreen
import org.betofly.app.ui.screens.statistics.StatisticsScreen
import org.betofly.app.viewModel.FavoritesViewModel
import org.betofly.app.viewModel.JournalViewModel
import org.betofly.app.viewModel.RecordingViewModel
import org.betofly.app.viewModel.SettingsViewModel
import org.betofly.app.viewModel.StatisticsViewModel
import org.betofly.app.viewModel.TripDetailsViewModel
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun App(
    themeRepository: ThemeRepository,
    billingRepository: BillingRepository
) {
    LaunchedEffect(Unit) {
        println("Initializing themes in App()...")
        themeRepository.initializeThemes()
        println("Themes initialized, currentThemeId=${themeRepository.currentThemeId.value}")
    }

    val navController = rememberNavController()
    val viewModelFavorites: FavoritesViewModel = koinInject()
    val viewModelJournal: JournalViewModel = koinInject()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController, themeRepository) }
        composable(Screen.CreateTrip.route) {
            val currentThemeId by themeRepository.currentThemeId.collectAsState()
            CreateTripScreen(
                navController = navController,
                currentThemeId = currentThemeId ?: "theme_light"
            )
        }
        composable(Screen.Search.route) { SearchScreen(navController, themeRepository) }
        composable(Screen.TripDetails.route) { TripDetailsScreen(navController) }
        composable(Screen.Edit.route) {
            val tripDetailsViewModel: TripDetailsViewModel = koinInject()
            val currentThemeId by themeRepository.currentThemeId.collectAsState()
            EditTripScreen(
                tripId = tripDetailsViewModel.selectedTripId ?: 0L,
                navController = navController,
                currentThemeId = currentThemeId ?: "theme_light"
            )
        }
        composable(Screen.Recording.route) {
            val recordingViewModel: RecordingViewModel = koinInject()
            RecordingScreen(
                viewModel = recordingViewModel,
                navController = navController
            )
        }
        composable(Screen.Favorites.route) { FavoritesScreen(navController, viewModelFavorites, themeRepository) }
        composable(Screen.Journal.route) { JournalScreen(navController, themeRepository) }
        composable(Screen.EditEntry.route) { EditEntryScreen(navController,viewModelJournal, themeRepository) }
        composable(Screen.Statistics.route) {
            val statisticsViewModel: StatisticsViewModel = koinInject()
            StatisticsScreen(navController, statisticsViewModel, themeRepository)
        }
        composable(Screen.Settings.route) {
            val settingsViewModel = remember {
                SettingsViewModel(
                    settingsRepository = getKoin().get(),
                    tripRepository = getKoin().get(),
                    entryRepository = getKoin().get(),
                    backupStorage = getKoin().get(),
                    themeRepository = getKoin().get(),
                    billingRepository = billingRepository
                )
            }
            SettingsScreen(settingsViewModel, navController, themeRepository)
        }
        composable(Screen.About.route) { AboutScreen(navController, themeRepository) }
    }
}
