package net.oc_soft.site


import net.oc_soft.site.href.EachAccesses
import net.oc_soft.site.href.Registration

/**
 * site manager application
 */
class App {
    
    /**
     * database instance
     */
    var db: Db? = null

    
    /**
     * manager for each access
     */
    var eachAccesses: EachAccesses? = null


    /**
     * manage href tracking registration
     */
    var registration: Registration? = null

    /**
     * bind this object into html elements
     */
    fun bind() {
        this.db = Db()
        this.db!!.bind()

        this.eachAccesses = EachAccesses()
        this.eachAccesses!!.bind()

        this.registration = Registration()
        this.registration!!.bind()
    }

    /**
     * detach this object from html elements.
     */
    fun unbind() {

        if (this.registration != null) {
            this.registration!!.unbind()
            this.registration = null
        }

        if (this.eachAccesses != null) {
            this.eachAccesses!!.unbind()
            this.eachAccesses = null
        }
        if (this.db != null) {
            this.db!!.unbind()
            this.db = null
        }
    }
}

// vi: se ts=4 sw=4 et:
