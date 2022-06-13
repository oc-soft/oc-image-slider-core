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
                    status.pageIndex = it 
                }
            }
        }

    /**
     * loop paging
     */
    var loopPaging = false


    /**
     * page contents loader
     */
    var contentsLoader: (()->Array<HTMLElement>)? = null

    /**
     * load backgrounds
     */
    var contentBackgroundsLoader: (()->Array<String>)? = null

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
        pagers.forEach {
            it?.let {
                val (_, elem) = it
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
    fun loadContents(): Array<HTMLElement> {
        return contentsLoader?.let {
            it()
        }?: loadContentTemplates()
    }

    /**
     * load content templates
     */
    fun loadContentTemplates(): Array<HTMLElement> {
        return pagingContainer?.let {
            it.querySelector("template.pages")?.let {
                val tmpl = it as HTMLTemplateElement
                

                val children = tmpl.content.children
                Array<HTMLElement>(children.length) {
                    children[it] as HTMLElement
                }
            }?: emptyArray<HTMLElement>()
        }?: emptyArray<HTMLElement>()
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
        pages: Array<HTMLElement>,
        backgrounds: Array<String>) {
        pagingContainer?.let {
            val pagerContainer = it
            val createPage: (Int) -> HTMLElement = {
                pages[it].cloneNode(true) as HTMLElement
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
            val pages = loadContents()  
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

                val pagingAnim = getPager(pagingStatus0.pageIndex)?.let {
                    it.first.nextPage()
                }?: Promise.resolve(Unit)

                pagingAnim.then {
                    val lastPager = getPager(pagingStatus0.pageIndex)
                    pagingStatus0.pageIndex++ 
                    if (loopPaging) {
                        val pageSize = pagingStatus0.pages.size
                        val pageIdx = pagingStatus0.pageIndex % pageSize 
                        pagingStatus0.pageIndex = pageIdx

                    }
                    val currentPager = getPager(pagingStatus0.pageIndex)
                    if (pagingStatus0.pageIndex < 
                            pagingStatus0.pages.size - 1
                        || loopPaging) {

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
                        autoPlay0()
                    } else {
                        pagingStatus?.let {
                            it.statusPromise = null
                        }
                        currentPager?.let {
                            it.first.page = pagingStatus0.pageIndex
                        }
                        resolve(Unit)
                    }
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
        if (pagingStatus.statusPromise == null) { 
            pagingStatus.statusPromise = proceedPage0(forward)
        }
        return pagingStatus.statusPromise!!
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
                        resolve(Unit)
                    }
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
}

// vi: se ts=4 sw=4 et:
