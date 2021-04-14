package io.github.doorbash.ilb

import android.app.Application
import io.github.doorbash.ilb.core.su.initSU
import io.github.doorbash.ilb.utils.AppBroadcastManager

const val SHARED_PREFS_NAME = "prefs"

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        initSU()
        AppBroadcastManager(applicationContext)
    }
}