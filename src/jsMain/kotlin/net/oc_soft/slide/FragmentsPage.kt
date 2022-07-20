package net.oc_soft.slide

import kotlin.js.Promise

import kotlinx.browser.document

import kotlin.collections.MutableList
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableListOf
import kotlinx.js.Object

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

/**
 * fragments page
 */
class FragmentsPage {

    /**
     * class instance
     */
    companion object {
        /**
         * get option value
         */
        fun getOptionValue(keys: Array<String>, option: dynamic): Any? {
            var result: Any? = null
            for (idx in keys.indices) {
                result = option[keys[idx]] as Any?
                if (result != null) {
                    break
                }
            } 
            return result
        }

        /**
         * any object to map
         */
        fun anyToMap(objData: Any): Map<String, Any> {
            val result = mutableMapOf<String, Any>()

            Object.keys(objData).forEach {
                val key = it
                val dynData: dynamic = objData
                val value = dynData[key] as Any?
                value?.let {
                    result[key] = it
                }
            }
            return result
        }
    }


    /**
     * root element
     */
    var rootElement: HTMLElement? = null


    /**
     * content container
     */
    val contentFrame: HTMLElement?
        get() {
            return rootElement?.querySelector(
                ".content.frame") as HTMLElement?
        }

    /**
     * effect frame
     */
    val effectFrame: HTMLElement?
        get() {
            return rootElement?.querySelector(
                ".effect.frame") as HTMLElement?
        }

    /**
     * contents makers
     */
    val contentMakers = 
        mutableListOf<Pair<HTMLElement, (HTMLElement)->HTMLElement>>()


    /**
     * page Index
     */
    var pageIndex = -1

    /**
     * the flag for endless paging
     */
    var loopPage = false

    /**
     * animation object
     */
    var animation: Animation? = null


    /**
     * create space for fragments
     */
    fun createSpace(
        rootElement: HTMLElement) {

        rootElement.style.position = "relative"

        this.rootElement = rootElement
        

        val contentFrame = createContentFrame()
        rootElement.append(contentFrame)

        val effectFrame = createEffectFrame()
        rootElement.append(effectFrame)

    }
    
    /**
     * create content frame
     */
    fun createContentFrame(): HTMLElement {
        val result = document.createElement("div") as HTMLElement

        result.style.position = "absolute"
        result.style.left = "0"
        result.style.top = "0"
        result.style.width = "100%"
        result.style.height = "100%"
        result.classList.add("content")
        result.classList.add("frame")
        return result
    }

    /**
     * create content frame
     */
    fun createEffectFrame(): HTMLElement {
        val result = document.createElement("div") as HTMLElement

        result.style.position = "absolute"
        result.style.left = "0"
        result.style.top = "0"
        result.style.width = "100%"
        result.style.height = "100%"
        result.classList.add("effect")
        result.classList.add("frame")
        return result
    }


