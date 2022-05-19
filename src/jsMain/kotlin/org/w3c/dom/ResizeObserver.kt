package org.w3c.dom

public external open class ResizeObserver(
    callback: (Array<ResizeObserverEntry>, ResizeObserver)->Unit) {


    fun disconnect()

    fun observe(
        target: dynamic,
        options: dynamic = definedExternally)

    fun unobserve(
        target: dynamic)
}

// vi: se ts=4 sw=4 et:
