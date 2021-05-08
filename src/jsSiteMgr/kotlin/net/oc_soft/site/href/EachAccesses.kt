
package net.oc_soft.site.href

import kotlin.js.Promise

import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.toList

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.HTMLTableSectionElement
import org.w3c.dom.HTMLTableRowElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.Document
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.RequestInit
import org.w3c.dom.url.URL
import org.w3c.dom.DocumentReadyState
import org.w3c.dom.COMPLETE


/**
 * manage tracking each access item list
 */
class EachAccesses {


    /**
     * get table element for  each accesses 
     */
    val tableUi: HTMLTableElement?
        get() {
           return document.querySelector("table.each-accesses-list")
            as HTMLTableElement?
        }

    /**
     * begin date input element
     */
    val accessBeginUi: HTMLInputElement?
        get() {
            return document.querySelector(
                "input[type='date'].each-accesses.access-begin")
                as HTMLInputElement?
        }

    /**
     * end date input element
     */
    val accessEndUi: HTMLInputElement?
        get() {
            return document.querySelector(
                "input[type='date'].each-accesses.access-end")
                as HTMLInputElement?
        }


    /**
     * src href view ui 
     */
    val srcViewUi: HTMLIFrameElement?
        get() {
            return document.querySelector(".access-panel.source iframe")
                as HTMLIFrameElement?
        }

    /**
     * deteminated href view ui 
     */
    val destViewUi: HTMLIFrameElement?
        get() {
            return document.querySelector(".access-panel.destination iframe")
                as HTMLIFrameElement?
        }


    /**
     * src view document
     */
    val srcViewDocument: Document?
        get() {
            return srcViewUi?.contentDocument 
        }

    /**
     * determinated view document
     */
    val destViewDocument: Document?
        get() {
            return destViewUi?.contentDocument 
        }

    /**
     * source view' Window 
     */
    val srcViewWindow: Window?
        get() {
            return srcViewUi?.contentWindow 
        }

    /**
     * determinated view's window 
     */
    val destViewWindow: Window?
        get() {
            return destViewUi?.contentWindow 
        }


    /**
     * src view marker
     */
    var srcViewMarker: HTMLElement?
        get() {
            return srcViewDocument?.querySelector(
                "div.marker").unsafeCast<HTMLElement?>()
        }
        set(value) {
            val currentMarker = srcViewMarker
            if (value == null) {
                if (currentMarker != null) {
                    currentMarker.remove()
                }
            } else {
                if (value != currentMarker) {
                    if (currentMarker != null) {
                        currentMarker.remove()
                    }
                    val srcDoc = srcViewDocument
                    if (srcDoc != null) {
                        srcDoc.body!!.appendChild(value)
                    } 
                }
            }

        }
    

    /**
     * determinated view marker
     */
    var destViewMarker: HTMLElement?
        get() {
            return destViewDocument?.querySelector(
                "div.marker").unsafeCast<HTMLElement?>()
        }
        set(value) {
            val currentMarker = destViewMarker
            if (value == null) {
                if (currentMarker != null) {
                    currentMarker.remove()
                }
            } else {
                if (value != currentMarker) {
                    if (currentMarker != null) {
                        currentMarker.remove()
                    }
                    val destDoc = destViewDocument
                    if (destDoc != null) {
                        destDoc.body!!.appendChild(value)
                    } 
                }
            }
        }
 

    /**
     * update command user interface
     */
    val updateCommandUi: HTMLElement?
        get() {
            return document.querySelector(
                "button.update-each-accesses") as HTMLElement?
        }

    /**
     * begin access datetime
     */
    val beginAccessDateTime: String?
        get() {
            var result: String? = null

            val accessBeginUi = this.accessBeginUi
            if (accessBeginUi != null) {
                val dateValue = accessBeginUi.value
                if (dateValue.isNotEmpty()) {  
                    result = "${dateValue} 00:00:00"
                }
            }
            return result
        }

    /**
     * end access datetime
     */
    val endAccessDateTime: String?
        get() {
            var result: String? = null

            val accessEndUi = this.accessEndUi
            if (accessEndUi != null) {
                val dateValue = accessEndUi.value
                if (dateValue.isNotEmpty()) {  
                    result = "${dateValue} 23:59:59"
                }
            }
            return result
        }


    /**
     * event handler to click event
     */
    var updateClickHdlr: ((Event)->Unit)? = null

    /**
     * event handler to  click event on table
     */
    var tableRowClickHdlr: ((Event)->Unit)? = null

    /**
     * bind this object into html elements
     */
    fun bind() {
        val updateCommandUi = this.updateCommandUi
        if (updateCommandUi != null) {
            val handler: ((Event)->Unit) = {
               handleEventToClick(it) 
            }
            updateCommandUi.addEventListener("click", handler)
            this.updateClickHdlr = handler
        } 
        val table = tableUi
        if (table != null) {
            val handler: (Event)->Unit = {
                handleEventForSelectTableRow(it)
            }
            table.addEventListener("click", handler)
            this.tableRowClickHdlr = handler
        }
    }

    /**
     * detach this object from html elements
     */
    fun unbind() {
        var handler = this.tableRowClickHdlr
        if (handler != null) {
            tableUi!!.removeEventListener("click", handler)
            this.tableRowClickHdlr = null
        }
        handler = this.updateClickHdlr
        
        if (handler != null) {
            this.updateCommandUi!!.removeEventListener("click", handler)
            this.updateClickHdlr = null
        } 
    } 


    /**
     * handle update access table event
     */
    fun handleEventToClick(evt: Event) {
        syncAccessListWithSite()  
        evt.preventDefault()
    }

    /**
     * handle the event which is rised by click table row
     */
    fun handleEventForSelectTableRow(event: Event) {
        markHref(event.target as HTMLElement)
    }


    /**
     * mark href in view 
     */
    fun markHref(element: HTMLElement) {
        val hrefs = getSrcAndDestHref(element)
        if (hrefs != null) {
            markHref(hrefs.first, hrefs.second)
        }
    }
    /**
     * mark herf in view
     */
    fun markHref(srcHref: String, destHref: String) {

        markSrcViewDocument(srcHref)
        markDestViewDocument(destHref)
    }

    /**
     * mark on srcview
     */
    fun markSrcViewDocument(href: String) {
        val doc = srcViewDocument 
        if (doc != null) {
            val url = URL("${document.location!!.origin}${href}")
            val win = srcViewWindow!!
            if (doc.location!!.href != url.href) {
                val view = srcViewUi!!
                waitForDocumentLoadedOnIFrame(
                    view, url).then({
                    val win = srcViewWindow!!
                    var hdlr = Array<((Event)->Unit)?>(1) { null }
                    hdlr[0] = {
                        markSrcViewDocument(href)
                        win.removeEventListener("load", hdlr[0]) 
                    }
                    win.addEventListener("load", hdlr[0])
                })
                
            } else {
                markSrcElement(href)
            }
        } else {
            val view = srcViewUi
            if (view != null) {
                view.src = href
                val window = srcViewWindow
                if (window != null) {
                    val hdlr = Array<((Event)->Unit)?>(1) { null }
                    hdlr[0] = {
                        markSrcViewDocument(href)
                        window.removeEventListener("load", hdlr[0])
                    }
                    window.addEventListener("load", hdlr[0])
                }
            }
        }
    }



    /**
     * mark on destview
     */
    fun markDestViewDocument(href: String) {
        val doc = destViewDocument 
        if (doc != null) {
            val url = URL("${document.location!!.origin}${href}")
            if (doc.location!!.href!! != url.href) {
                val view = destViewUi!!
                waitForDocumentLoadedOnIFrame(
                    view, url).then({
                    val win = destViewWindow!!
                    var hdlr = Array<((Event)->Unit)?>(1) { null }
                    hdlr[0] = {
                        markDestViewDocument(href)
                        win.removeEventListener("load", hdlr[0]) 
                    }
                    win.addEventListener("load", hdlr[0])
                })
       
            } else {
                markDestElement(href)
            }
        } else {
            val view = destViewUi
            if (view != null) {
                view.src = href
                val window = destViewWindow
                if (window != null) {
                    val hdlr = Array<((Event)->Unit)?>(1) { null }
                    hdlr[0] = {
                        markDestViewDocument(href)
                        window.removeEventListener("load", hdlr[0])
                    }
                    window.addEventListener("load", hdlr[0])
                }
            }
        } 
    }


    /**
     * waite document loaded on iframe
     */
    fun waitForDocumentLoadedOnIFrame(
        element: HTMLIFrameElement,
        url: URL): Promise<Unit> {
        
        element.contentWindow!!.location.assign(url.href) 
        return Promise<Unit> {
            resolve, reject ->
            val intervalId = Array<Int>(0) { 0 }
            var observingCount = 0
            intervalId[0] = window.setInterval({ 
                if (element.contentWindow!!.location.href == url.href) {
                    window.clearInterval(intervalId[0])
                    resolve(Unit) 
                } else {
                    observingCount++
                    if (observingCount > 1000) {
                        window.clearInterval(intervalId[0])
                        reject(Throwable("time out"))
                    }
                }
            }, 100) 
        }
    }


    /**
     * get src and dest href
     */
    fun getSrcAndDestHref(element: HTMLElement): Pair<String, String>? {
        val tableRow = findTableRow(element)
        return if (tableRow != null) {
            val cells = tableRow.querySelectorAll("td")
            if (cells.length > 1) {
                Pair((cells[0] as HTMLElement).innerHTML, 
                    (cells[1] as HTMLElement).innerHTML)
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * find table row
     */
    fun findTableRow(element: HTMLElement): HTMLTableRowElement? {
        return if (element is HTMLTableRowElement) {
            element
        } else {
            if (element.parentElement != null) {
                findTableRow(element.parentElement as HTMLElement) 
            } else { 
                null
            }
        }
    }

    /**
     * extract selector from url
     */
    fun extractSelector(url: URL): String {
        val hash = url.hash   
        return if (hash.isNotEmpty()) {
            hash
        } else {
            "body" 
        }
    }

    /**
     * mark src element
     */
    fun markSrcElement(href: String) {
        var marker = srcViewMarker
        if (marker == null) {
            marker = createSrcMarker()
            srcViewMarker = marker
        }
        
        val doc = srcViewDocument
        
        
        val marked = if (doc != null) {
            markElement(
                doc, 
                extractSelector(
                    URL("${document.location!!.origin}${href}")), marker)
        } else {
            false
        }
        if (!marked) {
            marker.style.display = "none"
        } else {
            marker.style.display = "block"
        }
    }

    /**
     * mark src element
     */
    fun markDestElement(href: String) {
        var marker = destViewMarker
        if (marker == null) {
            marker = createDestMarker()
            destViewMarker = marker
        }
        
        val doc = destViewDocument
        
        val marked = if (doc != null) {
            markElement(doc,
                extractSelector(
                    URL("${document.location!!.origin}${href}")), marker)
        } else {
            false
        }
        if (!marked) {
            marker.style.display = "none"
        } else {
            marker.style.display = "block"
        }
    }


    /**
     * mark an element on document
     */
    fun markElement(document: Document,
        selector: String,
        marker: HTMLElement): Boolean {
        
        val element = document.querySelector(
            selector).unsafeCast<Element?>()
        return if (element != null) {
            val bounds = element.getBoundingClientRect()

            val left = document.defaultView!!.pageXOffset + bounds.left
            val top = document.defaultView!!.pageYOffset + bounds.top
            marker.style.left = "${left}px"
            marker.style.top = "${top}px"
            marker.style.width = "${bounds.width}px"
            marker.style.height = "${bounds.height}px"
            true
        } else {
            false
        }
    }

    /**
     * create src marker
     */
    fun createSrcMarker() : HTMLElement {
        val result = createMarker(srcViewDocument!!)
        result.classList.add("src")
        return result
    }

    /**
     * create determinated marker
     */
    fun createDestMarker() : HTMLElement {
        val result = createMarker(destViewDocument!!)
        result.classList.add("dest")
        return result
    }


    /**
     * create marker
     */
    fun createMarker(document: Document): HTMLElement {
        val result = document.createElement("div").unsafeCast<HTMLElement>()

        result.classList.add("marker")
        
        return result
    }

    /**
     * synchronize access list with site
     */

    fun syncAccessListWithSite() {

        val accessBegin = this.beginAccessDateTime
        val accessEnd = this.endAccessDateTime

        if (accessBegin != null && accessEnd != null) {
            readAccessList(accessBegin, accessEnd).then({
                updateTable(it)
            })
        } else {
            window.setTimeout({
                updateTable(Array<Array<String>>(0) {
                    Array<String>(0) { "" }
                })
            }) 
        }
    }

    /**
     * read access list from site
     */
    fun readAccessList(
        accessBegin: String,
        accessEnd: String): Promise<Array<Array<String>>> {
        
        val body = URLSearchParams()
        body.append("href-access", "")
        body.append("list-access", "")
        body.append("begin", accessBegin)
        body.append("end", accessEnd)
        return window.fetch("/mgr-rest.php",
            RequestInit(
                method = "POST",
                body = body,
            )).then({
            it.json()
        }).then({
            val anObj: dynamic = it
            anObj["access-list"] as Array<Array<String>>
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
