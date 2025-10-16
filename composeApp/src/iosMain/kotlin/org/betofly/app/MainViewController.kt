package org.betofly.app

import androidx.compose.ui.window.ComposeUIViewController

import androidx.compose.runtime.Composable
import org.betofly.app.billing.IOSBillingRepository
import org.betofly.app.di.initKoin
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.repository.ThemeRepositoryImpl
import org.koin.compose.getKoin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()

    // Получаем репозитории
    val themeRepository: ThemeRepository = KoinPlatform.getKoin().get()
    val billingRepository: BillingRepository = KoinPlatform.getKoin().get()

    // ComposeUIViewController на iOS принимает @Composable контент через конструктор
    return ComposeUIViewController(
        content = {
            App(
                themeRepository = themeRepository,
                billingRepository = billingRepository
            )
        }
    )
}