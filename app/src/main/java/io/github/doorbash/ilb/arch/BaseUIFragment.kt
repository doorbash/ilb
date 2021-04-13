package io.github.doorbash.ilb.arch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import io.github.doorbash.ilb.ktx.startAnimations

open class BaseUIFragment<Binding: ViewDataBinding> (protected val layoutId : Int) : Fragment() {

    protected lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.lifecycleOwner = this

        binding.addOnRebindCallback(object : OnRebindCallback<Binding>() {
            override fun onPreBind(binding: Binding): Boolean {
                Log.d("BaseUIFragment", "onPreBind()")
                this@BaseUIFragment.onPreBind(binding)
                return true
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    private fun onPreBind(binding: Binding) {
        (binding.root as? ViewGroup)?.startAnimations()
    }
}