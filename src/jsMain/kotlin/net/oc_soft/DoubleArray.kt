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
            if (it <= 0xf) {
                "0${it.toString(16)}"
            } else {
                it.toString(16) 
            }
        })
    return "#${hexStr}"
}


// vi: se ts=4 sw=4 et:
