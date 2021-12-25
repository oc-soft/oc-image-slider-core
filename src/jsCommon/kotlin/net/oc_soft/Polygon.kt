package net.oc_soft

/**
 * helper functions for polygon
 */
class Polygon {

    /**
     * class instance
     */
    companion object {


        /**
         * convert rect parameter to polygon
         */
        fun convertFromRect(
            left: Double,
            top: Double,
            right: Double,
            bottom: Double): Array<DoubleArray> {
            return arrayOf(
                doubleArrayOf(left, top),
                doubleArrayOf(left, bottom),
                doubleArrayOf(right, bottom),
                doubleArrayOf(right, top))
        }
    }
}


/**
 * create polygon paramater
 */
fun DoubleArray.createPolygon(
    offset: Int = 0): Array<DoubleArray>? {

    return if (offset >= 0 && offset + 4 <= this.lastIndex) {
        Polygon.convertFromRect(
            this[offset], this[offset + 1],
            this[offset + 2], this[offset + 3])
    } else {
        null
    }
}

// vi: se ts=4 sw=4 et:
