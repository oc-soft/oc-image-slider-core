package bootstrap

import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.HTMLElement

/**
 * manage modal
 */
class Modal {

    /**
     * class instance
     */
    companion object {

        /**
         * boot strap instance
         */
        val instance: dynamic get() = window["bootstrap"]


        /**
         * attach modal instance into html element
         */
        fun attach(element: HTMLElement) {
            instance.Modal.getOrCreateInstance(element)
        }


        /**
         * show modal 
         */
        fun show(element: HTMLElement) {
            val modal = instance.Modal.getInstance(element)
            if (modal != null) {
                modal.show()
            }
        }

        /**
         * hide modal 
         */
        fun hide(element: HTMLElement) {
            val modal = instance.Modal.getInstance(element)
            if (modal != null) {
                modal.hide()
            }
        }


        /**
         * detach modal
         */
        fun detach(element: HTMLElement) {
            val modal = instance.Modal.getInstance(element)
            if (modal != null) {
                modal.dispose()
            }
        } 
    }
}

// vi: se ts=4 sw=4 et:
