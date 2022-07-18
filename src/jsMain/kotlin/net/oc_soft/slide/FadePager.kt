package net.oc_soft.slide

import kotlin.js.Promise
import org.w3c.dom.HTMLElement

class FadePager {

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

            val fadePage = FadePage()
            fadePage.createSpace(containerElement)
            return createPager(fadePage, pagingOption)
        }

        /**
         * create pager
         */
        fun createPager(
            fadePage: FadePage,
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
                    setupPages(fadePage, numberOfPages, 
                        createPage, getBackground)
                    
                }

                override var page: Int
                    get() {
                        return fadePage.pageIndex
                    }
                    set(value) {
                        fadePage.pageIndex = value
                        fadePage.syncPageWithPageIndex()
                    }

                override var loopPage: Boolean
                    get() {
                        return fadePage.loopPaging 
                    }
                    set(value) {
                        fadePage.loopPaging = value
                    }

                /**
                 * next page
                 */
                override fun nextPage(): Promise<Unit> {
                    return nextPage(fadePage, pagingOption)
                }

                /**
                 * previous page 
                 */
                override fun prevPage(): Promise<Unit> {
                    return prevPage(fadePage, pagingOption)
                }
                
                /**
                 * tear down object
                 */
                override fun destroy() {
                    fadePage.tearDown()
                }
            }
        }
        /**
         * setup pages
         */
        fun setupPages(
            fadePage: FadePage,
            numberOfPages: Int,
            createPage:
                (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
            getBackgournd: (Int)->String?) {
            
            val pageGenerators = 
                Array<Pair<HTMLElement, (HTMLElement)->HTMLElement>>(
                    numberOfPages) {
                createPage(it)
            }
            fadePage.setPages(pageGenerators)
        }


        /**
         * next page
         */
        fun nextPage(
            fadePage: FadePage,
            pagerSetting: Map<String, Any>):Promise<Unit> {
            val nextPageOption = pagerSetting["next-page"]

            return if (nextPageOption != null) {
                fadePage.proceedPage(1, nextPageOption)
            } else {
                Promise.resolve(Unit)
            }
        }

        /**
         * previous page
         */
        fun prevPage(
            fadePage: FadePage,
            pagerSetting: Map<String, Any>):Promise<Unit> {
            val prevPageOption = pagerSetting["previous-page"]

            return if (prevPageOption != null) {
                fadePage.proceedPage(-1, prevPageOption)
            } else {
                Promise.resolve(Unit)
            }
        }
    }
}

// vi: se ts=4 sw=4 et:
