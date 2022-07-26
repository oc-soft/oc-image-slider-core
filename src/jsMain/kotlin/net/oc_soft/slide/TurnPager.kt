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
import kotlin.collections.ArrayList
import kotlin.collections.mutableListOf
import kotlin.js.Promise

import kotlinx.browser.document
import kotlinx.browser.window

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

import org.w3c.dom.ResizeObserver
import org.w3c.dom.ResizeObserverEntry


import net.oc_soft.animation.PointsAnimation
import net.oc_soft.animation.QubicBezier
import net.oc_soft.BackgroundStyle

/**
 * turn pager 
 */
class TurnPager {

    /**
     * class instance
     */
    companion object {

        /**
         * create animation setting
         */
        fun createPager(
            containerElement: HTMLElement,
            direction: TurnPage.Direction,
            flipStart: Int,
            cornerLines: Array<Array<Array<Number>>>,
            steps: Int,
            loopPage: Boolean,
            animationOption: Map<String, Any>): Pager {

            
            val turnPage = TurnPage()

            val animationParams = createAnimationParams(
                containerElement, turnPage,
                direction, flipStart, cornerLines, steps) 

            turnPage.loopPage = loopPage
            
            turnPage.createFoldingSpace(containerElement, direction)

            return createPager(
                turnPage,
                PointsAnimation(containerElement),
                animationParams,
                animationOption)
        }
        /**
         * create animation setting
         */
        fun createPager0(
            containerElement: HTMLElement,
            direction: TurnPage.Direction,
            flipStart: Int,
            cornerLines: Array<Array<Array<Number>>>,
            steps: Int,
            loopPage: Boolean,
            animationOption: Map<String, Any>): Pager {


            val animationParamsProc: ()->Triple<
                Array<TurnPage.MotionbaseParam>,
                Array<Array<DoubleArray>>, PointsAnimation> = {
                createAnimationParams(
                    containerElement, direction, flipStart, cornerLines, steps)
            }
            val turnPage = TurnPage()
            turnPage.loopPage = loopPage
            
            turnPage.createFoldingSpace(containerElement, direction)

            return createPager(
                turnPage, 
                animationParamsProc,
                animationOption)
        }

        /**
         * create animation parameters
         */
        fun createAnimationParams(
            containerElement: HTMLElement,
            direction: TurnPage.Direction,
            flipStart: Int,
            cornerLines: Array<Array<Array<Number>>>,
            steps: Int):
            Triple<Array<TurnPage.MotionbaseParam>,
                Array<Array<DoubleArray>>,
                PointsAnimation> {
            val bounds = TurnPage.getFoldingSpaceBounds(
                containerElement)
            
            val motionbaseParams = Array<TurnPage.MotionbaseParam>(2) {
                val idx = it
                TurnPage.createMotionbaseParam(
                    bounds, direction, idx, flipStart)
            }

            val bezierPointsArray = Array<Array<DoubleArray>>(2) {
                val idx = it
                val motionbaseParam = motionbaseParams[idx]

                val baseLine = motionbaseParam.linesParam.baseLine

                val lineForBezier = 
                    TurnPage.Line(
                        baseLine[motionbaseParam.startIndex],
                        baseLine[motionbaseParam.endIndex])
                
                val bezier = TurnPage.createBezier(
                    lineForBezier, cornerLines[idx])
            
                TurnPage.createBezierPoints(bezier, steps,
                    doubleArrayOf(0.9, 0.9),
                    doubleArrayOf(0.1, 0.1))
            }
            return Triple(motionbaseParams, bezierPointsArray,
                PointsAnimation(containerElement))
        }
 
        /**
         * create animation parameters
         */
        fun createAnimationParams(
            containerElement: HTMLElement,
            turnPage: TurnPage,
            direction: TurnPage.Direction,
            flipStart: Int,
            cornerLines: Array<Array<Array<Number>>>,
            steps: Int): AnimationParams {
            
            val (motionbaseParamsProc,
                bezierPointsArrayProc) = createMotionbaseParamsCore(
                    containerElement,
                    direction, flipStart,
                    cornerLines, steps)

            
            val handlersProc: ()->Array<Array<()->Unit>> = {
                val motionbaseParams = motionbaseParamsProc()
                arrayOf(
                    Array<()->Unit>(motionbaseParams.size) {
                        createStartAnimationHandler(
                            turnPage, motionbaseParams[it])
                    },
                    Array<()->Unit>(motionbaseParams.size) {
                        createFinishAnimationHandler(
                            turnPage, motionbaseParams[it])
                    }
                )
            }

            val animatorsProc: ()->Array<(DoubleArray, Double)->Unit> = {
                val motionbaseParams = motionbaseParamsProc()
                Array<(DoubleArray, Double)->Unit>(
                    motionbaseParams.size) {
                    TurnPage.createAnimator(turnPage, motionbaseParams[it])
                }
            }
             
            return AnimationParams(
                motionbaseParamsProc, 
                bezierPointsArrayProc,
                handlersProc,
                animatorsProc)
        }

        /**
         * create motion base param core
         */
        fun createMotionbaseParamsCore(
            containerElement: HTMLElement,
            direction: TurnPage.Direction,
            flipStart: Int,
            cornerLines: Array<Array<Array<Number>>>,
            steps: Int):
            Pair<()->Array<TurnPage.MotionbaseParam>,
                ()->Array<Array<DoubleArray>>> {
            var motionbaseParamsCache: Array<TurnPage.MotionbaseParam>? = null
            val motionbaseParamsProc: ()->Array<TurnPage.MotionbaseParam> = {
                var result = motionbaseParamsCache
                if (result == null) {
                    val bounds = TurnPage.getFoldingSpaceBounds(
                        containerElement)
 
                    result = Array<TurnPage.MotionbaseParam>(2) {
                        val idx = it
                        TurnPage.createMotionbaseParam(
                            bounds, direction, idx, flipStart)
                    }
                    motionbaseParamsCache = result
                }
                result
            }

            var bezierPointsArrayCache: Array<Array<DoubleArray>>? = null
            val bezierPointsArrayProc: ()->Array<Array<DoubleArray>> = {

                var result = bezierPointsArrayCache
                if (result == null) {
                    val motionbaseParams = motionbaseParamsProc()
                    result = Array<Array<DoubleArray>>(2) {

                        val idx = it
                        val motionbaseParam = motionbaseParams[idx]

                        val baseLine = motionbaseParam.linesParam.baseLine

                        val lineForBezier = 
                            TurnPage.Line(
                                baseLine[motionbaseParam.startIndex],
                                baseLine[motionbaseParam.endIndex])
                        
                        val bezier = TurnPage.createBezier(
                            lineForBezier, cornerLines[idx])
                    
                        TurnPage.createBezierPoints(bezier, steps,
                            doubleArrayOf(0.9, 0.9),
                            doubleArrayOf(0.1, 0.1))
                    }
                    bezierPointsArrayCache = result
                }
                result!!
            }
            
            val resizeObserver = ResizeObserver {
                entries, observer ->
                motionbaseParamsCache = null
                bezierPointsArrayCache = null
            }

            resizeObserver.observe(containerElement)

            return Pair(motionbaseParamsProc, bezierPointsArrayProc)
        }
        
        /**
         * create points animation
         */
        fun createInitPointsAnimationProc(
            pointsAnimation: PointsAnimation,
            pointsArrayProc: ()->Array<Array<DoubleArray>>,
            animatorsProc: ()->Array<(DoubleArray, Double)->Unit>,
            handlersProc: ()->Array<Array<()->Unit>>,
            duration: Double,
            rateToIndex: (Double)->Double,
            delay: Double,
            endDelay: Double): (Int)->PointsAnimation {

            
            val result: (Int)->PointsAnimation = {
                pointsSelector ->

                val pointsArray = pointsArrayProc()
                val animators = animatorsProc()

                val (startHandlers, finishHandlers) = handlersProc()
               

                val idx = pointsSelector % pointsArray.size
                val points = pointsArray[idx]
                pointsAnimation.add(points,
                    animators[idx],
                    startHandlers[idx],
                    finishHandlers[idx],
                    duration,
                    rateToIndex,
                    delay,
                    endDelay)
                pointsAnimation
            }
            return result
        }



        /**
         * create pager 
         */
        fun createPager(
            turnPage: TurnPage,
            animationParamsProc: ()->Triple<
                Array<TurnPage.MotionbaseParam>,
                Array<Array<DoubleArray>>, PointsAnimation>,
            animationOption: Map<String, Any>): Pager {

            val (motionbaseParams, pointsArray, pointsAnimation) = 
                animationParamsProc()
            
            val pointsArrayProc: ()->Array<Array<DoubleArray>> = {
                pointsArray
            }
            val handlersProc: ()->Array<Array<()->Unit>> = {
                arrayOf(
                    Array<()->Unit>(motionbaseParams.size) {
                        createStartAnimationHandler(
                            turnPage, motionbaseParams[it])
                    },
                    Array<()->Unit>(motionbaseParams.size) {
                        createFinishAnimationHandler(
                            turnPage, motionbaseParams[it])
                    }
                )
            }

            val animatorsProc: ()->Array<(DoubleArray, Double)->Unit> = {
                Array<(DoubleArray, Double)->Unit>(
                    motionbaseParams.size) {
                    TurnPage.createAnimator(turnPage, motionbaseParams[it])
                }
            }
            val initPointsAnimation = createInitPointsAnimationProc(
                pointsAnimation,
                pointsArrayProc,
                animatorsProc,
                handlersProc,
                TurnPage.getDuration(animationOption),
                { QubicBezier.easeInOut(it) },
                TurnPage.getAnimationDelay(animationOption),
                TurnPage.getAnimationEndDelay(animationOption))
            
            
            val motionbaseParamsProc: ()->Array<TurnPage.MotionbaseParam> = {
                motionbaseParams
            }
            return createPager(turnPage, 
                motionbaseParamsProc, initPointsAnimation,
                pointsAnimation)

        }

        /**
         * create pager
         */
        fun createPager(turnPage: TurnPage,
            getMotionbaseParams: ()->Array<TurnPage.MotionbaseParam>,
            initPointsAnimation: (Int)->PointsAnimation,
            pointsAnimation: PointsAnimation): Pager {
            val animatingStatus = AnimatingStatus()

            return object: Pager {

                /**
                 * setup pages
                 */
                override fun setupPages(
                    numberOfPages: Int, 
                    createPage: 
                        (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
                    getBackground: (Int)->String? ) {
                    setupPages(turnPage, numberOfPages, 
                        createPage, getBackground) 
                }

                /**
                 * page
                 */
                override var page:Int
                    get() = getPage(turnPage)
                    set(value) = setPage(turnPage, value)


                /**
                 * endlress page setting
                 */
                override var loopPage: Boolean
                    get() = turnPage.loopPage
                    set(value) {
                        turnPage.loopPage = value
                    }

                /**
                 * proceed a page to next
                 */
                override fun nextPage(): Promise<Unit> {
                    return proceedPage(turnPage, 
                        1,
                        getMotionbaseParams()[1],
                        initPointsAnimation, 
                        animatingStatus)
                }
                /**
                 * proceed a page to previous
                 */
                override fun prevPage(): Promise<Unit> {
                    return proceedPage(turnPage, 
                        0,
                        getMotionbaseParams()[0],
                        initPointsAnimation, 
                        animatingStatus)
                }
                /**
                 * release all attached resource
                 */
                override fun destroy() {
                    pointsAnimation.stop()
                    turnPage.detachFoldingSpace()
                }

            }

        }


        /**
         * create pager 
         */
        fun createPager(
            turnPage: TurnPage,
            pointsAnimation: PointsAnimation, 
            animationParams: AnimationParams,
            animationOption: Map<String, Any>): Pager {

            val motionbaseParamsProc = animationParams.motionbaseParamsProc
            val pointsArrayProc = animationParams.pointsArrayProc

            val handlersProc = animationParams.handlersProc
            val animatorsProc = animationParams.animatorsProc

            val initPointsAnimation = createInitPointsAnimationProc(
                pointsAnimation,
                pointsArrayProc,
                animatorsProc,
                handlersProc,
                TurnPage.getDuration(animationOption),
                { QubicBezier.easeInOut(it) },
                TurnPage.getAnimationDelay(animationOption),
                TurnPage.getAnimationEndDelay(animationOption))
            
            return createPager(turnPage, 
                motionbaseParamsProc, initPointsAnimation,
                pointsAnimation)

        }


        /**
         * handle event to start animation
         */
        fun createStartAnimationHandler(
            turnPage: TurnPage,
            motionbaseParam: TurnPage.MotionbaseParam): ()->Unit {
            val result: ()->Unit = {
                turnPage.prepareFlipping(motionbaseParam) 
            }
            return result
        }
        /**
         * handle event to stop doing animation
         */
        fun createFinishAnimationHandler(
            turnPage: TurnPage,
            motionbaseParam: TurnPage.MotionbaseParam): ()->Unit {
            val result: ()->Unit = {
                turnPage.visibleFoldingElement = false 
            }
            return result
        }


        /**
         * setup pages
         */
        fun setupPages(turnPage: TurnPage,
            numberOfPages: Int,
            createPage: (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
            getBackgroundStyle: (Int)->String?) {
            val direction = turnPage.direction 
            val rowColumn = if (direction == TurnPage.Direction.HORIZONTAL) {
                intArrayOf(1, 2)
            } else {
                intArrayOf(2, 1)
            }
            val foldingSpace = turnPage.foldingSpace!!
            
            
            val pages = ArrayList<
                Pair<HTMLElement, (HTMLElement)->HTMLElement>>()
            
            val cells = ArrayList<ImageDivision.Cell>()
            turnPage.foldingSpaceSize?.let {
                val pageSize = it
                for (pageIdx in 0 until numberOfPages) {
                    
                    val cells0 = ImageDivision.create(
                        rowColumn[0], rowColumn[1],
                        pageSize[0],
                        pageSize[1],
                        { createElement(
                            foldingSpace,
                            pageSize,
                            pageIdx,
                            createPage,
                            getBackgroundStyle) }    
                    )
                    cells0.forEach {
                        cells.add(it)
                        val (elem, cloneElem) = it 
                        pages.add(Pair(elem, cloneElem))
                    }
                }
            }
            
            val resizeObserver = ResizeObserver {
                enntries, observer ->
                turnPage.foldingSpaceSize?.let {
                    val pageSize = it
                    cells.forEach {
                        it.updateBounds(pageSize[0], pageSize[1])     
                    }
                }
            }
            resizeObserver.observe(foldingSpace) 

            turnPage.setPages(pages.toTypedArray()) 
        }

        /**
         * create 
         */
        fun createElement(
            foldingSpace: HTMLElement,
            foldingSpaceSize: DoubleArray,
            pageIndex: Int,
            createPage: (Int)->Pair<HTMLElement, (HTMLElement)->HTMLElement>,
            getBackgroundStyle: (Int)->String?): 
                Pair<HTMLElement, (HTMLElement)->HTMLElement> {

            val pageParent = document.createElement("div") as HTMLElement
            pageParent.style.width = "${foldingSpaceSize[0]}px"
            pageParent.style.height = "${foldingSpaceSize[1]}px"
            pageParent.style.position = "relative"


            getBackgroundStyle(pageIndex)?.let {
                 
                pageParent.style.background = it
            }

            val (elem, clonePageElement) = createPage(pageIndex)
             
            pageParent.append(elem)

            val grandPageParent = document.createElement("div") as HTMLElement

            val elements = ArrayList<Array<HTMLElement>>()
            val cloneElement: (HTMLElement)->HTMLElement = {
                val result = it.cloneNode() as HTMLElement
                val pageParent = it.firstElementChild as HTMLElement

                val pageParent0 = pageParent.cloneNode() as HTMLElement
                val elem = pageParent.firstElementChild as HTMLElement
                val elem0 = clonePageElement(elem)
                pageParent0.append(elem0)
                
                result.append(pageParent0)
                elements.add(arrayOf(result, pageParent0, elem0))
                result
            }

            grandPageParent.append(pageParent)
            
            return Pair(grandPageParent, cloneElement)
        }



        /**
         * update element size
         */
        fun updateSize(element: HTMLElement, width: Double, height: Double) {
            element.style.width = "${width}px"
            element.style.height = "${height}px"
        }
        

        /**
         * set page index
         */
        fun setPage(
            turnPage: TurnPage,
            pageIndex: Int) {
           
            turnPage.pageIndex = 2 * pageIndex  
        }
        /**
         * get page index
         */
        fun getPage(
            turnPage: TurnPage): Int {
            return turnPage.pageIndex / 2
        }
    
        /**
         * proceed page
         */
        fun proceedPage(
            turnPage: TurnPage,
            pointsSelector: Int,
            motionbaseParam: TurnPage.MotionbaseParam,
            initPointsAnimation: (Int)-> PointsAnimation,
            animatingStatus: AnimatingStatus): Promise<Unit> {

            val handler = animatingStatus.animationEventHandler
            return if (handler == null) {
                Promise<Unit>() {
                    resolve, _ ->
                    val handler0: (Event)->Unit = {
                        if (it.type == "finish") {
                            animatingStatus.animationEventHandler = null
                            resolve(Unit) 
                            turnPage.visibleFoldingElement = false 
                        }
                    }
                    turnPage.foldingSpace!!.addEventListener(
                        "finish", 
                        handler0, object {
                            @JsName("once")
                            val once = true
                        })
                    animatingStatus.animationEventHandler = handler0

                    initPointsAnimation(pointsSelector).start()
                }
                
            } else {
                Promise.reject(IllegalStateException())
            }
        }
    } 

    /**
     * animating status
     */
    data class AnimatingStatus(
        /**
         * anition event handler
         */
        var animationEventHandler: ((Event)->Unit)? = null)


    /**
     * animation parameter
     */
    class AnimationParams(
        /**
         * procedure to create motion base paramers 
         */
        val motionbaseParamsProc: ()->Array<TurnPage.MotionbaseParam>,
        /**
         * procedure to create bezier points
         */
        val pointsArrayProc: ()->Array<Array<DoubleArray>>,
        /**
         * procedure to create event handlers
         */
        val handlersProc: ()->Array<Array<()->Unit>>,
        /**
         * procedure to create animator procedure's array
         */
        val animatorsProc: ()->Array<(DoubleArray, Double)->Unit>) {

        /**
         * component 1
         */
        operator fun component1(): ()->Array<TurnPage.MotionbaseParam> {
            return motionbaseParamsProc
        }
        /**
         * component 2
         */
        operator fun component2(): ()->Array<Array<DoubleArray>> {
            return pointsArrayProc 
        }

        /**
         * component 3
         */
        operator fun component3(): ()->Array<Array<()->Unit>> {
            return handlersProc
        }

        /**
         * component 4
         */
        operator fun component4(): ()->Array<(DoubleArray, Double)->Unit> {
            return animatorsProc
        }
    }
}

// vi: se ts=4 sw=4 et:
