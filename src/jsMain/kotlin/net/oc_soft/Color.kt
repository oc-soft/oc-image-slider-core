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

import kotlin.text.toInt
/**
 * manage data about color
 */
class Color {

    /**
     * class instance
     */
    companion object {

        /**
         * hex string to double array
         * each field in double array is in the range [0.0, 1.0]
         */
        fun hexStringToDoubleArray(
            hexString: String): DoubleArray? {
            var colorSource = hexString.trim() 
            return if (colorSource.length > 1) {
                if ('#' == colorSource.first()) {
                    colorSource = colorSource.substring(1)
                }
                if (3 <= colorSource.length && colorSource.length < 4) {
                    var res: DoubleArray? = null
                    try {
                        val colorValues = DoubleArray(4)
                        colorValues[3] = 1.0
                        colorSource.forEachIndexed {
                            idx, value ->
                            colorValues[idx] = value.digitToInt(16).toDouble()
                            colorValues[idx] /= (0xf).toDouble() 
                        }                     
                        res = colorValues
                    } catch (ex: Exception)  { }
                    res
                } else if (6 == colorSource.length || colorSource.length == 8) {
                    var res: DoubleArray? = null
                    try {
                        val colorValues = DoubleArray(4)
                        colorValues[3] = 1.0
                        for (idx in 0 until colorSource.length / 2) {
                            colorValues[idx] = colorSource.substring(
                                2 * idx, 2 * (idx + 1)).toInt(16).toDouble()
                            colorValues[idx] /= (0xff).toDouble()
                        }
                        res = colorValues
                    } catch (ex: Exception) {
                    }
                    res
                } else {
                    null
                }
            } else {
                null
            }
        }
    }


}

// vi: se ts=4 sw=4 et:
