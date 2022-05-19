@file:JsModule("@floating-ui/dom")
@file:JsNonModule
package floating_ui.dom

import kotlin.js.Promise

/**
 * compute postion
 */
external fun computePosition(
    reference: dynamic,
    floating: dynamic,
    config: dynamic = definedExternally): Promise<dynamic>


// vi: se ts=4 sw=4 et:
