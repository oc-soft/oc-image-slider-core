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

import kotlinx.browser.document

import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.url.URL

import net.oc_soft.Grid
import net.oc_soft.Polygon

/**
 * image division
 */
class ImageDivision {

    /**
     * class instance
     */
    companion object {
        /**
         * create devided image
         */
        fun create(
            rowCount: Int,
            columnCount: Int,
            width: Double,
            height: Double,
            url: URL): Array<Cell> {
            
            val createElement:
                ()->Pair<HTMLElement, (HTMLElement)->HTMLElement> = {
                val imgElem = document.createElement(
                    "img") as HTMLImageElement
                imgElem.src = url.href
                val cloneElem: (HTMLElement)->HTMLElement = {
                    it.cloneNode() as HTMLElement
                }
                Pair(imgElem, cloneElem)
            }
            return create(rowCount, columnCount, width, height, createElement) 
        }

        /**
         * create devided image
         */
        fun create(
            rowCount: Int,
            columnCount: Int,
            width: Double,
            height: Double,
            createElement: ()->Pair<HTMLElement, (HTMLElement)->HTMLElement>): 
                Array<Cell> {
            
            val elements = ArrayList<Cell>()
            if (rowCount > 0 && columnCount > 0) {
                val grids = Grid.generate(doubleArrayOf(
                    0.0, 0.0, 
                    width.toDouble(),
                    height.toDouble()),
                    rowCount,
                    columnCount) 

                for (rowIdx in 0 until rowCount) {
                    for (colIdx in 0 until columnCount) {

                        val (elem, cloneElem) = createElement()
                        val elementContainer = 
                            document.createElement("div") as HTMLElement
                        elem.style.position = "absolute"  
                        elementContainer.style.position = "absolute"
                        elementContainer.style.overflowX = "hidden"
                        elementContainer.style.overflowY = "hidden"

                        elementContainer.append(elem)
                        val boundsUpdater: (Double, Double)->DoubleArray = {
                            w, h ->
                            updateBounds(elem, elementContainer,
                                doubleArrayOf(0.0, 0.0, w, h),
                                rowCount, columnCount,
                                rowIdx, colIdx)
                        } 
                        val grid = boundsUpdater(width, height)
                        elements.add(
                            Cell(elementContainer, elem, 
                                rowCount, columnCount, rowIdx, colIdx,
                                cloneElem))
                    }
                } 
            }
            return elements.toTypedArray()
        }


        /**
         * update size
         */
        fun updateBounds(
            element: HTMLElement,
            elementContainer: HTMLElement,
            bounds: DoubleArray, 
            rowCount: Int,
            columnCount: Int,
            row: Int,
            column: Int): DoubleArray {

            val grid = Grid.calcBound(bounds,rowCount, columnCount, row, column)
            element.style.left = "-${grid[0]}px"
            element.style.top = "-${grid[1]}px"

            val imgWidth = grid[2] - grid[0]
            val imgHeight = grid[3] - grid[1]

            elementContainer.style.width = "${imgWidth}px"
            elementContainer.style.height = "${imgHeight}px" 

            val result = grid
            return result
        }
        
    }

    /**
     * divided cell
     */
    class Cell(
        elementContainer: HTMLElement,
        element: HTMLElement,
        /**
         * row count of cell's container
         */
        val rowCount: Int,
        /**
         * column count of cell's container
         */
        val columnCount: Int,
        /**
         * row index
         */
        val row: Int,
        /**
         * column index
         */
        val column: Int,
        /**
         * duplicate element
         */
        val childClone: (HTMLElement)->HTMLElement) {
        
        /**
         * cloned elements
         */
        val elements: MutableList<Array<HTMLElement>> = 
            mutableListOf(arrayOf(elementContainer, element)) 
        /**
         * divived cell root container
         */
        val elementContainer: HTMLElement get() = elements[0][0]
        /**
         * the contents of cell
         */
        val element: HTMLElement get() = elements[0][1]

        /**
         * clone elemtn procedure
         */
        val cloneElement: (HTMLElement)->HTMLElement = 
            { this.cloneElement0(it) }

        operator fun component1(): HTMLElement {
            return elementContainer
        }

        operator fun component2(): (HTMLElement)->HTMLElement {
            return cloneElement
        }

        /**
         * procedure to update size and location.
         */
        fun updateBounds(width: Double, height: Double) {
            
            elements.forEach {
                updateBounds(it[1], it[0], 
                    doubleArrayOf(0.0, 0.0, width, height),
                    rowCount, columnCount, row, column)
            }
        }


        /**
         * clone element
         */
        fun cloneElement0(elementContainer: HTMLElement): HTMLElement {
            val result = elementContainer.cloneNode() as HTMLElement


            val child = childClone(
                elementContainer.firstElementChild as HTMLElement)
            result.append(child)
            
            elements.add(arrayOf(result, child))

            return result
        }

    }
}

// vi: se ts=4 sw=4 et:
