package io.github.doorbash.ilb.utils

import android.view.View

interface HideableBehavior<V : View> {

    fun setHidden(view: V, hide: Boolean, lockState: Boolean = false)

}