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

class FragmentsPager {

    /**
     * class instance
     */
    companion object {

        /**
         * create pager
         */
        fun createPager(
            containerElement: HTMLElement,
            pagingOption: Map<String, Any>): Pager {

            val fragmentsPage = FragmentsPage()
            fragmentsPage.createSpace(containerElement)
            return createPager(fragmentsPage, pagingOption)
        }

        /**
         * create pager
         */
        fun createPager(
            fragmentsPage: FragmentsPage,
            pagingOption: Map<String, Any>): Pager {

            return object: Pager {
                /**
                 * setup pages
                 */
                override fun setupPages(
                    numberOfPages: Int,
                    createPage:
                        (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
                    getBackground: (Int)->String?) {
                    setupPages(fragmentsPage, numberOfPages, 
                        createPage, getBackground)
                    
                }

                override var page: Int
                    get() {
                        return fragmentsPage.pageIndex
                    }
                    set(value) {
                        fragmentsPage.pageIndex = value
                        fragmentsPage.syncPageWithPageIndex()
                    }

                override var loopPage: Boolean
                    get() {
                        return fragmentsPage.loopPage
                    }
                    set(value) {
                        fragmentsPage.loopPage = value
                    }

                /**
                 * next page
                 */
                override fun nextPage(): Promise<Unit> {
                    return nextPage(fragmentsPage, pagingOption)
                }

                /**
                 * previous page 
                 */
                override fun prevPage(): Promise<Unit> {
                    return prevPage(fragmentsPage, pagingOption)
                }
                
                /**
                 * tear down object
                 */
                override fun destroy() {
                    fragmentsPage.tearDown()
                }
            }
        }
        /**
         * setup pages
         */
        fun setupPages(
            fragmentsPage: FragmentsPage,
            numberOfPages: Int,
            createPage:
                (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
            getBackgournd: (Int)->String?) {
            
            
            fragmentsPage.setupPages(numberOfPages, createPage)
        }


        /**
         * next page
         */
        fun nextPage(
            fragmentsPage: FragmentsPage,
            pagerSetting: Map<String, Any>):Promise<Unit> {
            val nextPageOption = pagerSetting["next-page"]

            return if (nextPageOption != null) {
                fragmentsPage.proceedPage(1, nextPageOption)
            } else {
                Promise.resolve(Unit)
            }
        }

        /**
         * previous page
         */
        fun prevPage(
            fragmentsPage: FragmentsPage,
            pagerSetting: Map<String, Any>):Promise<Unit> {
            val prevPageOption = pagerSetting["previous-page"]

            return if (prevPageOption != null) {
                fragmentsPage.proceedPage(-1, prevPageOption)
            } else {
                Promise.resolve(Unit)
            }
        }
    }
}



// vi: se ts=4 sw=4 et:
