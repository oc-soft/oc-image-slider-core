package net.oc_soft

import kotlin.js.Promise

import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.HashMap

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.HTMLElement 
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URL
import org.w3c.dom.events.Event
import org.w3c.dom.get

/**
 * application
 */
class App(
    /**
     * mange modal for verify dialog
     */
    val modalVerify: ModalVerify = ModalVerify(),
    val jobSelector: JobSelector = JobSelector()) { 

    /**
     * class instance
     */
    companion object {

        /**
         * current site directory
         */
        val currentSiteDirectory: String
            get() { 
                val path = window.location.pathname
                return if (path.length > 1) {
                    val lastIdx = path.lastIndexOf('/')
                    path.substring(0, lastIdx + 1)
                } else {
                    "/"
                }
            }

        /**
         * send message
         */
        fun sendMessage(parameter: Map<String, String>): Promise<String> {
            val url = URL("${currentSiteDirectory}work-us.php",
                window.location.origin)
            val searchParams = url.searchParams
            parameter.forEach {
                searchParams.append(it.key, it.value)
            }

            val result = window.fetch(url).then({
                it.text()
            }).then({ it })
            return result
        }
    }

    /**
     * verify command html element
     */
    val verifyElementUi: HTMLElement?
        get() {
            return document.querySelector("button.verify-and-send")
                as HTMLElement?
        }  

    /**
     * form element
     */
    val formElementUi: HTMLFormElement?
        get() {
            return document.querySelector(".contact-us")
                as HTMLFormElement?
        }

    /**
     * command handler to verify
     */
    var verifyCmdHdlr: ((Event)->Unit)? = null

    /**
     * handler for sending message
     */
    var sendCmdHdlr: ((String, ModalVerify)->Unit)? = null

    /**
     * handler for cancel to send message
     */
    var cancelCmdHdlr: ((String, ModalVerify)->Unit)? = null

    /**
     * bind this application into html elements
     */
    fun bind() {
        val verifyCmdHdlr: (Event)->Unit = { handleEventToVerify(it) }

        val sendCmdHdlr: (String, ModalVerify)->Unit  = {
            kind, sender ->
            handleEventToSendMessage(kind, sender) 
        }
        val cancelCmdHdlr:(String, ModalVerify)->Unit  = {
            kind, sender ->
            handleEventToCancleMessage(kind, sender)
        }
 
        modalVerify.bind()
        modalVerify.addEventListener("send", sendCmdHdlr)
        modalVerify.addEventListener("cancel", cancelCmdHdlr)


        jobSelector.bind()
        verifyElementUi?.let {
            it.addEventListener("click", verifyCmdHdlr)
        }
        this.verifyCmdHdlr = verifyCmdHdlr
        this.sendCmdHdlr = sendCmdHdlr
        this.cancelCmdHdlr  = cancelCmdHdlr
    }


    /**
     * detach this application from html elements
     */
    fun unbind() {
        jobSelector.unbind()
        modalVerify.unbind()
        sendCmdHdlr?.let {
            modalVerify.removeEventListener("send", it)
        }
        cancelCmdHdlr?.let {
            modalVerify.removeEventListener("cancel", it)
        }
        verifyCmdHdlr?.let {
            val hdlr = it
            verifyElementUi?.let {
                it.removeEventListener("click", hdlr)
            }
        }
        this.verifyCmdHdlr = null
        this.sendCmdHdlr = null 
        this.cancelCmdHdlr = null 
    }


    /**
     * handle event to verify message
     */
    fun handleEventToVerify(event: Event) {

        formElementUi?.let { 
            if (it.reportValidity()) {
                sendMessageForVerify().then({
                     
                    it?.let {
                        modalVerify.body = it
                        modalVerify.show()
                    }
                })
            }
        }
    }


    /**
     * send message for verify
     */
    fun sendMessageForVerify(): Promise<String?> {

        val params = createParameterForMessage()
        params["jsk-preview"] = true.toString()
        return sendMessage(params) 
    }


    /**
     * create parameter for message
     */
    fun createParameterForMessage(): MutableMap<String, String> {
        val result = HashMap<String, String>()
        formElementUi?.let {
            val inputElements = it.querySelectorAll("input")
            for (idx in 0 until inputElements.length) {
                val inputElem = inputElements[idx] as HTMLInputElement
                result[inputElem.name] = inputElem.value
            }
            val selectElements = it.querySelectorAll("select") 
            for (idx in 0 until selectElements.length) {
                val selectElem = selectElements[idx] as HTMLSelectElement
                result[selectElem.name] = selectElem.value
            }
            val textAreaElements = it.querySelectorAll("textarea") 
            for (idx in 0 until textAreaElements.length) {
                val textAreaElem = textAreaElements[idx] as HTMLTextAreaElement
                result[textAreaElem.name] = textAreaElem.value
            }
        } 
        return result 
    } 
    


    /**
     * handler for sending message
     */
    fun handleEventToSendMessage(kind: String, sender: ModalVerify) {
        modalVerify.hide()
        formElementUi?.let {
            it.submit()
        }
    }

    /**
     * handler for cancel to send message
     */
    fun handleEventToCancleMessage(kind: String, sender: ModalVerify) {
        modalVerify.hide()
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
