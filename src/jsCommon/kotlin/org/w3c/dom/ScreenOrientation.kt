package org.w3c.dom

public external abstract class ScreenOrientation {

    open val type:String = definedExternally  

    open val angle:Int = definedExternally

}

val Screen.orientation: ScreenOrientation
    get() {
        val screen = this
        return js("screen.orientation") as ScreenOrientation
    }


// vi: se ts=4 sw=4 et:
