
import kotlinx.browser.window
import net.oc_soft.track.Tracker

fun main(args: Array<String>) {

    window.addEventListener("load", {
        evt ->
        val tracker = Tracker() 
        tracker.bind()
    })
}

// vi: se ts=4 sw=4 et:
