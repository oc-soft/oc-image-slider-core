package net.oc_soft.track

import kotlinx.browser.document
import kotlinx.browser.window


import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.get

import org.w3c.dom.url.URLSearchParams
import org.w3c.dom.url.URL

import org.w3c.fetch.RequestInit


/**
 * track user input
 */
class Tracker {

    /**
     * class instance
     */
    companion object {

        /**
         * create href for element
         */
        fun createHref(element: Element): String {
            val loc = document.location!!
            val url = URL(loc.href)
            url.hash = element.id

            return "${url.pathname}${url.search}${url.hash}"
        }
        /**
         * create href for element
         */
        fun createHref(href: String): String {
            val loc = document.location!!
            val url = URL(href)

            return "${url.pathname}${url.search}${url.hash}"
        }
     }
    
    /**
     * handler to track href request
     */
    var hrefRequestHdlr: ((Event)->Unit)? = null

    /**
     * bind this object into html elements
     */
    fun bind() {
        hrefRequestHdlr = { handleHrefRequest(it) }
        val elements = document.getElementsByTagName("a")
        for (idx in 0 until elements.length) {
            bind(elements[idx] as EventTarget)
        }
    }  


    /**
     * unbind this object from html elements
     */
    fun unbind() {
        if (hrefRequestHdlr !=  null) {
            val elements = document.getElementsByTagName("a")
            for (idx in 0 until elements.length) {
                unbind(elements[idx] as EventTarget)
            }
            hrefRequestHdlr =  null
        }
    }


    /**
     * bind this object to a html element
     */
    fun bind(element: EventTarget) {
        element.addEventListener("click", hrefRequestHdlr) 
    }

    /**
     * disconnect this object from a html element
     */
    fun unbind(element: EventTarget) {
        element.removeEventListener("click", hrefRequestHdlr)
    }
    
    /**
     * handle href request
     */
    fun handleHrefRequest(event: Event) {
        val element = event.currentTarget
        sendHrefRequest(element as HTMLElement)
    }


    /**
     * send href to server
     */
    fun sendHrefRequest(element: HTMLElement) {
         when (element) {
            is HTMLAnchorElement -> {
                val anchor = element 
                sendHrefRequest(createHref(anchor), createHref(anchor.href))
            }  
        }       
    }

    


    /**
     * send href request
     */
    fun sendHrefRequest(
        srcHref: String,
        destHref: String) {

        val body = URLSearchParams()
        body.append("track", "")
        body.append("insert", "")
        body.append("href-src", srcHref)
        body.append("href-dst", destHref)
        window.fetch("/mgr-rest.php",
            RequestInit(
                method = "POST",
                body = body))
    }
}

// vi: se ts=4 sw=4 et:
