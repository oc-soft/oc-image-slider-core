package net.oc_soft

import kotlinx.browser.document
import kotlinx.browser.window

import net.oc_soft.track.Tracker
import net.oc_soft.InitEffect

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLMediaElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event
import org.w3c.dom.get

class App(
    /**
     * initial effect
     */
    val initEffect: InitEffect = InitEffect(),
    /**
     * lazy video loader
     */
    val video: Video = Video()) {

    /**
     * banner video
     */
    val bannerVideo: HTMLElement?
        get() {
            return document.querySelector("div.movie video") as HTMLElement?
        }

    /**
     * tack user activity
     */
    var tracker: Tracker? = null
     
    /**
     * simulate anchor element
     */
    var anchor: Anchor? = null


    /**
     * event handler for banner video at ended
     */
    var bannerVideoEndedHdlr: ((Event)->Unit)? = null

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

        video.startLoadResource()
        bindTopBannerVideo()
    }

    /**
     * unbind this object from html elements
     */
    fun unbind() {
        unbindTopBannerVideo()
        video.unbind()
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
     * bind banner video
     */
    fun bindTopBannerVideo() {
        bannerVideo?.let { 
            val hdlr: (Event)->Unit = {
                handleBannerEnded(it)
            }
            it.addEventListener("ended", hdlr)
            bannerVideoEndedHdlr = hdlr 
        }
    }
    /**
     * unbind banner video
     */
    fun unbindTopBannerVideo() {
        bannerVideoEndedHdlr?.let {
            val hdlr = it
            bannerVideo?.let {
                it.removeEventListener("ended", hdlr)
            }
        }
        bannerVideoEndedHdlr = null
    }

    /**
     * handle banner video ended event
     */
    fun handleBannerEnded(event: Event) {
        stopVideoLastFrame(event.currentTarget as HTMLVideoElement)
    }


    /**
     * html media element
     */
    fun stopVideoLastFrame(element: HTMLVideoElement) {
        element.dataset["lastFrame"]?.let {
            element.poster = it
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
