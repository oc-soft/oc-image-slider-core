package net.oc_soft.site

import kotlinx.browser.window
import kotlinx.browser.document
import kotlin.js.Promise

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.HTMLTableSectionElement
import org.w3c.dom.HTMLTableRowElement
import org.w3c.dom.get
import org.w3c.dom.events.Event
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.RequestInit

/**
 * data base 
 */
class Db {

    /**
     * class instance
     */
    companion object {

        /**
         * convert string array from dynamic object
         */
        fun dynamicToStringArray(obj: dynamic): Array<String> {
            return Array<String>(obj.length) {
                obj[it] as String
            }
            
        }
    }
    
    /**
     * ui to create tables
     */
    val createTablesUi: HTMLElement?
        get() {
            return document.querySelector(".create-table") as HTMLElement?
        }
    
    /**
     * container to hold table
     */
    val tableUi: HTMLTableElement?
        get() {
            return document.querySelector(".tables") as HTMLTableElement? 
        }


    /**
     * event handler to create tables
     */
    var createTablesHdlr: ((Event)->Unit)? = null

    /**
     * bind this object into html element
     */
    fun bind() {
        
        val createTablesUi = this.createTablesUi
        if (createTablesUi != null) {
            val handler: (Event)->Unit = {
                onHandleToCreateTables(it)
            }
            createTablesUi.addEventListener("click", handler)
            this.createTablesHdlr = handler
        }         

    }

    /**
     * detach this object from html elements
     */
    fun unbind() {
        if (createTablesHdlr != null) {
            val createTablesUi = this.createTablesUi!!
            createTablesUi.removeEventListener("click", createTablesHdlr)
                
            createTablesHdlr = null
        }
    }

    /**
     * handle to create tables
     */
    fun onHandleToCreateTables(evt: Event) {
        doCreateTables().then({
            if (it) {
                syncTablesWithSite()
            }
        })
        evt.preventDefault()
    }


    /**
     * create tables
     */
    fun doCreateTables(): Promise<Boolean> {
        val params = URLSearchParams()
        params.append("db", "")
        params.append("create", "")

        return window.fetch("/mgr-rest.php", 
           RequestInit(
                method = "POST",
                body = params)).then({
            it.json()
        }).then({
            val anObj: dynamic = it
            anObj["state"] as Boolean
        })
    }

    /**
     * read table names from site
     */
    fun readTableNamesFromSite(): Promise<Array<String>> {
        val params = URLSearchParams()
        params.append("db", "")
        params.append("list-tables", "")

        return window.fetch("/mgr-rest.php", 
           RequestInit(
                method = "POST",
                body = params)).then({
            it.json()
        }).then({
            val anObj: dynamic = it
            dynamicToStringArray(anObj["state"])
        })
    }

    

    /**
     * synchronize ui with site data
     */
    fun syncTablesWithSite() {

        readTableNamesFromSite().then({
           updateTable(it) 
        })
    }

    /**
     * update table ui with table names
     */
    fun updateTable(tables: Array<String>) {
        val tableUi = this.tableUi
        if (tableUi != null) {
            val tBody = tableUi.tBodies[0] as HTMLTableSectionElement
            while (tBody.children.length > tables.size) {
                tBody.deleteRow(-1)
            }
            while (tBody.children.length < tables.size) {
                val tableRow = tBody.insertRow(-1) as HTMLTableRowElement
                tableRow.insertCell(-1)
            }
            for (idx in 0 until tBody.children.length) {
                val tableRow = tBody.children[idx]
                    as HTMLTableRowElement

                val elem = tableRow.children[0] as HTMLElement
                elem.innerText = tables[idx]
            }  
        }

    }
}


// vi: se ts=4 sw=4 et:
