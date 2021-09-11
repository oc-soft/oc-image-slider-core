package net.oc_soft

import kotlin.js.Promise
import kotlin.js.Json
import kotlin.js.json

import kotlin.text.Regex
import kotlin.text.MatchResult

import kotlinx.browser.window

import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
import org.w3c.dom.get

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
                return Regex("/((\\w)(/([-\\w]+))?)/(page/(\\d+)/?)*$") 
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
         * read post query
         */
        fun readPostQuery(): PostQuery {
            var postQuery =  readPostQueryFromLocation()
            if (postQuery == null) {
                postQuery = readPostQueryFromSearch()
            }
            if (postQuery == null) {
                postQuery = readPostQueryFromGlobal()
            }
            return postQuery?: PostQuery()
        }

        /**
         * read post query from window location
         */
        fun readPostQueryFromLocation(): PostQuery? {
            val queryFromPath = readPostQueryFromPath()
            val queryFromSearch = readPostQueryFromSearch()

            return if (queryFromPath != null) {
                val query = queryFromPath
                queryFromSearch?.let {
                    query.lineCountPerPage = it.lineCountPerPage
                    query.tagNames = it.tagNames
                } 
                query 
            } else {
                null
            }
        }

        /**
         * read cagegory and page from window location
         */
        fun readPostQueryFromPath(): PostQuery? {
            val path = window.location.pathname
            return ContentManagement.pathToParamMatcher.find(path)?.let {
                val values = it.groupValues 
                val queryFromGlobal = readPostQueryFromGlobal()
                
                val category: String? = if (values.size > 4) {
                    values[4]
                } else {
                    queryFromGlobal?.let {
                        it.categorySlug
                    }
                }
                val page: Int = if (values.size > 5) {
                    values[5].toIntOrNull()?: 1
                } else {
                    queryFromGlobal?.let {
                        it.page
                    }?: 1
                }
                val lineCountPerPage = queryFromGlobal?.let {
                    it.lineCountPerPage
                }?: 10
                val tagNames = queryFromGlobal?.let {
                    it.tagNames
                }?: Array<String>(0) { "" } 
                PostQuery(page, lineCountPerPage, category, tagNames)
            }
        }


        /**
         * read page and lines per page from document location
         */
        fun readPostQueryFromSearch(): PostQuery? {
            val search = window.location.search
            val result: PostQuery? = if (search.length > 1) {
                val searchParams = URLSearchParams(search.substring(1))
                val queryFromGlobal = readPostQueryFromGlobal()
                var page = 1
                queryFromGlobal?.let {
                    page = it.page
                }
                searchParams.get("page")?.let {
                    it.toIntOrNull()?.let {
                        page = it
                    }
                } 
                var linesPerPage:Int = if (queryFromGlobal != null) {
                    queryFromGlobal.lineCountPerPage
                } else 10
                searchParams.get("lines-per-page")?.let {
                    it.toIntOrNull()?.let {
                        linesPerPage = it
                    } 
                }
                var categorySlug: String? = if (queryFromGlobal != null) {
                    queryFromGlobal.categorySlug
                } else null
                searchParams.get("category-slug")?.let {
                    categorySlug = it
                }

                var tagNames: Array<String> = searchParams.getAll(
                    "tag-names[]")
                if (tagNames.size == 0) {
                    queryFromGlobal?.let {
                        tagNames = it.tagNames
                    }
                }
                PostQuery(page, linesPerPage, categorySlug, tagNames)

            } else {
                null
            }
            return result
        }

        /**
         * read page and lines per page from document location
         */
        fun readPostQueryFromGlobal(): PostQuery? {

            val setting = window["wesgeePostsMgr"]
            val result : PostQuery? = if (setting != null) {
                val postQuery = setting["post-query"]
                if (postQuery != null) {
                    var value = postQuery["page"]
                    val page: Int = if (value != null) {
                        val page: Int = value.toIntOrNull()?: 1 
                        page
                    } else {
                        1
                    }

                    value = postQuery["lines-per-page"]
                    val lineCountPerPage: Int = if (value != null) {
                        value.toIntOrNull()?: 10 
                    } else {
                        10
                    }
                    value = postQuery["category-slug"]
                    val slug: String? = if (value != null) {
                        value as String
                    } else {
                        null
                    }
                    value = postQuery["tag-names"]
                    val tagNames: Array<String> = if (value != null) {
                        val tagNames0 = value
                        Array<String>(tagNames0.length as Int) {
                            tagNames0[it] as String
                        }
                    } else {
                        Array<String>(0) { "" }
                    }
                    PostQuery(page, lineCountPerPage, slug, tagNames) 
                } else {
                    null
                }
            } else {
                null
            }
            return result
        }
        
 
        /**
         * build url
         */
        fun buildUrl(category: String?,
            page: Int, linesPerPage: Int?,
            tagNames: Array<String>): URL {
            val path = window.location.pathname
            val matcher = pathToParamMatcher.find(path)
            val result = if (matcher != null) {
                var newPath = path.substring(0, matcher.range.start)
                newPath += category?.let {
                    val matchValues = matcher.groupValues
                    val categoryDir = matchValues[2]
                    "/${categoryDir}/${category}/page/${page}/"
                } ?: "/page/${page}/" 
                URL(newPath, window.location.origin)
            } else  {
                val url = URL(path, window.location.origin)
                url.searchParams.append("page", page.toString())
                category?.let {
                    url.searchParams.append("slug", it)
                }
                url
            }
            tagNames.forEach {
                result.searchParams.append("tag-names[]", it)
            }
            linesPerPage?.let {
                result.searchParams.append("lines-per-page", it.toString())
            }
            return result
        }
        /**
         * read digests
         */
        fun readPosts(page: Int, linesPerPage: Int,
            categorySlug: String? = null,
            tagNames: Array<String> = Array<String>(0) { "" }): Promise<Json> {
            val url = URL(
                "${ContentManagement.currentSiteDirectory}posts.php",
                window.location.origin)
            val searchParams = url.searchParams
            searchParams.append("read", "")
            searchParams.append("page", page.toString())
            searchParams.append("per_page", linesPerPage.toString())
            categorySlug?.let {
                searchParams.append("category-slug", it)
            }
            tagNames.forEach {
                searchParams.append("tag-names[]", it)
            }
            val response = json()
            return window.fetch(url).then({
                val headers = it.headers
                headers.get("X-WP-Total")?.let {
                    response["total-posts"] = it 
                }
                headers.get("X-WP-TotalPages")?.let {
                    response["total-pages"] = it
                }
                it.json()
            }).then({
                response["posts"] = it
                response
            })
        }

	}
}

// vi: se ts=4 sw=4 et:
