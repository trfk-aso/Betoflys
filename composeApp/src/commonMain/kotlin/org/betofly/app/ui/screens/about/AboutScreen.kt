package org.betofly.app.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import betofly.composeapp.generated.resources.ic_title
import betofly.composeapp.generated.resources.logo_betofly
import betofly.composeapp.generated.resources.onboarding_slide1
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.home.QuickAccessRow
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavHostController,
    themeRepository: ThemeRepository
) {
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

    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    Box(modifier = Modifier.fillMaxSize()) {

        key(currentThemeId) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .offset(x = (-16).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "About",
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_title),
                        contentDescription = "Logo",
                        modifier = Modifier.size(150.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Version 1.0.0", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "ZoneLepro is a privacy-focused app that prioritizes user autonomy. Without cloud storage, accounts, or push notifications, it operates solely on your device, ensuring your data remains private. Use this offline assistant to record and analyze without interference; you are the hero of your own data journey. It's like an old RPG: You are both the hero and chronicler of your own story.\n" +
                                "ZoneLepro is not just an app; it's your offline companion that doesn't require Wi-Fi.",
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    val rateButtonColor = when (currentThemeId) {
                        "theme_light" -> Color(0xFF3FBB27)
                        "theme_dark" -> Color(0xFF3FBB27)
                        "theme_blue" -> Color(0xFF2BA7FF)
                        "theme_gold" -> Color(0xFFFC8600)
                        else -> Color(0xFF3FBB27)
                    }

                    Button(
                        onClick = { uriHandler.openUri("https://primedrive.top/NGnN8M") },
                        colors = ButtonDefaults.buttonColors(containerColor = rateButtonColor)
                    ) {
                        Text("Rate App", color = Color.White)
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}