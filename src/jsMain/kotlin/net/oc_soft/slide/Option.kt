package net.oc_soft.slide

import kotlin.js.Json
import kotlin.collections.MutableMap

import org.w3c.dom.HTMLElement

import net.oc_soft.BackgroundStyle

/**
 * slide option
 */
class Option {

    /**
     * class instance
     */
    companion object {

        /**
         * update option with json setting
         */
        fun createFragments(
            containerElement: HTMLElement,
            generateElement: ()-> HTMLElement?,
            backgroundStyle: BackgroundStyle,
            optionMap: Map<String, Any>)
            : Animation? {
            return optionMap["type"]?.let {
                if (it is String) {
                    when (it) {
                        "square" -> createSquareFragment(containerElement,
                            generateElement,
                            optionMap)
                        "rect" -> createRectFragment(containerElement,
                            generateElement,
                            optionMap)
                        else -> null
                    }
                } else {
                    null
                }
            }
        }  


        /**
         * create square fragments
         */
        fun createSquareFragment(
            containerElement: HTMLElement,
            generateElement: ()->HTMLElement?,
            optionMap: Map<String, Any>): Animation? {

            val sizeAsPercent = optionMap["size"]?.let {
                 when(it) {
                    is String -> it.toDoubleOrNull()
                    is Number -> it.toDouble()
                    else -> null
                }
            }
            val steps = optionMap["steps"]?.let {
                when (it) {
                    is String -> it.toIntOrNull()
                    is Number -> it.toInt()
                    else -> null
                }
            }
            val anchor = optionMap["anchor"]?.let {
                when (it) {
                    is String -> it.toIntOrNull()?.let { intArrayOf(it) }
                    is Number -> intArrayOf(it.toInt())
                    is Array<*> -> {
                        val numbers = it.filterIsInstance<Number>()  
                        IntArray(numbers.size) {
                            numbers[it].toInt()
                        }
                    }
                    else -> null
                }
            }
            val animationOption = getAnimationOption(optionMap)
            return if (sizeAsPercent != null
                && steps != null
                && anchor != null
                && animationOption != null) {
            
                Square.createFragments(
                    containerElement,
                    generateElement,
                    anchor,
                    steps,
                    sizeAsPercent,
                    animationOption)
            } else {
                null
            } 
        }

        /**
         * create rect fragments
         */
        fun createRectFragment(
            containerElement: HTMLElement,
            generateElement: ()->HTMLElement?,
            optionMap: Map<String, Any>): Animation? {

            val rowColumns = optionMap["division"]?.let {
                 when(it) {
                    is String -> it.toIntOrNull()?.let {
                        intArrayOf(it, it) 
                    }
                    is Number -> intArrayOf(it.toInt(), it.toInt())
                    is Array<*> -> {
                        val numbers = it.filterIsInstance<Number>()  
                        IntArray(numbers.size) {
                            numbers[it].toInt()
                        }
                    }
                    else -> null
                }
            }
            val steps = optionMap["steps"]?.let {
                when (it) {
                    is String -> it.toIntOrNull()
                    is Number -> it.toInt()
                    else -> null
                }
            }
            val anchor = optionMap["anchor"]?.let {
                when (it) {
                    is String -> it.toIntOrNull()?.let { intArrayOf(it) }
                    is Number -> intArrayOf(it.toInt())
                    is Array<*> -> {
                        val numbers = it.filterIsInstance<Number>()  
                        IntArray(numbers.size) {
                            numbers[it].toInt()
                        }
                    }
                    else -> null
                }
            }
            val animationOption = getAnimationOption(optionMap)
            return if (rowColumns != null
                && rowColumns.size > 1
                && steps != null
                && anchor != null
                && animationOption != null) {
            
                Rect.createFragments(
                    containerElement,
                    generateElement,
                    anchor,
                    steps,
                    rowColumns[0],
                    rowColumns[1],
                    animationOption)
            } else {
                null
            } 
        }


        /**
         * extract animation option from option map
         */
        fun getAnimationOption(
            optionMap: Map<String, Any>): Map<String, Any>? {
            return optionMap["animation"]?.let {
                val optionValue: dynamic = it
                val optionType = js("typeof optionValue")

                if (!(optionValue is Array<*>) && optionType == "object") {
                    val animMap = HashMap<String, Any>()    
                    val keys = js("Object.keys(optionValue)")
                    for (idx in 0 until keys.length as Int) {
                        val keyStr = keys[idx] as String 
                        animMap[keyStr] = optionValue[keyStr] as Any
                    }
                    animMap
                } else {
                    null
                }
            }
        }
    }
}

// vi: se ts=4 sw=4 et:
