package org.betofly.app

import android.app.Application
import android.content.Context
import org.betofly.app.di.initKoin

import org.koin.android.ext.koin.androidContext

class AppBetofly: Application() {
    override fun onCreate() {
        super.onCreate()
        androidContext = this
        initKoin { androidContext(this@AppBetofly) }
    }

    companion object {
        lateinit var androidContext: Context
            private set
    }
}