package net.oc_soft


import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.events.Event
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

/**
 * make a html element like a anchor element
 */
class Anchor {

    /**
     * click handler
     */
    var clickHdlr: ((Event)->Unit)? = null

    /**
     * bind this object into html element
     */
    fun bind() {
        clickHdlr = { handleClick(it) }

        val targets = document.querySelectorAll(".anchor[data-href]")
        
        for (idx in 0 until targets.length) {
            bindTarget(targets[idx] as HTMLElement)
        }
    }


    /**
     * unbind this object from html element
     */
    fun unbind() {

 
        if (clickHdlr != null) {
            val targets = document.querySelectorAll(".anchor[data-href]")
            
            for (idx in 0 until targets.length) {
                unbindTarget(targets[idx] as HTMLElement)
            }
            clickHdlr = null
        }
 
    }

    /**
     * make html element like anchor element
     */
    fun bindTarget(target: HTMLElement) {
        target.addEventListener("click", clickHdlr!!)
    }

    /**
     * detatch anchor attribute from html element
     */
    fun unbindTarget(target: HTMLElement) {
        target.removeEventListener("click", clickHdlr!!)
    }

    /**
     * handle the click event
     */
    fun handleClick(evt: Event) {

        val element = evt.currentTarget as HTMLElement
        val href = element.dataset["href"]
        if (href != null) {
            window.location.href = href
        }
    }

}

// vi: se ts=4 sw=4 et:
