package net.oc_soft.slide

import kotlin.collections.Map
import kotlin.collections.ArrayList
import kotlin.js.Promise

import kotlinx.browser.document

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

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
            
                TurnPage.createBezierPoints(bezier, steps)
            }
            
            val pointsAnimation = PointsAnimation(containerElement)

            val turnPage = TurnPage()
            turnPage.loopPage = loopPage
            
            turnPage.createFoldingSpace(containerElement, direction)

            return createPager(
                turnPage, 
                motionbaseParams, containerElement,
                pointsAnimation,
                bezierPointsArray,
                animationOption)

        }
        
        /**
         * create points animation
         */
        fun createInitPointsAnimationProc(
            pointsAnimation: PointsAnimation,
            pointsArray: Array<Array<DoubleArray>>,
            animators: Array<(DoubleArray, Double)->Unit>,
            startHandlers: Array<()->Unit>,
            finishHandlers: Array<()->Unit>,
            duration: Double,
            rateToIndex: (Double)->Double,
            delay: Double,
            endDelay: Double): (Int)->PointsAnimation {

            
            val result: (Int)->PointsAnimation = {
                pointsSelector ->
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
            motionbaseParams: Array<TurnPage.MotionbaseParam>,
            container: HTMLElement,
            pointsAnimation: PointsAnimation,
            pointsArray: Array<Array<DoubleArray>>,
            animationOption: Map<String, Any>): Pager {
            val startHdlrs = Array<()->Unit>(motionbaseParams.size) {
                createStartAnimationHandler(
                    turnPage, motionbaseParams[it])
            }

            val finishHdlrs = Array<()->Unit>(motionbaseParams.size) {
                createFinishAnimationHandler(
                    turnPage, motionbaseParams[it])
            }

            val animators = Array<(DoubleArray, Double)->Unit>(
                motionbaseParams.size) {
                TurnPage.createAnimator(turnPage, motionbaseParams[it])
            }
            
            val initPointsAnimation = createInitPointsAnimationProc(
                pointsAnimation,
                pointsArray,
                animators,
                startHdlrs,
                finishHdlrs,
                TurnPage.getDuration(animationOption),
                { QubicBezier.easeInOut(it) },
                TurnPage.getAnimationDelay(animationOption),
                TurnPage.getAnimationEndDelay(animationOption))
            val animatingStatus = AnimatingStatus()

            return object: Pager {

                /**
                 * setup pages
                 */
                override fun setupPages(
                    numberPages: Int, 
                    createPage: (Int)->HTMLElement,
                    getBackground: (Int)->String? ) {
                    setupPages(turnPage, numberPages, 
                        createPage, getBackground) 
                }

                /**
                 * page
                 */
                override var page:Int
                    get() = getPage(turnPage)
                    set(value) = setPage(turnPage, value)


                /**
                 * proceed a page to next
                 */
                override fun nextPage(): Promise<Unit> {
                    return proceedPage(turnPage, 
                        0,
                        motionbaseParams[0],
                        initPointsAnimation, 
                        animatingStatus)
                }
                /**
                 * proceed a page to previous
                 */
                override fun prevPage(): Promise<Unit> {
                    return proceedPage(turnPage, 
                        1,
                        motionbaseParams[1],
                        initPointsAnimation, 
                        animatingStatus)
                }
            }
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
            createPage: (Int)->HTMLElement,
            getBackgroundStyle: (Int)->String?) {
            val direction = turnPage.direction 
            val rowColumn = if (direction == TurnPage.Direction.HORIZONTAL) {
                intArrayOf(1, 2)
            } else {
                intArrayOf(2, 1)
            }
            

            val pageSize = turnPage.foldingSpaceSize
            val pages = ArrayList<HTMLElement>()
            for (pageIdx in 0 until numberOfPages) {
                
                val pageAndGrids = ImageDivision.create(
                    rowColumn[0], rowColumn[1],
                    kotlin.math.round(pageSize[0]).toInt(),
                    kotlin.math.round(pageSize[1]).toInt(),
                    { createElement(
                        pageSize,
                        pageIdx,
                        createPage,
                        getBackgroundStyle) }    
                )
                pageAndGrids.forEach {
                    pages.add(it.first)
                }
            }
            turnPage.setPages(pages.toTypedArray()) 
        }

        /**
         * create 
         */
        fun createElement(
            foldingSpaceSize: DoubleArray,
            pageIndex: Int,
            createPage: (Int)->HTMLElement,
            getBackgroundStyle: (Int)->String?): HTMLElement {

            val pageParent = document.createElement("div") as HTMLElement

            pageParent.style.width = "${foldingSpaceSize[0]}px"
            pageParent.style.height = "${foldingSpaceSize[1]}px"
            pageParent.style.position = "relative"


            getBackgroundStyle(pageIndex)?.let {
                 
                pageParent.style.background = it
            }
             
            pageParent.append(createPage(pageIndex))
            val result = document.createElement("div") as HTMLElement

            result.append(pageParent)
            
            return result
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
}

// vi: se ts=4 sw=4 et:
