package net.oc_soft

import kotlin.js.Promise

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.get

class App {
    /**
     * back ground element loader
     */
    val elementBackground: ElementBackground = ElementBackground()
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
        ".header-image.container",
        "get-header-image-params") 

    /**
     * manage header slide image
     */
    val slideImage: Slide = Slide(
        headerImageContainerQuery,
        ".tmpl-header-image",
        "get-header-images",
        "get-slide-image-params",
        "get-slide-image-layout")


    /**
     * start animation
     */
    fun start() {
        headerImageContainer?.let {
            val imageContainer = it

            val promises = arrayOf(
                headerImage.startSyncSetting(),
                slideImage.startSetting())

            Promise.all(promises).then({
                val headerImageHdlr: (Event)->Unit = {
                    if (it.type == "finish") {
                        unbindHeaderImage()
                        slideImage.startToUpdateImages()
                    }
                }
                bindHeaderImage(headerImageHdlr)
                val animationRunner = headerImage.createAnimation(
                    imageContainer) 
                animationRunner()
            })
        }
    }


    /**
     * bind this object into html element
     */
    fun bind() {
        slideImage.bind()
    }

    /**
     * unbind this object from html elements
     */
    fun unbind() {
        slideImage.unbind()

    }

    /**
     * bind header image
     */
    fun bindHeaderImage(eventHdlr: (Event)->Unit) {
        headerImageContainer?.let {
            it.addEventListener("start", eventHdlr)
            it.addEventListener("finish", eventHdlr)
            headerImageHdlr = eventHdlr 
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
            start()
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
