package org.w3c.dom

import kotlin.js.Promise
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget


public external open class Animation:EventTarget  {

    var currentTime: Number? = definedExternally

    val finised: Boolean = definedExternally

    var id: String = definedExternally

    var pending: Boolean = definedExternally

    val playState: String = definedExternally

    var playbackRate: Number = definedExternally

    val ready: Promise<Unit> = definedExternally

    var replaceState: String = definedExternally

    var startTime: Number = definedExternally

    var timeline: AnimationTimeline? = definedExternally

    var oncancel: ((Event)->dynamic)? = definedExternally
   
    var onfinish: ((Event)->dynamic)? = definedExternally 

    var onremove: ((Event)->dynamic)? = definedExternally 

    fun cancel()

    fun commitStyles()

    fun finish()

    fun pause()

    fun persist()

    fun play()

    fun reverse()

    fun updatePlaybackRate(playbackRage: Number) 
    

    
}


fun Element.getAnimations(
    options: dynamic): Array<Animation> {
    
    val elem: dynamic = this
    return elem.getAnimations(options) as Array<Animation>
}


fun Element.animate(keyFrames: dynamic,
    options: dynamic): Animation {
    val elem: dynamic = this
    return elem.animate(keyFrames, options) as Animation
}


// vi: se ts=4 sw=4 et:

