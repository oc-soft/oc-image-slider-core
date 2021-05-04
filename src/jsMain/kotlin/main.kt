
import kotlinx.browser.window

import net.oc_soft.App

fun main(args: Array<String>) {

    window.addEventListener("load", {
        evt ->
        val app = App()
        app.bind()
    })
}

// vi: se ts=4 sw=4 et:
