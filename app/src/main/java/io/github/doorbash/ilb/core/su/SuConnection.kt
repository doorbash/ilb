package io.github.doorbash.ilb.core.su

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import io.github.doorbash.ilb.core.su.SuServiceApi.Companion.initBusyBox

private const val TAG = "SuConnection"

class SuConnection(val messenger: Messenger) : ServiceConnection {

    companion object {
        var remoteMessenger: Messenger? = null
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        Log.d(TAG, "service onServiceConnected")
        remoteMessenger = Messenger(service)

        initBusyBox(messenger)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Log.d(TAG, "service onServiceDisconnected")
        remoteMessenger = null
    }

}