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
import tsstdlib.Partial

external interface Window {
    var innerHeight: Number
    var offsetHeight: Number
    var innerWidth: Number
    var offsetWidth: Number
    var pageXOffset: Number
    var pageYOffset: Number
    var getComputedStyle: Any
    fun addEventListener(type: Any, listener: Any, optionsOrUseCapture: Any = definedExternally)
    fun removeEventListener(type: Any, listener: Any, optionsOrUseCapture: Any = definedExternally)
    var Element: Element
    var HTMLElement: HTMLElement
    var Node: Node
    override fun toString(): String /* "[object Window]" */
    var devicePixelRatio: Number
    var visualViewport: EventTarget? /* EventTarget? & `T$8`? */
        get() = definedExternally
        set(value) = definedExternally
    var ShadowRoot: ShadowRoot
}

external interface Rect {
    var width: Number
    var height: Number
    var x: Number
    var y: Number
}

external interface Offsets {
    var y: Number
    var x: Number
}

external interface StateRects {
    var reference: Rect
    var popper: Rect
}

external interface StateOffsets {
    var popper: Offsets
    var arrow: Offsets?
        get() = definedExternally
        set(value) = definedExternally
}

typealias OffsetData = Any

external interface `T$0` {
    var reference: dynamic /* Element | VirtualElement */
        get() = definedExternally
        set(value) = definedExternally
    var popper: HTMLElement
    var arrow: HTMLElement?
        get() = definedExternally
        set(value) = definedExternally
}

external interface `T$1` {
    var reference: Array<dynamic /* Element | Window | EventTarget & `T$8` */>
    var popper: Array<dynamic /* Element | Window | EventTarget & `T$8` */>
}

external interface `T$2` {
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface `T$3` {
    @nativeGetter
    operator fun get(key: String): dynamic /* String? | Boolean? */
    @nativeSetter
    operator fun set(key: String, value: String)
    @nativeSetter
    operator fun set(key: String, value: Boolean)
}

external interface `T$4` {
    @nativeGetter
    operator fun get(key: String): `T$3`?
    @nativeSetter
    operator fun set(key: String, value: `T$3`)
}

external interface `T$5` {
    var x: Number?
        get() = definedExternally
        set(value) = definedExternally
    var y: Number?
        get() = definedExternally
        set(value) = definedExternally
    var centerOffset: Number
}

external interface `T$6` {
    var isReferenceHidden: Boolean
    var hasPopperEscaped: Boolean
    var referenceClippingOffsets: SideObject
    var popperEscapeOffsets: SideObject
}

external interface `T$7` {
    var arrow: `T$5`?
        get() = definedExternally
        set(value) = definedExternally
    var hide: `T$6`?
        get() = definedExternally
        set(value) = definedExternally
    var offset: OffsetData?
        get() = definedExternally
        set(value) = definedExternally
    var preventOverflow: Offsets?
        get() = definedExternally
        set(value) = definedExternally
    var popperOffsets: Offsets?
        get() = definedExternally
        set(value) = definedExternally
    @nativeGetter
    operator fun get(key: String): Any?
    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface State {
    var elements: `T$0`
    var options: OptionsGeneric<Any>
    var placement: dynamic /* "auto" | "auto-start" | "auto-end" | Any | "top-start" | "top-end" | "bottom-start" | "bottom-end" | "right-start" | "right-end" | "left-start" | "left-end" */
        get() = definedExternally
        set(value) = definedExternally
    var strategy: String /* "absolute" | "fixed" */
    var orderedModifiers: Array<Modifier<Any, Any>>
    var rects: StateRects
    var scrollParents: `T$1`
    var styles: `T$2`
    var attributes: `T$4`
    var modifiersData: `T$7`
    var reset: Boolean
}

external interface StatePartial {
    var elements: `T$0`?
        get() = definedExternally
        set(value) = definedExternally
    var options: OptionsGeneric<Any>?
        get() = definedExternally
        set(value) = definedExternally
    var placement: dynamic /* "auto" | "auto-start" | "auto-end" | Any? | "top-start" | "top-end" | "bottom-start" | "bottom-end" | "right-start" | "right-end" | "left-start" | "left-end" */
        get() = definedExternally
        set(value) = definedExternally
    var strategy: String? /* "absolute" | "fixed" */
        get() = definedExternally
        set(value) = definedExternally
    var orderedModifiers: Array<Modifier<Any, Any>>?
        get() = definedExternally
        set(value) = definedExternally
    var rects: StateRects?
        get() = definedExternally
        set(value) = definedExternally
    var scrollParents: `T$1`?
        get() = definedExternally
        set(value) = definedExternally
    var styles: `T$2`?
        get() = definedExternally
        set(value) = definedExternally
    var attributes: `T$4`?
        get() = definedExternally
        set(value) = definedExternally
    var modifiersData: `T$7`?
        get() = definedExternally
        set(value) = definedExternally
    var reset: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface Instance {
    var state: State
    var destroy: () -> Unit
    var forceUpdate: () -> Unit
    var update: () -> Promise<StatePartial>
    var setOptions: (options: OptionsGenericPartial<Any>) -> Promise<StatePartial>
}

external interface ModifierArguments<Options> {
    var state: State
    var instance: Instance
    var options: Partial<Options>
    var name: String
}

external interface Modifier<Name, Options> {
    var name: Name
    var enabled: Boolean
    var phase: Any
    var requires: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var requiresIfExists: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var fn: (arg0: ModifierArguments<Options>) -> dynamic
    var effect: ((arg0: ModifierArguments<Options>) -> dynamic)?
        get() = definedExternally
        set(value) = definedExternally
    var options: Partial<Options>?
        get() = definedExternally
        set(value) = definedExternally
    var data: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ModifierPartial<Name, Options> {
    var name: Name?
        get() = definedExternally
        set(value) = definedExternally
    var enabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var phase: Any?
        get() = definedExternally
        set(value) = definedExternally
    var requires: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var requiresIfExists: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var fn: ((arg0: ModifierArguments<Options>) -> dynamic)?
        get() = definedExternally
        set(value) = definedExternally
    var effect: ((arg0: ModifierArguments<Options>) -> dynamic)?
        get() = definedExternally
        set(value) = definedExternally
    var options: Partial<Options>?
        get() = definedExternally
        set(value) = definedExternally
    var data: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface EventListeners {
    var scroll: Boolean
    var resize: Boolean
}

external interface Options {
    var placement: dynamic /* "auto" | "auto-start" | "auto-end" | Any | "top-start" | "top-end" | "bottom-start" | "bottom-end" | "right-start" | "right-end" | "left-start" | "left-end" */
        get() = definedExternally
        set(value) = definedExternally
    var modifiers: Array<ModifierPartial<Any, Any>>
    var strategy: String /* "absolute" | "fixed" */
    var onFirstUpdate: ((arg0: StatePartial) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

external interface OptionsGeneric<TModifier> {
    var placement: dynamic /* "auto" | "auto-start" | "auto-end" | Any | "top-start" | "top-end" | "bottom-start" | "bottom-end" | "right-start" | "right-end" | "left-start" | "left-end" */
        get() = definedExternally
        set(value) = definedExternally
    var modifiers: Array<TModifier>
    var strategy: String /* "absolute" | "fixed" */
    var onFirstUpdate: ((arg0: StatePartial) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

external interface OptionsGenericPartial<TModifier> {
    var placement: dynamic /* "auto" | "auto-start" | "auto-end" | Any? | "top-start" | "top-end" | "bottom-start" | "bottom-end" | "right-start" | "right-end" | "left-start" | "left-end" */
        get() = definedExternally
        set(value) = definedExternally
    var modifiers: Array<TModifier>?
        get() = definedExternally
        set(value) = definedExternally
    var strategy: String? /* "absolute" | "fixed" */
        get() = definedExternally
        set(value) = definedExternally
    var onFirstUpdate: ((arg0: Partial<State>) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

typealias UpdateCallback = (arg0: State) -> Unit

external interface ClientRectObject {
    var x: Number
    var y: Number
    var top: Number
    var left: Number
    var right: Number
    var bottom: Number
    var width: Number
    var height: Number
}

external interface SideObject {
    var top: Number
    var left: Number
    var right: Number
    var bottom: Number
}

external interface SideObjectPartial {
    var top: Number?
        get() = definedExternally
        set(value) = definedExternally
    var left: Number?
        get() = definedExternally
        set(value) = definedExternally
    var right: Number?
        get() = definedExternally
        set(value) = definedExternally
    var bottom: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external interface VirtualElement {
    var getBoundingClientRect: () -> dynamic
    var contextElement: Element?
        get() = definedExternally
        set(value) = definedExternally
}
