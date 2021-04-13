package io.github.doorbash.ilb.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

abstract class AppBroadcastManager {
    companion object {
        const val EVENT_FIND_INTERNET_CONNECTIONS = "find_internet_connections"
        const val EVENT_VPN_START = "vpn_start"
        const val EVENT_VPN_STOP = "vpn_stop"
        const val EVENT_VPN_STOPPED = "vpn_stopped"
        const val EVENT_VPN_STARTED = "vpn_started"
        const val EVENT_VPN_START_ERROR = "vpn_start_err"
        const val EVENT_VPN_PING = "ping"
        const val EVENT_VPN_PONG = "pong"

        var instance: LocalBroadcastManager? = null

        operator fun invoke(context: Context) {
            instance = LocalBroadcastManager.getInstance(context)
        }

        fun emitEvent(event: String) {
            instance?.sendBroadcast(Intent(event))
        }

        fun registerReceiver(event: String, br: BroadcastReceiver) {
            instance?.registerReceiver(br, IntentFilter(event))
        }

        fun unregisterReceiver(br: BroadcastReceiver) {
            instance?.unregisterReceiver(br)
        }
    }
}