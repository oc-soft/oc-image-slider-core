@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")
package popper

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

external interface PreventOverflowOptions {
    var mainAxis: Boolean
    var altAxis: Boolean
    var boundary: dynamic /* HTMLElement | Array<HTMLElement> | Any */
        get() = definedExternally
        set(value) = definedExternally
    var rootBoundary: dynamic /* Any | "document" */
        get() = definedExternally
        set(value) = definedExternally
    var altBoundary: Boolean
    var tether: Boolean
    var tetherOffset: dynamic /* (arg0: `T$12`) -> Number | Number */
        get() = definedExternally
        set(value) = definedExternally
    var padding: dynamic /* Number | SideObjectPartial */
        get() = definedExternally
        set(value) = definedExternally
}

typealias PreventOverflowModifier = Modifier<String /* "preventOverflow" */, PreventOverflowOptions>
