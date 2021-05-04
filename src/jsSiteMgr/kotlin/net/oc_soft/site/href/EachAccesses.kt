
package net.oc_soft.site.href

import kotlin.js.Promise

import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.toList

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.HTMLTableSectionElement
import org.w3c.dom.HTMLTableRowElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.RequestInit


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
    }

    /**
     * detach this object from html elements
     */
    fun unbind() {
        var handler = this.updateClickHdlr
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
