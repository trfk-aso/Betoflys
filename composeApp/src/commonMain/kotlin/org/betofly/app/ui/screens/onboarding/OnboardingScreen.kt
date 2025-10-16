package org.betofly.app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.btn_continue
import betofly.composeapp.generated.resources.btn_get_started
import betofly.composeapp.generated.resources.btn_skip
import betofly.composeapp.generated.resources.onboarding_slide1
import betofly.composeapp.generated.resources.onboarding_slide2
import betofly.composeapp.generated.resources.onboarding_slide3
import kotlinx.coroutines.launch
import org.betofly.app.ui.screens.Screen
import org.jetbrains.compose.resources.painterResource

@Composable
fun OnboardingScreen(navController: NavHostController) {
    val pages = listOf(
        Res.drawable.onboarding_slide1,
        Res.drawable.onboarding_slide2,
        Res.drawable.onboarding_slide3
    )
    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState) { page ->
            Image(
                painter = painterResource(pages[page]),
                contentDescription = "Onboarding Slide ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Image(
            painter = painterResource(Res.drawable.btn_skip),
            contentDescription = "Skip",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 2.dp)
                .size(width = 120.dp, height = 50.dp)
                .clickable {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White else Color.Gray.copy(alpha = 0.6f)
                        )
                )
            }
        }

        val isLastPage = pagerState.currentPage == pages.lastIndex

        Image(
            painter = painterResource(
                if (isLastPage) Res.drawable.btn_get_started
                else Res.drawable.btn_continue
            ),
            contentDescription = if (isLastPage) "Get Started" else "Continue",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .size(
                    width = if (isLastPage) 240.dp else 200.dp,
                    height = 240.dp
                )
                .clickable {
                    if (isLastPage) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
        )
    }
}
