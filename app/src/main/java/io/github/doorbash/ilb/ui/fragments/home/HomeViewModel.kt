package io.github.doorbash.ilb.ui.fragments.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.internal.MainShell
import io.github.doorbash.ilb.model.PublicIP
import io.github.doorbash.ilb.utils.AppBroadcastManager
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_FIND_INTERNET_CONNECTIONS
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_START
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_STOP

private const val TAG = "HomeViewModel"

//private const val LOGS_LENGTH = 2000

enum class RootStatus {
    PENDING,
    OK,
    ERROR
}

enum class PublicIpsState {
    IDLE,
    LOADING,
    OK,
    ERROR
}

private const val FIND_IPS_TEXT_LOADING = "Please wait..."

class HomeViewModel : ViewModel() {
    val marks = ArrayList<String>()

    val rootStatus = MutableLiveData(RootStatus.PENDING)

//    val publicIps = MutableLiveData<List<PublicIP>>(arrayListOf())
    val publicIPsLoadingState = MutableLiveData(PublicIpsState.IDLE)
    val findIpsText = MutableLiveData(FIND_IPS_TEXT_LOADING)

    val notes = MutableLiveData("")

    val vpnRunning = MutableLiveData(false)
    val vpnStarting = MutableLiveData(false)
    val vpnStopping = MutableLiveData(false)

//    fun logCatOutput() = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
//        if (Shell.rootAccess()) {
//            Runtime.getRuntime().exec("su -c logcat -v tag | grep -e SHELL_IN -e SHELLOUT")
//                .inputStream
//                .bufferedReader()
//                .useLines { lines ->
//                    lines.forEach { line -> if (line.length > 12) emit(line.substring(12)) }
//                }
//        } else {
//            Runtime.getRuntime().exec("logcat -v tag | grep io.github.doorbash")
//                .inputStream
//                .bufferedReader()
//                .useLines { lines ->
//                    lines.forEach { line -> emit(line) }
//                }
//        }
//    }

    fun checkRootAccess() {
        Log.d(TAG, "checking root access...")
        rootStatus.value = RootStatus.PENDING

        if (!Shell.rootAccess()) MainShell.mainShell = null
        if (Shell.rootAccess()) {
            Log.d(TAG, "[!] Root is available.")
            rootStatus.value = RootStatus.OK

            publicIPsLoadingState.postValue(PublicIpsState.IDLE)
        } else {
            Log.d(TAG, "[!] Root is not available.")
            rootStatus.value = RootStatus.ERROR
        }
    }

    fun findPublicIps() {
        marks.clear()

        AppBroadcastManager.emitEvent(EVENT_FIND_INTERNET_CONNECTIONS)

        findIpsText.postValue(FIND_IPS_TEXT_LOADING)
        publicIPsLoadingState.postValue(PublicIpsState.LOADING)
    }

    fun startVPN() {
        AppBroadcastManager.emitEvent(EVENT_VPN_START)
    }

    fun stopVPN() {
        AppBroadcastManager.emitEvent(EVENT_VPN_STOP)
    }

    init {
        checkRootAccess()
    }

    fun foundInternetConnections(list: List<PublicIP>) {
//        publicIps.postValue(list)
        when {
            list.isEmpty() -> {
                publicIPsLoadingState.postValue(PublicIpsState.ERROR)
                findIpsText.postValue("No Internet connection found.")
            }
            list.size == 1 -> {
                publicIPsLoadingState.postValue(PublicIpsState.ERROR)
                findIpsText.postValue("Only 1 Internet connection found.")
            }
            else -> {
                publicIPsLoadingState.postValue(PublicIpsState.OK)
                findIpsText.postValue("${list.size} Internet connections found.")
            }
        }
    }
}