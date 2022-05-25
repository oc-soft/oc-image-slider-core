package net.oc_soft.slide

import kotlin.math.pow

/**
 * mutiliply each emelements by scale
 */
fun DoubleArray.scale(scale: Double): DoubleArray {
    return DoubleArray(size) {
        this[it] * scale
    }
}

/**
 * plus operation
 */
operator fun DoubleArray.plus(pt: DoubleArray): DoubleArray {
    return DoubleArray(size) {
        this[it] + pt[it]
    }
}

/**
 * minus operation
 */
operator fun DoubleArray.minus(pt: DoubleArray): DoubleArray {
    return DoubleArray(size) {
        this[it] - pt[it]
    }
}


/**
 * calcurate norm
 */
fun DoubleArray.distance(pt: DoubleArray): Double {
    var dis2 = 0.0

    forEachIndexed{
        idx, elem ->
        dis2 += (elem - pt[idx]).pow(2.0)
    }
    return dis2.pow(0.5)

}

/**
 * calcurate norm
 */
val DoubleArray.distance: Double 
    get() {
        var dis2 = 0.0
        forEach {
            dis2 += it.pow(2.0)
        }
        return dis2.pow(0.5)
    }
// vi: se ts=4 sw=4 et:
