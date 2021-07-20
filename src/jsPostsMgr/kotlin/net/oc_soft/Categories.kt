package net.oc_soft

import kotlin.js.Json
import kotlin.js.Promise

import kotlin.collections.Map
import kotlin.collections.HashMap

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

        /**
         * read all categories
         */
        fun readCategories(): Promise<Json> {
            val url = URL(
                "${ContentManagement.currentSiteDirectory}categories.php",
                window.location.origin)
            val searchParams = url.searchParams
            searchParams.append("read", "")
            return window.fetch(url).then({
                it.json() as Json
            })
        }

        /**
         * read all categories
         */
        fun readCategoriesAsMap(): Promise<Map<Int, Json>> {
            return readCategories().then({
                val response = HashMap<Int, Json>()
                val jsObj: dynamic = it
                val len = jsObj.length.toString().toInt()
                for (idx in 0 until len) {
                    val categoryEntry = jsObj[idx] as Json
                    response[categoryEntry["id"] as Int] = categoryEntry
                }
                response
            })
        }
    } 
}

// vi: se ts=4 sw=4 et:
