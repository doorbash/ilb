package io.github.doorbash.ilb.model

import io.github.doorbash.ilb.utils.getSubnetMaskLength
import io.github.doorbash.ilb.utils.humanReadableByteCountBin
import java.net.Inet4Address

class PublicIP {
    var publicIPv4: String? = null
    var iface: String? = null
    var mac: String? = null
    var ipv4: String? = null
    var mask: String? = null
    var mark: String? = null
    var rx: Long = 0
    var tx: Long = 0
    val rxs: String get() = humanReadableByteCountBin(rx)!!
    val txs: String get() = humanReadableByteCountBin(tx)!!
    val prefixMask: Int get() = getSubnetMaskLength(Inet4Address.getByName(this.mask).address)
    val ipv4AndMask: String get() = "$ipv4/$prefixMask"

//    override fun equals(other: Any?): Boolean {
//        if (other is PublicIP) {
//            if (publicIPv4 != other.publicIPv4) return false
//            if (iface != other.iface) return false
//            if (mac != other.mac) return false
//            if (ipv4 != other.ipv4) return false
//            if (mask != other.mask) return false
//            if (mark != other.mark) return false
//            if (rx != other.rx) return false
//            if (tx != other.tx) return false
//            return true
//        }
//        return false
//    }

    fun clone(): PublicIP {
        return PublicIP().apply {
            this@PublicIP.also {
                publicIPv4 = it.publicIPv4
                iface = it.iface
                mac = it.mac
                ipv4 = it.ipv4
                mask = it.mask
                mark = it.mark
                rx = it.rx
                tx = it.tx
            }
        }
    }
}