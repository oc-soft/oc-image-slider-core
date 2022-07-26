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
 * paging direction
 */
enum class PagingDirection(
    /**
     * displacement
     */
    val displacement: Int) {
    /**
     * proceed a page forward
     */
    FORWARD(1),
    /**
     * proceed a page backward
     */
    BACKWARD(-1);

    companion object {
        
        /**
         * int to direction
         */
        fun intToDirection(value: Int): PagingDirection {
            return if (value >= 0) {
                FORWARD
            } else {
                BACKWARD
            }
        }
    }

}


// vi: se ts=4 sw=4 et:
