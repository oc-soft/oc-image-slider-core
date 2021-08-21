package net.oc_soft

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLTemplateElement
import org.w3c.dom.DocumentFragment
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.dom.events.Event

/**
 * job selector
 */
class JobSelector {


    /**
     * service selector element
     */
    val serviceSelectorElementUi: HTMLSelectElement?
        get() {
            return document.querySelector("[name='jsk-service-kind']")
                as HTMLSelectElement?
        } 

    /**
     * selected service option element
     */
    val selectedServiceOptionUi: HTMLOptionElement?
        get() {
            return serviceSelectorElementUi?.let {
                val idx = it.selectedIndex
                if (idx >= 0) {
                    it.options[idx] as HTMLOptionElement
                } else {
                    null
                }
            }
        }

    /**
     * job kind container element
     */
    val jobKindContainerElementUi: HTMLElement?
        get() {
            return document.querySelector(".job-kind-container")
                as HTMLElement?
        } 

    /**
     * selected job kind
     */
    val selectedJobKind: String?
        get() {
            return selectedServiceOptionUi?.let {
                it.dataset["jobKind"]
            }
        }

    /**
     * last job kind
     */
    var lastJobKind: String?
        get() {
            return serviceSelectorElementUi?.let {
                return it.dataset["lastService"]
            } ?: null

        }
        set(value) {
            serviceSelectorElementUi?.let {
                it.dataset["lastService"] = value ?: ""
            }
        }


    /**
     * sevice input event handler
     */
    var serviceInputEventHdlr: ((Event)->Unit)? = null

    /**
     * bind this object into html elements
     */
    fun bind() {
        val serviceInputEventHdlr: (Event)->Unit = 
            { handleServiceInputEvent(it) }

        serviceSelectorElementUi?.let {
            it.addEventListener("input", serviceInputEventHdlr)
            this.serviceInputEventHdlr = serviceInputEventHdlr
        }
        window.setTimeout({
            syncJobKindWithServiceSelector()
        })
    }

    /**
     * detatch this object from html elemets
     */
    fun unbind() {
        serviceInputEventHdlr?.let {
            val hdlr = it
            serviceSelectorElementUi?.let {
                it.removeEventListener("input", hdlr)
            } 
        }
        serviceInputEventHdlr = null
        lastJobKind = null
    }


    /**
     * handle service input event
     */
    fun handleServiceInputEvent(event: Event) {
        syncJobKindWithServiceSelector()
    }

    /**
     * synchronize job kind with service selector
     */
    fun syncJobKindWithServiceSelector() {
        selectedJobKind?.let {
            val newJobKind = it
            if (newJobKind != lastJobKind) {
                jobKindContainerElementUi?.let {
                    it.firstElementChild?.let {
                        it.replaceWith(createJobSelector(newJobKind))
                    }?: it.append(createJobSelector(newJobKind)) 
                    
                    lastJobKind = newJobKind
                }

            }
        }
    }


    /**
     * create job selector
     */
    fun createJobSelector(jobKind: String): HTMLElement {
        val template = document.getElementById(jobKind) as HTMLTemplateElement

        val docFrag = template.content.cloneNode(true) as DocumentFragment
        return docFrag.firstElementChild as HTMLElement
    }
    
}

// vi: se ts=4 sw=4 et:
