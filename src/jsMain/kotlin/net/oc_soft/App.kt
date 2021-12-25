package net.oc_soft

import kotlinx.browser.document
import kotlinx.browser.window


import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.get

class App(
    /**
     * manage header image
     */
    val headerImage: Slide = Slide()) {


    /**
     * bind this object into html element
     */
    fun bind() {
        headerImage.bind()        
    }

    /**
     * unbind this object from html elements
     */
    fun unbind() {
        headerImage.unbind()
    }

    fun run() {
        window.addEventListener("load", {
            evt ->
            bind()
        },
        object {
            @JsName("once")
            val once: Boolean = true
        })
        window.addEventListener("unload", {
            evt ->
            unbind()
        },
        object {
            @JsName("once")
            val once: Boolean = true
        })
    }

}

// vi: se ts=4 sw=4 et:
