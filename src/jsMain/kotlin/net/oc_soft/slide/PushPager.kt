package net.oc_soft.slide

import kotlin.js.Promise
import org.w3c.dom.HTMLElement

class PushPager {

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

            val pushPage = PushPage()
            pushPage.createSpace(containerElement)
            return createPager(pushPage, pagingOption)
        }

        /**
         * create pager
         */
        fun createPager(
            pushPage: PushPage,
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
                    setupPages(pushPage, numberOfPages, 
                        createPage, getBackground)
                    
                }

                override var page: Int
                    get() {
                        return pushPage.pageIndex
                    }
                    set(value) {
                        pushPage.pageIndex = value
                        pushPage.syncPageWithPageIndex()
                    }

                override var loopPage: Boolean
                    get() {
                        return pushPage.loopPaging 
                    }
                    set(value) {
                        pushPage.loopPaging = value
                    }

                /**
                 * next page
                 */
                override fun nextPage(): Promise<Unit> {
                    return nextPage(pushPage, pagingOption)
                }

                /**
                 * previous page 
                 */
                override fun prevPage(): Promise<Unit> {
                    return prevPage(pushPage, pagingOption)
                }
                
                /**
                 * tear down object
                 */
                override fun destroy() {
                    pushPage.tearDown()
                }
            }
        }
        /**
         * setup pages
         */
        fun setupPages(
            pushPage: PushPage,
            numberOfPages: Int,
            createPage:
                (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
            getBackgournd: (Int)->String?) {
            
            val pageGenerators = 
                Array<Pair<HTMLElement, (HTMLElement)->HTMLElement>>(
                    numberOfPages) {
                createPage(it)
            }
            pushPage.setPages(pageGenerators)
        }


        /**
         * next page
         */
        fun nextPage(
            pushPage: PushPage,
            pagerSetting: Map<String, Any>):Promise<Unit> {
            val nextPageOption = pagerSetting["next-page"]

            return if (nextPageOption != null) {
                pushPage.proceedPage(1, nextPageOption)
            } else {
                Promise.resolve(Unit)
            }
        }

        /**
         * previous page
         */
        fun prevPage(
            pushPage: PushPage,
            pagerSetting: Map<String, Any>):Promise<Unit> {
            val prevPageOption = pagerSetting["previous-page"]

            return if (prevPageOption != null) {
                pushPage.proceedPage(-1, prevPageOption)
            } else {
                Promise.resolve(Unit)
            }
        }
     }
}

// vi: se ts=4 sw=4 et:
