package net.oc_soft

import kotlin.js.Promise

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTemplateElement
import org.w3c.dom.events.Event
import org.w3c.dom.get

class App {
    /**
     * header image container html element query
     */
    val headerImageContainerQuery =".header-image.container" 

    /**
     * header message query
     */
    val headerMessageBoxQuery = ".header-image.container .message"

    /**
     * header message base  query
     */
    val headerMessageBoxBaseQuery = ".header-image.container .message-base"
 
    /**
     * slide fragment template query
     */
    val slideFragmentQuery = ".tmpl-header-image"

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
     * message box element in header image container 
     */
    val headerMessageBox: HTMLElement?
        get() {
            return document.querySelector(headerMessageBoxQuery)?.let {
                it as HTMLElement
            }
        }

    /**
     * message box base element in header image container
     */
    val headerMessageBoxBase: HTMLElement?
        get() {
            return document.querySelector(
                headerMessageBoxBaseQuery)?.let {
                it as HTMLElement
            }
        }

    /**
     * header image handler
     */
    var headerImageHdlr: ((Event)->Unit)? = null


    /**
     * background image generartor 
     */
    val backgroundStyle  = arrayOf(
        BackgroundStyle("get-header-image-params"),
        BackgroundStyle("get-slide-image-layout"))


    /**
     * header images
     */
    val headerImages = HeaderImages(
        "get-header-images",
        "get-additions-for-header-image")

    /**
     * header image
     */
    val headerImage: HeaderImage = HeaderImage(
        headerMessageBoxQuery,
        headerMessageBoxBaseQuery,
        ".header-image.container",
        "get-header-image-params",
        backgroundStyle[0]) 



    /**
     * manage header slide image
     */
    val slideImage: Slide = Slide(
        "get-slide-image-params",
        backgroundStyle[1])



    /**
     * start animation
     */
    fun start() {
        headerImageContainer?.let {
            val imageContainer = it

            val promises = arrayOf(
                headerImages.startSyncSetting(),
                headerImage.startSyncSetting(),
                backgroundStyle[0].startSyncSetting(),
                backgroundStyle[1].startSyncSetting(),
                slideImage.startSyncSetting())
            
            Promise.all(promises).then({
                val headerImageHdlr: (Event)->Unit = {
                    if (it.type == "finish") {
                        unbindHeaderImage()
                        startSlideImage()
                    }
                }
                val urls = getHeaderImageUrls()
                headerImage.imageUrls = urls
                bindHeaderImage(headerImageHdlr)
                val animationRunner = headerImage.createAnimation(
                    imageContainer) 
                animationRunner()
            })
        }
    }


    /**
     * get header image url
     */
    fun getHeaderImageUrls(): Array<Array<String>> {
        return if (headerImages.imagesSize > 0) {
            arrayOf(
                headerImages.getImageUrls(0 .. 0),
                headerImages.getImageUrls(0 .. 0))
        } else {
            emptyArray<Array<String>>()
        }
    }

    /**
     * start slide image
     */
    fun startSlideImage() {

        val urlsRange = if (headerImages.imagesSize > 1) {
            1 until headerImages.imagesSize
        } else {
            0 until headerImages.imagesSize
        }
        val urls = headerImages.getImageUrls(
            urlsRange).map({ arrayOf(it) }).toTypedArray()
        
        val colorRange = if (headerImages.colorsSize > 1) {
            1 until headerImages.colorsSize
        } else {
            0 until headerImages.colorsSize
        }
        val colors = headerImages.getColors(colorRange)

        if (urls.isNotEmpty() && colors.isNotEmpty()) {
            headerImageContainer?.let {
                slideImage.startToUpdateImages(
                    it, urls, colors,
                    { createSlideFragment() },
                    { parent, child ->
                        insertElementIntoHeaderImage(parent, child) })
            }
        }
    } 


    /**
     * insert element into image container
     */
    fun insertElementIntoHeaderImage(
        imageContainer: HTMLElement,
        childElement: HTMLElement) {
        imageContainer.append(childElement)  
    }

    /**
     * create slide fragment
     */
    fun createSlideFragment(): HTMLElement? {
        return document.querySelector(slideFragmentQuery)?.let {
            val tmpElem = it as HTMLTemplateElement
        
            val slideFragment = tmpElem.content.firstElementChild?.let {
                it.cloneNode(true) as HTMLElement
            }
            slideFragment?.let {
                setupMessageStyle(it)
            }
            slideFragment
        }
    }


    /**
     * setup message style in element
     */
    fun setupMessageStyle(
        element: HTMLElement) {

        element.querySelector(".message")?.let {
            val dest = it as HTMLElement
            headerMessageBox?.let {
                headerImage.copyColorLayerStyle(it, dest)
            } 
        }

        element.querySelector(".message-base")?.let {
            val dest = it as HTMLElement
            headerMessageBoxBase?.let {
                headerImage.copyBlurColorLayerStyle(it, dest)
            }
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
