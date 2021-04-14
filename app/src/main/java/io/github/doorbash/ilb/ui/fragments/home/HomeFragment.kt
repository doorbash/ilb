package io.github.doorbash.ilb.ui.fragments.home

import PublicIPsAdapter
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import io.github.doorbash.ilb.R
import io.github.doorbash.ilb.arch.BaseUIFragment
import io.github.doorbash.ilb.core.su.findInternetConnections
import io.github.doorbash.ilb.databinding.FragmentHomeBinding
import io.github.doorbash.ilb.ktx.blink
import io.github.doorbash.ilb.ktx.coroutineScope
import io.github.doorbash.ilb.model.PublicIP
import io.github.doorbash.ilb.utils.AppBroadcastManager
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_FIND_INTERNET_CONNECTIONS
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_PING
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_PONG
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_START
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_STARTED
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_START_ERROR
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_STOP
import io.github.doorbash.ilb.utils.AppBroadcastManager.Companion.EVENT_VPN_STOPPED
import io.github.doorbash.ilb.utils.readRawFile
import io.github.doorbash.ilb.vpn.ILBVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "HomeFragment"

class HomeFragment : BaseUIFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    val viewModel: HomeViewModel by viewModels()

    var iplist: List<PublicIP>? = null

    lateinit var publicIpsRecyclerView: RecyclerView
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                EVENT_FIND_INTERNET_CONNECTIONS -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        iplist = findInternetConnections(context)
                        (publicIpsRecyclerView.adapter as PublicIPsAdapter).submitList(iplist)
                        viewModel.foundInternetConnections(iplist!!)
                        binding.findingStatusText.blink()
                    }
                }
                EVENT_VPN_STOPPED -> {
                    viewModel.vpnStarting.postValue(false)
                    viewModel.vpnRunning.postValue(false)
                    viewModel.vpnStopping.postValue(false)
                }
                EVENT_VPN_STARTED -> {
                    viewModel.vpnStarting.postValue(false)
                    viewModel.vpnRunning.postValue(true)
                    viewModel.vpnStopping.postValue(false)
                    viewModel.publicIPsLoadingState.postValue(PublicIpsState.IDLE)
                }
                EVENT_VPN_START_ERROR -> {
                    viewModel.vpnStarting.postValue(false)
                    viewModel.vpnRunning.postValue(false)
                    viewModel.vpnStopping.postValue(false)
                }
                EVENT_VPN_PONG -> {
                    viewModel.vpnStarting.postValue(false)
                    viewModel.vpnRunning.postValue(true)
                    viewModel.vpnStopping.postValue(false)
                }

                EVENT_VPN_START -> {
                    with(viewModel) {
                        if (!vpnRunning.value!! && !vpnStarting.value!!) {
                            vpnStarting.postValue(true)
                            val i = VpnService.prepare(requireContext())
                            if (i != null) {
                                startActivityForResult(i, 1)
                            } else {
                                onActivityResult(1, Activity.RESULT_OK, null);
                            }
                        }
                    }
                }
                EVENT_VPN_STOP -> {
                    with(viewModel) {
                        if (vpnRunning.value!! && !vpnStopping.value!!) {
                            vpnStopping.postValue(true)
                            AppBroadcastManager.emitEvent(EVENT_VPN_STOPPED)
                        }
                    }
                }
            }
        }
    }
//    var logs = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = viewModel

        publicIpsRecyclerView = binding.publicIps
        publicIpsRecyclerView.setHasFixedSize(true)
        publicIpsRecyclerView.adapter = PublicIPsAdapter()

        binding.notes.coroutineScope = viewModel.viewModelScope

        viewModel.notes.postValue(readRawFile(requireContext(), R.raw.notes))

        viewModel.publicIPsLoadingState.observe(viewLifecycleOwner) {
            if (it == PublicIpsState.OK || it == PublicIpsState.IDLE) {
                activity?.invalidateOptionsMenu()
            }
        }
//        viewModel.rootStatus.observe(viewLifecycleOwner) {
//            binding.rootNoticeText.blink()
//        }
        viewModel.vpnRunning.observe(viewLifecycleOwner) {
            activity?.invalidateOptionsMenu()
        }

//        binding.logs.setLines(20)
//        viewModel.logCatOutput().observe(viewLifecycleOwner) { logMessage ->
//            logs = (logs + logMessage + "\n")
//            logs = logs.split("\n").takeLast(20).joinToString("\n")
//            binding.logs.text = logs
//        }


        with(broadcastReceiver) {
            AppBroadcastManager.registerReceiver(EVENT_FIND_INTERNET_CONNECTIONS, this)

            AppBroadcastManager.registerReceiver(EVENT_VPN_STOPPED, this)
            AppBroadcastManager.registerReceiver(EVENT_VPN_STARTED, this)
            AppBroadcastManager.registerReceiver(EVENT_VPN_START_ERROR, this)
            AppBroadcastManager.registerReceiver(EVENT_VPN_PONG, this)
            AppBroadcastManager.registerReceiver(EVENT_VPN_START, this)
            AppBroadcastManager.registerReceiver(EVENT_VPN_STOP, this)

            AppBroadcastManager.emitEvent(EVENT_VPN_PING)
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.publicIPsLoadingState.value == PublicIpsState.OK && !viewModel.vpnRunning.value!!) {
            inflater.inflate(R.menu.menu_home_with_refresh_md2, menu)
        } else {
            inflater.inflate(R.menu.menu_home_md2, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings ->
                findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
            R.id.action_refresh -> viewModel.publicIPsLoadingState.postValue(PublicIpsState.IDLE)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            val intent = Intent(requireContext(), ILBVpnService::class.java)
            val m = arrayListOf<String>()
            for (ip in iplist!!) m.add(ip.mark!!)
            intent.putExtra("marks", m.joinToString(" "))
            requireContext().startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        AppBroadcastManager.emitEvent(EVENT_VPN_PING)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppBroadcastManager.unregisterReceiver(broadcastReceiver)
    }
}