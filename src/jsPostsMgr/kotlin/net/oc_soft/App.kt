package net.oc_soft

import kotlinx.browser.window

/**
 * application
 */
class App(
    /**
     * manage posts
     */
    val posts: Posts = Posts()) {



    /**
     * bind this object into html element
     */
    fun bind() {
        posts.bind()
    }

    /**
     * detach this object from html element
     */
    fun unbind() {
        posts.unbind()
    } 

    /**
     * run application
     */
    fun run() {
        window.addEventListener(
            "load",
            { bind() },
            object {
                @JsName("once")
                val once = true
            }) 

        window.addEventListener(
            "unload",
            { unbind() },
            object {
                @JsName("once")
                val once = true
            }) 
    }
}

// vi: se ts=4 sw=4 et:
