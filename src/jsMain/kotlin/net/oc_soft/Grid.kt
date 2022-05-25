package net.oc_soft


/**
 * helper to generate grid
 */
class Grid {

    /**
     * class instance
     */
    companion object {

        /**
         * create separated grid
         */
        fun generate(
            bound: DoubleArray,
            rowCount: Int,
            colCount: Int): Array<Array<DoubleArray>> {
            val width = (bound[2] - bound[0]) / colCount.toDouble()
            val height = (bound[3] - bound[1]) / rowCount.toDouble()
            return Array<Array<DoubleArray>> (rowCount) {
                val rowIdx = it
                Array<DoubleArray>(colCount) {
                    val colIdx = it
                    doubleArrayOf(
                        bound[0] + colIdx * width,
                        bound[1] + rowIdx * height,
                        bound[0] + (colIdx + 1) * width,
                        bound[1] + (rowIdx + 1) * height)
                }
            }  
        } 
    }
}

// vi: se ts=4 sw=4 et:
