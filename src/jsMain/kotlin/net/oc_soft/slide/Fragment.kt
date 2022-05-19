package net.oc_soft.slide

import org.w3c.dom.HTMLElement

class Fragment(
    /**
     * html element
     */
    val element: HTMLElement,
    /**
     * key frames. It is used for Element.animate
     */
    val keyFrames: Array<dynamic>,
    /**
     * options for Element.animate
     */
    val options: dynamic) 

// vi: se ts=4 sw=4 et:
