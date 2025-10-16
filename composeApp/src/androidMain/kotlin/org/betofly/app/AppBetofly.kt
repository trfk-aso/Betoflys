package org.betofly.app

import android.app.Application
import org.betofly.app.di.initKoin

import org.koin.android.ext.koin.androidContext

class AppBetofly: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@AppBetofly) }
    }
}