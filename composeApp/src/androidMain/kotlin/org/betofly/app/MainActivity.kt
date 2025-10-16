package org.betofly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.launch
import org.betofly.app.billing.AndroidBillingRepository
import org.betofly.app.data.Betofly
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.repository.ThemeRepositoryImpl
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform.getKoin

class MainActivity : ComponentActivity() {

    private lateinit var billingRepository: BillingRepository
    private lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themeRepository = getKoin().get()

        billingRepository = AndroidBillingRepository(
            context = this,
            themeRepository = themeRepository
        )

        lifecycleScope.launch {
            themeRepository.initializeThemes()
        }

        setContent {
            App(
                themeRepository = themeRepository,
                billingRepository = billingRepository
            )
        }
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}