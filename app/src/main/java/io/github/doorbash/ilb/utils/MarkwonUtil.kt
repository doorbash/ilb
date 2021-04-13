package io.github.doorbash.ilb.utils

import android.content.Context
import io.github.doorbash.ilb.ktx.precomputedText
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin

class MarkwonUtil {
    companion object {
        private var instance: Markwon? = null
        operator fun invoke(context: Context): Markwon {
            if(instance == null) {
               init(context)
            }
            return instance!!
        }
        private fun init(context: Context) {
            instance = Markwon.builder(context)
                .textSetter { textView, spanned, _, onComplete ->
                    textView.tag = onComplete
                    textView.precomputedText = spanned
                }
                .usePlugin(HtmlPlugin.create())
                .build()
        }
    }
}