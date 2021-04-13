package io.github.doorbash.ilb.ktx

import android.app.Activity
import android.content.ContextWrapper
import android.os.Build
import android.text.PrecomputedText
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.view.isGone
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import io.github.doorbash.ilb.MainActivity
import io.github.doorbash.ilb.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun ViewGroup.startAnimations() {
    val transition = AutoTransition()
        .setInterpolator(FastOutSlowInInterpolator())
        .setDuration(400)
    TransitionManager.beginDelayedTransition(
        this,
        transition
    )
}

val View.activity: Activity
    get() {
        var context = context
        while (true) {
            if (context !is ContextWrapper)
                error("View is not attached to activity")
            if (context is Activity)
                return context
            context = context.baseContext
        }
    }

var View.coroutineScope: CoroutineScope
    get() = getTag(R.id.coroutineScope) as? CoroutineScope
        ?: (activity as? MainActivity)?.lifecycleScope
        ?: GlobalScope
    set(value) = setTag(R.id.coroutineScope, value)

@set:BindingAdapter("precomputedText")
var TextView.precomputedText: CharSequence
    get() = text
    set(value) {
        val callback = tag as? Runnable

        // Don't even bother pre 21
        if (Build.VERSION.SDK_INT < 21) {
            post {
                text = value
                isGone = false
                callback?.run()
            }
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= 29) {
                // Internally PrecomputedTextCompat will use platform API on API 29+
                // Due to some stupid crap OEM (Samsung) implementation, this can actually
                // crash our app. Directly use platform APIs with some workarounds
                val pre = PrecomputedText.create(value, textMetricsParams)
                post {
                    try {
                        text = pre
                    } catch (e: IllegalArgumentException) {
                        // Override to computed params to workaround crashes
                        textMetricsParams = pre.params
                        text = pre
                    }
                    isGone = false
                    callback?.run()
                }
            } else {
                val tv = this@precomputedText
                val params = TextViewCompat.getTextMetricsParams(tv)
                val pre = PrecomputedTextCompat.create(value, params)
                post {
                    TextViewCompat.setPrecomputedText(tv, pre)
                    isGone = false
                    callback?.run()
                }
            }
        }
    }

@BindingAdapter("isSelected")
fun View.isSelected(isSelected: Boolean) {
    this.isSelected = isSelected
}

fun TextView.blink() {
    val anim: Animation = AlphaAnimation(0.0f, 1.0f)
    anim.duration = 100
    anim.startOffset = 100
    anim.repeatMode = Animation.REVERSE
    anim.repeatCount = 3
    startAnimation(anim)
}