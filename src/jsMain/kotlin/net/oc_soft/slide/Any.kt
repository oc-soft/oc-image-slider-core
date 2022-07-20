package net.oc_soft.slide

import kotlin.text.toInt
import kotlin.text.toDouble
/**
 * convert any to integer
 */
fun Any.toInt(): Int {
    return when (this) {
        is String -> toInt()
        is Number -> toInt()
        else -> throw IllegalStateException()
    }
}
/**
 * convert any to boolean
 */
fun Any.toBoolean(): Boolean {
    return when (this) {
        is Number -> toInt() != 0
        is Boolean -> this
        is String -> toBoolean()
        else -> throw IllegalStateException()
    }
}

/**
 * convert any to double
 */
fun Any.toDouble(): Double {
    return when (this) {
        is Number -> toDouble()
        is String -> toDouble()
        else -> throw IllegalStateException()
    }
}

/**
 * convert to int array
 */
fun Any.toIntArray(): IntArray {
    return when (this) {
        is String -> {
            intArrayOf(this.toInt())
        }
        is Array<*> -> {
            IntArray(this.size) {
                val item = this[it]
                when (item) {
                    is Number -> item.toInt()
                    is String -> item.toInt()
                    else -> throw IllegalArgumentException()
                }
            }
        }
        is Number -> {
            intArrayOf(this.toInt())
        }
        else -> throw IllegalArgumentException()
    }
}


// vi: se ts=4 sw=4 et:
