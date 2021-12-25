package net.oc_soft
import kotlin.collections.MutableMap
import kotlin.collections.HashMap
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
     * query for image container
     */
    val containerQuery: String = ".header-image.container",
    /**
     * query for template query
     */
    val fragmentQuery: String = ".tmpl-header-image") {


    /**
     * class instance
     */
    companion object {


    }

    /** 
     * slide setting
     */
    val setting: MutableMap<String, Any> = HashMap<String, Any>()

    /**
     * header root image container
     */
    val imageContainer: HTMLElement?
        get() {
            return document.querySelector(
                containerQuery) as HTMLElement?
        }

    /**
     * bind this object with window system
     */
    fun bind() {

        startToSyncSetting().then({
            startToUpdateImages()
        })
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
        searchParams.append("action", "get-header-image-params") 
        return window.fetch(url).then({
            it.json()
        }).then({
            updateSetting(it as Json)
        })
    }

    /**
     * start to create images
     */    
    fun startToUpdateImages(): Promise<Unit> {
        val url = Site.requestUrl
        val searchParams = url.searchParams
        searchParams.append("action", "get-header-images") 

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

        val keys = js("Object.keys(params)")

        setting.clear()
        for (i in 0 until (keys.length as Int)) {
            val key = keys[i] as String
            setting[key] = params[key] as Any
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
                getImageSourceUrl(imageSources, it[0]!!)?.let {
                    val url = it
                    imageContainer?.let {
                        setBackgroundImage(it, url)
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
                        setBackgroundImage(it.element, imageUrl)
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
    fun setBackgroundImage(element: HTMLElement, url: String) {
        element.style.backgroundImage = "url($url)"
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
