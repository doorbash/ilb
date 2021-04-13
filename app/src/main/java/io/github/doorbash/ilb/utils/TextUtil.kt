package io.github.doorbash.ilb.utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

fun fromHtml(html: String?): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)
    }
}

fun readRawFile(context: Context, res: Int): String {
    val inputStream: InputStream =  context.getResources().openRawResource(res)
    println(inputStream)
    val byteArrayOutputStream = ByteArrayOutputStream()
    var i: Int
    try {
        i = inputStream.read()
        while (i != -1) {
            byteArrayOutputStream.write(i)
            i = inputStream.read()
        }
        inputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return byteArrayOutputStream.toString()
}

