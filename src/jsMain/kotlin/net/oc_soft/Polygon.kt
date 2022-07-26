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
