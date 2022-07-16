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

