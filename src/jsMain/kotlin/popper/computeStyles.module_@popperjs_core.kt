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

external interface `T$13` {
    var x: Number?
        get() = definedExternally
        set(value) = definedExternally
    var y: Number?
        get() = definedExternally
        set(value) = definedExternally
    var centerOffset: Number?
        get() = definedExternally
        set(value) = definedExternally
}

typealias RoundOffsets = (offsets: `T$13`) -> Offsets

external interface ComputeStyleOptions {
    var gpuAcceleration: Boolean
    var adaptive: Boolean
    var roundOffsets: dynamic /* Boolean? | RoundOffsets? */
        get() = definedExternally
        set(value) = definedExternally
}

typealias ComputeStylesModifier = Modifier<String /* "computeStyles" */, ComputeStyleOptions>
