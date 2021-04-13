package io.github.doorbash.ilb.utils

import android.annotation.SuppressLint
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.experimental.and

@SuppressLint("DefaultLocale")
fun humanReadableByteCountBin(bytes: Long): String? {
    val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
    if (absB < 1024) {
        return "$bytes B"
    }
    var value = absB
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    var i = 40
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci.next()
        i -= 10
    }
    value *= java.lang.Long.signum(bytes).toLong()
    return java.lang.String.format("%.1f %cB", value / 1024.0, ci.current())
}

fun getSubnetMaskLength(subnetMask: ByteArray): Int {
    var maskLength = 0
    if (subnetMask.size == 4) {
        var index = 0
        while (index < subnetMask.size && subnetMask[index] == 0xFF.toByte()) {
            maskLength += 8
            index++
        }
        if (index != subnetMask.size) {
            var bits = 7
            while (bits >= 0 && (subnetMask[index] and 1).toInt() shl 7 != 0) {
                bits--
                maskLength++
            }
        }
    }
    return maskLength
}