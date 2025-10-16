package org.betofly.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.btn_create_blue
import betofly.composeapp.generated.resources.btn_create_gold
import betofly.composeapp.generated.resources.btn_create_light_dark
import betofly.composeapp.generated.resources.btn_retry_blue
import betofly.composeapp.generated.resources.btn_retry_gold
import betofly.composeapp.generated.resources.btn_retry_light_dark
import betofly.composeapp.generated.resources.img_empty_trips_blue
import betofly.composeapp.generated.resources.img_empty_trips_gold
import betofly.composeapp.generated.resources.img_empty_trips_light_dark
import betofly.composeapp.generated.resources.img_error_blue
import betofly.composeapp.generated.resources.img_error_gold
import betofly.composeapp.generated.resources.img_error_light_dark
import org.jetbrains.compose.resources.painterResource

@Composable
fun EmptyTripsState(
    currentThemeId: String,
    onCreateTrip: () -> Unit
) {
    val backgroundImage = when (currentThemeId) {
        "theme_light", "theme_dark" -> Res.drawable.img_empty_trips_light_dark
        "theme_blue" -> Res.drawable.img_empty_trips_blue
        "theme_gold" -> Res.drawable.img_empty_trips_gold
        else -> Res.drawable.img_empty_trips_light_dark
    }

    val buttonImage = when (currentThemeId) {
        "theme_light", "theme_dark" -> Res.drawable.btn_create_light_dark
        "theme_blue" -> Res.drawable.btn_create_blue
        "theme_gold" -> Res.drawable.btn_create_gold
        else -> Res.drawable.btn_create_light_dark
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(Modifier.height(48.dp))
        Image(
            painter = painterResource(buttonImage),
            contentDescription = "Create your first Trip",
            modifier = Modifier
                .size(200.dp, 70.dp)
                .clickable(onClick = onCreateTrip)
        )
    }
}

@Composable
fun ErrorState(
    currentThemeId: String,
    onRetry: () -> Unit
) {
    val errorImage = when (currentThemeId) {
        "theme_light", "theme_dark" -> Res.drawable.img_error_light_dark
        "theme_blue" -> Res.drawable.img_error_blue
        "theme_gold" -> Res.drawable.img_error_gold
        else -> Res.drawable.img_error_light_dark
    }

    val retryButtonImage = when (currentThemeId) {
        "theme_light", "theme_dark" -> Res.drawable.btn_retry_light_dark
        "theme_blue" -> Res.drawable.btn_retry_blue
        "theme_gold" -> Res.drawable.btn_retry_gold
        else -> Res.drawable.btn_retry_light_dark
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(errorImage),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(Modifier.height(48.dp))
        Image(
            painter = painterResource(retryButtonImage),
            contentDescription = "Retry",
            modifier = Modifier
                .size(200.dp, 70.dp)
                .clickable(onClick = onRetry)
        )
    }
}
