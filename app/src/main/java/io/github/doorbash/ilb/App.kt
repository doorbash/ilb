package io.github.doorbash.ilb

import android.app.Application
import io.github.doorbash.ilb.core.su.initSU
import io.github.doorbash.ilb.utils.AppBroadcastManager

class App: Application() {

    companion object{
        const val PUBLIC_IP_API = "https://api.ipify.org"
        const val LOCAL_SOCKS5_SERVER_PORT = 8000
    }

    override fun onCreate() {
        super.onCreate()
        initSU()
        AppBroadcastManager(applicationContext)
    }
}