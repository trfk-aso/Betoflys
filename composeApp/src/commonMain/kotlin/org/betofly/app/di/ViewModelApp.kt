package org.betofly.app.di

import backup.BackupStorage
import com.russhwolf.settings.Settings
import org.betofly.app.ui.screens.search.SearchScreen
import org.betofly.app.viewModel.FavoritesViewModel
import org.betofly.app.viewModel.HomeViewModel
import org.betofly.app.viewModel.JournalViewModel
import org.betofly.app.viewModel.RecordingViewModel
import org.betofly.app.viewModel.SearchViewModel
import org.betofly.app.viewModel.SettingsViewModel
import org.betofly.app.viewModel.SplashViewModel
import org.betofly.app.viewModel.StatisticsViewModel
import org.betofly.app.viewModel.TripDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModule = module {

    single { Settings() }
    viewModelOf(::SplashViewModel)
    single { HomeViewModel(get(), get()) }
    single { SearchViewModel(get(),get() )}
    single { TripDetailsViewModel(get()) }
    single { RecordingViewModel(get(), get()) }
    single { FavoritesViewModel(get(),get(),get(), get()) }
    viewModelOf(::JournalViewModel)
    viewModelOf(::StatisticsViewModel)
    single { SettingsViewModel(get(),get(),get(),get(),get(),get())}
    single<BackupStorage> { BackupStorage }

}