package io.github.doorbash.ilb.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.github.doorbash.ilb.MainActivity
import io.github.doorbash.ilb.R
import io.github.doorbash.ilb.arch.BaseUIFragment
import io.github.doorbash.ilb.databinding.FragmentSettingsBinding

class SettingsFragment : BaseUIFragment<FragmentSettingsBinding>(R.layout.fragment_settings) {

    val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = viewModel
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_back_md2)
        setHasOptionsMenu(true)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity?.onBackPressed()
        return true
    }

}