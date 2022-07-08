package net.oc_soft.animation

import kotlinx.browser.window

import kotlin.collections.MutableSet
import kotlin.collections.HashSet
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

import org.w3c.dom.Element
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.Event

/**
 * manage clippath animation
 */
class PointsAnimation(
    /**
     * event target
     */
    val eventTarget: EventTarget)  {

    /**
     * animation element
     */
    data class Element(
        val points: Array<DoubleArray>,
        val animate: (DoubleArray, frameRatio: Double)->Unit,
        val begin: ()->Unit,
        val finish: ()->Unit,
        val duration: Double,
        val rateToIndex: (Double)->Double,
        val delay: Double,
        val endDelay: Double)

    /**
     * internal use while animating
     */
    data class Animating(
        /**
         * the time animation started
         */
        var startTime: Double? = null,
        /**
         * these elements are animating now
         */
        val animatingElements: MutableSet<Element> = HashSet<Element>(),
        /**
         * these elements are waiting to call finish for end delay
         */
        val watingForFinishElements: MutableSet<Element> = HashSet<Element>())
    

    /**
     * elements move frome points[0] to points[n]
     */
    val elements: MutableList<Element> = ArrayList<Element>()

    /**
     * animation id
     */
    var animationId: Int? = null

    /**
     * add points moving from 0 to point.size
     */
    fun add(
        points: Array<DoubleArray>,
        animate: (DoubleArray, Double)->Unit,
        begin: ()->Unit,
        finish: ()->Unit,
        duration: Double,
        rateToIndex: (Double)->Double,
        delay: Double = 0.0,
        endDelay: Double = 0.0) {
        add(Element(points, animate, begin, finish,
            duration, rateToIndex, delay, endDelay))
    }
    /**
     * add points moving from 0 to point.size
     */
    fun add(
        element: Element) {
        elements.add(element)
    }


    /**
     * start to move points
     */
    fun start() {
        if (animationId == null) {
            val animating = Animating()
            var requestHandler: ((Double)->Unit)? = null
            requestHandler = {
                timeToMove(it, animating)
                if (elements.size > 0) {
                    animationId = window.requestAnimationFrame(requestHandler!!)
                } else {
                    animationId = null
                }
            } 
            animationId = window.requestAnimationFrame(requestHandler) 
        }
    }

    /**
     * stop to move points
     */
    fun stop() {
        animationId?.let {
            window.cancelAnimationFrame(it)
            animationId = null
        }
    }

    /**
     * time to move
     */ 
    fun timeToMove(
        timeStamp: Double,
        animating: Animating) {
        if (animating.startTime == null) {
            animating.startTime = timeStamp
            eventTarget.dispatchEvent(Event("start"))
        } 

        val duration = timeStamp - animating.startTime!!

        val elementsRemoved = HashSet<Element>()

        elements.forEach {
            if (it.delay <= duration) {
                if (!(it in animating.animatingElements)) {
                    animating.animatingElements.add(it)
                    it.begin() 
                }
                val localDuration = duration - it.delay
                val animDuration = it.duration
                if (it.points.size > 1) {
                    val basePoint = it.points[0]
                    var ffDuration = animDuration 
                    ffDuration /= (it.points.size - 1).toDouble()
                    var idx = kotlin.math.floor(
                        localDuration / ffDuration).toInt()
                    val points = it.points
                    if (idx < points.lastIndex) {
                        val point0 = DoubleArray(basePoint.size) {
                            if (it < points[idx].size) {
                                points[idx][it]
                            } else {
                                basePoint[it]
                            }
                        } 
                        val point1 = DoubleArray(basePoint.size) {
                            if (it < points[idx + 1].size) {
                                points[idx + 1][it]
                            } else {
                                basePoint[it]
                            }
                        } 
                        
                        val rateToIndex = it.rateToIndex
                        val timeRatio = localDuration / animDuration
                        val frameRatio = rateToIndex(timeRatio)
                        val newPoints = DoubleArray(basePoint.size) {
                            calcValue(point0[it], point1[it], frameRatio) 
                        }
                        it.animate(newPoints, frameRatio)
                    } else {
                        if (!(it in animating.watingForFinishElements)) {
                            val newPoints = DoubleArray(basePoint.size) {
                                if (it < points.last().size) {
                                    points.last()[it]
                                } else {
                                    basePoint[it]
                                }
                            }
                            it.animate(newPoints, 1.0)
                            animating.watingForFinishElements.add(it)
                        } else {
                            if (it.duration + it.endDelay <= localDuration) {
                                elementsRemoved.add(it)
                                animating.watingForFinishElements.remove(it)
                            }
                        }
                    }
                }
            }
        }
        elementsRemoved.forEach {
            it.finish()
        }
        elements.removeAll(elementsRemoved)
        animating.animatingElements.removeAll(elementsRemoved)

        if (elements.size == 0) {
            eventTarget.dispatchEvent(Event("finish"))
        }
    }
    

    /**
     * geneate points 
     */
    fun calcValue(
        startPoint: Double,
        endPoint: Double,
        frameIdx: Double): Double {

        var frameIdx0 = frameIdx
        frameIdx0 = kotlin.math.min(frameIdx0, 1.0)
        frameIdx0 = kotlin.math.max(frameIdx0, 0.0)
        
        return startPoint * (1.0 - frameIdx0) + endPoint * frameIdx0
    }
    
}


// vi: se ts=4 sw=4 et:
