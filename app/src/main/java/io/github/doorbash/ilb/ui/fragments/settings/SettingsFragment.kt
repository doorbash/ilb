package io.github.doorbash.ilb.ui.fragments.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import io.github.doorbash.ilb.MainActivity
import io.github.doorbash.ilb.R
import io.github.doorbash.ilb.arch.BaseUIFragment
import io.github.doorbash.ilb.core.su.Utils
import io.github.doorbash.ilb.databinding.FragmentSettingsBinding
import io.github.doorbash.ilb.ktx.sharedPrefs
import io.github.doorbash.ilb.utils.AppBroadcastManager
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_SHOW_DIALOG_DNS1
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_SHOW_DIALOG_DNS2
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_SHOW_DIALOG_PIP


class SettingsFragment : BaseUIFragment<FragmentSettingsBinding>(R.layout.fragment_settings) {

    val viewModel: SettingsViewModel by viewModels()
    val broadcastManager = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                EVENT_SHOW_DIALOG_PIP -> {
                    val inflater: LayoutInflater = activity!!.layoutInflater
                    val mView: View = inflater.inflate(R.layout.dialog_settings_pip, null)
                    val text =
                        mView.findViewById(R.id.dialog_settings_pip_value) as TextInputEditText

                    MaterialAlertDialogBuilder(activity!!)
                        .setTitle("Public IP API")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            context?.sharedPrefs()
                                ?.edit {
                                    val t = text.text.toString()
                                    if (t.startsWith("http://") || t.startsWith("https://")) {
                                        viewModel.itemPipUrl.postValue(t)
                                        putString(PREF_KEY_PIP, t)
                                    }
                                }
                        }
                        .setView(mView).show();
                }

                EVENT_SHOW_DIALOG_DNS1 -> {
                    val inflater: LayoutInflater = activity!!.layoutInflater
                    val mView: View = inflater.inflate(R.layout.dialog_settings_dns1, null)
                    val text =
                        mView.findViewById(R.id.dialog_settings_pip_value) as TextInputEditText

                    MaterialAlertDialogBuilder(activity!!)
                        .setTitle("Dns 1")
                        .setPositiveButton("OK", null)
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            context?.sharedPrefs()
                                ?.edit {
                                    val t = text.text.toString()
                                    if (Utils.isIpv4(t)) {
                                        viewModel.itemDns1.postValue(t)
                                        putString(PREF_KEY_DNS1, t)
                                    }
                                }
                        }
                        .setView(mView)
                        .show()
                }

                EVENT_SHOW_DIALOG_DNS2 -> {
                    val inflater: LayoutInflater = activity!!.layoutInflater
                    val mView: View = inflater.inflate(R.layout.dialog_settings_dns2, null)
                    val text =
                        mView.findViewById(R.id.dialog_settings_pip_value) as TextInputEditText

                    MaterialAlertDialogBuilder(activity!!)
                        .setTitle("Dns 2")
                        .setPositiveButton("OK", null)
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            context?.sharedPrefs()
                                ?.edit {
                                    val t = text.text.toString()
                                    if (Utils.isIpv4(t)) {
                                        viewModel.itemDns2.postValue(t)
                                        putString(PREF_KEY_DNS2, t)
                                    }
                                }
                        }
                        .setView(mView)
                        .show()
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = viewModel
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_back_md2)
        setHasOptionsMenu(true)

        AppBroadcastManager.registerReceiver(EVENT_SHOW_DIALOG_PIP, broadcastManager)
        AppBroadcastManager.registerReceiver(EVENT_SHOW_DIALOG_DNS1, broadcastManager)
        AppBroadcastManager.registerReceiver(EVENT_SHOW_DIALOG_DNS2, broadcastManager)

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity?.onBackPressed()
        return true
    }

    override fun onDestroy() {
        AppBroadcastManager.unregisterReceiver(broadcastManager)
        super.onDestroy()
    }

}