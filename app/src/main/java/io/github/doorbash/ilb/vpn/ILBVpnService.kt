package io.github.doorbash.ilb.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.*
import android.util.Log
import com.topjohnwu.superuser.ipc.RootService
import io.github.doorbash.ilb.App.Companion.LOCAL_SOCKS5_SERVER_PORT
import io.github.doorbash.ilb.core.su.SuConnection
import io.github.doorbash.ilb.core.su.SuService
import io.github.doorbash.ilb.core.su.SuServiceApi
import io.github.doorbash.ilb.utils.AppBroadcastManager
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_PING
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_PONG
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_STARTED
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_STOP
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_STOPPED
import tun2socks.PacketFlow
import tun2socks.Tun2socks
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

private const val TAG = "ILBVpnService"

private const val socket = "@ilbmark"

open class ILBVpnService : VpnService() {

    private var pfd: ParcelFileDescriptor? = null
    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null
    private var buffer = ByteBuffer.allocate(1501)
    private var marks: String? = null

    @Volatile
    private var running = false
    private var bgThread: Thread? = null

    private val myMessenger: Messenger =
        Messenger(Handler(Looper.getMainLooper()) { msg ->
            return@Handler handleMessage(msg)
        })

    private fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            SuService.COMMAND_INIT_BUSYBOX -> {
//                val ls = LocalSocket().localSocketAddress
                 SuServiceApi.startMarkServer(myMessenger, socket)
            }

            SuService.COMMAND_START_MARK_SERVER -> {
                val builder = Builder().setSession("ilb")
                    .setMtu(1500)
                    .addAddress("192.168.56.1", 24)
                    .addDnsServer("8.8.8.8")
                    .addDnsServer("1.1.1.1")
                    .addRoute("0.0.0.0", 0)
                    .setBlocking(true)

                pfd = builder.establish()

                bgThread?.interrupt()
                bgThread = thread {

                    inputStream = FileInputStream(pfd!!.fileDescriptor)
                    outputStream = FileOutputStream(pfd!!.fileDescriptor)

                    Tun2socks.start(
                        Service(this),
                        Flow(outputStream),
                        socket,
                        marks,
                        "127.0.0.1",
                        LOCAL_SOCKS5_SERVER_PORT.toLong()
                    )

                    AppBroadcastManager.emitEvent(EVENT_VPN_STARTED)

                    running = true

                    while (running) {
                        try {
                            val n = inputStream?.read(buffer.array())
                            n ?: return@thread
                            if (n > 0) {
                                buffer.limit(n)
                                Tun2socks.inputPacket(buffer.array())
                                buffer.clear()
                            }
                        } catch (e: Exception) {
                            println("failed to read bytes from TUN fd ${e.message}")
                        }
                    }
                }
            }
        }
        return true
    }


    private val conn = SuConnection(myMessenger)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                EVENT_VPN_STOP -> {
                    stopVPN()
                }
                EVENT_VPN_PING -> {
                    if (running) {
                        AppBroadcastManager.emitEvent(EVENT_VPN_PONG)
                    }
                }
            }
        }
    }

    private fun stopVPN() {
        AppBroadcastManager.emitEvent(EVENT_VPN_STOPPED)
        SuServiceApi.stopMarkServer(myMessenger)
        Tun2socks.stop()
        pfd?.close()
        pfd = null
        inputStream = null
        outputStream = null
        running = false
        stopSelf()
    }

    class Flow(stream: FileOutputStream?) : PacketFlow {
        private val flowOutputStream = stream
        override fun writePacket(pkt: ByteArray?) {
            try {
                flowOutputStream?.write(pkt)
            } catch (e: java.lang.Exception) {
                Log.d(TAG, e.message ?: "writePacket error")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppBroadcastManager.registerReceiver(EVENT_VPN_STOP, broadcastReceiver)
        AppBroadcastManager.registerReceiver(EVENT_VPN_PING, broadcastReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        marks = intent?.getStringExtra("marks")
        val i = Intent(applicationContext, SuService::class.java)
        RootService.bind(i, conn)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        RootService.unbind(conn)
        AppBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun onRevoke() {
        super.onRevoke()
        stopVPN()
    }

    class Service(service: VpnService) : tun2socks.VpnService {
        private val vpnService = service
        override fun protect(fd: Long): Boolean {
            return vpnService.protect(fd.toInt())
        }
    }
}