/*
 * Copyright 2022 oc-soft
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.oc_soft.slide


import kotlin.collections.Map

import org.w3c.dom.HTMLElement


/**
 * pager option
 */
class PagerOption {

    /**
     * class instance
     */
    companion object {
        /**
         * update option with json setting
         */
        fun createPager(
            containerElement: HTMLElement,
            optionMap: Map<String, Any>)
            : Pager? {
            return optionMap["type"]?.let {
                if (it is String) {
                    when (it) {
                        "turn-page" -> createTurnPager(
                            containerElement,
                            optionMap)
                        "slide-page" -> createSlidePager(
                            containerElement,
                            optionMap)
                        "push-page" -> createPushPager(
                            containerElement,
                            optionMap)
                        "fade-page" -> createFadePager(
                            containerElement,
                            optionMap)
                        "fragments-page" -> createFragmentsPager(
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
            optionMap: Map<String, Any>): Pager? {


            val direction = TurnPage.Direction.HORIZONTAL

            
            val flipStart: Int = optionMap["flip-start"]?.let {
                when (it) {
                is String -> if (it.toInt() >= 0) { 1 } else { -1 }
                is Number -> if (it.toInt() >= 0) { 1 } else { -1 }
                else -> 1
                }
            }?:1
            
            val cornerLineDefault = arrayOf(
                arrayOf(
                    arrayOf(0.05 as Number, 0.05 as Number),
                    arrayOf(0.95 as Number, 0.05 as Number)),
                arrayOf(
                    arrayOf(0.5 as Number, -0.05 as Number),
                    arrayOf(0.95 as Number, -0.05 as Number)))

            val cornerLine = optionMap["corner-line"]?.let {
                when (it) { 
                is Array<*> -> it as Array<Array<Array<Number>>>
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

            val loopPage: Boolean = optionMap["loop"]?.let {
                when (it) {
                    is String -> it.toBoolean()
                    is Number -> it.toInt() != 0
                    is Boolean -> it
                    else -> false
                }
            }?: false
            val animationOption = Option.getAnimationOption(optionMap)           
            return if (steps != null && animationOption != null) {
                TurnPager.createPager(containerElement,
                    direction, flipStart,
                    cornerLine, steps, loopPage, animationOption)
            } else null 
        }

        /**
         * create slide pager
         */
        fun createSlidePager(
            containerElement: HTMLElement,
            optionMap: Map<String, Any>): Pager? {

            
            val animationOption = Option.getAnimationOption(optionMap)           
            return if (animationOption != null) {
                SlidePager.createPager(containerElement, animationOption)
            } else null 
        }
        /**
         * create push pager
         */
        fun createPushPager(
            containerElement: HTMLElement,
            optionMap: Map<String, Any>): Pager? {

            
            val animationOption = Option.getAnimationOption(optionMap)           
            return if (animationOption != null) {
                PushPager.createPager(containerElement, animationOption)
            } else null 
        }
        /**
         * create fade pager
         */
        fun createFadePager(
            containerElement: HTMLElement,
            optionMap: Map<String, Any>): Pager? {

            
            val animationOption = Option.getAnimationOption(optionMap)           
            return if (animationOption != null) {
                FadePager.createPager(containerElement, animationOption)
            } else null 
        }

        /**
         * create fragments pager
         */
        fun createFragmentsPager(
            containerElement: HTMLElement,
            optionMap: Map<String, Any>): Pager? {
            val animationOption = Option.getAnimationOption(optionMap)           
            return if (animationOption != null) {
                FragmentsPager.createPager(containerElement, animationOption)
            } else null 
                
        }
    } 
}


// vi: se ts=4 sw=4 et:
