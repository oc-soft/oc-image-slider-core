package net.oc_soft.slide

import kotlinx.browser.document

import kotlin.collections.ArrayList

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
            width: Int,
            height: Int,
            url: URL): Array<Pair<HTMLElement, DoubleArray>> {
            
            val createElement: ()->HTMLElement = {
                val imgElem = document.createElement(
                    "img") as HTMLImageElement
                imgElem.src = url.href
                imgElem
            }
            return create(rowCount, columnCount, width, height, createElement) 
        }

        /**
         * create devided image
         */
        fun create(
            rowCount: Int,
            columnCount: Int,
            width: Int,
            height: Int,
            createElement: ()->HTMLElement): 
                Array<Pair<HTMLElement, DoubleArray>> {
            
            val elements = ArrayList<Pair<HTMLElement, DoubleArray>>()
            if (rowCount > 0 && columnCount > 0) {
                val grids = Grid.generate(doubleArrayOf(
                    0.0, 0.0, 
                    width.toDouble(),
                    height.toDouble()),
                    rowCount,
                    columnCount) 
                grids.forEach {
                    val row = it
                    row.forEach {
                        val grid = it
                        val elem = createElement()
                        val elementContainer = 
                            document.createElement("div") as HTMLElement

                        val imgWidth = kotlin.math.round(
                            grid[2] - grid[0]).toInt()
                        val imgHeight = kotlin.math.round(
                            grid[3] - grid[1]).toInt()


                        elem.style.position = "absolute"  
                        elem.style.left = "-${grid[0]}px"
                        elem.style.top = "-${grid[1]}px"

                        elementContainer.style.position = "absolute"
                        elementContainer.style.overflowX = "hidden"
                        elementContainer.style.overflowY = "hidden"

                        elementContainer.style.width = "${imgWidth}px"
                        elementContainer.style.height = "${imgHeight}px" 
                        elementContainer.append(elem)

                        
                        elements.add(Pair(elementContainer, grid))
                    }
                } 
            }
            return elements.toTypedArray()
        }

        
    }
}

// vi: se ts=4 sw=4 et:
