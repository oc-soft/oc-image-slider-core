package net.oc_soft

import kotlinx.browser.document
import kotlinx.browser.window

import net.oc_soft.track.Tracker
import net.oc_soft.InitEffect

class App(
    val initEffect: InitEffect = InitEffect()) {


    /**
     * tack user activity
     */
    var tracker: Tracker? = null
     
    /**
     * simulate anchor element
     */
    var anchor: Anchor? = null

    /**
     * bind this object into html element
     */
    fun bind() {
        tracker = Tracker() 
        tracker!!.bind()
        anchor = Anchor()    
        anchor!!.bind()

        initEffect.ownerElement = document.body
        initEffect.finishedLoaded()
    }

    /**
     * unbind this object from html elements
     */
    fun unbind() {
        initEffect.unbind()

        val tracker = this.tracker
        if (tracker != null) {
            tracker.unbind()
            this.tracker = null
        }
        val anchor = this.anchor
        if (anchor != null) {
            anchor.unbind()
            this.anchor = null
        }
    }


    /**
     * run this object
     */
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
