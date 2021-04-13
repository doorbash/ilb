package io.github.doorbash.ilb.core.su

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import io.github.doorbash.ilb.core.su.SuConnection.Companion.remoteMessenger

private const val TAG = "SuServiceApi"

class SuServiceApi {
    companion object {
        fun initBusyBox(messenger: Messenger) {
            val message: Message = Message.obtain(null, SuService.COMMAND_INIT_BUSYBOX)
            message.replyTo = messenger
            try {
                remoteMessenger?.send(message)
            } catch (e: RemoteException) {
                Log.e(TAG, "Remote error", e)
            }
        }

        fun startMarkServer(messenger: Messenger, socket: String): Boolean {
            val message: Message = Message.obtain(null, SuService.COMMAND_START_MARK_SERVER)
            message.data = Bundle().apply {
                putString("socket", socket)
            }
            message.replyTo = messenger
            try {
                remoteMessenger?.send(message)
                return true
            } catch (e: RemoteException) {
                Log.e(TAG, "Remote error", e)
            }
            return false
        }

        fun stopMarkServer(messenger: Messenger): Boolean {
            val message: Message = Message.obtain(null, SuService.COMMAND_STOP_MARK_SERVER)
            message.replyTo = messenger
            try {
                remoteMessenger?.send(message)
                return true
            } catch (e: RemoteException) {
                Log.e(TAG, "Remote error", e)
            }
            return false
        }
    }
}