import net.oc_soft.site.App

import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * entry point
 */
fun main(args: Array<String>) {

    val app = App()


    val loadHdlr: (Event)->Unit = {
        app.bind()
    }
    
    window.addEventListener("load", loadHdlr)

}

// vi:se ts=4 sw=4 et:
