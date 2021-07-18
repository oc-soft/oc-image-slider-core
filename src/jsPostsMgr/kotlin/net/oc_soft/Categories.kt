package net.oc_soft

import kotlin.js.Json
import kotlin.js.Promise

import kotlinx.browser.window
import org.w3c.dom.url.URL

/**
 * manage categories
 */
class Categories {

    /**
     * class instance
     */
    companion object {

        /**
         * read 
         */
        fun readCategory(slug: String): Promise<Json> {
            val url = URL(
                "${ContentManagement.currentSiteDirectory}posts.php",
                window.location.origin)
            val searchParams = url.searchParams
            searchParams.append("read", "")
            searchParams.append("slug", slug)
            return window.fetch(url).then({
                it.json() as Json
            })
        }        
    } 
}

// vi: se ts=4 sw=4 et:
