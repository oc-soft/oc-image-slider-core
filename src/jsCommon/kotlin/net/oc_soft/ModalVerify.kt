package net.oc_soft


import kotlin.collections.Map
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.MutableList
import kotlin.collections.HashMap
import kotlin.collections.ArrayList

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

/**
 * message verify modal
 */
class ModalVerify {

    /**
     * modal user interface
     */
    val modalElementUi: HTMLElement?
        get() {
            return document.querySelector(".modal.verify")
                as HTMLElement?
        }

    /**
     * message body
     */
    val bodyElementUi: HTMLElement?
        get() {
            return modalElementUi?.let {
                it.querySelector(".modal-body") as HTMLElement?
            }
        }

    /**
     * send command user interface
     */
    val sendElementUi: HTMLElement?
        get() {
            return modalElementUi?.let {
                it.querySelector("button.send")
                    as HTMLElement?
            }
        }
        

    /**
     * cancel command user interface
     */
    val cancelElementUi: HTMLElement?
        get() {
            return modalElementUi?.let {
                it.querySelector("button.cancel")
                    as HTMLElement?
            }
        }


    /**
     * verify body content
     */
    var body: String
        get() {
            return bodyElementUi?.let {
                it.innerHTML
            }?: "" 
        }
        set(value) {
            bodyElementUi?.let {
                it.innerHTML = value
            }
        }
    

    /**
     * eventListeners
     */
    val eventListeners: MutableMap<
            String, MutableList<(String, ModalVerify)->Unit>>
        = HashMap<String, MutableList<(String, ModalVerify)->Unit>>()
    /**
     * command handler to send message
     */
    var sendCmdHdlr: ((Event)->Unit)? = null
     
    /**
     * command handler to send message
     */
    var cancelCmdHdlr: ((Event)->Unit)? = null
 
    /**
     * bind this object into html element
     */
    fun bind() {

        val sendCmdHdlr: (Event)->Unit = { handleEventToSend(it) }
        val cancelCmdHdlr: (Event)->Unit = { handleEventToCancel(it) }

        
        sendElementUi?.let {
            it.addEventListener("click", sendCmdHdlr)
        }  
        cancelElementUi?.let {
            it.addEventListener("click", cancelCmdHdlr)
        }
        this.sendCmdHdlr = sendCmdHdlr
        this.cancelCmdHdlr = cancelCmdHdlr


        modalElementUi?.let {
            bootstrap.Modal.attach(it)
        }
    }


    /**
     * detach this object from html element
     */
    fun unbind() {
        modalElementUi?.let {
            bootstrap.Modal.detach(it)
        }
        sendCmdHdlr?.let {
            val hdlr = it
            sendElementUi?.let {
                it.removeEventListener("click", hdlr)
            }   
        }
        cancelCmdHdlr?.let {
            val hdlr = it
            cancelElementUi?.let {
                it.removeEventListener("click", hdlr)
            }
        }
        this.sendCmdHdlr = null
        this.cancelCmdHdlr = null 
    }

    /**
     * show modal
     */
    fun show() {
        modalElementUi?.let {
            bootstrap.Modal.show(it)
        } 
    }


    /**
     * hide modal
     */
    fun hide() {
        modalElementUi?.let {
            bootstrap.Modal.hide(it)
        } 
    }

    
    /**
     * handle event to send message
     */
    fun handleEventToSend(event: Event) {
        notify("send")
    }  


    /**
     * handle event to cancel to send message
     */
    fun handleEventToCancel(event: Event) {
        notify("cancel")
    }


    /**
     * add event listener
     */
    fun addEventListener(kind: String, listener: (String, ModalVerify)->Unit) {
        var listeners = eventListeners[kind]
        if (listeners == null) {
            listeners = ArrayList<(String, ModalVerify)->Unit>()
            eventListeners[kind] = listeners
        }
        listeners.add(listener)
    }

    /**
     * remove event listener
     */
    fun removeEventListener(
        kind: String, listener: (String, ModalVerify)->Unit) {
        var listeners = eventListeners[kind]
        if (listeners != null) {
            listeners.remove(listener)
        }
    }


    /**
     * notify event
     */
    fun notify(kind: String) {
        var listeners = eventListeners[kind]
        if (listeners != null) {
            val sender = this 
            listeners.forEach { it(kind, sender) }
        }
    }

}

// vi: se ts=4 sw=4 et:
