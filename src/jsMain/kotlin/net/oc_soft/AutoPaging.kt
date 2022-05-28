package net.oc_soft

import kotlin.js.Json
import kotlin.js.Promise

import kotlin.collections.MutableList
import kotlin.collections.ArrayList
import kotlin.collections.MutableMap
import kotlin.collections.HashMap

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.url.URL
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTemplateElement
import org.w3c.dom.get

import net.oc_soft.slide.AutoPagingOption

/**
 * page style slide
 */
class AutoPaging(
    /**
     * setting 
     */
    val settingQuery: String) {

    /**
     * paging protocol
     */
    interface Pager {
        /**
         * set all pages
         */
        fun setupPages(
            numberOfPages: Int, 
            createPage: (Int)->HTMLElement)

        /**
         * set page no effect
         */
        var page: Int


        /**
         * next page
         */
        fun nextPage(): Promise<Unit>
    }

    /**
     * paging status
     */
    data class PagingStatus(
        /**
         * contents
         */
        var pages: Array<HTMLElement>,
        /**
         * paging index
         */
        var pageIndex: Int = 0,
        /**
         * paging status promise
         */
        var statusPromise: Promise<Unit>? = null)

    /**
     * pagers
     */
    val pagers: MutableList<Pair<Pager, HTMLElement>?> = 
        ArrayList<Pair<Pager, HTMLElement>?>()


    /**
     * pager setting
     */
    val setting: MutableList<MutableMap<String, Any>> =
        ArrayList<MutableMap<String, Any>>() 
    /**
     * paging container
     */
    var pagingContainer: HTMLElement? = null

    /**
     * paging status
     */
    var pagingStatus: PagingStatus? = null
    

    /**
     * loop paging
     */
    var loopPaging = false

    /**
     * load setting
     */
    fun loadSetting(url: URL):Promise<Unit> {
        val searchParams = url.searchParams
        searchParams.append("action", settingQuery)
        return window.fetch(url).then({
            it.json()
        }).then({
            updateSetting(it as Json)
        })
    }

    /**
     * setup auto paging
     */
    fun setup(
        createPage :(Int)->HTMLElement,
        countOfPages: Int) {
    }

    /**
     * setting
     */
    fun updateSetting(param: Json) {

        val settings = param["settings"]
        val params0 = settings?.let {
            if (it is Array<*>) {
                it as Array<Json>
            } else {
                arrayOf(it as Json)
            }
        }?: emptyArray<Json>()
        params0.forEachIndexed {
            idx, elem ->
            val settingElem = if (idx >= setting.size) {
                setting.add(HashMap<String, Any>())
                setting[idx]
            } else {
                setting[idx].clear()
                setting[idx]
            }

            val keys = js("Object.keys(elem)")

            for (i in 0 until (keys.length as Int)) {
                val key = keys[i] as String
                settingElem[key] = elem[key] as Any
            }
        }
        while (params0.size > setting.size) {
            setting.removeAt(setting.lastIndex)
        }
        val control = param["control"]
        control?.let {
            val control0 = it as Json 
            it["loop"]?.let { 
                this.loopPaging = when (it) {
                is Boolean ->  it 
                is String -> it.toBoolean()
                else -> this.loopPaging
                }
            }
        }
    }


    /**
     * attach this object to html elelements
     */
    fun bind(
        pagingContainer: HTMLElement) {

        this.pagingContainer = pagingContainer
    }

    /**
     * detach this object from html elelements
     */
    fun unbind() {
    }


    /**
     * setup paging container
     */
    fun setupPagingContainer() {

        pagers.clear()
        pagingContainer?.let { 
            val pagerParent = it 
            setting.forEachIndexed {
                idx, pagerSetting ->

                val pagerContainer = createPagingContainerElement()
                pagerParent.append(pagerContainer)
                val elem = AutoPagingOption.createPager(
                    pagerContainer, pagerSetting)?.let {
                    Pair(it, pagerContainer)
                }
                pagers.add(elem) 
                pagerContainer.remove()

            }
        }
    }

    /**
     * tear down paging cotainer
     */
    fun teardownPaingContainer() {
        pagers.forEach {
            it?.let {
                it.second.remove()
            }
        }
    }

    /**
     * create paging container element
     */
    fun createPagingContainerElement(): HTMLElement {

        val result = document.createElement("div") as HTMLElement

        result.style.top = "0px"
        result.style.left = "0px"
        result.style.bottom = "0px"
        result.style.right = "0px"
        result.style.position = "relative"
        result.classList.add("pager")
        result.classList.add("container")
        result.classList.add("root")
        return result
    }


    /**
     * load content templates
     */
    fun loadContentTemplates(): Array<HTMLElement> {
        return pagingContainer?.let {
            it.querySelector("template")?.let {
                val tmpl = it as HTMLTemplateElement
                

                val children = tmpl.content.children
                Array<HTMLElement>(children.length) {
                    children[it] as HTMLElement
                }
            }?: emptyArray<HTMLElement>()
        }?: emptyArray<HTMLElement>()
    }
   
    /**
     * animation sequence
     */
    fun animationSequence0(
        pages: Array<HTMLElement>) {
        pagingContainer?.let {
            val pagerContainer = it
            val createPage: (Int) -> HTMLElement = {
                pages[it].cloneNode(true) as HTMLElement
            } 

            pagers.forEach {
                it ?.let {
                    pagerContainer.append(it.second)
                    it.first.setupPages(pages.size, createPage) 
                    it.second.remove()
                }
            }  
        }
    }


    /**
     * get pager by index
     */
    fun getPager(
        pageIndex: Int): Pair<Pager, HTMLElement>? {
        return if (pagers.size > 0) {
            pagers[pageIndex % pagers.size]
        } else null
    }

    /**
     * prepare auto play
     */
    fun prepareAutoPlay() {
        pagingContainer?.let {
            val pagerContainer = it
            val pages = loadContentTemplates()   
            val pagingStatus = PagingStatus(pages)
            animationSequence0(pages)
            getPager(pagingStatus.pageIndex)?.let {
                pagerContainer.append(it.second)
                it.first.page = pagingStatus.pageIndex
            }
            this.pagingStatus = pagingStatus
        }
    } 

    /**
     * play automatically
     */
    fun autoPlay(): Promise<Unit> {
        var pagingStatus = this.pagingStatus
        return if (pagingStatus == null) {
            prepareAutoPlay()
            pagingStatus = this.pagingStatus!!
            pagingStatus.statusPromise = autoPlay0()
            pagingStatus.statusPromise!!
        } else {
            pagingStatus.statusPromise!!
        }
    }

    /**
     * play automatically
     */
    fun autoPlay0(): Promise<Unit> {
        return pagingStatus?.let {
            Promise<Unit>() {
                resolve, _ ->
                val pagingStatus0 = it

                val pagingAnim = getPager(pagingStatus0.pageIndex)?.let {
                    it.first.nextPage()
                }?: Promise.resolve(Unit)

                pagingAnim.then {
                    getPager(pagingStatus0.pageIndex)?.let {
                        it.second.remove()
                    }

                    pagingStatus0.pageIndex++ 
                    if (loopPaging) {
                        pagingStatus0.pageIndex %= pagingStatus0.pages.size
                    }

                    if (pagingStatus0.pageIndex < pagingStatus0.pages.size) {
                         getPager(pagingStatus0.pageIndex)?.let {
                            pagingContainer!!.append(it.second)
                            it.first.page = pagingStatus0.pageIndex
                        }
                        autoPlay0()
                    } else {
                        this.pagingStatus = null
                        resolve(Unit)
                    }
                }
            }
        }?: Promise.resolve(Unit)
    }
}

// vi: se ts=4 sw=4 et:
