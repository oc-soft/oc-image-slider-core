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

external interface `T$12` {
    var popper: Rect
    var reference: Rect
    var placement: dynamic /* "auto" | "auto-start" | "auto-end" | Any | "top-start" | "top-end" | "bottom-start" | "bottom-end" | "right-start" | "right-end" | "left-start" | "left-end" */
        get() = definedExternally
        set(value) = definedExternally
}

typealias OffsetsFunction = (arg0: `T$12`) -> dynamic

external interface OffsetOptions {
    var offset: dynamic /* OffsetsFunction | JsTuple<Number?, Number?> */
        get() = definedExternally
        set(value) = definedExternally
}

typealias OffsetModifier = Modifier<String /* "offset" */, OffsetOptions>
