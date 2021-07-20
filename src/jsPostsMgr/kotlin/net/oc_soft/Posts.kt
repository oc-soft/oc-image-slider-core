package net.oc_soft


import kotlin.text.Regex
import kotlin.text.MatchResult
import kotlin.collections.Map

import kotlin.js.Promise
import kotlin.js.Json
import kotlin.js.JSON
import kotlin.js.json
import kotlin.js.Date

import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams

import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

import org.w3c.dom.DocumentFragment
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableSectionElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTemplateElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.set
import org.w3c.dom.get

import org.w3c.fetch.Headers

import kotlin.text.toIntOrNull

import kotlinx.browser.window
import kotlinx.browser.document



/**
 * posts
 */
class Posts {

    /**
     * class instance
     */
    companion object {

        /**
         * read post query from window location
         */
        fun readPostQueryFromLocation(): PostQuery {
            val queryFromPath = readPostQueryFromPath()
            val queryFromSearch = readPostQueryFromSearch()
            return if (queryFromPath != null) {
                val query = queryFromPath
                queryFromSearch?.let {
                    query.lineCountPerPage = it.lineCountPerPage
                } 
                query 
            } else {
                queryFromSearch?: PostQuery()
            }
        }

        /**
         * read cagegory and page from window location
         */
        fun readPostQueryFromPath(): PostQuery? {
            val path = window.location.pathname
            return ContentManagement.pathToParamMatcher.find(path)?.let {
                val values = it.groupValues 
                
                val slug: String? = if (values.size > 1) {
                    values[1]    
                } else {
                    null
                }
                val page: Int = if (values.size > 3) {
                    values[3].toIntOrNull()?: 1
                } else {
                    1
                }
                PostQuery(page, 10, slug)
            }
        }

        /**
         * read page and lines per page from document location
         */
        fun readPostQueryFromSearch(): PostQuery? {
            val search = window.location.search
            val result: PostQuery? = if (search.length > 1) {
                val searchParams = URLSearchParams(search.substring(1))
                val page: Int = searchParams.get("page")?.let {
                    val page: Int = it.toIntOrNull()?.let {
                        it
                    }?: 1 
                    page
                }?: 1 
                val linesPerPage: Int = 
                    searchParams.get("lines-per-page")?.let {
                    val linesPerPage = it.toIntOrNull()?.let {
                        it
                    }?: 10 
                    linesPerPage
                }?: 10
                val slug: String? = searchParams.get("slug")
                    
                PostQuery(page, linesPerPage, slug)
            } else {
                null
            }
            return result
        }

        /**
         * read digests
         */
        fun readPosts(page: Int, linesPerPage: Int,
            categorySlug: String? = null): Promise<Json> {
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
    /**
     * digest table
     */
    val digestTableElementUi: HTMLElement?
        get() {
            return document.body?.querySelector(".list-of-posts")
                as HTMLElement?
        }

    
    /**
     * body object to show digest
     */
    val digestBodyElementUi: HTMLTableSectionElement?
        get() {
            return this.digestTableElementUi?.let {
                it.querySelector("tbody") as HTMLTableSectionElement?
            }
        }

    
    /**
     * the input to control number of lines in page
     */
    val linesPerPageInputElementUi: HTMLInputElement?
        get() {
            return document.body?.querySelector(".lines-per-page")
                as HTMLInputElement? 
        }
    /**
     * the input to control page number
     */
    val pageNumberInputElementUi: HTMLInputElement?
        get() {
            return document.body?.querySelector(".page")
                as HTMLInputElement? 
        }

    /**
     * max page number input
     */
    var maxPageNumberUi: Int?
        get() {
            return pageNumberInputElementUi?.let {
                it.getAttribute("max")?.let { it.toIntOrNull() }
            }
        }
        set(value) {
            pageNumberInputElementUi?.let {
                val valueStr = value?.let { it.toString() }?: ""
                it.setAttribute("max", valueStr)
            }
        }

    /**
     * the container element to show total page number
     */
    val numberOfPageContainerElementUi: HTMLElement?
        get() {
            return document.body?.querySelector(".number-of-pages")
                as HTMLElement?
        }

    /**
     * the input to control number of lines in page
     */
    var linesPerPageUi: Int?
        get() {
            return linesPerPageInputElementUi?.let {
                it.value.toIntOrNull()
            }
        }
        set(value) {
            val valueStr = value?.let { value.toString() }?: "" 
            linesPerPageInputElementUi?.let {
                it.value = valueStr 
            }
        }
    /**
     * the input to control page number
     */
    var pageNumberUi: Int?
        get() {
            return pageNumberInputElementUi?.let {
                it.value.toIntOrNull()
            }
        }
        set(value) {
            val valueStr = value?.let { value.toString() }?: ""
            pageNumberInputElementUi?.let {
                it.value = valueStr 
            }
        }

    /**
     * number of pages in user interface
     */
    var numberOfPageUi: Int?
        get() {
            return numberOfPageContainerElementUi?.let {
                it.innerHTML.toIntOrNull()
            }
        }
        set(value) {
            val valueStr = value?.let { it.toString() } ?: ""
            numberOfPageContainerElementUi?.let {
                it.innerHTML = valueStr
            }
        }

    /**
     * read slug 
     */
    val categorySlug: String? get() = readPostQueryFromLocation().categorySlug


 
    /**
     * posts page related parameter handler
     */
    var postParameterInputHdlr: ((Event)->Unit)? = null

    /**
     * post page related parameter handler
     */
    var postParameterByLineNumberPerPageHdlr: ((Event)->Unit)? = null

    /**
     * bind this object into html element
     */
    fun bind() {
        this.bindDigest()
        syncUiWithUrlParam()
    }

    /**
     * detach this object from html element
     */

    fun unbind() {
        this.unbindDigest()
    }

    /**
     * synchronize user interface with current search params
     */
    fun syncUiWithUrlParam() {
        val postQuery = readPostQueryFromLocation()

        pageNumberUi = postQuery.page
        linesPerPageUi = postQuery.lineCountPerPage
        startUpdateDigestTable() 
    }

    /**
     * bind this object into html element to show posts digest
     */
    fun bindDigest() {
        val postParameterInputHdlr: (Event)->Unit = {
            this.updatePostsDigest(it)
        }
        val postParameterByLineNumberPerPageHdlr: (Event)->Unit = {
            this.updatePostsDigestByLineCountPerPage(it)
        }

        linesPerPageInputElementUi?.let {
            it.addEventListener("blur", postParameterByLineNumberPerPageHdlr)
            it.addEventListener("focus",
                postParameterByLineNumberPerPageHdlr)
            it.addEventListener("keydown",
                postParameterByLineNumberPerPageHdlr)
        }
        pageNumberInputElementUi?.let {
            it.addEventListener("blur", postParameterInputHdlr)
            it.addEventListener("focus", postParameterInputHdlr)
            it.addEventListener("keydown", postParameterInputHdlr)
        }
        this.postParameterByLineNumberPerPageHdlr =
            postParameterByLineNumberPerPageHdlr
        this.postParameterInputHdlr = postParameterInputHdlr
    }

    /**
     * detach this object from html element to show posts digest
     */
    fun unbindDigest() {
        linesPerPageInputElementUi?.let {
            it.removeEventListener("blur",
                postParameterByLineNumberPerPageHdlr)
            it.removeEventListener("focus",
                postParameterByLineNumberPerPageHdlr )
            it.removeEventListener("keydown",
                postParameterByLineNumberPerPageHdlr)
        }
        pageNumberInputElementUi?.let {
            it.removeEventListener("blur", postParameterInputHdlr)
            it.removeEventListener("focus", postParameterInputHdlr)
            it.removeEventListener("keydown", postParameterInputHdlr)
        }
        this.postParameterInputHdlr = null
    }

    /**
     * update posts digest with user interface 
     */
    fun updatePostsDigest(event: Event) {

        var doUpdate = false

        if (event.type == "blur") {
            val inputElement = event.currentTarget as HTMLInputElement
            val lastInput = readLastInput(inputElement)
            doUpdate = lastInput?.let {
                it != inputElement.value
            }?: false
            
        } else if (event.type == "keydown") {
            val keyEvent = event as KeyboardEvent
            doUpdate = if ("Enter" == keyEvent.code) {
                val inputElement = event.currentTarget as HTMLInputElement
                inputElement.reportValidity()
            } else {
                false
            }
        } else if (event.type == "focus") {
            saveLastInput(event.currentTarget as HTMLInputElement)
        }
        if (doUpdate) {
            startUpdateDigestTable().then {
                if (it) {
                    appendHistoryWithCurrentUi()
                } 
            }
        }
    }

    /**
     * update posts digest with user interface 
     */
    fun updatePostsDigestByLineCountPerPage(event: Event) {

        var doUpdate = false

        if (event.type == "blur") {
            val inputElement = event.currentTarget as HTMLInputElement
            val lastInput = readLastInput(inputElement)
            doUpdate = lastInput?.let {
                it != inputElement.value
            }?: false
            
        } else if (event.type == "keydown") {
            val keyEvent = event as KeyboardEvent
            doUpdate = if ("Enter" == keyEvent.code) {
                val inputElement = event.currentTarget as HTMLInputElement
                inputElement.reportValidity()
            } else {
                false
            }
        } else if (event.type == "focus") {
            saveLastInput(event.currentTarget as HTMLInputElement)
        }
        if (doUpdate) {
            pageNumberUi = 1 
            startUpdateDigestTable().then {
                if (it) {
                    appendHistoryWithCurrentUi()
                } 
            }
        }
    }


    /**
     * save last input
     */
    fun saveLastInput(element: HTMLInputElement) {
        element.dataset["lastInput"] = element.value
    }

    /**
     * read last input value
     */
    fun readLastInput(element: HTMLElement): String? {
        return element.dataset["lastInput"]
    }

    /**
     * append browser history with current user interface status
     */
    fun appendHistoryWithCurrentUi() {
        val categorySlug = this.categorySlug
        val linesPerPage = this.linesPerPageUi
        val pageNumber = this.pageNumberUi 
        if (categorySlug != null && pageNumber != null) {
            val url = ContentManagement.buildUrl(
                categorySlug, pageNumber, linesPerPage)         
            window.history.pushState(null, document.title, url.toString())
        } 
    }
    

    /**
     * do start update digest table
     */
    fun startUpdateDigestTable(): Promise<Boolean> {
        val categorySlug = this.categorySlug
        val linesPerPage = this.linesPerPageUi
        val pageNumber = this.pageNumberUi 
        return if (linesPerPage != null && pageNumber != null) {
            readPosts(pageNumber, linesPerPage, categorySlug).then({
                updatePostsTable(it)
            })
        } else {
            Promise<Boolean> {
                resolve, reject ->
                resolve(false)
            } 
        }
    }


    /**
     * update posts table
     */
    fun updatePostsTable(postsContainer: Json): Boolean {
        val posts = postsContainer["posts"] as Json
        return if (posts["code"] == null) {
            clearPosts()
            addPosts(posts)
            numberOfPageUi = postsContainer["total-pages"]?.let { 
                it.toString().toIntOrNull()
            } 
            maxPageNumberUi = numberOfPageUi
            true
        } else {
            false
        }
    }

    /**
     * clear posts table rows
     */
    fun clearPosts() {
        digestBodyElementUi?.let {
            while (it.childElementCount > 0) {
                it.lastElementChild?.remove()
            }
        }
    }
    
    /**
     * add posts
     */
    fun addPosts(posts: Json) {
        digestBodyElementUi?.let {
            val body = it
            val rows: dynamic = posts 
            for (idx in 0 until rows.length as Int) {
                val row = createDigestRow(rows[idx] as Json)
                body.append(row)
            }
        }
    }

    /**
     * get digest row template
     */
    fun getDigestRowTemplate(): HTMLTemplateElement? {
        return document.body?.querySelector("#tmpl-posts-digest")
            as HTMLTemplateElement?
    }

    /**
     * get digest row cell template
     */
    fun getCellTemplate(): HTMLTemplateElement? {
        return document.body?.querySelector("#tmpl-post-cell")
            as HTMLTemplateElement?
    }

    /**
     * create digest row
     */
    fun createDigestRow(post: Json): HTMLElement {
        var rowElement: HTMLElement? = null
        getDigestRowTemplate()?.let {
            val rowContainer = it.content.cloneNode(true) as DocumentFragment
            val row = rowContainer.firstElementChild 
                as HTMLElement
            fillDigestRow(row, post) 
            rowElement = row
        }
        return rowElement!!
    }

    /**
     * create cell element with post
     */
    fun createCellElement(post: Json): HTMLElement {
        var cellElement: HTMLElement? = null

        getCellTemplate()?.let {
            val fragment = it.content.cloneNode(true) as DocumentFragment
            val cell = fragment.firstElementChild as HTMLAnchorElement 
            val postId = post["id"] as Int
            cell.pathname += "${postId.toString()}/"
            cellElement = cell 
        }
        return cellElement!!
    }

    /**
     * fill digest row with row
     */
    fun fillDigestRow(row: HTMLElement, post: Json) {
        val fillProcs: Array<Pair<String, (HTMLElement, Any)->Unit>> 
            = arrayOf(
                "date" to 
                    {
                        elem, dateStr ->  
                        val dt = Date(dateStr as String) 
                        val year = dt.getFullYear()
                        val month = dt.getMonth()
                        val date = dt.getDate()
                        elem.innerHTML = "${year}.${month}.${date}"
                    },
                "title" to
                    {
                        elem, title ->
                        elem.innerHTML = (title as Json)["rendered"] as String
                    }
            )
        fillProcs.forEachIndexed {
            idx, elem ->
            val cell = createCellElement(post)
            elem.second(cell, post[elem.first]!!)
            (row.children[idx] as Element).append(cell)
        }
    }

    
}

// vi: se ts=4 sw=4 et:
