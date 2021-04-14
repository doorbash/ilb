package io.github.doorbash.ilb.ui.fragments.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.github.doorbash.ilb.ktx.sharedPrefs
import io.github.doorbash.ilb.utils.AppBroadcastManager
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_SHOW_DIALOG_DNS1
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_SHOW_DIALOG_DNS2
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_SHOW_DIALOG_PIP

const val PREF_KEY_PIP = "key_public_ip_api"
const val PREF_DEFAULT_PIP = "https://api.ipify.org/"

const val PREF_KEY_DNS1 = "key_dns1"
const val PREF_DEFAULT_DNS1 = "8.8.8.8"

const val PREF_KEY_DNS2 = "key_dns2"
const val PREF_DEFAULT_DNS2 = "1.1.1.1"

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val itemPipUrl = MutableLiveData(application.sharedPrefs().getString(PREF_KEY_PIP, PREF_DEFAULT_PIP))
    val itemDns1 = MutableLiveData(application.sharedPrefs().getString(PREF_KEY_DNS1, PREF_DEFAULT_DNS1))
    val itemDns2 = MutableLiveData(application.sharedPrefs().getString(PREF_KEY_DNS2, PREF_DEFAULT_DNS2))

    fun itemPIPClicked() {
        AppBroadcastManager.emitEvent(EVENT_SHOW_DIALOG_PIP)
    }
    fun itemDns1Clicked() {
        AppBroadcastManager.emitEvent(EVENT_SHOW_DIALOG_DNS1)
    }
    fun itemDns2Clicked() {
        AppBroadcastManager.emitEvent(EVENT_SHOW_DIALOG_DNS2)
    }
}