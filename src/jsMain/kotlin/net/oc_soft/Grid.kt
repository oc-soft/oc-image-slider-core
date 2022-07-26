/*
 * Copyright 2022 oc-soft
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
