package net.oc_soft.slide

import kotlin.collections.MutableList
import kotlin.collections.mutableListOf

import kotlin.js.Promise

import kotlinx.browser.document


import org.w3c.dom.HTMLElement
import org.w3c.dom.animate
import org.w3c.animation.Animation
import org.w3c.dom.events.Event
import org.w3c.dom.get

/**
 * fading page 
 */
class FadePage {


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
     * frame elements
     */
    val frameElements: Array<HTMLElement>
        get() {
            return rootElement?.let {
                val items = it.querySelectorAll(".frame")
                Array<HTMLElement>(items.length) {
                    items[it] as HTMLElement
                }
            }?: emptyArray<HTMLElement>()
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
     * front frame element
     */
    var frontFrameElement: HTMLElement?
        get() {
            return rootElement?.let {
                it.querySelector(".front") as HTMLElement?
            }    
        }
        set(value) {
            if (value != frontFrameElement) {
                val frames = frameElements
                if (value in frames) {
                    frames.forEach {
                        it.classList.remove("front")
                    }
                    value?.let {
                        it.classList.add("front")
                    } 
                } 
            }
        }

    /**
     * next frame element
     */
    val nextFrameElement: HTMLElement?
        get() {
            return frameElements.find { !it.classList.contains("front") }  
        }

    /**
     * visibility of next frame element
     */
    var visibleNextFrameElement: Boolean?
        get() {
            return nextFrameElement?.let {
                it.style.display != "none"
            }            
        }
        set(value) {
            if (visibleNextFrameElement != value) {
                value?.let {
                    val disp = if (it) "block" else "none"
                    nextFrameElement?.let {
                        it.style.display = disp
                    }
                }
            }
        }

    /**
     * visibility of next frame element
     */
    var visibleFrontFrameElement: Boolean?
        get() {
            return frontFrameElement?.let {
                it.style.display != "none"
            }            
        }
        set(value) {
            if (visibleFrontFrameElement != value) {
                value?.let {
                    val disp = if (it) "block" else "none"
                    frontFrameElement?.let {
                        it.style.display = disp
                    }
                }
            }
        }


    /**
     * animation object
     */
    var animations: Array<Animation>? = null


    /**
     * create slide page widgets
     */
    fun createSpace(
        container: HTMLElement) {

        container.style.overflowX = "hidden"
        container.style.overflowY = "hidden"

        val frames = arrayOf(createFrame(), createFrame())
        container.append(frames[0])
        container.append(frames[1])
        
        rootElement = container

        frames[0].style.display = "block"
        frames[0].style.top = "0"
        frames[0].style.left = "0"
        
        frontFrameElement = frames[0]
    }

    /**
     * tear down 
     */
    fun tearDown() {
        animations?.let {
            it.forEach { it.cancel() }
        }
        
        frameElements?.forEach {
            it.remove()
        }
        contents.clear()
    }

    /**
     * create frame
     */
    fun createFrame(): HTMLElement {
        val result = document.createElement("div") as HTMLElement
        result.classList.add("frame")

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
        setFrontFrameContent(pageIndex) 
    }

    /**
     * set base frame content
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
     * set fade fade content
     */
    fun setNextFrameContent(pageIndex: Int) {
        nextFrameElement?.let {
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

        optionValue = getOptionValue(
            arrayOf("fade-in", "fadeIn"), option)
        
        val fadeIn = optionValue?.toBoolean()

        optionValue = getOptionValue(
            arrayOf("fade-out", "fadeOut"), option)
        
        val fadeOut = optionValue?.toBoolean()

        optionValue = option.duration as Any?
        
        val duration = optionValue?.toInt()
        
        val delay = (option.delay as Any?)?.let {
            it.toInt()
        }?: 0

        val endDelay = getOptionValue(
            arrayOf("end-delay", "endDelay"), option)?.let {
            it.toInt()
        }?: 0


        optionValue = option.easing as Any?
        val easing = if (optionValue is String) {
            optionValue
        } else {
            "linear"
        }
        
        return if (fadeIn != null 
            && fadeOut != null
            && duration != null) {
            proceedPage(displacement,
                fadeIn, fadeOut,
                duration, delay, endDelay, easing)
        } else {
            Promise.resolve(Unit)
        }
    }



    /**
     * proceed page
     */
    fun proceedPage(
        displacement: Int,
        fadeIn: Boolean,
        fadeOut: Boolean,
        duration: Int,
        delay: Int = 0,
        endDelay: Int = 0,
        easing: String = "linear"): Promise<Unit> {

        
        var currentAnims = animations
        if (currentAnims == null && displacement != 0) {
            var newIndex = pageIndex + displacement
            if (loopPaging) {
                newIndex %= contents.size
                newIndex += contents.size
                newIndex %= contents.size
            }

            setNextFrameContent(newIndex) 
            
            currentAnims = proceedPage(
                fadeIn, fadeOut,
                duration, delay, endDelay, easing)

            currentAnims?.let {
                val anims = it
                val promises = Array<Promise<Unit>>(anims.size) { 
                    val anim = anims[it]
                    Promise<Unit> {
                        resolve, reject ->
                        val finishedAnimation: (Event)->Unit = {
                            resolve(Unit)
                        } 
                        anim.addEventListener("finish", finishedAnimation,
                            object {
                                @JsName("once")
                                val once = true
                            }) 
                    }
                }
                Promise.all(promises).then {
                    handleAnimationFinished(newIndex)
                }
            }
            this.animations = currentAnims
        }
        return Promise.all(Array<Promise<Unit>> (currentAnims!!.size) {
            val currentAnim = currentAnims!![it]
            Promise<Unit> {
                resolve, reject ->
                val finishHdlr: (Event)->Unit = {
                    resolve(Unit)
                }
                currentAnim.addEventListener("finish", finishHdlr,
                    object {
                        @JsName("once")
                        val once = true
                    })
            }
        }).then { Unit }
    }

    /**
     * proceed page
     */
    fun proceedPage(
        fadeIn: Boolean,
        fadeOut: Boolean,
        duration: Int,
        delay: Int = 0,
        endDelay: Int = 0,
        easing: String = "linear"): Array<Animation>? {


        val fadeInKeyFrame = if (fadeIn) {
            arrayOf(
                object {
                    @JsName("opacity")
                    val opacity = 0.0
                },
                object {
                    @JsName("opacity")
                    val opacity = 1.0
                }
            )
        } else null
        val fadeOutKeyFrame = if (fadeOut) {
            arrayOf(
                object {
                    @JsName("opacity")
                    val opacity = 1.0
                },
                object {
                    @JsName("opacity")
                    val opacity = 0.0
                }
            ) 
        } else null
        
        val options: dynamic = object {
            @JsName("delay")
            val delay = delay
            @JsName("duration")
            val duration = duration
            @JsName("endDelay")
            val endDelay = endDelay
            @JsName("easing")
            val easing = easing
        }

        val nextFrame = nextFrameElement
        val frontFrame = frontFrameElement

        return if (nextFrame != null && frontFrame != null) {

            visibleNextFrameElement = true
            val animations = mutableListOf<Animation>() 

            fadeInKeyFrame?.let {
                animations.add(nextFrame.animate(it, options))
            }
            fadeOutKeyFrame?.let {
                animations.add(frontFrame.animate(it, options))
            }
            animations.forEach { it.play() }

            animations.toTypedArray()
        } else null
    }

    /**
     * handle animation finished event
     */
    fun handleAnimationFinished(pageIndex: Int) {
        frontFrameElement = nextFrameElement
        visibleNextFrameElement = false
        this.pageIndex = pageIndex
        animations = null
    }
}


// vi: se ts=4 sw=4 et:
