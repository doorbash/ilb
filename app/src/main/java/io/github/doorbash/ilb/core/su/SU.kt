package io.github.doorbash.ilb.core.su

import android.content.Context
import android.util.Log
import com.topjohnwu.superuser.BusyBoxInstaller
import com.topjohnwu.superuser.Shell
import io.github.doorbash.ilb.App
import io.github.doorbash.ilb.App.Companion.PUBLIC_IP_API
import io.github.doorbash.ilb.BuildConfig
import io.github.doorbash.ilb.model.PublicIP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.*

private const val TAG = "SU"

fun initSU() {
    Shell.enableVerboseLogging = BuildConfig.DEBUG
    Shell.setDefaultBuilder(
        Shell.Builder.create()
            .setFlags(Shell.FLAG_REDIRECT_STDERR) // BusyBoxInstaller should come first!
            .setInitializers(BusyBoxInstaller::class.java)
    )
}

suspend fun findInternetConnections(context: Context): List<PublicIP> {
    Log.d(TAG, "foundInternetConnections()")
    return withContext(Dispatchers.Default) {
        val publicIpsFound: MutableList<String> = ArrayList()
        val ips = arrayListOf<PublicIP>()
        val command = "ifconfig | grep HWaddr"
        Log.d(TAG, command)
        val result = Shell.su(command).exec()
        val list: MutableList<String> = ArrayList()
        Log.d(TAG, "num lines: " + result.out.size)
        for (line in result.out) {
            Log.d(TAG, line)
            if (line.contains("HWaddr")) {
                list.add(line.substring(0, line.indexOf(" ")))
            }
        }
        // list contains interfaces names
        val arr = ArrayList<PublicIP>()
        for (i in list.indices) {
            val o = PublicIP()
            val name = list[i]
            o.iface = name
            var hasIP = false
            val result1 = Shell.su("ifconfig $name").exec()
            for (l in result1.out) {
                val line = l.trim { it <= ' ' }

//                            Log.d(TAG, line);
                if ("HWaddr" in line) {
                    val hwaddr = line.substring(line.indexOf("HWaddr ") + 7).trim { it <= ' ' }
                    if (hwaddr.length == 17) {
                        o.mac = hwaddr
                    }
                }
                if (line.startsWith("inet addr:")) {
                    val ip = line.substring(10, line.indexOf(" ", 10))
                    val mask = line.substring(line.indexOf("Mask:") + 5)
                    o.ipv4 = ip
                    o.mask = mask
                    hasIP = true
                }
                if (line.startsWith("RX bytes:")) {
                    val rx = line.substring(9, line.indexOf(" ", 9)).toLong()
                    val startIndex = line.indexOf("TX bytes:") + 9
                    val endIndex = if (line.indexOf(" ", startIndex) > -1) line.indexOf(
                        " ",
                        startIndex
                    ) else line.length
                    val tx =
                        line.substring(startIndex, endIndex).toLong()
                    o.rx = rx
                    o.tx = tx
                }
            }
            if (hasIP /*&& !name.startsWith("tun")*/) arr.add(o)
        }
        arr.sortWith { o1: PublicIP, o2: PublicIP ->
            try {
                val rx1 = o1.rx
                val rx2 = o2.rx
                return@sortWith rx2.compareTo(rx1)
            } catch (ignored: Exception) {
            }
            0
        }
        for (o in arr) {
            val name = o.iface
            Log.d(TAG, "interface: $name")
            val result1 =
                Shell.su("iproute show table all | grep \"default via \" | grep $name")
                    .exec()
            for (l in result1.out) {
                val line = l.trim { it <= ' ' }
                Log.d(TAG, line)
                var startIndex = line.indexOf("default via ") + 12
                val gw = line.substring(startIndex, line.indexOf(" ", startIndex))
                Log.d(TAG, "gw = $gw")
                if (!Utils.isIpv4(gw)) continue  // ipv6 not supported
                if (line.contains("table ")) {
                    startIndex = line.indexOf("table ") + 6
                    var endIndex = if (line.indexOf(" ", startIndex) > 0) line.indexOf(
                        " ",
                        startIndex
                    ) else line.length
                    val table = line.substring(startIndex, endIndex)
                    Log.d(TAG, "table = $table")
                    val result2 = Shell.su(
                        "iprule show | grep \"lookup $table\" | grep fwmark"
                    ).exec()
                    for (l2 in result2.out) {
                        val line2 = l2.trim { it <= ' ' }
                        Log.d(TAG, line2)
                        startIndex = line2.indexOf("fwmark ") + 7
                        endIndex = if (line2.indexOf(" ", startIndex) > 0) line2.indexOf(
                            " ",
                            startIndex
                        ) else line2.length
                        val fwmark = line2.substring(startIndex, endIndex)
                        Log.d(TAG, "fwmark = $fwmark")
                        val lib =
                            File(context.applicationInfo.nativeLibraryDir, "libilb.so")
                        val result3 = Shell.su("$lib ip $PUBLIC_IP_API $fwmark").exec()
                        for (line3 in result3.out) {
                            Log.d(TAG, "ip: $line3")
                            if (Utils.isIpv4(line3) && !publicIpsFound.contains(line3)) {
                                publicIpsFound.add(line3)
                                o.mark = fwmark //.put("table", table).put("gw", gw);
                                val jo = o.clone()
                                jo.publicIPv4 = line3
                                ips.add(jo)
                            }
                        }
                    }
                } else {
                    val lib = File(context.applicationInfo.nativeLibraryDir, "libilb.so")
                    val result2 =
                        Shell.su("$lib ip $PUBLIC_IP_API 0x0").exec()
                    for (line2 in result2.out) {
                        Log.d(TAG, "ip: $line2")
                        if (Utils.isIpv4(line2) /* && !publicIpsFound.contains(line2)*/) {
                            publicIpsFound.add(line2)
                            o.mark = "0x0" //.put("table", table).put("gw", gw);
                            val jo = o.clone()
                            jo.publicIPv4 = line2
                            //                                        if (publicIpsFound.contains(line2)) {
//                                            int deleteIndex = -1;
//                                            for (int i = 0; i < ips.length(); i++) {
//                                                if (ips.getJSONObject(i).getString("public_ipv4").equals(line2)) {
//                                                    deleteIndex = i;
//                                                    break;
//                                                }
//                                            }
//                                            if (deleteIndex > -1) ips.remove(deleteIndex);
//                                        }
                            ips.add(jo)
                        }
                    }
                }
            }
        }
        return@withContext ips
    }
}