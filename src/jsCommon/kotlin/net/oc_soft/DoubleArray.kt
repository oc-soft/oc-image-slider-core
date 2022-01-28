package net.oc_soft

import kotlin.math.roundToInt

/**
 * convert double array to hex string
 */
fun DoubleArray.toHexString(): String {

    val intArray = IntArray(size) {
        kotlin.math.min(0xff, kotlin.math.max(
            0, (this[it] * 255.0).roundToInt()))
    }
    val hexStr = intArray.joinToString(
        separator = "",
        transform = { 
            if (it < 0xf) {
                "0${it.toString(16)}"
            } else {
                it.toString(16) 
            }
        })
    return "#${hexStr}"
}


// vi: se ts=4 sw=4 et:
