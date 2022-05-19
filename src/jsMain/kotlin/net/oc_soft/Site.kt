package net.oc_soft

import kotlinx.browser.window
import org.w3c.dom.url.URL
import org.w3c.dom.get

/**
 * manage site related information
 */
class Site {

    /**
     * class instance
     */
    companion object {

        /**
         * request url
         */
        val requestUrl: URL get() = URL(window["oc"].ajax.url as String)
            
    }
}

// vi: se ts=4 sw=4 et:
