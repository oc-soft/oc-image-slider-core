package net.oc_soft.slide

import kotlin.collections.MutableList
import kotlin.collections.mutableListOf

import kotlin.js.Promise

import kotlinx.browser.document


import org.w3c.dom.HTMLElement
import org.w3c.dom.animate
import org.w3c.animation.Animation
import org.w3c.dom.events.Event

/**
 * page slider
 */
class SlidePage {


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
    }

    /**
     * root element
     */
    var rootElement: HTMLElement? = null

    /**
     * front frame element
     */
    val frontFrameElement: HTMLElement?
        get() {
            return rootElement?.let {
                it.querySelector(".frame.front")as HTMLElement?
            }
        }

    /**
     * base frame element
     */
    val baseFrameElement: HTMLElement?
        get() {
            return rootElement?.let {
                it.querySelector(".frame.base")as HTMLElement?
            }
        }


    /**
     * page index
     */
    var pageIndex = -1
    
    /**
     * endless paging
     */
    var loopPaging: Boolean = false

    /**
     * page contents
     */
    val contents: MutableList<HTMLElement> = mutableListOf()   


    /**
     * animation object
     */
    var animation: Animation? = null

    /**
     * create slide page widgets
     */
    fun createSlidePageSpace(
        container: HTMLElement) {

        container.style.overflowX = "hidden"
        container.style.overflowY = "hidden"

        container.append(createFrame("base"))
        container.append(createFrame("front"))

        rootElement = container

        baseFrameElement?.let {
            it.style.display = "block"
            it.style.top = "0"
            it.style.left = "0"
        }
    }

    /**
     * tear down 
     */
    fun tearDown() {
        animation?.let {
            it.cancel()
        }
        
        baseFrameElement?.let {
            it.remove()
        }
        frontFrameElement?.let {
            it.remove()
        }
        contents.clear()
    }

    /**
     * create frame
     */
    fun createFrame(className: String): HTMLElement {
        val result = document.createElement("div") as HTMLElement
        result.classList.add("frame")
        result.classList.add(className)

        result.style.position = "absolute"
        result.style.display = "none"
        result.style.width = "100%"
        result.style.height = "100%"
        return result
    }

     
    @Suppress("UNUSED_PARAMETER")
    fun setPages(
        elements: Array<Pair<HTMLElement, (HTMLElement)->HTMLElement>>) {
        contents.clear()
        
        elements.forEach {
            contents.add(it.first)
        }
    }



    /**
     * synchronize page with index
     */
    fun syncPageWithPageIndex() {
        setBaseFrameContent(pageIndex) 
    }

    /**
     * set base frame content
     */
    fun setBaseFrameContent(pageIndex: Int) {
        baseFrameElement?.let {
            val contents = arrayOf(
                it.firstElementChild as HTMLElement?,
                getContent(pageIndex))
            if (contents[0] != contents[1]) {
                contents[0]?.let { it.remove() }
                val frameElem = it
                contents[1]?.let {
                    frameElem.append(it)
                }
            }
        }
    } 


    /**
     * set front frame content
     */
    fun setFrontFrameContent(pageIndex: Int) {
        frontFrameElement?.let {
            val contents = arrayOf(
                it.firstElementChild as HTMLElement?,
                getContent(pageIndex))
            if (contents[0] != contents[1]) {
                contents[0]?.let { it.remove() }
                val frameElem = it
                contents[1]?.let {
                    frameElem.append(it)
                }
            }
        }
    } 

    /**
     * content element
     */
    fun getContent(pageIndex: Int): HTMLElement? {
        return if (pageIndex in contents.indices) {
            contents[pageIndex]
        } else null
    }
    
    /**
     * proceed page
     */
    fun proceedPage(
        displacement: Int,
        option: dynamic): Promise<Unit> {

        
        var optionValue: Any? = null 

        val moveIn = getOptionValue(
            arrayOf("move-in", "moveIn"), option)?.toBoolean()

        optionValue = getOptionValue(
            arrayOf("start-position", "startPosition"), option)
        
        val startPosition = optionValue?.let {
            val intArray = it.toIntArray()
            when (intArray.size) {
                0 -> null
                1 -> intArrayOf(intArray[0], intArray[0])
                else -> intArray 
            }
        }
        optionValue = getOptionValue(
            arrayOf("stop-position", "stopPosition"), option)
        
        val stopPosition = optionValue?.let {
            val intArray = it.toIntArray()
            when (intArray.size) {
                0 -> null
                1 -> intArrayOf(intArray[0], intArray[0])
                else -> intArray 
            }
        }

        optionValue = option.duration as Any?
        
        val duration = optionValue?.toInt()
        
        val delay = (option.delay as Any?)?.let {
            it.toInt()
        }?: 0

        val endDelay = getOptionValue(
            arrayOf("end-delay", "endDelay"), option)?.let {
            it.toInt()
        }?: 0

        optionValue = option.direction as Any?
        val direction = if (optionValue is String) {
            optionValue
        } else {
            "normal"
        }

        optionValue = option.easing as Any?
        val easing = if (optionValue is String) {
            optionValue
        } else {
            "linear"
        }
        
        return if (moveIn != null
            && startPosition != null 
            && stopPosition != null
            && duration != null) {
            proceedPage(moveIn, displacement,
                startPosition, stopPosition,
                duration, delay, endDelay, direction, easing)
        } else {
            Promise.resolve(Unit)
        }
    }



    /**
     * proceed page
     */
    fun proceedPage(
        moveIn: Boolean,
        displacement: Int,
        startPosition: IntArray,
        stopPosition: IntArray,
        duration: Int,
        delay: Int = 0,
        endDelay: Int = 0,
        direction: String = "normal",
        easing: String = "linear"): Promise<Unit> {

        
        var currentAnim = animation
        if (currentAnim == null && displacement != 0) {
            var newIndex = pageIndex + displacement
            if (loopPaging) {
                newIndex %= contents.size
                newIndex += contents.size
                newIndex %= contents.size
            }

            val finishEventListener: (Event)->Unit = {
                handleAnimationFinished(it, moveIn, newIndex)
            }
            if (moveIn) {
                setFrontFrameContent(newIndex) 
            } else {
                setFrontFrameContent(pageIndex)
                setBaseFrameContent(newIndex)
            }

            currentAnim = proceedPage(
                startPosition, stopPosition,
                duration, delay, endDelay, direction, easing)

            currentAnim!!.addEventListener("finish", finishEventListener,
                object {
                    @JsName("once")
                    val once = true
                }) 
            this.animation = currentAnim
        }
        return Promise<Unit> {
            resolve, reject ->
            val finishHdlr: (Event)->Unit = {
                resolve(Unit)
            }
            currentAnim!!.addEventListener("finish", finishHdlr,
                object {
                    @JsName("once")
                    val once = true
                })
        }
    }

    /**
     * proceed page
     */
    fun proceedPage(
        startPosition: IntArray,
        stopPosition: IntArray,
        duration: Int,
        delay: Int = 0,
        endDelay: Int = 0,
        direction: String = "normal",
        easing: String = "linear"): Animation? {

        val startPositionLeft = "${startPosition[0]}%"
        val startPositionTop = "${startPosition[1]}%"

        val stopPositionLeft = "${stopPosition[0]}%"
        val stopPositionTop = "${stopPosition[1]}%"


        val keyFrame: dynamic = object {
            @JsName("left")
            val left = arrayOf(startPositionLeft, stopPositionLeft)
            @JsName("top")
            val top = arrayOf(startPositionTop, stopPositionTop)
        }

        val options: dynamic = object {
            @JsName("delay")
            val delay = delay
            @JsName("duration")
            val duration = duration
            @JsName("endDelay")
            val endDelay = endDelay
            @JsName("direction")
            val direction = direction
            @JsName("easing")
            val easing = easing
        }

        return frontFrameElement?.let {
            it.style.display = "block"
            when (direction) {
                "reverse",
                "alternate-reverse" -> {
                    it.style.left = stopPositionLeft
                    it.style.top = stopPositionTop
                }
                else -> {
                    it.style.left = startPositionLeft
                    it.style.top = startPositionTop
                }
            }

            val res = it.animate(keyFrame, options) 
            res.play()
            res
        }
    }

    /**
     * handle animation finished event
     */
    fun handleAnimationFinished(e: Event, moveIn: Boolean, newPageIndex: Int) {
        pageIndex = newPageIndex
        if (moveIn) {
            syncPageWithPageIndex()
        }
        frontFrameElement?.let {
            it.style.display = "none"
        }
        animation = null
    }
}


// vi: se ts=4 sw=4 et:
