package net.oc_soft

import kotlin.text.Regex
import kotlin.text.MatchResult

import kotlinx.browser.window

import org.w3c.dom.url.URL

/**
 * content management system
 */
class ContentManagement {

	/**
	 * class instance
	 */
	companion object {
        /**
         * regular expression to convert path to query 
         */
        val pathToParamMatcher: Regex
            get() {
                return Regex("/c/([-\\w]+)/(page/(\\d+)/?)*$") 
            }


        /**
         * current site directory
         */
        val currentSiteDirectory: String
            get() {
                val path = window.location.pathname
                return pathToParamMatcher.find(path)?.let {
                    path.substring(0, it.range.start + 1)
                    
                }?: if (path.length > 1) {
                        val lastIdx = path.lastIndexOf('/')
                        path.substring(0, lastIdx + 1)
                    } else {
                        "/"
                    }
                
            }

        /**
         * build url
         */
        fun buildUrl(slug: String, page: Int, linesPerPage: Int?): URL {
            val path = window.location.pathname
            val matcher = pathToParamMatcher.find(path)
            val result = if (matcher != null) {
                var newPath = path.substring(0, matcher.range.start)
                newPath += "/c/${slug}/page/${page}/"
                URL(newPath, window.location.origin)
            } else  {
                val url = URL(path, window.location.origin)
                url.searchParams.append("page", page.toString())
                url.searchParams.append("slug", slug)
                url
            }
            linesPerPage?.let {
                result.searchParams.append("lines-per-page", it.toString())
            }
            return result
        }
	}
}

// vi: se ts=4 sw=4 et:
