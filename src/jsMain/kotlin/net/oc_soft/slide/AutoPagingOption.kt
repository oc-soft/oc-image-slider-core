package net.oc_soft.slide


import kotlin.collections.Map

import org.w3c.dom.HTMLElement

import net.oc_soft.AutoPaging

/**
 * paging option
 */
class AutoPagingOption {

    companion object {

        /**
         * update option with json setting
         */
        fun createPager(
            containerElement: HTMLElement,
            optionMap: Map<String, Any>)
            : AutoPaging.Pager? {
            return optionMap["type"]?.let {
                if (it is String) {
                    when (it) {
                        "turn-page" -> createTurnPager(
                            containerElement,
                            optionMap)
                        else -> null
                    }
                } else {
                    null
                }
            }
        }  

        /**
         * create turn fragment
         */
        fun createTurnPager(
            containerElement: HTMLElement,
            optionMap: Map<String, Any>): AutoPaging.Pager? {


            val direction = TurnPage.Direction.HORIZONTAL

            
            val flipOrder: Int =  optionMap["flip-order"]?.let {
                when (it) {
                is String -> if (it.toInt() >= 0) { 1 } else { -1 }
                is Number -> if (it.toInt() >= 0) { 1 } else { -1 }
                else -> 1
                } 
            }?: 1 
            val flipStart: Int = optionMap["flip-start"]?.let {
                when (it) {
                is String -> if (it.toInt() >= 0) { 1 } else { -1 }
                is Number -> if (it.toInt() >= 0) { 1 } else { -1 }
                else -> 1
                }
            }?:1
            
            val cornerLineDefault = arrayOf(
                arrayOf(0.05 as Number, 0.05 as Number),
                arrayOf(0.95 as Number, 0.05 as Number))
            val cornerLine = optionMap["corner-line"]?.let {
                when (it) { 
                is Array<*> -> it as Array<Array<Number>>
                else -> cornerLineDefault
                }
            }?: cornerLineDefault

            val steps: Int? = optionMap["steps"]?.let {
                when (it) {
                    is String -> it.toIntOrNull()
                    is Number -> it.toInt()
                    else -> null
                }
            }
            val animationOption = Option.getAnimationOption(optionMap)           
            return if (steps != null && animationOption != null) {
                TurnAutoPager.createPager(containerElement,
                    direction, flipOrder, flipStart,
                    cornerLine, steps!!, animationOption!!)
            } else null 
        }

        
    } 
}


// vi: se ts=4 sw=4 et:
