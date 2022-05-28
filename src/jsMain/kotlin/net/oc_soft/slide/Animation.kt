package net.oc_soft.slide

import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.Event

/**
 * animation 
 */
class Animation(
    /**
     * animation fragments
     */
    val fragments: Array<Fragment>,
    /**
     * run animation
     */
    val runAnimation: ()->Unit) {



    /**
     * start animation
     */
    fun play() {

        runAnimation()
    }

}

// vi: se ts=4 sw=4 et:
