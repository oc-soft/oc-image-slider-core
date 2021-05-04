package net.oc_soft

import net.oc_soft.track.Tracker

class App {


    /**
     * tack user activity
     */
    var tracker: Tracker? = null
     
    /**
     * simulate anchor element
     */
    var anchor: Anchor? = null

    /**
     * bind this object into html element
     */
    fun bind() {
        tracker = Tracker() 
        tracker!!.bind()
        anchor = Anchor()    
        anchor!!.bind()
    }

    /**
     * unbind this object from html elements
     */
    fun unbind() {
        val tracker = this.tracker
        if (tracker != null) {
            tracker.unbind()
            this.tracker = null
        }
        val anchor = this.anchor
        if (anchor != null) {
            anchor.unbind()
            this.anchor = null
        }
    }

}

// vi: se ts=4 sw=4 et:
