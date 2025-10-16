package org.betofly.app.di

import org.betofly.app.data.Betofly
import org.betofly.app.data.DatabaseDriverFactory
import org.koin.dsl.module

val databaseModule = module {
    single { Betofly(get<DatabaseDriverFactory>().createDriver()) }
}