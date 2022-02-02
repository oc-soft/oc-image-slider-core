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
     * attach menu bar
     */
    fun bind(
        rootMenu: HTMLElement) {

        this.rootMenu = rootMenu
        this.subMenuContainerHdlr  = { handleSubMenuContainerClick(it) }
        this.subMenuItemHdlr = { handleSubMenuItem(it) }
        this.subMenuHideHdlr = { handleEventOnDocument(it) }

        // attachAnchorContainer()
        // attachDisclosure()
        
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
            // detachDisclosure()
            // detachAnchorContainer()
            rootMenu = null
        } 
        subMenuItemHdlr = null
        subMenuContainerHdlr = null
        subMenuHideHdlr = null
    }

    /**
     * attach container into ancho
     */
    fun attachAnchorContainer() {
        rootMenu?.let {
            attachAnchorContainer(it)
        }
    }

    /**
     * attach anchor container
     */
    fun attachAnchorContainer(
        menuContainer: HTMLElement) {
        getMenuAnchorContainerTemplate()?.let {
            val anchorItems = menuContainer.querySelectorAll("a") 
            for (idx in 0 until anchorItems.length) {
                val elem = anchorItems[idx] as Element 
                val originalContents = createAnchorContainer(it, elem.innerHTML)
                elem.innerHTML = ""
                elem.append(originalContents) 
            }
        }
    }

    /**
     * detach container from anchor element
     */
    fun detachAnchorContainer() {
        rootMenu?.let {
            detachAnchorContainer(it)
        }
    }

    /**
     * detach anchor container
     */
    fun detachAnchorContainer(
        menuContainer: HTMLElement) {
        val anchorItems = menuContainer.querySelectorAll("a") 
        for (idx in 0 until anchorItems.length) {
            val elem = anchorItems[idx] as Element
            elem.firstElementChild?.let {
                val originalContent = it.innerHTML
                it.remove()
                elem.innerHTML = originalContent
            } 
        } 
    }


    /**
     * attach disclosure icons
     */
    fun attachDisclosure() {
        val disclosureTemplates = getMenuDisclosureTemplate()
        rootMenu?.let {
            attachDisclosure(it, disclosureTemplates,
                intArrayOf(1, 1), 0)
        }
    }



    /**
     * attach disclosure icons
     */
    fun attachDisclosure(
        menuElement: Element,
        disclosureTemplates: Array<Array<HTMLTemplateElement?>>,
        minorIndices: IntArray,
        level: Int) {
        val children = menuElement.children
        for (idx in 0 until children.length) {
            val child = children[idx] as Element
            child.querySelector(".sub-menu")?.let {
                attachDisclosure(it as Element, 
                    disclosureTemplates,
                    minorIndices, level + 1)
                createDisclosureElement(
                    disclosureTemplates,
                    level % 2,
                    minorIndices[level % 2])?.let {
                    attachDisclosureIcon(child, it)
                }
            }
        }
    }

    /**
     * detach dislosure icons
     */
    fun detachDisclosure() {
        rootMenu?.let {
            detachDisclosure(it)
        }
    }

    /**
     * detach disclosure icons
     */
    fun detachDisclosure(
        menuElement: Element) {
        val children = menuElement.children
        for (idx in 0 until children.length) {
            val child = children[idx] as Element
            child.querySelector(".sub-menu")?.let {
                detachDisclosure(it as Element)
                detachDisclosureIcon(child)
            }
        }
    }


    /**
     * attach disclosure
     */
    fun attachDisclosureIcon(
        menuElement: Element,
        disclosureIcon: SVGElement) {
        menuElement.querySelector("a")?.let {
            val elem = it as Element
            elem.appendChild(disclosureIcon)
        }
    }

    /**
     * detach disclosure
     */
    fun detachDisclosureIcon(
        menuElement: Element) {
        menuElement.querySelector("a")?.let {
            val elem = it as Element
            elem.querySelector("svg")?.let {
                elem.removeChild(it as Element)
            }
        }
    }
          

    /**
     * layout menu location
     */
    fun layout():Promise<Unit> {
        
        return Promise.all(rootMenu?.let {
            val promises = ArrayList<Promise<Unit>>()

            layoutMenus(
                it, 0, arrayOf(
                    "bottom",
                    "right"),
                promises) 
            promises.toTypedArray()
        }?: emptyArray<Promise<Unit>>()).then { Unit }
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
        side: Array<String>,
        layoutPromises: MutableList<Promise<Unit>>) {

        val children = menuElement.children
        for (idx in 0 until children.length) {
            val child = children[idx] as Element
            child.querySelector(".sub-menu")?.let {
                val computed = floating_ui.dom.computePosition(
                    child, it, object {
                        @JsName("placement")
                        val placement = side[level % side.size]
                    }) 
                layoutPromises.add(setupComputedItem(
                    it, 
                    level + 1, side, computed,
                    layoutPromises))
                
            }
        }
    }

    /**
     * setup computed item
     */
    fun setupComputedItem(
        element: Element,
        level: Int,
        side: Array<String>,
        computedItem: Promise<dynamic>,
        layoutPromises: MutableList<Promise<Unit>>): Promise<Unit> {
        return computedItem.then<Unit> {
            layoutMenus(element, level, side, layoutPromises)    
            Unit 
        }
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
            if (event.target is HTMLAnchorElement) {
                event.preventDefault()
            }
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
    } 

    /**
     * create disclosure element
     */
    fun createDisclosureElement(
        templates: Array<Array<HTMLTemplateElement?>>,
        majorIdx: Int,
        minorIdx: Int): SVGElement?  {
        return templates[majorIdx][minorIdx]?.let {
            it.content.firstElementChild?.let {
                it.cloneNode(true) as SVGElement
            }
        }
    }

    /**
     * get menu disclosure template
     */
    fun getMenuDisclosureTemplate(): Array<Array<HTMLTemplateElement?>> {
        val queries = Array<String>(4) { "template.menu-more-less-${it + 1}" }

        return Array<Array<HTMLTemplateElement?>>(2) {
            val idx0 = it 
            Array<HTMLTemplateElement?>(2) {
                document.querySelector(queries[idx0 * 2 + it])?.let {
                    it as HTMLTemplateElement
                }
            } 
        } 
    }

    /**
     * create inner html
     */
    fun createAnchorContainer(
        template: HTMLTemplateElement,
        innerHTML: String): Element? {

        return template.content.firstElementChild?.let {
            val container = it.cloneNode(true) as Element
            container.innerHTML = innerHTML
            container
        }
    }
    

    /**
     * get anchor container
     */
    fun getMenuAnchorContainerTemplate(): HTMLTemplateElement? {
        return document.querySelector("template.menu-anchor-container")?.let {
            it as HTMLTemplateElement
        }
    }

}

// vi: se ts=4 sw=4 et:
