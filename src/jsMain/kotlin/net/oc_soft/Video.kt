package net.oc_soft;

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.HTMLSourceElement
import org.w3c.dom.events.Event
import org.w3c.dom.get

/**
 * video helper
 */
class Video {


    /**
     * all lazy video element
     */
    val videoElements: Array<HTMLVideoElement>
        get() {
            val elements = document.querySelectorAll("video.lazy") 
            val result = Array<HTMLVideoElement>(elements.length) {
                elements[it] as HTMLVideoElement
            }
            return result
        }

    /**
     * atatch this object into html elements
     */
    fun bind() {
        window.addEventListener(
            "load", { handleLoaded(it) },
            object {
                @JsName("once")
                val once = true
            })
    }

    /**
     * detach this object from html elements
     */
    fun unbind() {
    }

    /**
     * start load resource
     */
    fun startLoadResource() {
        val videos = videoElements 
        videos.forEach {
            val sourceItems = it.querySelectorAll("source") 
            for (idx in 0 until sourceItems.length) {
                val sourceElem = sourceItems[idx] as HTMLSourceElement
                sourceElem.dataset["src"]?.let {
                    sourceElem.src = it
                }
            } 
            val video = it
            it.addEventListener("canplay",
                { video.classList.remove("lazy") },
                object {
                    @JsName("once")
                    val once = true
                })
            it.load()
        }    }

    /**
     * handle loaded events
     */
    fun handleLoaded(event: Event) {
        startLoadResource()
    }
}

// vi: se ts=4 sw=4 et:
