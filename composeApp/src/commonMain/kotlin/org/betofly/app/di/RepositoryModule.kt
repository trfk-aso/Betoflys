package org.betofly.app.di

import org.betofly.app.repository.EntryRepository
import org.betofly.app.repository.EntryRepositoryImpl
import org.betofly.app.repository.SearchRepository
import org.betofly.app.repository.SearchRepositoryImpl
import org.betofly.app.repository.SettingsRepository
import org.betofly.app.repository.SettingsRepositoryImpl
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.repository.ThemeRepositoryImpl
import org.betofly.app.repository.TripRepository
import org.betofly.app.repository.TripRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<TripRepository> { TripRepositoryImpl(get()) }
    single<SearchRepository> { SearchRepositoryImpl(get()) }
    single<EntryRepository> { EntryRepositoryImpl(get()) }
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
}
