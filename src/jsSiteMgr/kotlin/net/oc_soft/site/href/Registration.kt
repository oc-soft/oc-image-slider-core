package net.oc_soft.site.href

import kotlin.js.Promise

import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.toList
import kotlin.collections.Map
import kotlin.collections.ArrayList
import kotlin.collections.mapOf

import kotlin.text.toDouble

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.HTMLTableSectionElement
import org.w3c.dom.HTMLTableRowElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.DOMRect
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.RequestInit

import popper.Popper
import jQuery
/**
 * href registration
 */
class Registration {
    

    /**
     * class instance
     */
    companion object {

        /**
         * encode json
         */
        fun toJsonObject(queryOptions: Array<Map<String, String>>): Any  {
            val result = Array<Any>(queryOptions.size) {
                val map = queryOptions[it]
                val item : dynamic = object { }
                map.forEach {
                    item[it.key] = it.value
                }
                item
            }
            return result
        }
    }

    /**
     * get table element for  each accesses 
     */
    val tableUi: HTMLTableElement?
        get() {
           return document.querySelector("table.href-registration-list")
            as HTMLTableElement?
        }

    /**
     * narrow down menu item
     */
    val narrowDownUi: HTMLElement?
        get() {
           return document.querySelector(
                ".narrow-down.href-registration")
            as HTMLElement?
        }

    /**
     * order menu item
     */
    val orderUi: HTMLElement?
        get() {
           return document.querySelector(
                ".order.href-registration")
            as HTMLElement?
        }

    /**
     * narrow down menu 
     */
    val narrowDownMenuUi: HTMLElement?
        get() {
            return document.querySelector(
                ".dropdown-ex-menu.narrow-down") as HTMLElement?
        }

    /**
     * order menu 
     */
    val orderMenuUi: HTMLElement?
        get() {
            return document.querySelector(
                ".dropdown-ex-menu.order") as HTMLElement?
        }
    
    /**
     * user interface to show record input modal
     */
    val visibleRecordInputModalUi: HTMLElement?
        get() {
            return document.querySelector(
                ".visible-href-input") as HTMLElement?
        }

    /**
     * href record input modal element
     */
    val recordInputModalUi: HTMLElement?
        get() {
            return document.querySelector(
                ".modal.href-registration") as HTMLElement? 
        }
    /**
     * get href user interface in record input modal
     */
    val recordInputHrefUi: HTMLInputElement?
        get() {
            val modal = recordInputModalUi
            var result: HTMLInputElement? = null
            if (modal != null) {
                result = modal.querySelector("input[name='href']")
                    as HTMLInputElement?
            }
            return result
        }

    /**
     * get selector input user interface in record input modal
     */
    val recordInputSelectorUi: HTMLInputElement?
        get() {
            val modal = recordInputModalUi
            var result: HTMLInputElement? = null
            if (modal != null) {
                result = modal.querySelector("input[name='selector']")
                    as HTMLInputElement?
            }
            return result
        }
    /**
     * get href in record input modal
     */
    var recordInputHref: String?
        get() {
            var result: String? = null
            val ui = recordInputHrefUi
            if (ui != null) {
                result = ui.value
            }
            return result 
        }
        set(value) {
            val ui = recordInputHrefUi
            if (ui != null) {
                ui.value = if (value != null) {
                    value
                } else {
                    ""
                }
            }
        }

    /**
     * selector in record input modal
     */
    var recordInputSelector: String?
        get() {
            var result: String? = null
            val ui = recordInputSelectorUi
            if (ui != null) {
                result = ui.value
            }
            return result 
        }
        set(value) {
            val ui = recordInputSelectorUi
            if (ui != null) {
                ui.value = if (value != null) {
                    value
                } else {
                    ""
                }
            }
        }



    /**
     * button to add href record
     */
    val buttonToAddRecordUi: HTMLElement?
        get() {
            return document.querySelector(
                ".add-record") as HTMLElement?
        }
    /**
     * sort icon map
     */
    val sortIconMap: Map<String, String> by lazy {
        mapOf(
            "icon-no-sort" to "icon-sort-asc",
            "icon-sort-asc" to "icon-sort-desc",
            "icon-sort-desc" to "icon-no-sort") 
    }

    /**
     * sort icon to order map
     */
    val sortIconOrderMap: Map<String, String?> by lazy {
        mapOf(
            "icon-no-sort" to null,
            "icon-sort-asc" to "ASC",
            "icon-sort-desc" to "DESC")
    }

    /**
     * narrow down user iterface items
     */
    val narrowDownItemUiList: Array<HTMLElement>? 
        get() {
            val narrowDownMenuUi = this.narrowDownMenuUi
            var result: Array<HTMLElement>? = null 
            
            if (narrowDownMenuUi != null) {
                val items = narrowDownMenuUi.querySelectorAll("[data-name]") 
                result = Array<HTMLElement>(items.length) {
                    items[it] as HTMLElement
                }
            } 
            return result
        }
    /**
     * order user iterface items
     */
    val orderItemUiList: Array<HTMLElement>?
        get() {
            val orderMenuUi = this.orderMenuUi
            var result: Array<HTMLElement>? = null 
            
            if (orderMenuUi != null) {
                val items = orderMenuUi.querySelectorAll("[data-name]") 
                result = Array<HTMLElement>(items.length) {
                    items[it] as HTMLElement
                }
            } 
            return result
        }
    
    /**
     * narrow down items
     */
    val narrowDownItems: Array<Map<String, String>> 
        get() {
            val itemList = ArrayList<Map<String, String>>()
            val uiItems = narrowDownItemUiList
            if (uiItems != null) {
                uiItems.forEach {
                    val strItem = it.innerHTML
                    if (strItem.isNotEmpty()) {
                        itemList.add(
                            mapOf("column" to it.dataset["name"]!!,
                                "pattern" to strItem))  
                    }
                }
            }
            return Array<Map<String, String>>(itemList.size) { itemList[it] }
        }

    /**
     * order items
     */
    val orderItems: Array<Map<String, String>> 
        get() {
            val itemList = ArrayList<Map<String, String>>()
            val uiItems = orderItemUiList
            if (uiItems != null) {
                uiItems.forEach {
                    val iconContainer = it.querySelector("span") as HTMLElement
                    val icon = getSortIcon(iconContainer) 
                    val sortKind = sortIconOrderMap[icon]
                    if (sortKind != null) {
                        itemList.add(   
                            mapOf(
                                "column" to it.dataset["name"]!!,
                                "sort" to sortKind))
                    }
                }
            }
            return Array<Map<String, String>>(itemList.size) { itemList[it] }
        }

    /**
     * last query option. it is valid only when menu is opening
     */
    var lastQueryOption: Array<Map<String, String>>? = null


    /**
     * popup menu hdlr
     */
    var popupMenuHdlr: ((Event)->Unit)? = null

    /**
     * event handler to close opened menu.
     */
    var closeMenuHdlr: ((Event)->Unit)? = null


    /**
     * handle to move ghost node on window
     */
    var movingGhostHdlr: ((Event)->Unit)? = null

    /**
     * handle mouse up event while you move ghost node
     */
    var mouseupOnGhostHdlr: ((Event)->Unit)? = null

    /**
     * handler to start move order item
     */
    var orderToStartMoveHdlr: ((Event)->Unit)? = null

    /**
     * handle event to change sort order
     */
    var sortOrderHdlr: ((Event)->Unit)? = null

    /**
     * handle event to show href input modal
     */
    var visibleInputModalHdlr: ((Event)->Unit)? = null

 
    /**
     * handle event to add record
     */
    var addRecordHdlr: ((Event)->Unit)? = null

    

    /**
     * ghost element
     */
    var ghostElement: HTMLElement? = null

    /**
     * narrow down menu item popper
     */
    var narrowDownPopper: popper.Instance? = null

    /**
     * order popper instance
     */
    var orderPopper: popper.Instance? = null


    /**
     * order menu
     */
    val orderMenuItems: Array<HTMLTableRowElement>?
        get() {
            val orderMenuUi = this.orderMenuUi
            var result: Array<HTMLTableRowElement>? = null
            if (orderMenuUi != null) {
                val items = orderMenuUi.querySelectorAll("table tbody tr")
                result = Array<HTMLTableRowElement>(items.length) {
                    items[it] as HTMLTableRowElement
                } 
            } 
            return result
        }

    /**
     * items to manipulate sort kind
     */
    val orderSortItems: Array<HTMLElement>?
        get() {
            val orderMenuItems = this.orderMenuItems
            var result: Array<HTMLElement>? = null

            if (orderMenuItems != null) {
               result = Array(orderMenuItems.size) {
                    orderMenuItems[it].children[1] as HTMLElement
               }
            }

            return result
        }



    /**
     * bind this object into html elements
     */
    fun bind() {
        val clickHdlr: (Event)-> Unit = {
            handleToPopupMenu(it)
        } 
        val orderToStartMoveHdlr: (Event)->Unit = {
            handleStartToMoveItem(it)
        }
        val sortOrderHdlr: (Event)->Unit = {
            handleSortOrderEvent(it)
        } 
        val visibleInputModalHdlr: (Event)->Unit = {
            handleToShowRecordInput(it) 
        }
        val addRecordHdlr: (Event)->Unit =  {
            handleToAddRecord(it)
        }

 

        var boundElements = false
        val narrowDownUi = this.narrowDownUi
        val narrowDownMenuUi = this.narrowDownMenuUi
        if (narrowDownUi != null
            && narrowDownMenuUi != null) {
            narrowDownUi.addEventListener("click", clickHdlr)
            val option: dynamic = object { }

            narrowDownPopper = popper.Popper.createPopper(
                narrowDownUi, narrowDownMenuUi, option)
            boundElements = true
        }

        val orderUi = this.orderUi
        val orderMenuUi = this.orderMenuUi
        if (orderUi != null
            && orderMenuUi != null) {
            orderUi.addEventListener("click", clickHdlr)
            val option: dynamic = object { }

            orderPopper = popper.Popper.createPopper(
                orderUi, orderMenuUi!!, option)

            orderMenuUi.addEventListener("mousedown",
                orderToStartMoveHdlr)
            boundElements = true
        }
        val orderSortItems = this.orderSortItems
        if (orderSortItems != null) {

            orderSortItems.forEach {
                it.addEventListener("click", sortOrderHdlr)
            }
            boundElements = true
        }
        val visibleRecordInputModalUi = this.visibleRecordInputModalUi 
        if (visibleRecordInputModalUi != null) {
            visibleRecordInputModalUi.addEventListener(
                "click", visibleInputModalHdlr)
            boundElements = true
        }
        val buttonToAddRecordUi = this.buttonToAddRecordUi
        if (buttonToAddRecordUi != null) {
            buttonToAddRecordUi.addEventListener(
                "click", addRecordHdlr)
            boundElements = true
        }

        if (boundElements) {
            popupMenuHdlr = clickHdlr
            this.orderToStartMoveHdlr = orderToStartMoveHdlr
            this.sortOrderHdlr = sortOrderHdlr
            this.visibleInputModalHdlr = visibleInputModalHdlr
            this.addRecordHdlr = addRecordHdlr
        }
    }


    /**
     * unbind this object from html elements
     */
    fun unbind() {
        if (popupMenuHdlr != null) {
            val openedMenu = getOpenedMenu()
            if (openedMenu != null) {
                closeMenu(openedMenu)
            }

            if (narrowDownPopper != null) {
                narrowDownPopper!!.destroy()
                narrowDownPopper = null
                narrowDownUi!!.removeEventListener("click", popupMenuHdlr)
            }
            if (orderPopper != null) {
                orderPopper!!.destroy()
                orderPopper = null
                orderUi!!.removeEventListener("click", popupMenuHdlr)
            }
            popupMenuHdlr = null
        }
        if (sortOrderHdlr != null) {
            orderSortItems!!.forEach {
                it.removeEventListener("click", sortOrderHdlr)
            }
            sortOrderHdlr = null
        }

        if (orderToStartMoveHdlr != null) {
            orderMenuUi!!.removeEventListener("mousedown",
                orderToStartMoveHdlr)
            orderToStartMoveHdlr = null
        }
        if (visibleInputModalHdlr != null) {
            visibleRecordInputModalUi!!.removeEventListener(
                "click", visibleInputModalHdlr)
            visibleInputModalHdlr = null
        }
        if (addRecordHdlr != null) {
            buttonToAddRecordUi!!.removeEventListener(
                "click", addRecordHdlr)
            addRecordHdlr = null
        }

    }

    /**
     * popup menu hdlr
     */
    fun handleToPopupMenu(event: Event) {
        val popperInstance = getPopperInstanceFromReference(
            event.currentTarget as HTMLElement)
        
        if (popperInstance != null) {
            
            val popperElem = popperInstance.state.elements.popper
            if (!popperElem.classList.contains("visible")) {
                if (closeMenuHdlr == null) {
                    popperElem.classList.add("visible")
                    lastQueryOption = getQueryOption(
                        popperInstance.state.elements.reference as HTMLElement)
                    window.setTimeout({
                        val closeHdlr: (Event)->Unit = {
                            handleToCloseMenuHdlr(it)
                        }
                        window.addEventListener("click", closeHdlr)
                        this.closeMenuHdlr = closeHdlr
                    }, 100)
                }
            } else {
                closeMenu(popperElem)
            }
            event.preventDefault()
        }
    }


    /**
     * get database query option
     */
    fun getQueryOption(element: HTMLElement): Array<Map<String, String>>? {

        var result = when(element) {
            narrowDownUi,
            narrowDownMenuUi -> narrowDownItems
            orderUi,
            orderMenuUi -> orderItems
            else -> null
        } 
        return result 
    }



    /**
     * event handler to close opened menu.
     */
    fun handleToCloseMenuHdlr(event: Event) {
        val openedMenu = getOpenedMenu()
        if (openedMenu != null) {
            if (!isOwnerItem(openedMenu, event.target as HTMLElement)) {
                closeMenu(openedMenu)
                val queryOption = getQueryOption(openedMenu)
                
                updateTableIfQueryOptionIsNotEquals(
                    lastQueryOption, queryOption)
                lastQueryOption = null
            }
        }
    }

    /**
     * close menu
     */
    fun closeMenu(openedMenu: HTMLElement) {
        window.removeEventListener("click", closeMenuHdlr) 
        closeMenuHdlr = null
        openedMenu.classList.remove("visible") 
    }

    /**
     * update table if query option is not equals
     */
    fun updateTableIfQueryOptionIsNotEquals(
        queryA: Array<Map<String, String>>?,
        queryB: Array<Map<String, String>>?) {
        if (!(queryA contentEquals queryB)) {
           syncTableWithSite() 
        }
    }

    /**
     * get popper instance from a popper html element
     */
    fun getPopperInstanceFromPopperElement(
        element: HTMLElement): popper.Instance? {
        val instances = arrayOf(
            narrowDownPopper,
            orderPopper) 
        var result: popper.Instance? = null
        for (idx in 0 until instances.size) {
            val popperInstance = instances[idx]
            if (popperInstance != null) {
                if (popperInstance.state.elements.popper == element) {
                    result = popperInstance 
                    break
                }

            }
        }
        return result
    }

    /**
     * get popper instance from reference htlm lement
     */
    fun getPopperInstanceFromReference(
        element: HTMLElement): popper.Instance? {
        val instances = arrayOf(
            narrowDownPopper,
            orderPopper) 
        var result: popper.Instance? = null
        for (idx in 0 until instances.size) {
            val popperInstance = instances[idx]
            if (popperInstance != null) {
                if (popperInstance.state.elements.reference == element) {
                    result = popperInstance 
                    break
                }

            }
        }
        return result
    }


    /**
     * opend menu item
     */
    fun getOpenedMenu(): HTMLElement? {
        val items = arrayOf(
            narrowDownMenuUi,
            orderMenuUi)
        var result: HTMLElement? = null
        for (idx in 0 until items.size) {
            val item = items[idx]
            if (item != null) {
                if (item.classList.contains("visible")) {
                    result = item
                    break
                }
            }
        } 
        return result 
    }


    

    /**
     * get true if owner contained the element.
     */
    fun isOwnerItem(
        owner: HTMLElement,
        element: HTMLElement): Boolean {
        var result = owner == element 

        if (!result) {
            val parent = element.parentElement
            result = parent != null && parent != window
            if (result) {
                result = isOwnerItem(owner, parent as HTMLElement)
            } 
        }
        return result
    }


    /**
     * handle mouse press
     */
    fun handleStartToMoveItem(event: Event) {

        
        val target = if (isSorterSelector(event.target as HTMLElement)) {
            null
        } else {
            findTableRow(event.target as HTMLElement)
        }
        
        if (target != null) {
            val mouseEvent = event as MouseEvent
            val ghostNode = target.cloneNode(true) as HTMLElement
            val originalBounds = target.getBoundingClientRect()
            
            setupGhostNode(
                ghostNode,
                originalBounds,
                doubleArrayOf(mouseEvent.pageX, mouseEvent.pageY))
            setupInsertionMarker()
            setInitialOrderIndexIntoData(ghostNode, 
                calcRowIndex(target))
        }
    }

    /**
     * you get true if element is the element to select sort kind.
     */
    fun isSorterSelector(element: HTMLElement): Boolean {
        val orderSortItems = orderSortItems
        var result = false
        if (orderSortItems != null) {
            result = orderSortItems.indexOf(element) >= 0 
            if (!result) {
                val parent = element.parentElement
                if (parent != null && parent != window) {
                    result = isSorterSelector(parent as HTMLElement)
                }
            }
        }
        
        return result
    }
      

    /**
     * find table row element with traversing up html dom tree
     */
    fun findTableRow(element: HTMLElement): HTMLTableRowElement? {
        var result: HTMLTableRowElement? = null
        if (element is HTMLTableRowElement) {
            result = element as HTMLTableRowElement
        } else {
            val parent = element.parentElement
            if (parent != null && parent != window) {
                result = findTableRow(parent as HTMLElement) 
            }
 
        }
        return result
    }

    
    /**
     *  row index on parent node
     */
    fun calcRowIndex(element: HTMLTableRowElement): Int {

        val orderMenuItems = this.orderMenuItems!! 
        var result = -1
        for (idx in 0 until orderMenuItems.size) {
            if (orderMenuItems[idx] == element) {
                result = idx
                break
            }
        }
        return result 
    }
    

    /**
     * setup mouse movable ghost node
     */
    fun setupGhostNode(
        element: HTMLElement,
        originalBounds: DOMRect,
        startLocation: DoubleArray) {

        setLastLocationIntoData(element, startLocation)
       
        val rect = originalBounds 
         
        document.body!!.appendChild(element)
        val docElem = document.documentElement!!
        element.style.position = "absolute"
        element.style.top = "${rect.top + docElem.scrollTop}px"
        element.style.left = "${rect.left + docElem.scrollLeft}px"

        movingGhostHdlr = {
            handleEventToMoveGhost(it)   
        }
        mouseupOnGhostHdlr = {
            handleEventToReleaseButton(it)
        }
        document.addEventListener("mousemove", movingGhostHdlr)
        document.addEventListener("mouseup", mouseupOnGhostHdlr)
        this.ghostElement = element
    }

    /**
     * setup insertion marker on menu 
     */
    fun setupInsertionMarker() {
        val idx = findInsertionIndexFromGhostNode()  
        setInsertionMarker(idx) 
        val ghostElement = this.ghostElement!!
        setInsertionIndexIntoData(ghostElement, idx)
    }

    /**
     * clear insertion marker
     */
    fun clearInsertionMarker() {
        val ghostElement = this.ghostElement!!
        val idx = getInsertionIndexFromData(ghostElement)
        clearInsertionMarker(idx)    
    }
    
    /**
     * handle the event to move ghost element
     */
    fun handleEventToMoveGhost(event: Event) {
      
        val ghostElement = this.ghostElement

        if (ghostElement != null) {
            val mouseEvent = event as MouseEvent
            val lastLocation = getLastLocationFromData(ghostElement)!! 
            val curLocation = doubleArrayOf(
                mouseEvent.pageX, mouseEvent.pageY)
            
            val diff = doubleArrayOf(
                curLocation[0] - lastLocation[0],
                curLocation[1] - lastLocation[1])
            ghostElement.style.top = 
                "${ghostElement.offsetTop + diff[1]}px"
            ghostElement.style.left =
                "${ghostElement.offsetLeft + diff[0]}px"
            setLastLocationIntoData(ghostElement, curLocation)
            updateMenuItemInsertionPoint()
        }
        event.preventDefault()
    }


    /**
     * update menu item insertion pointer
     */
    fun updateMenuItemInsertionPoint() {
        val idx = findInsertionIndexFromGhostNode()

        if (idx >= 0) {
            val ghostElement = this.ghostElement!!
            val lastIndex = getInsertionIndexFromData(ghostElement)
            if (lastIndex != idx) {
                setInsertionMarker(idx)
                clearInsertionMarker(lastIndex)
                setInsertionIndexIntoData(ghostElement, idx)
            }
        }
    }

    /**
     * set insertion marker
     */
    private fun setInsertionMarker(index: Int) {
        if (index >= 0) {
            val orderMenuItems = this.orderMenuItems!!
            if (index == orderMenuItems.size) {
                orderMenuItems[index - 1].classList.add(
                    "insertion-after")
            } else {
                orderMenuItems[index].classList.add(
                    "insertion-before")
            }
        }
    }

    /**
     * clear insertion marker
     */
    private fun clearInsertionMarker(index: Int) {
        if (index >= 0) {
            val orderMenuItems = this.orderMenuItems!!
            if (index == orderMenuItems.size) {
                orderMenuItems[index - 1].classList.remove(
                    "insertion-after")
            } else {
                orderMenuItems[index].classList.remove(
                    "insertion-before")
            }
        }
    }

    /**
     * find insetion index from ghost element
     */
    fun findInsertionIndexFromGhostNode(): Int {
        var result = -1
        val orderMenuItems = this.orderMenuItems
        val ghostElement = this.ghostElement
        if (orderMenuItems != null
            && ghostElement != null) {

            if (orderMenuItems.size > 0) {
                val currentLoc = getElementLocationY(ghostElement)
                val beginRect = orderMenuItems[0].getBoundingClientRect()

                val endRect = orderMenuItems[
                    orderMenuItems.size - 1].getBoundingClientRect()
                if (currentLoc < beginRect.top) {
                    result = 0
                } else if (currentLoc > endRect.top + endRect.height / 2.0) {
                    result = orderMenuItems.size
                } else {
                    val coordinates = DoubleArray(orderMenuItems.size) {
                        getElementLocationY(orderMenuItems[it])
                    }
                    result = 0
                    var lastValue = kotlin.math.abs(
                        currentLoc - coordinates[result])
                    for (idx in 1 until coordinates.size) {
                        val a = kotlin.math.abs(currentLoc - coordinates[idx])
                        if (lastValue > a) {
                            lastValue = a
                            result = idx 
                        }
                    }
                }
            }
        }
        return result 
    }

    /**
     * get html element representive y location
     */
    fun getElementLocationY(element: HTMLElement): Double {
        val rect = element.getBoundingClientRect()
        return rect.top + rect.height / 2.0
    }

    
    
    /**
     * handle mouse up event while you move ghost node
     */
    fun handleEventToReleaseButton(event: Event) {

        val ghostElement = this.ghostElement
        clearInsertionMarker() 
        if (ghostElement != null) {
            updateOrderMenu(
                getInitialOrderIndexFromData(ghostElement),
                getInsertionIndexFromData(ghostElement))
            ghostElement.remove()
            this.ghostElement = null
        }
        document.removeEventListener("mouseup", mouseupOnGhostHdlr) 
        document.removeEventListener("mousemove", movingGhostHdlr)
        this.mouseupOnGhostHdlr = null
        this.movingGhostHdlr = null
        event.preventDefault()
    }

    /**
     * update order menu
     */
    fun updateOrderMenu(
        oldIndex: Int,
        newIndex: Int) {

        if (oldIndex != newIndex) {
            val orderMenuItems = orderMenuItems!!
            val movementItem = if (newIndex == orderMenuItems.size) {
                if (oldIndex != orderMenuItems.size - 1) {
                    orderMenuItems[oldIndex] 
                } else {
                    null
                }
            } else {
                orderMenuItems[oldIndex] 
            }
            if (movementItem != null) {
                if (newIndex < orderMenuItems.size) {
                    val refNode = orderMenuItems[newIndex] 
                    refNode.parentNode!!.insertBefore(movementItem, refNode)
                } else {
                    movementItem.parentNode!!.appendChild(movementItem)
                }
            }
        } 
        
    }

    /**
     *  get last location from html element
     */
    fun getLastLocationFromData(element: HTMLElement): DoubleArray? {
        val locStr = element.dataset["lastLocation"] 
        
        var result: DoubleArray? = null
        if (locStr != null) {
            val items: Array<Any> = JSON.parse (locStr)  
            result = DoubleArray(2) {
                when (items[it]) {
                    is Number -> (items[it] as Number).toDouble()
                    else -> items[it].toString().toDouble()
                }
            }
        } 
        return result
    }

    /**
     *  set last location into html element
     */
    fun setLastLocationIntoData(
        element: HTMLElement,
        location: DoubleArray) {

        val strLoc = JSON.stringify(location)
        element.dataset["lastLocation"] = strLoc
    }


    /**
     * set insertion index
     */
    fun getInsertionIndexFromData(
        element: HTMLElement): Int {
        val strData = element.dataset["insertionIndex"]!!

        val result = strData.toInt()

        return result 
    }
    
    /**
     * set insertion index
     */
    fun setInsertionIndexIntoData(
        element: HTMLElement,
        insertionIndex: Int) {
        element.dataset["insertionIndex"] = insertionIndex.toString()
    }


    /**
     * set initial order index into dataset
     */
    fun setInitialOrderIndexIntoData(
        element: HTMLElement,
        index: Int) {
        element.dataset["initialOrderIndex"] = index.toString()
    }


    /**
     * get initial order index
     */
    fun getInitialOrderIndexFromData(
        element: HTMLElement): Int {
        val strIdx = element.dataset["initialOrderIndex"]!!
        val result = strIdx.toInt()
        return result
    }

    /**
     * handle an event to sort items
     */
    fun handleSortOrderEvent(event: Event) {
        val element = event.currentTarget as HTMLElement

        val iconContainer = element.querySelector("span") as HTMLElement
        val currentIcon = getSortIcon(iconContainer)

        iconContainer.classList.remove(currentIcon)
        iconContainer.classList.add(sortIconMap[currentIcon]!!)
    }

    /**
     * handle the event to show record input
     */
    fun handleToShowRecordInput(event: Event) {
        val recordInput = this.recordInputModalUi
        if (recordInput != null) {
            val modalObj = jQuery(recordInput)
            js("modalObj.modal('show')")
        } 
        event.preventDefault()
    }

    /**
     * handle to add record input
     */
    fun handleToAddRecord(event: Event) {
        val recordInput = this.recordInputModalUi
        if (recordInput != null) {

            val inputs = recordInput.querySelectorAll("input")
            
            var allValid = true
            for (idx in 0 until inputs.length) {
                val valid = (inputs[idx] as HTMLInputElement).reportValidity()
                if (!valid) {
                    allValid = valid
                }
            }
            if (allValid) {
                val modalObj = jQuery(recordInput)
                js("modalObj.modal('hide')")
                insertHrefRecordIntoSite().then({
                    if (it) {
                        clearHrefRecordInput()
                        syncTableWithSite()
                    }
                })
            }
        }
        event.preventDefault()
    }

    /**
     * clear href record in modal
     */
    fun clearHrefRecordInput() {
        recordInputHref = null
        recordInputSelector = null
    }

    /**
     * get sort icon from html element
     */
    fun getSortIcon(element: HTMLElement): String {
        var result: String? = null
        
        sortIconMap.forEach findres@ { 
            if (element.classList.contains(it.key)) {
                result = it.key 
                return@findres
            }
        }
        return result!!
    }

    /**
     * insert href into site
     */
    fun insertHrefRecordIntoSite(): Promise<Boolean> {
        val href = recordInputHref
        val selector = recordInputSelector
        return if (selector != null
            && href != null && href.isNotEmpty()) {
            insertHrefRecordIntoSite(href, selector)
        } else {
            Promise<Boolean> {
                resolve, _ ->
                resolve(false)
            }
        }
    }


    /**
     * insert href into site database
     */
    fun insertHrefRecordIntoSite(
        href: String,
        selector: String): Promise<Boolean>  {
       
        val body = URLSearchParams()
        body.append("href-access", "")
        body.append("insert", "")
        body.append("href", href)
        body.append("selector", selector)
        
        return window.fetch("/mgr-rest.php",
           RequestInit(
                method = "POST",
                body = body)).then({
            it.json()
        }).then({
            val anObj: dynamic = it
            anObj["href-id"] != null
        })
    }


    /**
     * synchronize table with site database
     */
    fun syncTableWithSite() {
        readHrefListFromSite().then({
           updateTable(it) 
        })
    }

    /**
     * read href list from site
     */
    fun readHrefListFromSite(): Promise<Array<Array<String>>> {
        val body = URLSearchParams()
        body.append("href-access", "")
        body.append("list-href-selector", "")
        val narrowDownItems = this.narrowDownItems
        val orderItems = this.orderItems

        if (narrowDownItems.size > 0) {
            body.append("narrow-down", JSON.stringify(
                toJsonObject(narrowDownItems)))
        }
        if (orderItems.size > 0) {
            body.append("order-by", JSON.stringify(
                toJsonObject(orderItems)))
        }
 
        return window.fetch("/mgr-rest.php",
           RequestInit(
                method = "POST",
                body = body)).then({
            it.json()
        }).then({
            val anObj: dynamic = it
            anObj["href-list-selector"] as Array<Array<String>>
        })
 
    }

 
    /**
     * update access list user interface
     */
    fun updateTable(accessList: Array<Array<String>>) {
        val tableUi = this.tableUi
        if (tableUi != null) {
            val tHead = tableUi.tHead as HTMLTableSectionElement

            val tBody = tableUi.tBodies[0] as HTMLTableSectionElement
            while (tBody.children.length > accessList.size) {
                tBody.deleteRow(-1)
            }
            val cellCount = tHead.rows[0]!!.childElementCount
            while (tBody.children.length < accessList.size) {
                val tableRow = tBody.insertRow(-1) as HTMLTableRowElement
                while(tableRow.childElementCount < cellCount) {
                    tableRow.insertCell(-1)
                }
            }
            for (idx in 0 until tBody.children.length) {
                val tableRow = tBody.children[idx]
                    as HTMLTableRowElement
                val rowData = accessList[idx] 
                
                for (idx1 in 0 until rowData.size) {
                    val elem = tableRow.children[idx1] as HTMLElement
                    elem.innerText = rowData[idx1]
                }
            }  
        }
    }
}

// vi: se ts=4 sw=4 et:
