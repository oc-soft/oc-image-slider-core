package net.oc_soft

import kotlinx.browser.window

/**
 * application
 */
class App {



    /**
     * bind this application into html elements
     */
    fun bind() {
    }


    /**
     * detach this application from html elements
     */
    fun unbind() {
    }

    /**
     * run application
     */
    fun run() {

        window.addEventListener("load", 
            { bind() },
             object {
                @JsName("once")  
                val once = true
            })

        window.addEventListener("unload", 
            { unbind() },
            object {
                @JsName("once")  
                val once = true
            })
    }
}

// vi: se ts=4 sw=4 et:
