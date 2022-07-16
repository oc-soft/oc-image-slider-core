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
            return Array<Array<DoubleArray>> (rowCount) {
                val rowIdx = it
                Array<DoubleArray>(colCount) {
                    val colIdx = it
                    calcBound(bound, rowCount, colCount, rowIdx, colIdx) 
                }
            }  
        } 
        /**
         * calculate bound
         */
        fun calcBound(bounds: DoubleArray,
            rowCount: Int,
            colCount: Int,
            rowIndex: Int,
            colIndex: Int): DoubleArray {
            val width = (bounds[2] - bounds[0]) / colCount.toDouble()
            val height = (bounds[3] - bounds[1]) / rowCount.toDouble()
            return doubleArrayOf(
                bounds[0] + colIndex * width,
                bounds[1] + rowIndex * height,
                bounds[0] + (colIndex + 1) * width,
                bounds[1] + (rowIndex + 1) * height)
        } 
    }
}

// vi: se ts=4 sw=4 et:
