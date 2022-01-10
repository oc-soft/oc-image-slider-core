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
import org.w3c.dom.HTMLTemplateElement
import org.w3c.dom.events.Event
import org.w3c.dom.Image
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.dom.animate

import kotlin.text.toIntOrNull
import net.oc_soft.slide.Option
import net.oc_soft.slide.Animation

/**
 * manage slide  effect
 */
class Slide(
    /**
     * query for image container html element
     */
    val containerQuery: String,
    /**
     * query for html template 
     */
    val fragmentQuery: String,
    /**
     * action query to get images
     */
    val imagesQuery: String,
    /**
     * image parameter query
     */
    val imageParamsQuery: String,
    /**
     * action query to get image layout 
     */
    val imageLayoutQuery: String,
    /**
     * back ground element loader
     */
    val elementBackground: ElementBackground = ElementBackground()) {


    /**
     * class instance
     */
    companion object {


    }

    /** 
     * slide setting
     */
    val setting: MutableList<MutableMap<String, Any>> = 
        ArrayList<MutableMap<String, Any>>()

    /**
     * header root image container
     */
    val imageContainer: HTMLElement?
        get() {
            return document.querySelector(
                containerQuery) as HTMLElement?
        }


    /**
     * start to setup
     */
    fun startSetting(): Promise<Unit> {
        return Promise.all(
            arrayOf(startToSyncSetting(),
            startToSetupElementBackground())).then {
            Unit
        }
    }

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
    fun startToSyncSetting(): Promise<Unit> {
        val url = Site.requestUrl
        val searchParams = url.searchParams
        searchParams.append("action", imageParamsQuery)
        return window.fetch(url).then({
            it.json()
        }).then({
            updateSetting(it as Json)
        })
    }


    /**
     * start load to setup element background loader
     */
    fun startToSetupElementBackground(): Promise<Unit> {
        val url = Site.requestUrl
        val searchParams = url.searchParams
        searchParams.append("action", imageLayoutQuery) 
        return window.fetch(url).then({
            it.json()
        }).then({
            elementBackground.loadSetting(it as Json)
        })
    }

    
    /**
     * start to create images
     */    
    fun startToUpdateImages(): Promise<Unit> {
        val url = Site.requestUrl
        val searchParams = url.searchParams
        searchParams.append("action", imagesQuery) 

        return window.fetch(url).then({
            it.json()
        }).then({
            autoSlide(it as Array<Json>) 
        })
    }


    /**
     * update setting 
     */
    fun updateSetting(params: Json) {

        val params0 = if (params is Array<*>) {
            params as Array<Json>
        } else {
            arrayOf(params) 
        }

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
    }
    /**
     * start slide image automatically
     */
    fun autoSlide(imageSources: Array<Json>) {
        autoSlide(imageSources, 0, 1, HashSet<Element>())
    }


    /**
     * load images and do slide automatically
     */
    fun loadImages(
        imageSources: Array<Json>,
        backImageIndex: Int,
        imageIndex: Int): Promise<Array<out Int?>> {
        
        val indices = intArrayOf(backImageIndex, imageIndex)
        val promises = Array<Promise<Int?>>(indices.size) {
            val imgIdx = indices[it]
            Promise<Int?>() { 
                resolve, reject ->
                if (imgIdx < imageSources.size) {
                    getImageSourceUrl(imageSources, imgIdx)?.let {
                        val img = Image()
                        img.src = it
                        img.onload = { 
                            resolve(imgIdx)
                        }
                        img.onerror = { 
                            msg, src, lineno, col, err ->
                            resolve(null)
                        }
                    }?: resolve(null) 
                } else {
                    resolve(null)
                } 
            }
        }
        return Promise.all(promises) 
    }

 
    /**
     * start slide image automatically
     */
    fun autoSlide(imageSources: Array<Json>,
        backImageIndex: Int,
        imageIndex: Int,
        slideElements: Set<Element>) {
        loadImages(imageSources, backImageIndex, imageIndex).then {
            if (it[0] != null) {
                val imageIndex0 = it[0]!!
                getImageSourceUrl(imageSources, it[0]!!)?.let {
                    val url = it
                    imageContainer?.let {
                        setBackgroundImage(it, imageIndex0, url)
                        slideElements.forEach { it.remove() }
                    }
                } 
            }
            if (it[1] != null) {
                val slideHdlr: (Event, Set<Element>)->Unit = {
                    event, slideFragments ->
                    if ("finish" == event.type) {
                        autoSlide(imageSources, backImageIndex + 1,
                            imageIndex + 1, slideFragments)
                    }
                } 
                createImageEffect(imageSources, it[1]!!, slideHdlr)
            }
        }
    }


    /**
     * get image source url
     */
    fun getImageSourceUrl(
        imageSources: Array<Json>,
        index: Int): String? {
        return imageSources[index]["url"]?.let {
             it as String
        }
    }

    /**
     * create image effect
     */
    fun createImageEffect(
        imageSources: Array<Json>,
        imageIndex: Int,
        animationListener: (
            (Event, Set<Element>)->Unit)?) {

        val setting = if (this.setting.size > 0) {
            this.setting[imageIndex % this.setting.size]
        } else {
            null
        }
        if (setting != null) { 
            imageContainer?.let {
                val elementContainer = it 
                getImageSourceUrl(imageSources, imageIndex)?.let {
                    val imageUrl = it 

                    val slideFragments = HashSet<Element>()
                    val hdlr: (Event)->Unit = {
                        event ->
                        animationListener?.let {
                            it(event, slideFragments)
                        }

                        handleAnimationEvent(
                            event, imageSources, imageIndex, slideFragments)
                    }
                    Option.createFragments(elementContainer, 
                        { createPlainImageFragment() },
                        setting)?.let {
                        it.fragments.forEach {
                            setBackgroundImage(it.element, imageIndex, imageUrl)
                            elementContainer.append(it.element)
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
    }

    /**
     * handle animation event
     */
    fun handleAnimationEvent(
        event: Event,
        imageSources: Array<Json>,
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
     * clear images be contained in imageContainer
     */
    fun clearContainedImgages() {
        imageContainer?.let {

            while (it.firstElementChild != null) {
                it.lastElementChild?.let {
                    it.remove()
                }
            }
        }
    }

    /**
     * set image url into element
     */
    fun setBackgroundImage(
        element: HTMLElement, 
        imageLayoutIndex: Int,
        url: String) {
        setBackgroundImage(element, imageLayoutIndex, arrayOf(url))
    }



    /**
     * set image url into element
     */
    fun setBackgroundImage(
        element: HTMLElement, 
        imageLayoutIndex: Int,
        url: Array<String>) {

        elementBackground.createBackground(imageLayoutIndex, url)?.let {
            element.style.backgroundImage = it
        }
    }


    /**
     * create slide fragment
     */
    fun createPlainImageFragment(): HTMLElement? {
        return document.querySelector(fragmentQuery)?.let {
            val tmpElem = it as HTMLTemplateElement
            tmpElem.content.firstElementChild?.let {
                it.cloneNode(true) as HTMLElement
            }
        }
    }
}

// vi: se ts=4 sw=4 et:
