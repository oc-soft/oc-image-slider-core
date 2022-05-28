package net.oc_soft.slide

import kotlin.collections.Map
import kotlin.collections.ArrayList
import kotlin.js.Promise

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

import net.oc_soft.AutoPaging
import net.oc_soft.animation.PointsAnimation
import net.oc_soft.animation.QubicBezier

/**
 * turn page auto paging pager
 */
class TurnAutoPager {

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
            flipOrder: Int,
            flipStart: Int,
            cornerLine: Array<Array<Number>>,
            steps: Int,
            animationOption: Map<String, Any>): AutoPaging.Pager {

            val bounds = TurnPage.getFoldingSpaceBounds(
                containerElement)
            
            val motionbaseParam = TurnPage.createMotionbaseParam(
                bounds, direction, flipOrder, flipStart)

            val bezier = TurnPage.createBezier(
                motionbaseParam.linesParam.baseLine,
                cornerLine)
            
            val bezierPoints = TurnPage.createBezierPoints(bezier, steps)
            
            val pointsAnimation = PointsAnimation(containerElement)

            val turnPage = TurnPage()
            
            turnPage.createFoldingSpace(containerElement, direction)

            return createPager(
                turnPage, motionbaseParam, containerElement,
                pointsAnimation,
                bezierPoints,
                animationOption)

       }

        /**
         * create pager 
         */
        fun createPager(
            turnPage: TurnPage,
            motionbaseParam: TurnPage.MotionbaseParam,
            container: HTMLElement,
            pointsAnimation: PointsAnimation,
            points: Array<DoubleArray>,
            animationOption: Map<String, Any>): AutoPaging.Pager {
            val startHdlr = createStartAnimationHandler(
                turnPage, motionbaseParam)

            val finishHdlr = createFinishAnimationHandler(
                turnPage, motionbaseParam)

            val animator = TurnPage.createAnimator(turnPage, motionbaseParam)

            pointsAnimation.add(points,
                animator,
                startHdlr,
                finishHdlr,
                TurnPage.getDuration(animationOption),
                { QubicBezier.easeInOut(it) },
                TurnPage.getAnimationDelay(animationOption),
                TurnPage.getAnimationEndDelay(animationOption))
            val animatingStatus = AnimatingStatus()

            return object: AutoPaging.Pager {

                /**
                 * setup pages
                 */
                override fun setupPages(
                    numberPages: Int, 
                    createPage: (Int)->HTMLElement) {
                    setupPages(turnPage, numberPages, createPage) 
                }

                /**
                 * page
                 */
                override var page:Int
                    get() = getPage(turnPage)
                    set(value) = setPage(turnPage, value)

                /**
                 * proceed a page
                 */
                override fun nextPage(): Promise<Unit> {
                    return nextPage(turnPage, pointsAnimation, animatingStatus)
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
            }
            return result
        }

        /**
         * setup pages
         */
        fun setupPages(turnPage: TurnPage,
            numberOfPages: Int,
            createPage: (Int)->HTMLElement) {
            val direction = turnPage.direction 
            val rowColumn = if (direction == TurnPage.Direction.HORIZONTAL) {
                intArrayOf(1, 2)
            } else {
                intArrayOf(2, 1)
            }
            

            val pageSize = turnPage.pageSize
            val pages = ArrayList<HTMLElement>()
            for (pageIdx in 0 until numberOfPages) {
                
                val pageAndGrids = ImageDivision.create(
                    rowColumn[0], rowColumn[1],
                    kotlin.math.round(pageSize[0]).toInt(),
                    kotlin.math.round(pageSize[1]).toInt(),
                    { createPage(pageIdx) }    
                )
                pageAndGrids.forEach {
                    pages.add(it.first)
                }
            }
            turnPage.setPages(pages.toTypedArray()) 
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
         * foward page
         */
        fun nextPage(
            turnPage: TurnPage,
            pointsAnimation: PointsAnimation,
            animatingStatus: AnimatingStatus): Promise<Unit> {

            val handler = animatingStatus.animationEventHandler
            return if (handler == null) {
                Promise<Unit>() {
                    resolve, _ ->
                    val handler0: (Event)->Unit = {
                        if (it.type == "finish") {
                            animatingStatus.animationEventHandler = null
                            resolve(Unit)
                        }
                    }
                    turnPage.foldingSpace!!.addEventListener(
                        "finish", 
                        handler0, object {
                            @JsName("once")
                            val once = true
                        })
                    animatingStatus.animationEventHandler = handler0
                    pointsAnimation.start()
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
