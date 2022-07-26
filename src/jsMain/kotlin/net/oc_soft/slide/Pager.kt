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
package net.oc_soft.slide

import kotlin.js.Promise
import org.w3c.dom.HTMLElement

/**
 * paging protocol
 */
interface Pager {
    /**
     * set all pages
     */
    fun setupPages(
        numberOfPages: Int, 
        createPage: (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
        getBackground: (Int)->String?)

    /**
     * set page no effect
     */
    var page: Int


    /**
     * endless page setting
     */
    var loopPage: Boolean

    /**
     * next page
     */
    fun nextPage(): Promise<Unit>


    /**
     * previous page
     */
    fun prevPage(): Promise<Unit>


    /**
     * release holding resource
     */
    fun destroy()
}

// vi: se ts=4 sw=4 et:

