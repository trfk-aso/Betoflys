package org.betofly.app.ui.screens

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")

    data object Home : Screen("home")
    data object CreateTrip : Screen("create_trip")
    data object Search : Screen("search")
    data object TripDetails : Screen("trip_details")

    data object Edit : Screen("edit")
    data object Recording : Screen("recording")
    data object Favorites : Screen("favorites")

    data object Journal : Screen("journal")
    data object EditEntry: Screen("editEntry")
    data object Statistics: Screen("statistics")
    data object Settings: Screen("settings")
    data object About: Screen("about")
}