    /**
     * setup pages
     */
    fun setupPages(
        numberOfPages: Int,
        createPage: (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>) {

        contentMakers.clear()

        for (idx in 0 until numberOfPages) {
            val contentMaker = createPage(idx)
            setupStyleForContent(contentMaker.first)
            contentMakers.add(contentMaker)
        }
    }
    /**
     * setup style for content
     */
    fun setupStyleForContent(element: HTMLElement) {
        element.style.position = "absolute"
        element.style.left = "0"
        element.style.top = "0"
    }

    /**
     * tear down
     */
    fun tearDown() {
        contentFrame?.let {
            it.remove()
        }
    }

    /**
     * proceed page
     */
    fun proceedPage(
        displacement: Int,
        option: dynamic): Promise<Unit> {

        var optionValue = option.type as Any? 

        return optionValue?.let {
            when (optionValue) {
                "rect" -> proceedPageRect(displacement, option)
                "square" -> proceedPageSquare(displacement, option)
                else -> Promise.resolve(Unit)
            }
        }?: Promise.resolve(Unit)
    }

    /**
     * calculate next page index
     */
    fun calcNextPageIndex(
        pageIndex: Int,
        displacement: Int): Int {
        var result = pageIndex + displacement

        if (loopPage) {
            result %= contentMakers.size
            result += contentMakers.size
            result %= contentMakers.size
        }
        return result
    }

    /**
     * proceed page with rect fragment
     */
    fun proceedPageRect(displacement: Int,
        option: dynamic): Promise<Unit> {

        var newPageIndex = calcNextPageIndex(pageIndex, displacement)


        var optionValue = option.anchor as Any?
        
        val anchor = optionValue?.toIntArray()
         
        optionValue = option.steps as Any?

        val steps = optionValue?.toInt()

        optionValue = getOptionValue(
            arrayOf("row-count", "rowCount"), option) as Any?

        val rowCount = optionValue?.toInt()

        optionValue = getOptionValue(
            arrayOf("column-count", "columnCount"), option) as Any?

        val colCount = optionValue?.toInt()

        optionValue = option.animation as Any?
    
        val animationOption = optionValue?.let {
            anyToMap(it)             
        }
        val rootElement = this.rootElement
        return if (
            rootElement != null
            && anchor != null
            && steps != null
            && rowCount != null
            && colCount != null
            && animationOption != null) {

            val animation = createRectFragments(rootElement,
                newPageIndex, anchor, steps, rowCount, colCount,
                animationOption)
            animation?.let {
                this.animation = it
                updateEffectFrameWithAnimation()
                val res = Promise<Unit> {
                    resolve, reject ->

                    val hdlr: (Event)->Unit = {
                        handleAnimationFinished(it)
                        resolve(Unit)
                    }
                    rootElement.addEventListener(
                        "finish", hdlr,
                        object {
                            @JsName("once")
                            val once = true
                        })
                }
                it.play() 
                res 
            }?: Promise.resolve(Unit)
             
        } else Promise.resolve(Unit)
    }

    /**
     * create rect fragments animation
     */
    fun createRectFragments(
        element: HTMLElement,
        pageIndex: Int,
        anchor: IntArray,
        stpes: Int,
        rowCount: Int,
        colCount: Int,
        animationOption: Map<String, Any>): Animation? {

        return getContentMaker(pageIndex)?.let {
            val contentMaker = it
            val elemGen: ()->HTMLElement = {
                contentMaker.second(contentMaker.first)
            }

            Rect.createFragments(element, elemGen,
                anchor, stpes,
                rowCount, colCount,
                animationOption)
        }
    } 

    /**
     * proceed page with square fragment
     */
    fun proceedPageSquare(displacement: Int,
        option: dynamic): Promise<Unit> {

        var newPageIndex = calcNextPageIndex(pageIndex, displacement)


        var optionValue = option.anchor as Any?
        
        val anchor = optionValue?.toIntArray()
         
        optionValue = option.steps as Any?

        val steps = optionValue?.toInt()

        optionValue = option.size as Any?

        val size = optionValue?.toDouble()

        optionValue = option.animation as Any?
    
        val animationOption = optionValue?.let {
            anyToMap(it)             
        }
        val rootElement = this.rootElement
        return if (
            rootElement != null
            && anchor != null
            && steps != null
            && size != null
            && animationOption != null) {

            val animation = createSquareFragments(rootElement,
                newPageIndex, anchor, steps, size,
                animationOption)
            animation?.let {
                this.animation = it
                updateEffectFrameWithAnimation()
                val res = Promise<Unit> {
                    resolve, reject ->

                    val hdlr: (Event)->Unit = {
                        handleAnimationFinished(it)
                        resolve(Unit)
                    }
                    rootElement.addEventListener(
                        "finish", hdlr,
                        object {
                            @JsName("once")
                            val once = true
                        })
                }
                it.play() 
                res
            }?: Promise.resolve(Unit)
             
        } else Promise.resolve(Unit)
    }

    /**
     * create square fragments animation
     */
    fun createSquareFragments(
        element: HTMLElement,
        pageIndex: Int,
        anchor: IntArray,
        stpes: Int,
        size: Double,
        animationOption: Map<String, Any>): Animation? {

        return getContentMaker(pageIndex)?.let {
            val contentMaker = it
            val elemGen: ()->HTMLElement = {
                contentMaker.second(contentMaker.first)
            }

            Square.createFragments(element, elemGen,
                anchor, stpes,
                size, animationOption)
        } 
    } 


    /**
     * update effect frame with animation
     */
    fun updateEffectFrameWithAnimation() {
        val animation = this.animation
        effectFrame?.let {
            if (animation != null) {
                val elem = it
                animation.fragments.forEach {
                    val item = it
                    elem.append(item.element) 
                }
            } else {
                while (it.childElementCount > 0) {
                    it.lastElementChild?.remove()
                }    
            }
        }
    }
        
    /**
     * get content maker
     */
    fun getContentMaker(index: Int): 
        Pair<HTMLElement, (HTMLElement)->HTMLElement>? {
        return if (index in contentMakers.indices) {
            contentMakers[index]
        } else null
    }


    /**
     * handle animation finished
     */
    fun handleAnimationFinished(event: Event) {
        animation = null
        updateEffectFrameWithAnimation()
    }

    /**
     * synchronize page with index
     */
    fun syncPageWithPageIndex() {
        setRootPageContent(pageIndex)
    }

    /**
     * set root page content
     */
    fun setRootPageContent(pageIndex: Int) {
        
        contentFrame?.let {
            val elem = it
            val oldItem = elem.firstElementChild

            val newItem = getContentMaker(pageIndex)?.let {
                it.first
            }

            if (oldItem != newItem) {
                oldItem?.remove()
                newItem?.let {
                    elem.append(it)
                }
            }
        }
    }
    
}
// vi: se ts=4 sw=4 et:
