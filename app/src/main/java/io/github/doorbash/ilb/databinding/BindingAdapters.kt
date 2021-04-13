package io.github.doorbash.ilb.databinding

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isGone
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_END
import com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
import io.github.doorbash.ilb.R
import io.github.doorbash.ilb.ktx.coroutineScope
import io.github.doorbash.ilb.utils.MarkwonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@BindingAdapter("gone")
fun setGone(view: View, gone: Boolean) {
    view.isGone = gone
}

@BindingAdapter("goneUnless")
fun setGoneUnless(view: View, goneUnless: Boolean) {
    setGone(view, goneUnless.not())
}

@BindingAdapter("markdownText")
fun setMarkdownText(tv: TextView, text: CharSequence) {
    tv.coroutineScope.launch(Dispatchers.IO) {
        MarkwonUtil(tv.context).setMarkdown(tv, text.toString())
    }
}

@BindingAdapter("app:icon", "app:iconGravity", requireAll = true)
fun Button.setIcon(icon: Drawable, iconGravity: String) {
    (this as MaterialButton).iconGravity = when (iconGravity) {
        "textStart" -> ICON_GRAVITY_TEXT_START
        "textEnd" -> ICON_GRAVITY_TEXT_END
        else -> 0
    }
    icon.setTint(currentTextColor)
    if (this.iconGravity == ICON_GRAVITY_TEXT_START) {
        setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
    } else {
        setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
    }
}

@BindingAdapter("android:textColor")
fun TextView.setColor(color: String) {
    when (color) {
        "primary" -> {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            setTextColor(typedValue.data)
        }
        "secondary" -> {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
            setTextColor(typedValue.data)
        }
        "onSurface" -> {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
            setTextColor(typedValue.data)
        }
        else -> setTextColor(Color.parseColor(color))
    }
}