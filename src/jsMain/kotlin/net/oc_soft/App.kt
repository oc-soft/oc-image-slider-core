package net.oc_soft

import kotlin.js.Promise

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.get

class App {

    /**
     * header image container html element query
     */
    val headerImageContainerQuery =".header-image.container" 

    /**
     * header image container
     */
    val headerImageContainer: HTMLElement?
        get() {
            return document.querySelector(headerImageContainerQuery)?.let {
                it as HTMLElement 
            }
        }

    /**
     * header image handler
     */
    var headerImageHdlr: ((Event)->Unit)? = null

    /**
     * header image
     */
    val headerImage: HeaderImage = HeaderImage(
        ".header-image.container .message",
        ".header-image.container .message-base",
        ".header-image.container .message-base",
        "get-header-image-params") 

    /**
     * manage header slide image
     */
    val slideImage: Slide = Slide(
        headerImageContainerQuery,
        ".tmpl-header-image",
        "get-slide-image-params",
        "get-slide-image-layout")

    /**
     * bind this object into html element
     */
    fun bind() {
        bindHeaderImage()
        headerImageContainer?.let {
            val imageContainer = it
            headerImage.startSyncSetting().then({
               headerImage.createAnimation(imageContainer)()
            })
        }
        // slideImage.bind()        
    }

    /**
     * unbind this object from html elements
     */
    fun unbind() {
        // slideImage.unbind()

        unbindHeaderImage()
    }

    /**
     * bind header image
     */
    fun bindHeaderImage() {
        headerImageContainer?.let {
            val hdlr: (Event)->Unit = { 
            }    
            it.addEventListener("start", hdlr)
            it.addEventListener("finish", hdlr)
            headerImageHdlr = hdlr
        }
    }

    /**
     * unbind header image
     */
    fun unbindHeaderImage() {
        headerImageHdlr?.let {
            val hdlr = it
            headerImageContainer?.let {
                it.removeEventListener("start", hdlr)
                it.removeEventListener("finish", hdlr)
            } 
            headerImageHdlr = null
        }
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
