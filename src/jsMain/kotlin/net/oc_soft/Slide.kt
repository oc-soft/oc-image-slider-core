package net.oc_soft
import kotlin.collections.MutableMap
import kotlin.collections.HashMap
import kotlin.collections.MutableList
import kotlin.collections.ArrayList
import kotlin.collections.Set
import kotlin.collections.HashSet
import kotlin.collections.MutableSet
import kotlin.collections.get
import kotlin.collections.set

import kotlin.js.Json
import kotlin.js.Promise

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.Image
import org.w3c.dom.get
import org.w3c.dom.set

import org.w3c.dom.url.URL

import kotlin.text.toIntOrNull
import kotlin.text.toBoolean
import net.oc_soft.slide.Option
import net.oc_soft.slide.Animation

/**
 * manage slide  effect
 */
class Slide(
    /**
     * image parameter query
     */
    val imageParamsQuery: String,
    /**
     * back ground element loader
     */
    val elementBackground: BackgroundStyle) {

    /**
     * background style 
     */
    data class BackgroundStyleElement(
        /**
         * urls
         */
        val urls: Array<String>?,
        /**
         * background image color
         */
        val colors: Array<DoubleArray>)


    /** 
     * slide setting
     */
    val setting: MutableList<MutableMap<String, Any>> = 
        ArrayList<MutableMap<String, Any>>()

    /**
     * the slide is looping
     */
    var loopSlide: Boolean = false


    /**
     * bind this object with window system
     */
    fun bind() {
    }

    /**
     * unbind this object from window system
     */
    fun unbind() {

    }


    /**
     * sync parameter with site
     */
    fun startSyncSetting(url: URL): Promise<Unit> {
        val searchParams = url.searchParams
        searchParams.append("action", imageParamsQuery)
        return window.fetch(url).then({
            it.json()
        }).then({
            updateSetting(it as Json)
        })
    }


    
    /**
     * start to create images
     */    
    fun startToUpdateImages(
        imageContainer: HTMLElement,
        imageSource: Array<Array<String>>,
        colors: Array<Array<DoubleArray>>,
        createFragment: ()->HTMLElement?,
        insertElement: (HTMLElement, HTMLElement)->Unit) {
        autoSlide(imageContainer, imageSource, colors, 
            createFragment, insertElement) 
    }


    /**
     * update setting 
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
                this.loopSlide = when (it) {
                is Boolean ->  it 
                is String -> it.toBoolean()
                else -> this.loopSlide
                }
            }
        }
    }
    /**
     * start slide image automatically
     */
    fun autoSlide(
        imageContainer: HTMLElement,
        imageSources: Array<Array<String>>,
        colors: Array<Array<DoubleArray>>,
        createFragment: ()->HTMLElement?,
        insertElement: (HTMLElement, HTMLElement)->Unit) {
        autoSlide(imageContainer, imageSources, 
            colors, createFragment, insertElement, 0, 1, HashSet<Element>())
    }


 
    /**
     * start slide image automatically
     */
    fun autoSlide(
        imageContainer: HTMLElement,
        imageSources: Array<Array<String>>,
        colors: Array<Array<DoubleArray>>, 
        createFragment: ()->HTMLElement?,
        insertElement: (HTMLElement, HTMLElement)->Unit,
        backImageIndex: Int,
        imageIndex: Int,
        slideElements: Set<Element>) {
        getBackgroundStyleElement(
            imageSources, colors, backImageIndex)?.let {
            val styleElement = it
            setBackgroundStyle(imageContainer, styleElement)
            slideElements.forEach { it.remove() }
        }
        getBackgroundStyleElement(imageSources, colors, imageIndex)?.let {
            val styleElement = it 
            val slideHdlr: (Event, Set<Element>)->Unit = {
                event, slideFragments ->
                if ("finish" == event.type) {
                    autoSlide(imageContainer, 
                        imageSources, colors, 
                        createFragment,
                        insertElement,
                        backImageIndex + 1,
                        imageIndex + 1, slideFragments)
                }
            } 
            createImageEffect(
                imageContainer, imageSources, colors, 
                createFragment, insertElement, imageIndex, slideHdlr)
        }
    }


    /**
     * get image source url
     */
    fun getBackgroundStyleElement(
        imageSources: Array<Array<String>>,
        colors: Array<Array<DoubleArray>>,
        index: Int): BackgroundStyleElement? {
        
        var index0 = if (loopSlide) {
            index % imageSources.size
        } else {
            index
        }
        
        return if (index0 < imageSources.size) {
            BackgroundStyleElement(
                imageSources[index0],
                colors[index0 % colors.size])
        } else {
            null
        }
    }

    /**
     * create image effect
     */
    fun createImageEffect(
        imageContainer: HTMLElement,
        imageSources: Array<Array<String>>,
        colors: Array<Array<DoubleArray>>,
        createFragment: ()->HTMLElement?,
        insertElement: (HTMLElement, HTMLElement)->Unit,
        imageIndex: Int,
        animationListener: ((Event, Set<Element>)->Unit)?) {

        val setting = if (this.setting.size > 0) {
            this.setting[imageIndex % this.setting.size]
        } else {
            null
        }
        if (setting != null) { 
            val elementContainer = imageContainer 
            getBackgroundStyleElement(
                imageSources, colors, imageIndex)?.let {
                val styleElement = it 

                val slideFragments = HashSet<Element>()
                val hdlr: (Event)->Unit = {
                    event ->
                    animationListener?.let {
                        it(event, slideFragments)
                    }

                    handleAnimationEvent(
                        event, imageSources, colors,
                            imageIndex, slideFragments)
                }
                Option.createFragments(elementContainer, 
                    createFragment,
                    elementBackground, 
                    setting)?.let {
                    it.fragments.forEach {
                        setBackgroundStyle(
                            it.element, styleElement)
                        insertElement(elementContainer, it.element)
                        slideFragments.add(it.element)      
                    }
                    elementContainer.addEventListener(
                        "finish", hdlr, object {
                            @JsName("once")
                            val once = true
                        })
                    it.play()
                }
            }
        }
    }

    /**
     * handle animation event
     */
    fun handleAnimationEvent(
        event: Event,
        imageSources: Array<Array<String>>,
        colors: Array<Array<DoubleArray>>,
        imageIndex: Int,
        elements: Set<Element>) {
        when (event.type) {
            "finish" -> {
            }
        }

    }

        

    /**
     * routine be ran when a animation finished.
     */
    fun onFinishedAnimation(
        imageSources: Array<Json>,
        imageIndex: Int) {
    }


    /**
     * set image url into element
     */
    fun setBackgroundStyle(
        element: HTMLElement, 
        backgroundStyleElement: BackgroundStyleElement) {
        backgroundStyleElement.urls?.let {
            setBackgroundStyle(element, 
                it,
                backgroundStyleElement.colors)
        }
    }



    /**
     * set image url into element
     */
    fun setBackgroundStyle(
        element: HTMLElement, 
        url: Array<String>,
        colors: Array<DoubleArray>) {

        element.style.backgroundImage = 
            elementBackground.createBackgroundImageStyle(
                url, colors)
        element.style.backgroundRepeat = 
            elementBackground.backgroundRepeat
        element.style.backgroundPosition = 
            elementBackground.backgroundPosition
        element.style.backgroundSize =
            elementBackground.backgroundSize
    }



}

// vi: se ts=4 sw=4 et:
