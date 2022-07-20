package net.oc_soft

import kotlin.js.Json
import kotlin.js.JSON
import kotlin.js.Promise

import kotlin.collections.MutableList
import kotlin.collections.ArrayList
import kotlin.collections.MutableMap
import kotlin.collections.HashMap

import kotlinx.browser.window
import kotlinx.browser.document

import kotlinx.js.Object

import org.w3c.dom.url.URL
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTemplateElement
import org.w3c.dom.get

import net.oc_soft.slide.PagerOption
import net.oc_soft.slide.Pager

/**
 * page style slide
 */
class Paging(
    /**
     * setting 
     */
    val settingQuery: String) {

    /**
     * paging status
     */
    data class PagingStatus(
        /**
         * contents
         */
        var pages: Array<Pair<HTMLElement, (HTMLElement)->HTMLElement>>,
        /**
         * paging index
         */
        var pageIndex: Int = 0,
        /**
         * paging status promise
         */
        var statusPromise: Promise<Unit>? = null,
        /**
         * operate to stop paging
         */
        var stopPaging: Boolean = false)

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
     * page index
     */
    var pageIndex: Int?
        get() {
            return pagingStatus?.let {
                it.pageIndex
            }
        }
        set(value) {
            pagingStatus?.let {
                val status = it
                value?.let {
                    if (status.pageIndex != it) {
                        status.pageIndex = it
                        syncPageWithPager()
                        notifyEvent("page-index")
                    }
                }
            }
        }

    /**
     * loop paging
     */
    var loopPaging : Boolean = false
        set(value) {
            field = value
            syncPagersWithLoopPaging()
        }

    /**
     * auto paging direction
     */
    var autoPagingDirection: PagingDirection = PagingDirection.FORWARD


    /**
     * auto paging stop duration
     */
    var autoPagingStopDuration = 0

    /**
     * page contents loader
     */
    var contentsLoader: ((HTMLElement)->Array<
        Pair<HTMLElement, (HTMLElement)->HTMLElement>>)? = null


    /**
     * load backgrounds
     */
    var contentBackgroundsLoader: (()->Array<String>)? = null



    /**
     * event listener
     */
    val eventListeners: 
        MutableMap<String?, MutableList<(String, Paging)->Unit>> =
            HashMap<String?, MutableList<(String, Paging)->Unit>>()
        
    /**
     * load setting
     */
    fun loadSetting(url: URL?):Promise<Unit> {

        val settings = loadSettings()
        return if (settings != null) { 
            updateSetting(settings)
            Promise.resolve(Unit)
        } else {
            url?.let {
                val searchParams = url.searchParams
                searchParams.append("action", settingQuery)
                return window.fetch(url).then({
                    it.json()
                }).then({
                    updateSetting(it as Json)
                })
            }?: Promise.reject(IllegalArgumentException())
        }
    }

    /**
     * load settings from template
     */
    fun loadSettings():Json? {
        var settings = loadSettingsRaw()
        if (settings == null) {
            settings = loadSettings64()
        }
        return settings
    }


    /**
     * load settings from template as json format
     */
    fun loadSettingsRaw(): Json? {
        return pagingContainer?.let {
            it.querySelector("template.settings")?.let {
                val tmpl = it as HTMLTemplateElement
                tmpl.content.firstElementChild?.let {
                    val bgContainer = it as HTMLElement
                    JSON.parse<Json>(bgContainer.innerText)
                }
            }
        }
    }

    /**
     * load settings from template as json format
     */
    fun loadSettings64(): Json? {
        return pagingContainer?.let {
            it.querySelector("template.settings-b64")?.let {
                val tmpl = it as HTMLTemplateElement
                tmpl.content.firstElementChild?.let {
                    val bgContainer = it as HTMLElement
                    JSON.parse<Json>(
                        window.atob(bgContainer.innerText))
                }
            }
        }
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

            Object.keys(elem).forEach {
                val key = it
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

            val dirIdx = it["auto-direction"]?.let {
                when (it) {
                    is String -> it.toInt()
                    is Number -> it.toInt()
                    else -> 1
                }
            }?: 1

            autoPagingDirection = PagingDirection.intToDirection(dirIdx)

            it["auto-paging-stop-duration"]?.let {
                autoPagingStopDuration = when (it) {
                    is String -> it.toInt()
                    is Number -> it.toInt()
                    else -> autoPagingStopDuration
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
        pagers.forEach {
            it?.let {
                val (pager, elem) = it
                pager.destroy() 
                elem.remove()
            }
        }
        this.pagingContainer = null
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

                val elem = PagerOption.createPager(
                    pagerContainer, pagerSetting)?.let {
                    Pair(it, pagerContainer)
                }
                pagers.add(elem) 
                pagerContainer.remove()
            }
            syncPagersWithLoopPaging()
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

        result.style.position = "relative"
        result.style.width = "100%"
        result.style.height = "100%"
        result.classList.add("pager")
        result.classList.add("container")
        result.classList.add("root")
        return result
    }

    /**
     * load contents
     */
    fun loadContents(
        pagerContainer: HTMLElement): 
        Array<Pair<HTMLElement, (HTMLElement)->HTMLElement>> {
        return contentsLoader?.let {
            it(pagerContainer)
        }?: loadContentTemplates()
    }

    /**
     * load content templates
     */
    fun loadContentTemplates()
        : Array<Pair<HTMLElement, (HTMLElement)->HTMLElement>> {
        return pagingContainer?.let {
            it.querySelector("template.pages")?.let {
                val tmpl = it as HTMLTemplateElement
                

                val children = tmpl.content.children
                Array<Pair<HTMLElement, 
                    (HTMLElement)->HTMLElement>>(children.length) {
                    val cloneElem: (HTMLElement)->HTMLElement = {
                        it.cloneNode(true) as HTMLElement
                    }
                    Pair(children[it] as HTMLElement, cloneElem)
                }
            }?: emptyArray<Pair<HTMLElement, (HTMLElement)->HTMLElement>>()
        }?: emptyArray<Pair<HTMLElement, (HTMLElement)->HTMLElement>>()
    }

    
    /**
     * load background setting 
     */
    fun loadContentBackgrounds(): Array<String> {
        return contentBackgroundsLoader?.let {
            it()
        }?: loadContentBackgroundsByData()
    }

    /**
     * load backgrounds from template
     */
    fun loadContentBackgroundsByData(): Array<String> {
        var backgrounds = loadContentBackgroundsRaw()

        if (backgrounds == null) {
            backgrounds = loadContentBackgrounds64()
        }
        return backgrounds?.let {
            it
        }?: emptyArray<String>()
    }

    /**
     * load backgrounds from template
     */
    fun loadContentBackgroundsRaw(): Array<String>? {
        return pagingContainer?.let {
            it.querySelector("template.backgrounds")?.let {
                val tmpl = it as HTMLTemplateElement
                tmpl.content.firstElementChild?.let {
                    val bgContainer = it as HTMLElement
                    JSON.parse<Array<String>>(bgContainer.innerText)
                }
            }
        }
    }


    /**
     * load backgrounds from template
     */
    fun loadContentBackgrounds64(): Array<String>? {
        return pagingContainer?.let {
            it.querySelector("template.backgrounds-b64")?.let {
                val tmpl = it as HTMLTemplateElement
                tmpl.content.firstElementChild?.let {
                    val bgContainer = it as HTMLElement
                    JSON.parse<Array<String>>(window.atob(
                        bgContainer.innerText))
                }
            }
        }
    }
   
    /**
     * animation sequence
     */
    fun animationSequence0(
        pages: Array<Pair<HTMLElement, (HTMLElement)->HTMLElement>>,
        backgrounds: Array<String>) {
        pagingContainer?.let {
            val pagerContainer = it
            val createPage: 
                (Int) -> Pair<HTMLElement, (HTMLElement)->HTMLElement> = {
                val elem = pages[it].second(pages[it].first)
                Pair(elem, pages[it].second)
            } 

            val getBackgrounds: (Int)-> String? = {
                if (it in backgrounds.indices) {
                    backgrounds[it]
                } else null
            }

            pagers.forEach {
                it ?.let {
                    pagerContainer.append(it.second)
                    it.first.setupPages(pages.size, createPage, getBackgrounds) 
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
     * prepare play
     */
    fun preparePlay() {
        pagingContainer?.let {
            val pagerContainer = it
            val pages = loadContents(pagerContainer)  
            val backgrounds = loadContentBackgrounds()
            val pagingStatus = PagingStatus(pages)
            animationSequence0(pages, backgrounds)
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
        
        if (pagingStatus == null) {
            preparePlay()
        }
        pagingStatus = this.pagingStatus!!        
        if (pagingStatus.statusPromise == null) { 
            pagingStatus.statusPromise = autoPlay0()
        }
        return pagingStatus.statusPromise!!
    }

    /**
     * play automatically
     */
    fun autoPlay0(): Promise<Unit> {
        return pagingStatus?.let {
            Promise<Unit>() {
                resolve, _ ->
                val pagingStatus0 = it
                val direction = autoPagingDirection

                var doPaging = if (loopPaging) {
                    pagingStatus0.pages.size > 1
                } else {
                    val nextIdx = pagingStatus0.pageIndex + 
                        direction.displacement
                    nextIdx in pagingStatus0.pages.indices
                }
                if (doPaging && pagingStatus0.stopPaging) {
                    doPaging = false
                }
                
                if (doPaging) {
                    val pagingAnim = getPager(pagingStatus0.pageIndex)?.let {
                        val (pager, _) = it
                        if (direction == PagingDirection.FORWARD) {
                            pager.nextPage()
                        } else {
                            pager.prevPage() 
                        }
                    }?: Promise.resolve(Unit)

                    pagingAnim.then {
                        val lastPager = getPager(pagingStatus0.pageIndex)

                        pagingStatus0.pageIndex += direction.displacement
                        if (loopPaging) {
                            val pageSize = pagingStatus0.pages.size
                            var pageIdx = pagingStatus0.pageIndex % pageSize 
                            pageIdx += pageSize 
                            pageIdx %= pageSize
                            pagingStatus0.pageIndex = pageIdx
                        }
                        val currentPager = getPager(pagingStatus0.pageIndex)
                        if (currentPager != lastPager) { 
                            currentPager?.let {
                                pagingContainer!!.append(it.second)
                            }
                            lastPager?.let {
                                it.second.remove()
                            }
                        }
                        currentPager?.let {
                            it.first.page = pagingStatus0.pageIndex
                        }
                        window.setTimeout({
                            autoPlay0()
                        }, autoPagingStopDuration)
                    }
                } else {
                    pagingStatus?.let {
                        it.statusPromise = null
                    }
                    resolve(Unit)
                }
            }
        }?: Promise.resolve(Unit)
    }

    /**
     * play automatically
     */
    fun proceedPage(forward: Boolean): Promise<Unit> {
        var pagingStatus = this.pagingStatus
        
        if (pagingStatus == null) {
            preparePlay()
        }
        pagingStatus = this.pagingStatus!!        
        
        val statusPromise = pagingStatus.statusPromise
        return if (statusPromise == null) { 
            val statusPromise0 = proceedPage0(forward).then {
                pagingStatus.statusPromise = null    
                Unit
            }
            pagingStatus.statusPromise = statusPromise0
            statusPromise0
        } else statusPromise
    }


    /**
     * proceed page
     */
    fun proceedPage0(
        forward: Boolean): Promise<Unit> {

        val disp = if (forward) { 1 } else { -1 }
        return pagingStatus?.let {
            Promise<Unit>() {
                resolve, _ ->
                val pagingStatus0 = it

                val doPaging = if (loopPaging) {
                    pagingStatus0.pages.size > 1
                } else {
                    val nextIdx = pagingStatus0.pageIndex + disp
                    nextIdx in pagingStatus0.pages.indices
                }
                if (doPaging) {
                    val pagingAnim = getPager(pagingStatus0.pageIndex)?.let {
                        if (forward) {
                            it.first.nextPage()
                        } else {
                            it.first.prevPage()
                        }
                    }?: Promise.resolve(Unit)

                    pagingAnim.then {
                        val lastPager = getPager(pagingStatus0.pageIndex)
                        pagingStatus0.pageIndex += disp
                        if (loopPaging) {
                            val pageSize = pagingStatus0.pages.size
                            
                            var pageIdx = pagingStatus0.pageIndex
                            pageIdx += pageSize
                            pageIdx %= pageSize 
                            pagingStatus0.pageIndex = pageIdx

                        }
                        val currentPager = getPager(pagingStatus0.pageIndex)
                        if (pagingStatus0.pageIndex in 
                                0 until pagingStatus0.pages.size) {

                            if (currentPager != lastPager) { 
                                currentPager?.let {
                                    pagingContainer!!.append(it.second)
                                }
                                lastPager?.let {
                                    it.second.remove()
                                }
                            }
                            currentPager?.let {
                                it.first.page = pagingStatus0.pageIndex
                            }
                        } else {
                            pagingStatus?.let {
                                it.statusPromise = null
                            }
                            currentPager?.let {
                                it.first.page = pagingStatus0.pageIndex
                            }

                        }
                        resolve(Unit)
                    }
                } else {
                    resolve(Unit)
                }
            }
        }?: Promise.resolve(Unit)
    }

    /**
     * synchronize pager index with current page
     */
    fun syncPageWithPager() {
        pageIndex?.let {
            val index = it
            getPager(index)?.let {
                val (pager, _) = it
                pager.page = index
            }
        }
        
    }

    /**
     * add event listener
     */
    fun addEventListener(eventType: String?, 
        eventListener: (String, Paging)->Unit) {
        
        var listeners = eventListeners[eventType]
        if (listeners == null) {
            listeners = ArrayList<(String, Paging)->Unit>()
            eventListeners[eventType] = listeners!!
        }
        listeners?.add(eventListener)
    }

    /**
     * remove event listener
     */
    fun removeEventListener(eventType: String?, 
        eventListener: (String, Paging)->Unit) {
        eventListeners[eventType]?.remove(eventListener)
    }

    /**
     * synchronize pagers with loop paging setting
     */
    fun syncPagersWithLoopPaging() {
        pagers.forEach {
            it?.let {
                it.first.loopPage = this.loopPaging
            }
        }  
    }


    /**
     * notify event to listeners
     */
    fun notifyEvent(
        eventType: String) {
        val listersArray = arrayOf(
            eventListeners[null],
            eventListeners[eventType]).forEach {
            it?.let {
                it.forEach { it(eventType, this) }
            }
        }
    }
}

// vi: se ts=4 sw=4 et:
