package org.betofly.app.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.about_blue
import betofly.composeapp.generated.resources.about_gold
import betofly.composeapp.generated.resources.about_light
import betofly.composeapp.generated.resources.bg_dark
import betofly.composeapp.generated.resources.bg_graphite_gold
import betofly.composeapp.generated.resources.bg_light
import betofly.composeapp.generated.resources.bg_royal_blue
import betofly.composeapp.generated.resources.ic_back_blue
import betofly.composeapp.generated.resources.ic_back_dark
import betofly.composeapp.generated.resources.ic_back_gold
import betofly.composeapp.generated.resources.ic_back_light
import betofly.composeapp.generated.resources.theme_blue
import betofly.composeapp.generated.resources.theme_blue_purchased
import betofly.composeapp.generated.resources.theme_dark
import betofly.composeapp.generated.resources.theme_gold
import betofly.composeapp.generated.resources.theme_gold_purchased
import betofly.composeapp.generated.resources.theme_light
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.ui.screens.Screen
import org.betofly.app.ui.screens.home.QuickAccessRow
import org.betofly.app.viewModel.AppTheme
import org.betofly.app.viewModel.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavHostController,
    themeRepository: ThemeRepository
) {
    val uiState by viewModel.uiState.collectAsState()
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

    val textColor = Color.White

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth().offset(x = (-16).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Settings",
                            color = textColor,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            key(currentThemeId) {
                Image(
                    painter = painterResource(backgroundRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {

                item {
                    Text(
                        "Appearance",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                items(uiState.themes.chunked(2)) { rowThemes ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowThemes.forEach { theme ->
                            val isPurchased = theme.isPurchased

                            val themeImageRes = when (theme.id) {
                                "theme_light" -> Res.drawable.theme_light
                                "theme_dark" -> Res.drawable.theme_dark
                                "theme_blue" -> if (isPurchased) Res.drawable.theme_blue else Res.drawable.theme_blue_purchased
                                "theme_gold" -> if (isPurchased) Res.drawable.theme_gold else Res.drawable.theme_gold_purchased
                                else -> Res.drawable.theme_light
                            }

                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable { viewModel.selectTheme(theme) },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(themeImageRes),
                                    contentDescription = theme.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item {
                    Text(
                        "Data",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                val (exportColor, importColor, resetColor) = Triple(Color(0xFF3FBB27), Color(0xFFA9BD12), Color(0xFFBD1212))
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportAll() },
                            enabled = !uiState.isExporting,
                            modifier = Modifier.fillMaxWidth(0.7f),
                            colors = ButtonDefaults.buttonColors(containerColor = exportColor)
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Export All", color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.importZip() },
                            enabled = !uiState.isImporting,
                            modifier = Modifier.fillMaxWidth(0.7f),
                            colors = ButtonDefaults.buttonColors(containerColor = importColor)
                        ) {
                            if (uiState.isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Import", color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.showResetConfirm() },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            colors = ButtonDefaults.buttonColors(containerColor = resetColor)
                        ) {
                            Text("Reset All", color = Color.White)
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item {
                    Text(
                        "About",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable { navController.navigate("about") },
                        contentAlignment = Alignment.Center
                    ) {
                        val aboutImageRes = when (currentThemeId) {
                            "theme_light", "theme_dark" -> Res.drawable.about_light
                            "theme_blue" -> Res.drawable.about_blue
                            "theme_gold" -> Res.drawable.about_gold
                            else -> Res.drawable.about_light
                        }
                        Image(
                            painter = painterResource(aboutImageRes),
                            contentDescription = "About",
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }

    if (uiState.showResetConfirmDialog) {
        val cardBackgroundColor = when (currentThemeId) {
            "theme_light" -> Color(0xFF003322)
            "theme_dark" -> Color(0xFF003322)
            "theme_blue" -> Color(0xFF0A1A3D)
            "theme_gold" -> Color(0xFF814011)
            else -> Color(0xFF003322)
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissResetConfirm() },
            confirmButton = {
                TextButton(onClick = { viewModel.resetAll() }) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetConfirm() }) {
                    Text("Cancel", color = Color.White)
                }
            },
            title = { Text("Reset All Data", color = Color.White) },
            text = {
                Text(
                    "This will delete all your trips, entries, and settings. Are you sure?",
                    color = Color.White
                )
            },
            containerColor = cardBackgroundColor
        )
    }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SettingsScreen(
//    viewModel: SettingsViewModel,
//    navController: NavHostController,
//    themeRepository: ThemeRepository
//) {
//    val uiState by viewModel.uiState.collectAsState()
//    val currentThemeId by themeRepository.currentThemeId.collectAsState()
//
//    val backgroundRes = when (currentThemeId) {
//        "theme_light" -> Res.drawable.bg_light
//        "theme_dark" -> Res.drawable.bg_dark
//        "theme_blue" -> Res.drawable.bg_royal_blue
//        "theme_gold" -> Res.drawable.bg_graphite_gold
//        else -> Res.drawable.bg_light
//    }
//
//    val backgroundColor = when (currentThemeId) {
//        "theme_light" -> Color(0xFF00110D)
//        "theme_dark" -> Color(0xFF00110D)
//        "theme_blue" -> Color(0xFF060F22)
//        "theme_gold" -> Color(0xFF673001)
//        else -> Color(0xFF00110D)
//    }
//
//    Scaffold(
//        contentWindowInsets = WindowInsets(0, 0, 0, 0),
//        topBar = {
//            TopAppBar(
//                title = {
//                    Box(
//                        Modifier
//                            .fillMaxWidth()
//                            .offset(x = (-16).dp), // подвинуть немного влево
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "Settings",
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 24.sp
//                        )
//                    }
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        val backIconRes = when (currentThemeId) {
//                            "theme_light" -> Res.drawable.ic_back_light
//                            "theme_dark" -> Res.drawable.ic_back_dark
//                            "theme_blue" -> Res.drawable.ic_back_blue
//                            "theme_gold" -> Res.drawable.ic_back_gold
//                            else -> Res.drawable.ic_back_light
//                        }
//                        Image(
//                            painter = painterResource(backIconRes),
//                            contentDescription = "Back",
//                            modifier = Modifier.size(32.dp)
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
//            )
//        },
//        bottomBar = {
//            QuickAccessRow(
//                currentThemeId = currentThemeId ?: "theme_light",
//                onNewTrip = { navController.navigate(Screen.Home.route) },
//                onJournal = { navController.navigate(Screen.Journal.route) },
//                onSearch = { navController.navigate(Screen.Search.route) },
//                onFavorites = { navController.navigate(Screen.Favorites.route) },
//                onStatistics = { navController.navigate(Screen.Statistics.route) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .shadow(4.dp)
//            )
//        }
//    ) { padding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            key(currentThemeId) {
//                Image(
//                    painter = painterResource(backgroundRes),
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(6.dp),
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxSize()
//            ) {
//                item {
//                    Box(
//                        modifier = Modifier.fillMaxWidth(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "Appearance",
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold,
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                    }
//
//                    Spacer(Modifier.height(8.dp))
//
//                    Column(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//                        // Разбиваем список на строки по 2 элемента
//                        uiState.themes.chunked(2).forEach { rowThemes ->
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                rowThemes.forEach { theme ->
//                                    val isSelected = theme.id == uiState.currentThemeId
//                                    val isPurchased = theme.id in listOf("theme_blue", "theme_gold")
//
//                                    val themeImageRes = when (theme.id) {
//                                        "theme_light" -> Res.drawable.theme_light
//                                        "theme_dark" -> Res.drawable.theme_dark
//                                        "theme_blue" -> if (isPurchased) Res.drawable.theme_blue_purchased else Res.drawable.theme_blue
//                                        "theme_gold" -> if (isPurchased) Res.drawable.theme_gold_purchased else Res.drawable.theme_gold
//                                        else -> Res.drawable.theme_light
//                                    }
//
//                                    Box(
//                                        modifier = Modifier
//                                            .size(100.dp)
//                                            .clickable { viewModel.selectTheme(theme) },
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Image(
//                                            painter = painterResource(themeImageRes),
//                                            contentDescription = theme.name,
//                                            modifier = Modifier.fillMaxSize(),
//                                            contentScale = ContentScale.Fit
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // Остальные пункты Data / About оставляем без изменений
//                item { Spacer(Modifier.height(24.dp)) }
//                item {
//                    val (exportColor, importColor, resetColor) = when (currentThemeId) {
//                        "theme_light", "theme_dark" -> Triple(Color(0xFF3FBB27), Color(0xFFA9BD12), Color(0xFFBD1212))
//                        "theme_blue" -> Triple(Color(0xFF2BA7FF), Color(0xFFF3A30F), Color(0xFFBD1212))
//                        "theme_gold" -> Triple(Color(0xFFFC8600), Color(0xFFF3A30F), Color(0xFFBD1212))
//                        else -> Triple(Color(0xFF3FBB27), Color(0xFFA9BD12), Color(0xFFBD1212))
//                    }
//
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 24.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Text(
//                            "Data",
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold,
//                            style = MaterialTheme.typography.titleMedium
//                        )
//
//                        // Export All
//                        Button(
//                            onClick = { viewModel.exportAll() },
//                            enabled = !uiState.isExporting,
//                            modifier = Modifier.fillMaxWidth(0.7f),
//                            colors = ButtonDefaults.buttonColors(containerColor = exportColor)
//                        ) {
//                            if (uiState.isExporting) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(16.dp),
//                                    color = Color.White,
//                                    strokeWidth = 2.dp
//                                )
//                                Spacer(Modifier.width(8.dp))
//                            }
//                            Text("Export All", color = Color.White)
//                        }
//
//                        // Import
//                        Button(
//                            onClick = { viewModel.importZip() },
//                            enabled = !uiState.isImporting,
//                            modifier = Modifier.fillMaxWidth(0.7f),
//                            colors = ButtonDefaults.buttonColors(containerColor = importColor)
//                        ) {
//                            if (uiState.isImporting) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(16.dp),
//                                    color = Color.White,
//                                    strokeWidth = 2.dp
//                                )
//                                Spacer(Modifier.width(8.dp))
//                            }
//                            Text("Import", color = Color.White)
//                        }
//
//                        // Reset All
//                        Button(
//                            onClick = { viewModel.showResetConfirm() },
//                            modifier = Modifier.fillMaxWidth(0.7f),
//                            colors = ButtonDefaults.buttonColors(containerColor = resetColor)
//                        ) {
//                            Text("Reset All", color = Color.White)
//                        }
//                    }
//                }
//
//                item { Spacer(Modifier.height(24.dp)) }
//
//                item {
//                    val aboutImageRes = when (currentThemeId) {
//                        "theme_light", "theme_dark" -> Res.drawable.about_light
//                        "theme_blue" -> Res.drawable.about_blue
//                        "theme_gold" -> Res.drawable.about_gold
//                        else -> Res.drawable.about_light
//                    }
//
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()                 // Box на всю ширину
//                            .height(80.dp)                 // задаем высоту кнопки
//                            .clickable { navController.navigate("about") },
//                        contentAlignment = Alignment.Center // картинка по центру
//                    ) {
//                        Image(
//                            painter = painterResource(aboutImageRes),
//                            contentDescription = "About",
//                            modifier = Modifier
//                                .fillMaxWidth(0.6f)         // картинка занимает 70% ширины
//                                .fillMaxHeight(),            // растягиваем по высоте Box
//                            contentScale = ContentScale.Fit // сохраняем пропорции
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    if (uiState.showResetConfirmDialog) {
//        val cardBackgroundColor = when (currentThemeId) {
//            "theme_light" -> Color(0xFF003322)
//            "theme_dark" -> Color(0xFF003322)
//            "theme_blue" -> Color(0xFF0A1A3D)
//            "theme_gold" -> Color(0xFF814011)
//            else -> Color(0xFF003322)
//        }
//
//        AlertDialog(
//            onDismissRequest = { viewModel.dismissResetConfirm() },
//            confirmButton = {
//                TextButton(onClick = { viewModel.resetAll() }) {
//                    Text("Confirm", color = Color.White)
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { viewModel.dismissResetConfirm() }) {
//                    Text("Cancel", color = Color.White)
//                }
//            },
//            title = { Text("Reset All Data", color = Color.White) },
//            text = { Text("This will delete all your trips, entries, and settings. Are you sure?", color = Color.White) },
//            containerColor = cardBackgroundColor // <-- фон диалога
//        )
//    }
//}
