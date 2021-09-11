package net.oc_soft

import kotlin.js.Promise
import kotlin.js.Json

import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

import kotlin.text.toBoolean

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
import org.w3c.fetch.Response

/**
 * application
 */
class App(
    /**
     * manage address user interface
     */
    val address: Address = Address(),
    /**
     * manage modal for verify dialog
     */
    val modalVerify: ModalVerify = ModalVerify()) { 

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
            val url = URL("${currentSiteDirectory}message.php",
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


        /**
         * convert json to postal number
         */
        fun jsonToPostalNumber(
            postalJson: Json): Map<String, Array<String>> {

            val result = LinkedHashMap<String, Array<String>>()

            val keys: dynamic = js("Object.keys(postalJson)") 
            for (idx in 0 until (keys.length as Number).toInt()) {
                val key = keys[idx].toString()
                val values: dynamic = postalJson[key] 
                val valuesLength = (values.length as Number).toInt()
                val valueArray = Array<String>(valuesLength) {
                    values[it]
                }
                result[key] = valueArray
            }
            return result
        }


        /**
         * convert json to precture city map
         */
        fun jsonToPrefCity(
            prefCity: Json): 
                Map<String, Map<String, Map<String, Array<String>>>> {
            val result = LinkedHashMap<
                String, Map<String, Map<String, Array<String>>>>()
            val keys0: dynamic = js("Object.keys(prefCity)") 
            for (idx0 in 0 until (keys0.length as Number).toInt()) {
                val map0 = LinkedHashMap<String, Map<String, Array<String>>>()  
                val key0 = keys0[idx0] as String
                val pcMap0: dynamic = prefCity[key0]
                val keys1: dynamic = js("Object.keys(pcMap0)")
                for (idx1 in 0 until (keys1.length as Number).toInt()) {
                    val map1 = LinkedHashMap<String, Array<String>>() 
                    val key1 = keys1[idx1] as String
                    val pcMap1: dynamic = pcMap0[key1]
                    val keys2: dynamic = js("Object.keys(pcMap1)")
                    for (idx2 in 0 until (keys2.length as Number).toInt()) {
                        val key2 = keys2[idx2] as String
                        val pcArray2: dynamic = pcMap1[key2]
                        val keys2Length = (pcArray2.length as Number).toInt()
                        val items = Array<String>(keys2Length) {
                            pcArray2[it] as String
                        } 
                        map1[key2] = items
                    }
                    map0[key1] = map1
                }
                result[key0] = map0
            }
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
     * selector for service kind
     */
    val serviceKindSelectUi: HTMLSelectElement?
        get() {
            return formElementUi?.let {
                it.querySelector("select[name='jsk-service-kind']")
                    as HTMLSelectElement?
            }
        }

    /**
     * the user interface to control enable or not about tour
     */
    val visitingElementUi: HTMLInputElement?
        get() {
            return formElementUi?.let {
                it.querySelector("input[name='jsk-visiting-request']")
                    as HTMLInputElement?
            }
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
     * postal number address mapping
     */
    var postalNumberMap: Map<String, Array<String>>? = null 
        set(value: Map<String, Array<String>>?) {
            field = value 
            address.postalNumberPrefCityMap = value
        }

    /**
     * prefecture city map
     */
    var prefCityMap: 
        Map<String, Map<String, Map<String, Array<String>>>>? = null 
        set(value) {
            field = value
            address.prefCityMap = value 
        }

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
        verifyElementUi?.let {
            it.addEventListener("click", verifyCmdHdlr)
        }
        this.verifyCmdHdlr = verifyCmdHdlr
        this.sendCmdHdlr = sendCmdHdlr
        this.cancelCmdHdlr = cancelCmdHdlr
        
        address.bind(document.body!!)

        startLoadPostalNumberMap()
        startLoadPrefectureCityMap()
    }


    /**
     * detach this application from html elements
     */
    fun unbind() {
        address.unbind() 
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
     * synchronize visiting request user interface with service kind selector
     */
    fun syncVisigingRequestWithSelectedService() {
        var tourEnable = false 
        serviceKindSelectUi?.let {
            val options = it.selectedOptions
            if (options.length > 0) {
                val elem = options[0] as HTMLElement
                elem.dataset["visiting"]?.let {
                    tourEnable = it.toBoolean() 
                }
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
                if (inputElem.type == "checkbox") {
                    if (inputElem.checked) {
                        result[inputElem.name] = inputElem.value
                    }
                } else {
                    result[inputElem.name] = inputElem.value
                }
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
     * start to load postal number map
     */
    fun startLoadPostalNumberMap() {
        
        val url = URL("${currentSiteDirectory}address-jp.php",
            window.location.origin)
        val searchParams = url.searchParams
        searchParams.append("address", "postal-number.json")

        window.fetch(url).then({
            if (it.ok) {
                it.json()
            } else {
                Promise<Any?> { 
                    resolve, _ ->
                    resolve(null)
                }
            } 
        }).then({
            it?.let {
                postalNumberMap = jsonToPostalNumber(it as Json) 
                
            }
        })
    }

    /**
     * start to load prefecture city map 
     */
    fun startLoadPrefectureCityMap() {
        val url = URL("${currentSiteDirectory}address-jp.php",
            window.location.origin)
        val searchParams = url.searchParams
        searchParams.append("address", "pref-city.json")

        window.fetch(url).then({
            if (it.ok) {
                it.json()
            } else {
                Promise<Any?> { 
                    resolve, _ ->
                    resolve(null)
                }
            } 
        }).then({
            it?.let {
                prefCityMap = jsonToPrefCity(it as Json) 
            }
        })
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
