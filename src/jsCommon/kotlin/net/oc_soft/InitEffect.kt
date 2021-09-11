package net.oc_soft

import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.events.Event

/**
 * manage initial effect on page
 */
class InitEffect {

    /**
     * the onwer element to have control children.
     */
    var ownerElement: HTMLElement? = null


    /**
     * attach this object into an html element
     */
    fun bind(elem: HTMLElement) {
        ownerElement = elem

        window.addEventListener("load",
            { onLoaded(it) },
            object {
                @JsName("once")
                val once: Boolean = true
            })
    }

    /**
     * detach this object from last attached lement
     */
    fun unbind() {
        ownerElement = null
    }


    /**
     * handle the event for load
     */
    fun onLoaded(evt: Event) {
        finishedLoaded()
    }

    /**
     * If you loaded all element already, you should call this method.
     */
    fun finishedLoaded() {
        ownerElement?.let {
            val effectElements = it.querySelectorAll(".init-effect")
            for (idx in 0 until effectElements.length) {
                val elem = effectElements[idx] as HTMLElement
                elem.classList.add("loaded")
            } 
        }
    }
}


// vi: se ts=4 sw=4 et:
