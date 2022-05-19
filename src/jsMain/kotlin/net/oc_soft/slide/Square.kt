package net.oc_soft.slide

import kotlin.collections.ArrayList
import kotlin.collections.Map

import kotlin.text.toDoubleOrNull
import kotlin.text.toIntOrNull

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.Event

import net.oc_soft.Polygon
import net.oc_soft.animation.PointsAnimation
import net.oc_soft.animation.QubicBezier


/**
 * manage square flagments image
 */
class Square {
    /**
     * square elements
     */
    data class SquareElements(
        /**
         * animation squares
         */
        var squares: Array<Array<DoubleArray>>,
        /**
         * row count
         */
        var rowCount: Int,
        /**
         * column count
         */
        var colCount: Int)

    /**
     * class instance
     */
    companion object {
        /**
         * create animate squares
         */
        fun createAnimationSquares(
            elem: Element,
            anchor: IntArray,
            steps: Int,
            sizeAsPercent: Double): SquareElements {
            
            val endSquares = createSquares(elem, sizeAsPercent)
            
            return SquareElements(
                Array<Array<DoubleArray>>(steps) {
                    val scale = it.toDouble() / (steps - 1).toDouble()
                    Array<DoubleArray>(endSquares.first.size) {
                        resize(anchor, scale, endSquares.first[it])        
                    }
                },
                endSquares.second[1], endSquares.second[0])
        }


        /**
         * resize bounding
         */
        fun resize(
            anchor: IntArray,
            scale: Double,
            rect: DoubleArray): DoubleArray {

            val result = rect.copyOf()
            
            anchor.forEach {
                val length =  result[(it + 2) % 4] - result[it]
                result[(it + 2) % 4] = result[it] + length * scale
            }
            return result 
        }
   
        /**
         * create squares elements
         */
        fun createSquares(
            elem: Element,
            sizeAsPercent: Double): 
                Pair<Array<DoubleArray>, IntArray> {

            val rect = elem.getBoundingClientRect() 
            val sizePx = (rect.width * sizeAsPercent) / 100.0

            val tileCounts = intArrayOf(
                kotlin.math.ceil(rect.width / sizePx).toInt(),
                kotlin.math.ceil(rect.height / sizePx).toInt()
            )
            val wholeSize = doubleArrayOf(
                tileCounts[0] * sizePx,
                tileCounts[1] * sizePx
            )

            val startOffset = doubleArrayOf(
                (rect.width - wholeSize[0]) / 2.0,
                (rect.height - wholeSize[1]) / 2.0
            )
            val percentScale = doubleArrayOf(100.0 / rect.width,
                100.0 / rect.height) 
            return Pair(
                Array<DoubleArray>(tileCounts[0] * tileCounts[1]) {
                    val rowIdx = it / tileCounts[0]
                    val colIdx = it % tileCounts[0]
                    doubleArrayOf(
                        (startOffset[0] + sizePx * colIdx)
                            * percentScale[0],
                        (startOffset[1] + sizePx * rowIdx)
                            * percentScale[1],
                        (startOffset[0] + sizePx * (colIdx + 1))
                            * percentScale[0],
                        (startOffset[1] + sizePx * (rowIdx + 1))
                            * percentScale[1])
                },
                tileCounts)
        }

        /**
         * create fragments
         */
        fun createFragments(
            containerElement: HTMLElement,
            generateElement: ()-> HTMLElement?,
            anchor: IntArray,
            steps: Int,
            sizeAsPercent: Double,
            animationOption: Map<String, Any>): Animation {

            
            val pointsAnimation = PointsAnimation(containerElement)
            val animationSquares = createAnimationSquares(
                containerElement,
                anchor, steps, sizeAsPercent)  
            val fragments = ArrayList<Fragment>()

            if (animationSquares.squares.size > 0) {
                val initialSquare = animationSquares.squares.first()
                initialSquare.forEachIndexed {
                    idx, clipping ->
                    generateElement()?.let {
                        setClipLocation(it,
                            clipping[0], clipping[1],
                            clipping[2], clipping[3])
                        fragments.add(createFragment(
                            it,
                            animationSquares.squares.map {
                                it[idx]
                            }.toTypedArray(),
                            idx / animationSquares.colCount,
                            idx % animationSquares.colCount,
                            animationSquares.rowCount,
                            animationSquares.colCount,
                            pointsAnimation,
                            animationOption))
                    } 
                }
            } 

            return Animation(
                fragments.toTypedArray(),
                containerElement,
                { pointsAnimation.start() })
        }


        /**
         * create fragment
         */
        fun createFragment(
            element: HTMLElement,
            boundsSequence: Array<DoubleArray>,
            rowIndex: Int,
            colIndex: Int,
            rowCount: Int,
            colCount: Int,
            pointsAnimation: PointsAnimation,
            animationOption: Map<String, Any>): Fragment {

            val keyFrames = ArrayList<dynamic>()

            val option = createAnimateOption(
                animationOption,
                rowIndex, colIndex,
                rowCount, colCount)
             

            pointsAnimation.add(boundsSequence,
                { points, _ -> updateClipPath(element, points) },
                { handleStartAnimation() },
                { handleFinishAnimation() },
                getDuration(animationOption),
                { QubicBezier.easeInOut(it) }, 
                calcAnimationDelay(animationOption, 
                    rowIndex, colIndex,
                    rowCount, colCount),
                0.0 )

                 
            boundsSequence.forEach {
                
                keyFrames.add(
                    kotlin.js.json(
                        Pair<String, Any>("clip-path",
                        createRectPolygonCss(
                            it[0], it[1], 
                            it[2], 
                            it[3]))))
            }

            return Fragment(element, keyFrames.toTypedArray(), option) 
        }

        /**
         * update clip-path
         */
        fun updateClipPath(
            element: HTMLElement,
            bounds: DoubleArray) {
            setClipLocation(element,
                bounds[0], bounds[1], 
                bounds[2], 
                bounds[3])
        }

        /**
         * handle start animation
         */
        fun handleStartAnimation() {
        }

        /**
         * handle finish animation
         */
        fun handleFinishAnimation() {
        }

        /**
         * calculate delay
         */
        fun calcAnimationDelay(
            animationOption: Map<String, Any>,
            rowIdx: Int,
            colIdx: Int,
            rowCount: Int,
            colCount: Int): Double {
            val majorMinorSelector = animationOption["order"]?.let{
                when (it) {
                    "col" -> 1
                    else -> 0
                }
            }?: 0
            return calcAnimationDelay(animationOption,
                intArrayOf(rowIdx, colIdx),
                intArrayOf(rowCount, colCount),
                majorMinorSelector)?: 0.0
        }
 
        /**
         * get duration from animation option
         */
        fun getDuration(
            animationOption: Map<String, Any>): Double {
            return animationOption["duration"]?.let {
                when(it) {
                    is String -> it.toDoubleOrNull()
                    is Number -> it.toDouble()
                    else -> null
                }?: 0.0
            }?: 0.0
        }
         

        /**
         * create animate operation
         */
        fun createAnimateOption(
            animationOption: Map<String, Any>,
            rowIdx: Int,
            colIdx: Int,
            rowCount: Int,
            colCount: Int): dynamic {
            val result: dynamic = js("{}")

            
            result["delay"] = calcAnimationDelay(
                animationOption, rowIdx, colIdx,
                rowCount, colCount)
            result["duration"] = getDuration(animationOption)
            result["fill"] = "forwards"
            return result
        }

        /**
         * calculate animation delay
         */
        fun calcAnimationDelay(
            animationOption: Map<String, Any>,
            rowColIdx: IntArray,
            rowColCount: IntArray,
            majorMinorSelector: Int): Double? {

            val directionKeys = arrayOf(
                "major-direction", "minor-direction")
            val directions = Array<Int?>(directionKeys.size) {
                animationOption[directionKeys[it]]?.let {
                    when(it) {
                        is String -> it.toIntOrNull()?.let { it % 2 }
                        is Number -> it.toInt() % 2
                        else -> null
                    }
                }
            }
            val offsetKeys = arrayOf(
                "major-offset", "minor-offset",
                "stride-offset")
            val offsets = Array<Int?>(offsetKeys.size) {
                animationOption[offsetKeys[it]]?.let {
                    when(it) {
                        is String -> it.toIntOrNull()
                        is Number -> it.toInt()
                        else -> null
                    }
                }
            }
            val delayUnit = animationOption["delay"]?.let {
                when(it) {
                    is String -> it.toDoubleOrNull()
                    is Number -> it.toDouble() 
                    else -> null
                }
            }
            return if (directions[0] != null && directions[1] != null
                && offsets[0] != null && offsets[1] != null
                && offsets[2] != null
                && delayUnit != null) {
                clacAnimationDelay(rowColIdx, rowColCount,
                    directions[0]!!, directions[1]!!,
                    offsets[0]!!, offsets[1]!!, offsets[2]!!,
                    delayUnit, majorMinorSelector)
            } else {
                null
            } 
        }


        /**
         * calculate animation delay
         */
        fun clacAnimationDelay(
            rowColIdx: IntArray,
            rowColCount: IntArray,
            majorDirection: Int,
            minorDirection: Int,
            majorOffset: Int,
            minorOffset: Int,
            strideOffset: Int,
            delayUnit: Double,
            majorMinorSelector: Int): Double? {
            return calcAnimationDelay(
                rowColIdx[majorMinorSelector],
                rowColIdx[(majorMinorSelector + 1) % 2],
                rowColCount[majorMinorSelector],
                rowColCount[(majorMinorSelector + 1) % 2],
                majorDirection, minorDirection,
                majorOffset, minorOffset, 
                strideOffset, delayUnit)  
        }


        /**
         * create animate 
         */
        fun calcAnimationDelay(
            majorIdx: Int,
            minorIdx: Int,
            majorCount: Int,
            minorCount: Int,
            majorDirection: Int,
            minorDirection: Int,
            majorOffset: Int,
            minorOffset: Int,
            strideOffset: Int, 
            delayUnit: Double): Double {
            var delayIdx0 = majorOffset 
            delayIdx0 += majorIdx * majorDirection 
            delayIdx0 = (delayIdx0 % majorCount + majorCount) % majorCount
            delayIdx0 *= minorCount 
            var delayIdx1 = minorOffset 
            delayIdx1 += strideOffset * majorIdx
            delayIdx1 += minorIdx * minorDirection
            delayIdx1 = (delayIdx1 % minorCount + minorCount) % minorCount
            return delayUnit * (delayIdx0 + delayIdx1).toDouble()
        }
         
       

        /**
         * set element location 
         */
        fun setClipLocation(element: HTMLElement,
            left: Double, top: Double, right: Double, bottom: Double) {
            
            element.style.setProperty("clip-path",
                createRectPolygonCss(left, top, right, bottom))
        }

        /**
         * create rect polygon with css format
         */
        fun createRectPolygonCss( 
            left: Double, top: Double, right: Double, bottom: Double): String {
            return "polygon(${createRectPolygon(left, top, right, bottom)})"
        }

        /**
         * create rect polygon
         */
        fun createRectPolygon(
            left: Double, top: Double, right: Double, bottom: Double): String {

            return Polygon.convertFromRect(
                left, top, right, bottom).joinToString(
                transform = {
                    it.joinToString(" ",
                        transform = {
                            "${it}%"
                        })
                }) 
        }
    }
}

// vi: se ts=4 sw=4 et:
