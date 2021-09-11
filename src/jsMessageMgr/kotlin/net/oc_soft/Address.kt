package net.oc_soft

import kotlin.text.Regex

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * mange address input
 */
class Address {

    /**
     * class instance
     */
    companion object {

        /**
         * format postal number for search
         */
        fun formatPostalNumberForSearch(
            postalNumber: String): String? {

            val pattern = Regex("(\\d{3})-?(\\d{4,})")

            val result = pattern.find(postalNumber)?.let {
                var numStr2 = it.groups[2]!!.value.substring(0, 4)
                "${it.groups[1]!!.value}-${numStr2}"
            }
            return result 
        }

        /**
         * find prefecture and city from postal number
         */
        fun findPrefCity(
            postalNumberAddressMap: Map<String, Array<String>>,
            postalNumber: String): Array<String>? {
            val postalNumber0 = formatPostalNumberForSearch(postalNumber)
            return postalNumber0?.let {
                postalNumberAddressMap[it]
            }
        }
    }

    /**
     * address container user interface
     */
    var addressContainerElementUi: HTMLElement? = null

    /**
     * postal number to prefecture city map
     */
    var postalNumberPrefCityMap: Map<String, Array<String>>? = null
        

    /**
     * prefecture city map
     */
    var prefCityMap: 
        Map<String, Map<String, Map<String, Array<String>>>>? = null
        set(value) {
            field = value
            value?.let {
                val map = it
                window.setTimeout({ updatePrefectureDataList(map) })
            }
        }

    /**
     * postal number user interface
     */
    val postalNumberElementUi: HTMLElement?
        get() {
            return addressContainerElementUi?.let {
                it.querySelector(".postal-number") as HTMLElement?
            }
        }

    /**
     * prefecture element
     */
    val prefectureElementUi: HTMLElement?
        get() {
            return addressContainerElementUi?.let {
                it.querySelector(".prefecture") as HTMLElement?
            }
        }



    /**
     * prefectures data list
     */
    val prefectureDataListElemetUi: HTMLElement?
        get() {
            return addressContainerElementUi?.let {
                it.querySelector(".prefecture-list") as HTMLElement?
            }
        }
    /**
     * city element
     */
    val cityElementUi: HTMLElement?
        get() {
            return addressContainerElementUi?.let {
                it.querySelector(".city") as HTMLElement?
            }
        }
    /**
     * city data list
     */
    val cityDataListElemetUi: HTMLElement?
        get() {
            return addressContainerElementUi?.let {
                it.querySelector(".city-list") as HTMLElement?
            }
        }


    /**
     * prefecture value in user interface
     */
    var prefectureUi: String?
        get() {
            return prefectureElementUi?.let {
                val elem = it
                when (elem) {
                    is HTMLInputElement -> elem.value
                    else -> elem.innerHTML
                }
            }
        }

        set(value: String?) {
            prefectureElementUi?.let {
                val inputValue = value?: ""
                val elem = it
                when (elem) {
                    is HTMLInputElement -> elem.value = inputValue
                    else -> elem.innerHTML = inputValue
                }
            }
            
        }
    /**
     * last prefecture on user interface
     */
    var lastPrefectureUi: String?
        get() {
            return prefectureElementUi?.let {
                it.dataset["lastInput"]
            }
        } 
        set(value) {
            prefectureElementUi?.let {
                val elem = it
                value?.let {
                    elem.dataset["lastInput"] = it
                }
            }
        }


    /**
     * city value in user interface
     */
    var cityUi: String?
        get() {
            return cityElementUi?.let {
                val elem = it
                when (elem) {
                    is HTMLInputElement -> elem.value
                    else -> elem.innerHTML
                }
            }
        }

        set(value: String?) {
            cityElementUi?.let {
                val inputValue = value?: ""
                val elem = it
                when (elem) {
                    is HTMLInputElement -> elem.value = inputValue
                    else -> elem.innerHTML = inputValue
                }
            }
            
        }


    /**
     * postal number blur event handler
     */
    var postalNumberBlurHdlr: ((Event)->Unit)? = null


    /**
     * prefecture focus handler
     */
    var prefectureFocusHdlr: ((Event)->Unit)? = null 

    /**
     * prefecture blur handler
     */
    var prefectureBlurHdlr: ((Event)->Unit)? = null

    /**
     * attach this object
     */
    fun bind(
        addressContainerElement: HTMLElement) {
        this.addressContainerElementUi = addressContainerElement
        bindPostalNumberUi() 
        bindPrefectureUi()
    }

    /**
     * detach this object from html element
     */
    fun unbind() {
        unbindPrefectureUi()
        unbindPostalNumberUi()
        this.addressContainerElementUi = null
    }


    /**
     * attach this object to postal number input 
     */
    fun bindPostalNumberUi() {
        postalNumberElementUi?.let {
            val hdlr: (Event)->Unit = { handleBlurEventOnPostalNumber(it) }
            postalNumberBlurHdlr = hdlr 
            it.addEventListener("blur", hdlr)
        }
    }

    /**
     * detach this object from postal number input
     */
    fun unbindPostalNumberUi() {
        postalNumberBlurHdlr?.let {
            val hdlr = it
            postalNumberElementUi?.let {
                it.removeEventListener("blur", hdlr)
            }
        }
        postalNumberBlurHdlr = null
    }
    
    /**
     * attach prefecture element
     */
    fun bindPrefectureUi() {
        prefectureElementUi?.let { 
            val blurHdlr: ((Event)->Unit) = { handleBlurEventOnPrefecture(it) }
            val focusHdlr: ((Event)->Unit) = {
                handleFocusOnPrefecture(it)
            }
            it.addEventListener("blur", blurHdlr)
            it.addEventListener("focus", focusHdlr)
            prefectureBlurHdlr = blurHdlr
            prefectureFocusHdlr = focusHdlr
        }
    }

    /**
     * detach prefecture element
     */
    fun unbindPrefectureUi() {
        prefectureElementUi?.let {
            val elem = it
            prefectureBlurHdlr?.let {
                elem.removeEventListener("blur", it)
            }
            prefectureBlurHdlr = null
            prefectureFocusHdlr?.let {
                elem.removeEventListener("focus", it)
            }
            prefectureFocusHdlr = null
        }
    }

    /**
     * handle blur event 
     */
    fun handleBlurEventOnPostalNumber(e: Event) {
        e.currentTarget?.let {
            val elem = it as HTMLElement
            window.setTimeout({
               syncUiWithPostalNumberUi(elem) 
            })
        }
    } 

    /**
     * handle blur event on prefecture
     */
    fun handleBlurEventOnPrefecture(e: Event) {
        window.setTimeout({
            if (prefectureUi != lastPrefectureUi) {
                syncCityDataListWithPrefecture() 
            }
        })
    }

    /**
     * handle focus event on prefecture
     */
    fun handleFocusOnPrefecture(e: Event) {
        lastPrefectureUi = prefectureUi
    }

    /**
     * synchronize user input with postal number input
     */
    fun syncUiWithPostalNumberUi(elem: HTMLElement) {
        val inputValue = when (elem) {
            is HTMLInputElement -> elem.value
            else -> elem.innerHTML
        }
        postalNumberPrefCityMap?.let {
            findPrefCity(it, inputValue)?.let {
                if (it.size > 1) {
                    prefectureUi = it[0]
                    cityUi = it[1]
                }
            }
        }
    }


    /**
     * upate prefecture data list
     */
    fun updatePrefectureDataList(
        prefCityMap: Map<String, Map<String, Map<String, Array<String>>>>) {
        prefectureDataListElemetUi?.let {
             
            while (it.childElementCount > 0) {
                it.lastElementChild?.let {
                    it.remove()
                }
            }
            val dataList = it
            prefCityMap.forEach {
                val option = document.createElement("OPTION") 
                    as HTMLOptionElement
                option.value = it.key 
                dataList.append(option)
            }
        }
    }

    /**
     * update city data list
     */
    fun syncCityDataListWithPrefecture() {
        prefCityMap?.let {
            val mapData = it 
            prefectureUi?.let {
                val prefecture = it
                mapData[it]?.let {
                    val items = it
                    cityDataListElemetUi?.let {
                        
                        while (it.childElementCount > 0) {
                            it.lastElementChild?.let {
                                it.remove() 
                            }
                        }
                        val dataList = it 
                        items.forEach {
                            val option = document.createElement("OPTION") 
                                as HTMLOptionElement
                            option.value = it.key 
                            dataList.append(option)
                        }
                    } 
                }
            }
        }
    }
}

// vi: se ts=4 sw=4 et:
