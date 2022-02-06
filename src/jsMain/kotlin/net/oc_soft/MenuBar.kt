package net.oc_soft

import kotlin.js.Promise

import kotlinx.browser.window
import kotlinx.browser.document

import kotlin.collections.MutableList
import kotlin.collections.ArrayList
import kotlin.collections.MutableSet
import kotlin.collections.HashSet

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLTemplateElement
import org.w3c.dom.get
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

import org.w3c.dom.svg.SVGElement

import org.w3c.dom.ResizeObserver
import org.w3c.dom.ResizeObserverEntry

/**
 * menu bar
 */
class MenuBar {


    /**
     * root menu
     */
    var rootMenu: HTMLElement? = null

    /**
     * event handler for sub-menu item 
     */
    var subMenuItemHdlr: ((Event)->Unit)? = null

    /**
     * event handler for menu container 
     */
    var subMenuContainerHdlr: ((Event)->Unit)? = null

    /**
     * event handler to hide visible sub menu
     */
    var subMenuHideHdlr: ((Event)->Unit)? = null

    /**
     * visibled menus
     */
    val visibledMenus: MutableSet<HTMLElement> = HashSet<HTMLElement>()


    /**
     * resize observer
     */
    var resizeObserver: ResizeObserver? = null

    /**
     * root menu visibility
     */
    val rootMenuVisible: Boolean
        get() {
            return rootMenu?.let {
                window.getComputedStyle(it).visibility != "hidden"
            }?: false
        }
    /**
     * attach menu bar
     */
    fun bind(
        rootMenu: HTMLElement) {

        this.rootMenu = rootMenu
        this.subMenuContainerHdlr  = { handleSubMenuContainerClick(it) }
        this.subMenuItemHdlr = { handleSubMenuItem(it) }
        this.subMenuHideHdlr = { handleEventOnDocument(it) }

        
        layout().then {
            resizeObserver = ResizeObserver { 
                entries, observer ->
                handleResized(entries, observer)
            }
            resizeObserver?.let {
                it.observe(document.body)
            } 
            bindSubMenuContainerListener(rootMenu)
        }
    }


    /**
     * detach menu form html elements
     */
    fun unbind() {
        resizeObserver?.let {
            it.disconnect()
            resizeObserver = null
        }
        hideVisibledSubMenusAll() 
        rootMenu?.let {
            unbindSubMenuContainerListener(it)
            rootMenu = null
        } 
        subMenuItemHdlr = null
        subMenuContainerHdlr = null
        subMenuHideHdlr = null
    }

    /**
     * layout menu location
     */
    fun layout():Promise<Unit> {
        return rootMenu?.let {
            val children = it.children 
            Promise.all(Array<Promise<Unit>>(children.length) {
                val element = children[it] as Element
                layoutMenus(element, 0,
                    {
                        if (it == 0) { 
                            "bottom-start"
                        } else {
                            "right-start"
                        }
                    }) 
            }).then { Unit }
        }?: Promise.resolve(Unit)
    }


    /**
     * bind eventlistener
     */
    fun bindSubMenuContainerListener(menuElement: Element) {
        val children = menuElement.children
        for (idx in 0 until children.length) {
            children[idx]?.let {
                
                val child = it
                getSubMenu(child)?.let {
                    child.addEventListener("click", 
                        this.subMenuContainerHdlr!!)
                    bindSubMenuContainerListener(it)
                }?: child.addEventListener("click", subMenuItemHdlr)
                    
            }
        }
    }

    /**
     * you get true if the element is sub menu container
     */
    fun getSubMenu(element: Element): Element? {
        val children = element.children
        var result: Element? = null 
        for (idx in 0 until children.length) {
            result = children[idx]?.let {
                val child = it
                if (child.classList.contains("sub-menu")) {
                    child 
                } else {
                    null
                } 
            } 
            if (result != null) {
                break
            }
        }
        return result
    }    


    /**
     * unbind event listener from menu element
     */
    fun unbindSubMenuContainerListener(menuElement: Element) {
        val children = menuElement.children
        for (idx in 0 until children.length) {
            children[idx]?.let {
                val child = it 
                child.querySelector(".sub-menu")?.let {
                    child.removeEventListener("click", 
                        this.subMenuContainerHdlr!!)
                    unbindSubMenuContainerListener(it)
                }
            }
        }
     }


    /**
     * layout menus
     */
    fun layoutMenus(
        menuElement: Element,
        level: Int,
        placementProvider: (Int)->String): Promise<Unit> {

        val children = menuElement.children
        val subMenuItems = ArrayList<Element>()
        val computedPromises = ArrayList<Promise<Unit>>()
        for (idx in 0 until children.length) {
            val child = children[idx] as HTMLElement
            if (child.classList.contains("sub-menu")) {
                val computed = floating_ui.dom.computePosition(
                    menuElement, child, object {
                        @JsName("placement")
                        val placement = placementProvider(level)
                    }).then {
                        compRes: dynamic ->
                        child.style.left = "${compRes.x}px"
                        child.style.top = "${compRes.y}px"
                        Unit
                    }
                subMenuItems.addAll(getSubMenuItems(child))
                computedPromises.add(computed)
            }
        }
        

        return Promise<Unit>({
            resolve, reject ->
            Promise.all(computedPromises.toTypedArray()).then {
                val subMenuPromises = ArrayList<Promise<Unit>>() 
                subMenuItems.forEach {
                    subMenuPromises.add(
                        layoutMenus(it, level + 1, placementProvider))
                }
                Promise.all(subMenuPromises.toTypedArray()).then {
                    resolve(Unit)
                } 
            }
        })
    }
    
    /**
     * get sub menu items
     */
    fun getSubMenuItems(
        subMenuElement: Element): Array<Element> {
        val children = subMenuElement.children
        val subMenuItems = ArrayList<Element>()

        for (idx in 0 until children.length) {
            val child = children[idx] as Element
            if (child.classList.contains("menu-item")) {
                subMenuItems.add(child)
            }
        }
        
        return subMenuItems.toTypedArray()
    }


    /**
     * handle menu item
     */
    fun handleSubMenuContainerClick(event: Event): Unit {

        val elem = event.currentTarget as HTMLElement 
        
        getSubMenu(elem)?.let {
            if (!isVisibleSubMenuItem(it)) {
                val subMenu = it as HTMLElement

                val lastVisibledMenuContainer = 
                    findSiblingVisibleSubmenuContainer(elem)
                setVisibleSubMenuItem(subMenu, true) 
                lastVisibledMenuContainer?.let {
                    hideAllSubMenuInElement(it)
                }

            } else {
                hideAllSubMenuInElement(elem)
            }
            event.stopPropagation()
            //if (event.target is HTMLAnchorElement) {
                event.preventDefault()
            //}
        }
    }



    /**
     * handle sub-menu item
     */
    fun handleSubMenuItem(event: Event) {
        val element = event.currentTarget as HTMLElement
        event.stopPropagation()
        hideVisibledSubMenusAll() 
    }

    /**
     * get sub-menu item  visibility
     */
    fun isVisibleSubMenuItem(
        subMenuItem: Element): Boolean {
        val subMenuStyle = window.getComputedStyle(subMenuItem)
        return  "hidden" != subMenuStyle.visibility
     }
        

    /**
     * set visibility a submenu container
     */
    fun setVisibleSubMenuItem(
        subMenuItem: HTMLElement,
        visible: Boolean) {
        
        
        if (visible) {
            val doAddHideHdlr = visibledMenus.isEmpty()
            subMenuItem.style.visibility = "visible"
            visibledMenus.add(subMenuItem)
            if (doAddHideHdlr) {
                document.addEventListener("click", subMenuHideHdlr)
            }
        } else {
            visibledMenus.remove(subMenuItem)
            subMenuItem.style.visibility = "hidden"
            if (visibledMenus.isEmpty()) {
                document.removeEventListener("click", subMenuHideHdlr)
            }
        }
     }
    
    /**
     * hide all sub-menu
     */
    fun hideAllSubMenuInElement(element: HTMLElement) {
        val subMenus = element.querySelectorAll(".sub-menu")
        for (idx in 0 until subMenus.length) {
            val subMenu = subMenus[idx] as HTMLElement
            if (isVisibleSubMenuItem(subMenu)) {
                setVisibleSubMenuItem(subMenu, false)
            } 
        }
    }

    /**
     *  hide visibled sub-menus all
     */
    fun hideVisibledSubMenusAll() {
        val visibledMenus = HashSet<HTMLElement>(this.visibledMenus)
        visibledMenus.forEach {
            setVisibleSubMenuItem(it, false)
        }
    }

    /**
     * find a sibling element which have visble submenu
     */
    fun findSiblingVisibleSubmenuContainer(
        element: HTMLElement): HTMLElement? {

        var result: HTMLElement? = null
        element.parentElement?.let {
            val children = it.children
            for (idx in 0 until children.length) {
                result = children[idx]?.let {
                    val child = it as HTMLElement
                    if (element != child) {
                        getSubMenu(child)?.let {
                            if (it in visibledMenus) {
                                child    
                            } else {
                                null
                            }
                        } 
                    } else {
                        null
                    }
                }
                if (result != null) {
                    break
                }
            }
        }
        return result
    }

    /**
     * handle event on document 
     */
    fun handleEventOnDocument(event: Event) {
        if (visibledMenus.isNotEmpty()) {
            if (event is MouseEvent) {
                if (!isInVisibledMenus(event.screenX, event.screenY)) {
                     hideVisibledSubMenusAll()           
                }
            }
        }     
    }

    /**
     * the coordinate is in visible menu
     */
    fun isInVisibledMenus(
        x: Int,
        y: Int): Boolean {

        val hitElem = visibledMenus.find { isInElement(it, x, y) } 
        return hitElem != null
    } 

    /**
     * the coordinate is in a element
     */
    fun isInElement(
        element: Element,
        x: Int,
        y: Int): Boolean {
        val rect = element.getBoundingClientRect()

        var result = rect.left <= x && x <= rect.right
        if (result) {
            result = rect.top <= y && y <= rect.bottom
        }
        return result
    }


    /**
     * handle resized event
     */
    fun handleResized(
        entries: Array<ResizeObserverEntry>,
        observer: ResizeObserver) {
        layout()
        window.setTimeout({
            hideSubMenusAllIfRootNotVisible()
        }) 
    } 

    /**
     * hide sub-menus all if root menue is not visible
     */
    fun hideSubMenusAllIfRootNotVisible() {
        if (!rootMenuVisible) {
            hideVisibledSubMenusAll()
        }
    }

}

// vi: se ts=4 sw=4 et:
