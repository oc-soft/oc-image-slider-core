package net.oc_soft.slide

import kotlin.js.JSON
import kotlinx.js.ReadonlyArray

import kotlin.math.pow
import kotlin.collections.MutableList
import kotlin.collections.ArrayList

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.HTMLElement

import org.w3c.dom.url.URL

import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.dom.ResizeObserver
import org.w3c.dom.ResizeObserverEntry

import net.oc_soft.animation.PointsAnimation
import net.oc_soft.animation.QubicBezier


/**
 * simulate turning page with book.
 */
class TurnPage {

    /**
     * turn dieciton
     */
    enum class Direction {
        /**
         * standard book style
         */
        HORIZONTAL,
        /**
         * turn page vertically
         */    
        VERTICAL;

        /**
         * coordinate selector
         */
        val coordinateSelector: Int get() = ordinal
        /**
         * cross coordinate selector index
         */
        val crossCoordinateSelector: Int get() = (coordinateSelector + 1 ) % 2

        /**
         * cross direction
         */
        val crossDirection: Direction
            get() {
                return if (this == HORIZONTAL) {
                    VERTICAL
                } else {
                    HORIZONTAL
                }
            }
    }


    /**
     * class instance
     */
    companion object {

        /**
         * get container size
         */
        fun getFoldingSpaceSize(
            foldingSpace: HTMLElement): DoubleArray {
            val bounds = foldingSpace.getBoundingClientRect()
            return doubleArrayOf(bounds.width, bounds.height)
        }

        /**
         * folding space bounds
         */
        fun getFoldingSpaceBounds(
            foldingSpace: HTMLElement): Array<DoubleArray> {
            val spaceSize = getFoldingSpaceSize(foldingSpace)
            return arrayOf(
                doubleArrayOf(0.0, 0.0),
                doubleArrayOf(spaceSize[0], 0.0),
                doubleArrayOf(spaceSize[0], spaceSize[1]),
                doubleArrayOf(0.0, spaceSize[1]))
        }
 
        /**
         * calculate page size
         */
        fun calcPageSize(
            container: HTMLElement,
            direction: Direction): DoubleArray {
            val bounds = container.getBoundingClientRect()
            
            return if (direction == Direction.HORIZONTAL) { 
                doubleArrayOf(
                    bounds.width / 2.0,
                    bounds.height) 
            } else {
                doubleArrayOf(
                    bounds.width,
                    bounds.height / 2.0)
            }
        }

        /**
         * select base lines
         */
        fun selectBaseLines(
            bounds: Array<DoubleArray>,
            direction: Direction): Array<Line> {

            val indices = when (direction) {
            Direction.HORIZONTAL -> arrayOf(intArrayOf(0, 1), intArrayOf(3, 2))
            else -> arrayOf(intArrayOf(0, 3), intArrayOf(1, 2))
            }  
            return arrayOf(
                Line(bounds[indices[0][0]], bounds[indices[0][1]]),
                Line(bounds[indices[1][0]], bounds[indices[1][1]]))
        }
        
        /**
         * create motionbase parameter
         */
        fun createMotionbaseParam(
            bounds: Array<DoubleArray>,
            direction: Direction,
            flipOrder: Int,
            flipStart: Int): MotionbaseParam {
             
            val lines = selectBaseLines(bounds, direction)

            val startIdx = ((flipOrder + 1) / 2) % 2
            val lineIndex = ((- flipStart + 1) / 2) % 2
            return MotionbaseParam(LinesParam(lines, lineIndex), startIdx)
        }

    

        /**
         * create bezier
         */
        fun createBezier(
            line: Line,
            cornerLine: Array<Array<Number>>): Bezier {

            val dir = line[1] - line[0] 
            val crossDir = doubleArrayOf(dir[1], dir[0])
            
            val crossLine = Line(line[0], line[0] + crossDir)

            val p1 = doubleArrayOf( 
                line.pointAt(cornerLine[0][0].toDouble())[0],
                crossLine.pointAt(cornerLine[0][1].toDouble())[1])
            val p2 = doubleArrayOf(
                line.pointAt(cornerLine[1][0].toDouble())[0],
                crossLine.pointAt(cornerLine[1][1].toDouble())[1])
        
            return Bezier(line[0], p1, p2, line[1])
        }
        /**
         * create points on bezier curve
         */
        fun createBezierPoints(
            bezier: Bezier,
            steps: Int): Array<DoubleArray> {

            val points = ArrayList<DoubleArray>()
            points.add(bezier.calcPointAt(0.0)) 
            for (idx in 1 until steps) {
                points.add(bezier.calcPointAt(
                    idx.toDouble() / steps.toDouble()))
            }
            points.add(bezier.calcPointAt(1.0))
            return points.toTypedArray()
        }

        /**
         * turn page animator
         */
        fun createAnimator(
            turnPage: TurnPage,
            motionbaseParam: MotionbaseParam): (DoubleArray, Double)->Unit {
            
            val result: (DoubleArray, Double)->Unit = {
                pt, _ ->
                turnPage.flippingPage(pt, motionbaseParam) 
            }
            return result
        }
        /**
         * get duration from animation option
         */
        fun getDuration(
            animationOption: Map<String, Any>): Double {
            return animationOption["duration"]?.let {
                when(it) {
                    is String -> it.toDoubleOrNull()!!
                    is Number -> it.toDouble()
                    else -> 0.0
                }?: 0.0
            }?: 0.0
        }
        /**
         * get animation start delay from animation option
         */
        fun getAnimationDelay(
            animationOption: Map<String, Any>): Double {
            return animationOption["delay"]?.let {
                when(it) {
                    is String -> it.toDoubleOrNull()!!
                    is Number -> it.toDouble()
                    else -> 0.0
                }?: 0.0
            }?: 0.0
        }
        /**
         * get animation end from animation option
         */
        fun getAnimationEndDelay(
            animationOption: Map<String, Any>): Double {
            return animationOption["end-delay"]?.let {
                when(it) {
                    is String -> it.toDoubleOrNull()!!
                    is Number -> it.toDouble()
                    else -> 0.0
                }?: 0.0
            }?: 0.0
        }
    }

   /**
     * bezier curve
     */
    data class Bezier(
        val p0: DoubleArray,
        val p1: DoubleArray,
        val p2: DoubleArray,
        val p3: DoubleArray) {
        /**
         * calulate a point at time t
         */
        fun calcPointAt(
            t: Double): DoubleArray {
            val oneT = 1.0 - t 
            val oneTP2 = oneT.pow(2.0)
            val oneTP3 = oneT.pow(3.0)
            val tP2 = t.pow(2.0)
            val tP3 = t.pow(3.0)

            val a = oneTP3
            val b = 3 * oneTP2 * t
            val c = 3 * oneT * tP2
            val d = tP3
            
            return DoubleArray(p0.size) {
                var cmp = a * p0[it]
                cmp += b * p1[it]
                cmp += c * p2[it] 
                cmp += d * p3[it] 
                cmp
            }
        }
    }

    
    /**
     * line
     */
    data class Line(
        val p0: DoubleArray,
        val p1: DoubleArray) {

        /**
         * calculate raise and a point on line
         */
        constructor(a: Double,
            pt: DoubleArray): this(pt, pt + doubleArrayOf(1.0, a))
        
        /**
         * a: at ax + by + c = 0
         */
        val a: Double get() = p1[1] - p0[1]

        /**
         * b: at ax + by + c = 0
         */
        val b: Double get() = -(p1[0] - p0[0])

        /**
         * c: at ax + by + c = 0
         */
        val c: Double get() = 
            - p0[0] * (p1[1] - p0[1]) + p0[1] * (p1[0] - p0[0])

        /**
         * distance
         */
        fun distance(pt: DoubleArray): Double {
            var nominator = kotlin.math.abs(a * pt[0] + b * pt[1] + c)
            val denominator = (a.pow(2.0) + b.pow(2.0)).pow(0.5)
            return  nominator / denominator
        }
        
        /**
         * get point
         */
        operator fun  get(idx: Int): DoubleArray {
            return if (idx == 0) {
                p0
            } else if (idx == 1) {
                p1
            } else  {
                throw(IllegalArgumentException())
            }
        } 
        /**
         * point at parameter t
         */
        fun pointAt(t: Double): DoubleArray {
            return DoubleArray(p0.size) {
                (1 - t) * p0[it] + t * p1[it]
            }
        }
    }

    /**
     * lines parameter
     */
    data class LinesParam(
        val lines: Array<Line>,
        val index: Int) {

        /**
         * base line
         */
        val baseLine: Line get() = lines[index]
    }

    /**
     * motion base parameter
     */
    data class MotionbaseParam(
        val linesParam: LinesParam,
        val startIndex: Int) {
        val endIndex: Int get() = (startIndex + 1) % 2
    }

    /**
     * page index
     */
    var pageIndex: Int = -1
        set(value) {
            if (field != value) {
                field = value
                syncViewWithPageIndex()
            }
        }

    /**
     * folding page container
     */
    var foldingSpace: HTMLElement? = null

    /**
     * page contents
     */
    val contents: MutableList<HTMLElement> = ArrayList<HTMLElement>()

    /**
     * page count
     */
    val pageCount: Int get() = contents.size


    /**
     * get container direction
     */
    val direction: Direction get() = getContainerDirection(foldingSpace!!) 

    /**
     * folding page container
     */
    val frontFoldingElement: HTMLElement get() = 
        getFrontFoldingElement(foldingSpace!!)!!


    /**
     * folding page container
     */
    val backFoldingElement: HTMLElement get() = 
        getBackFoldingElement(foldingSpace!!)!!


    /**
     * front and back folding element
     */
    val foldingElements: Array<HTMLElement> get() = arrayOf(
        frontFoldingElement,
        backFoldingElement)

    /**
     * visibility of folding element
     */
    var visibleFoldingElement: Boolean  
        get() {
  
            val frontFoldingElement = this.frontFoldingElement

            return frontFoldingElement.style.display?.let {
                it == "block"
            }?: true
        }
        set(value) {
            if (value != visibleFoldingElement) {
                val displayOption = if (value) {
                    "block"
                } else {
                    "none"
                } 
                arrayOf(
                    this.frontFoldingElement,
                    this.backFoldingElement).forEach {
                    it.style.display = displayOption
                }
            }
        }


    /**
     * front spread page elements
     */
    val frontSpreadPageElements: Array<HTMLElement>
        get() {
            return arrayOf(
                foldingSpace!!.querySelector(".first.front")
                    as HTMLElement,
                foldingSpace!!.querySelector(".second.front")
                    as HTMLElement)
        }

    /**
     * back spread page elements
     */
    val backSpreadPageElements: Array<HTMLElement>
        get() {
            return arrayOf(
                foldingSpace!!.querySelector(".first.back")
                    as HTMLElement,
                foldingSpace!!.querySelector(".second.back")
                    as HTMLElement)
        }


    /**
     * frontFoldingShadeElement
     */
    val frontFoldingShadeElement: HTMLElement get() =
        getFrontFoldingShadeContainer()

    /**
     * back page shade container
     */
    val backShadeElement: HTMLElement get() =
        getBackShadeElement(foldingSpace!!)!!

    /**
     * folding element size
     */
    val foldingElementSize: Double get() = 
        getFoldingElementSize(foldingSpace!!, direction)

    /**
     * folding space element
     */
    val foldingSpaceSize: DoubleArray
        get() = getFoldingSpaceSize(foldingSpace!!)
    

    /**
     * page size
     */
    val pageSize: DoubleArray
        get() = calcPageSize(foldingSpace!!, direction)
    /**
     * page bounds
     */
    val pageBounds: Array<DoubleArray> get() = 
        getFoldingSpaceBounds(foldingSpace!!)
    /**
     * if loop page is true, out of bounds page index is into from 0 to 
     * page size
     */
    var loopPage = false


    /**
     * observe root folding space resizing
     */
    var resizeObserver: ResizeObserver? = null


    /**
     * handle resize  root content
     */
    fun handleResizeFoldingSpace(
        entries: ReadonlyArray<ResizeObserverEntry>,
        resizeObserver: ResizeObserver) {
        arrayOf(
            frontSpreadPageElements,
            backSpreadPageElements).forEach { 
            syncSpreadPagesGeometryWithFoldingSpace(
                foldingSpace!!,
                direction,
                it)
        }
             
        syncFoldingElementGeometryWithFoldingSpace(
            foldingSpace!!, 
            direction,
            foldingElements)

        syncShadeContainerGeometryWithFoldingSpace(
            foldingSpace!!,
            direction, backShadeElement)
    } 



      

    /**
     * get page content
     */
    fun getPageContent(pageIndex: Int): HTMLElement? {
        return if (0 <= pageIndex && pageIndex < pageCount) {
            contents[pageIndex]
        } else null  
    }

    /**
     * get content page index
     */
    fun getContentPageIndex(
        pageIndex: Int): Int {
        return if (loopPage) {
            ((pageIndex % pageCount) + pageCount) % pageCount
        } else {
            pageIndex
        }
    }

    /**
     * select base line
     */
    fun selectBaseLineFromBounds(
        bounds: Array<DoubleArray>,
        motionPoint: DoubleArray,
        direction: Direction): LinesParam {

        val baseLines = selectBaseLines(bounds, direction)

        val indexDistances = Array<Pair<Int, Double>>(baseLines.size) { 
            val elem = baseLines[it]
            Pair(it, elem.distance(motionPoint))
        }
    
        indexDistances.sortBy {it.second}
        
        return LinesParam(baseLines, indexDistances[0].first)
    }


    /**
     * select base line
     */
    fun selectBaseLine(
        point: DoubleArray,
        direction: Direction): LinesParam {
        return selectBaseLineFromBounds(pageBounds, point, direction)
    }

    /**
     * reate motion base parameter
     */
    fun createMotionbaseParam(
        point: DoubleArray): MotionbaseParam {
        return createMotionbaseParam(
            point,
            getContainerDirection(foldingSpace!!))
    }
 

    
    /**
     * reate motion base parameter
     */
    fun createMotionbaseParam(
        point: DoubleArray,
        direction: Direction): MotionbaseParam  {
        val linesParam = selectBaseLine(point, direction)

        val disIdxList = ArrayList<Pair<Double, Int>>()
        for (idx in 0 until 2) {
            val dis = linesParam.baseLine[idx].distance(point)
            disIdxList.add(Pair(dis, idx))
        } 

        disIdxList.sortBy { it.first }

        val idx = disIdxList.first().second

        return MotionbaseParam(linesParam, idx)
    }

    /**
     * create base point curve
     */
    fun createBasePointCurve(
        motionbaseParam: MotionbaseParam,
        pointStart: DoubleArray): Bezier {

        val startIdx = motionbaseParam.startIndex
        val endIdx = motionbaseParam.endIndex
        val dir = pointStart - motionbaseParam.linesParam.baseLine[startIdx]

        val cp1 = pointStart + dir
        val cp2 = motionbaseParam.linesParam.baseLine[endIdx]
        val cp3 = motionbaseParam.linesParam.baseLine[endIdx] 
        return Bezier(pointStart, cp1, cp2, cp3)
    }

    
    /**
     * calcuate traslate and rotation
     */
    fun calcTranslateAndRotation(
        point: DoubleArray,
        motionbaseParam: MotionbaseParam): Pair<DoubleArray, Double> {

        val baseLine = motionbaseParam.linesParam.baseLine
        val startPoint = baseLine[motionbaseParam.startIndex]
        val endPoint = baseLine[motionbaseParam.endIndex]

        val rel = point - startPoint
        

        val alpha = kotlin.math.atan2(rel[0], rel[1])

        println("endPoint: $endPoint")
        val middle = doubleArrayOf(endPoint[0] - rel[0] / 2.0, rel[1] / 2.0 )
        println("middle: $middle")

        val gamma = alpha - kotlin.math.atan2(middle[1], middle[0])
       
        val distance = kotlin.math.sin(gamma) * middle.distance                

        val tr = doubleArrayOf(distance * kotlin.math.sin(alpha),
            distance * kotlin.math.cos(alpha)) 
        return Pair(tr, alpha)
    } 


  
    /**
     * set pages
     */
    fun setPages(
        elements: Array<HTMLElement>) {
        foldingSpace?.let { 
            setPages(it, elements)
        }
    }    

    /**
     * add pages
     */
    @Suppress("UNUSED_PARAMETER")
    fun setPages(
        container: HTMLElement,
        elements: Array<HTMLElement>) {

        contents.clear()
        
        elements.forEach {
            contents.add(it)
        }
    }


    /**
     * get container direction
     */
    fun getContainerDirection(container: HTMLElement): Direction {
        return Direction.valueOf(container.dataset["direction"]!!)
    }
    

    /**
     * create folding space
     */
    fun createFoldingSpace(
        container: HTMLElement,
        direction: Direction) {

        val pageParams = arrayOf(
            arrayOf(0, "back"), 
            arrayOf(5, "front"))

        pageParams.forEach {
            val className = it[1] as String
            val pages = createSpreadPageElements(container, direction)
            pages.forEach {
                it.classList.add(className)
                container.append(it)
            }
        }
       

        createFoldingElements(container, direction).forEach {
            container.append(it)
        }

        container.append(createShadeContainerElement(container, direction))


        container.dataset["direction"] = direction.toString()
        container.style.overflowX = "hidden"
        container.style.overflowY = "hidden"
        foldingSpace = container
        val resizeObserveCB: 
            (ReadonlyArray<ResizeObserverEntry>, ResizeObserver)->Unit = {
            entries, observer ->
            handleResizeFoldingSpace(entries, observer)
        }

        val resizeObserver = ResizeObserver(resizeObserveCB)

        resizeObserver.observe(container)
        
        this.resizeObserver = resizeObserver
        
    }

    /**
     * detach this object from folding space element
     */
    fun detachFoldingSpace() {
        foldingSpace?.let {
            val rootContainer = it
            resizeObserver?.let {
                it.unobserve(rootContainer)
                resizeObserver = null
            }
            frontFoldingElement.remove()
            backFoldingElement.remove()
            foldingSpace = null
        }
        
    }

    /**
     * create pages
     */
    fun createSpreadPageElements( 
        container: HTMLElement,
        direction :Direction): Array<HTMLElement> {


        val pageParams = arrayOf("first", "second")
                             
        val result = Array<HTMLElement>(pageParams.size) {
            val pageClass = pageParams[it]
            val elem = document.createElement("div") as HTMLElement
            setupFoldingElementStyle(elem)
            elem.classList.add(pageClass)
            elem
        }

        syncSpreadPagesGeometryWithFoldingSpace(
            container, direction, result) 
        return result
    }

    /**
     * synchronize spread pages geometry with folding space
     */
    fun syncSpreadPagesGeometryWithFoldingSpace(
        foldingSpace: HTMLElement,
        direction: Direction,
        spreadPages: Array<HTMLElement>) {
        val pageSize = calcPageSize(foldingSpace, direction)
        val pageParams = if (direction == Direction.HORIZONTAL) {
            arrayOf(
                doubleArrayOf(0.0, 0.0),
                doubleArrayOf(pageSize[0], 0.0))
        } else {
            arrayOf(
                doubleArrayOf(0.0, 0.0),
                doubleArrayOf(0.0, pageSize[1]))
        } 
        pageParams.forEachIndexed {
            index, translation ->    
            val elem = spreadPages[index]
            elem.style.width = "${pageSize[0]}px"
            elem.style.height = "${pageSize[1]}px"
            elem.style.transform = 
                "translate(${translation[0]}px, ${translation[1]}px)"
        }
    }

    /**
     * create folding elements
     */
    fun createFoldingElements(
        container: HTMLElement,
        direction: Direction): Array<HTMLElement> {

        val classNames = arrayOf("back", "front")
        
        val result = Array<HTMLElement>(classNames.size) {
            val additionalClassName = classNames[it]
            val elem = document.createElement("div") as HTMLElement
            setupFoldingElementStyle(elem)

            val pageContainer = document.createElement("div") as HTMLElement

            pageContainer.classList.add("content")
            setupFoldingElementStyle(pageContainer)

            elem.append(pageContainer)

            val shadeContainer = document.createElement("div") as HTMLElement
            shadeContainer.classList.add("shade")
            setupFoldingElementStyle(shadeContainer)

            elem.append(shadeContainer)


            elem.classList.add("folding")
            elem.classList.add("$additionalClassName")

            elem.style.display = "none"
            elem
        }
        syncFoldingElementGeometryWithFoldingSpace(
            container, direction, result)
        return result
    }


    /**
     * synchronize folding element geometry with folding space
     */
    fun syncFoldingElementGeometryWithFoldingSpace(
        container: HTMLElement,
        direction: Direction,
        foldingElements: Array<HTMLElement>) {
        val pageSize = calcPageSize(container, direction)

        val maskSize = getFoldingElementSize(
            container, direction)

        foldingElements.forEach {
            val pageContainer = it.querySelector(".content") as HTMLElement
            pageContainer.style.width = "${pageSize[0]}px"
            pageContainer.style.height = "${pageSize[1]}px"

            val shadeContainer = it.querySelector(".shade") as HTMLElement
            shadeContainer.style.width = "${pageSize[0]}px"
            shadeContainer.style.height = "${pageSize[1]}px"

            it.style.width = "${maskSize}px"
            it.style.height = "${maskSize}px"
        }
    }

    /**
     * create shade container element
     */
    fun createShadeContainerElement(
        container: HTMLElement,
        direction: Direction): HTMLElement {
        val result = document.createElement("div") as HTMLElement

        setupFoldingElementStyle(result)
        result.classList.add("shade-container")
        syncShadeContainerGeometryWithFoldingSpace(
            container, direction, result)
        result.style.display = "none"
        return result
    }

    /**
     * synchronize shade container geometry with folding space
     */
    fun syncShadeContainerGeometryWithFoldingSpace(
        container: HTMLElement,
        direction: Direction,
        shadeContainer: HTMLElement) {
        val pageSize = calcPageSize(container, direction)
        shadeContainer.style.width = "${pageSize[0]}px"
        shadeContainer.style.height = "${pageSize[1]}px"
    } 

    /**
     * calculate initial
     */
    fun calcInitialTopMaskPositions(): Array<IntArray> {
        return calcInitialTopMaskPositions( 
            foldingSpace!!,
            getContainerDirection(foldingSpace!!),
            foldingElementSize) 
    }


    /**
     * calculate initial
     */
    fun calcInitialTopMaskPositions(
        container: HTMLElement,
        direction: Direction,
        maskSize: Double): Array<IntArray> {
        val bounds = container.getBoundingClientRect()
        val size = doubleArrayOf(bounds.width, bounds.height)

        val foldingMaskOffset = intArrayOf(
            - kotlin.math.round(maskSize).toInt(),
            kotlin.math.round(size[direction.coordinateSelector]).toInt())
        return if (direction == Direction.HORIZONTAL) {
            val vOffset = - kotlin.math.round(
                (maskSize - size[0]) / 2.0).toInt()
            arrayOf(
                foldingMaskOffset,
                intArrayOf(vOffset, vOffset))
        } else {
            val hOffset = - kotlin.math.round(
                (maskSize - size[1]) / 1.0).toInt()
            arrayOf(
                intArrayOf(hOffset, hOffset),
                foldingMaskOffset)
        }
    }


    /**
     * calculate folding element size
     */
    fun getFoldingElementSize(
        container: HTMLElement,
        direction :Direction): Double {
        val pageSize = calcPageSize(container, direction)

        
       return (pageSize[0].toDouble().pow(2.0) + 
                pageSize[1].toDouble().pow(2.0)).pow(0.5)
    }



    /**
     * setup page container location in folding element
     */
    fun setupPageContainerLocInFoldingElement(
        pageContainer: HTMLElement,
        direction: Direction,
        positionIndex: Int) {
        
        if (direction == Direction.HORIZONTAL)  {
            if (positionIndex == 0) {
                pageContainer.style.right = "0px"
            } else {
                pageContainer.style.left = "0px"  
            }
        } else {
            if (positionIndex == 0) {
                pageContainer.style.bottom = "0px"
            } else {
                pageContainer.style.top = "0px"  
            }
        }
    }


   
    /**
     * setup folding element style
     */
    fun setupFoldingElementStyle(element: HTMLElement) {
        element.style.position = "absolute"
        element.style.overflowX = "hidden"
        element.style.overflowY = "hidden"
    }
 

    /**
     * calculate page size
     */
    fun getPageSize(): DoubleArray? {
        return foldingSpace?.let {
            calcPageSize(it, getContainerDirection(it))
        } 
    }


    /**
     * synchronize view with current page index
     */
    fun syncViewWithPageIndex() {
        foldingSpace?.let {
            
            val pageIndex = this.pageIndex
            
            setBackPageContent(it,
                0, getPageContent(getContentPageIndex(pageIndex - 2)))
            setFrontPageContent(it,
                0, getPageContent(getContentPageIndex(pageIndex))) 
            setFrontPageContent(it,
                1, getPageContent(getContentPageIndex(pageIndex + 1))) 
            setBackPageContent(it,
                1, getPageContent(getContentPageIndex(pageIndex + 3)))
            
        }
    }


    /**
     * index to class name
     */
    fun Int.toClassName(): String {
        return if (this % 2 == 0) {
            "first"
        } else {
            "second"
        }
    }

    /**
     * get front page container
     */
    fun getFrontPageContainer(
        container: HTMLElement,
        index: Int): HTMLElement? {
        val query = ".front.${index.toClassName()}"
        return container.querySelector(query)?.let {
            it as HTMLElement
        }
    }

    /**
     * get back page container
     */
    fun getBackPageContainer(
        container: HTMLElement,
        index: Int): HTMLElement? {
        val query = ".back.${index.toClassName()}"
        return container.querySelector(query)?.let {
            it as HTMLElement
        }
    }


    /**
     * folding page container
     */
    fun getFrontFoldingElement(
        container: HTMLElement): HTMLElement? {
        return container.querySelector(".folding.front")?.let {
            it as HTMLElement
        }
    }

    /**
     * folding page container
     */
    fun getBackFoldingElement(
        container: HTMLElement): HTMLElement? {
        return container.querySelector(".folding.back")?.let {
            it as HTMLElement
        }
    }

    /**
     * folding page container
     */
    fun getPageContainerFromFrontFoldingElement(
        container: HTMLElement): HTMLElement? {

        return getFrontFoldingElement(container)?.let {
            it.querySelector(".content")?.let {
                it as HTMLElement
            }
        }
    }

    /**
     * folding page container
     */
    fun getPageContainerFromBackFoldingElement(
        container: HTMLElement): HTMLElement? {

        return getBackFoldingElement(container)?.let {
            it.querySelector(".content")?.let { 
                it as HTMLElement
            }
        }
    }

    /**
     * folding shade container
     */
    fun getShadeContainerFromFrontFoldingElement(
        container: HTMLElement): HTMLElement? {

        return getFrontFoldingElement(container)?.let {
            it.querySelector(".shade")?.let {
                it as HTMLElement
            }
        }
    }

    /**
     * folding page container
     */
    fun getShadeContainerFromBackFoldingElement(
        container: HTMLElement): HTMLElement? {

        return getBackFoldingElement(container)?.let {
            it.querySelector(".shade")?.let { 
                it as HTMLElement
            }
        }
    }


    /**
     * get page contaner in folding element
     */
    fun getFrontFoldingPageContainer(): HTMLElement {
        return getPageContainerFromFrontFoldingElement(
            foldingSpace!!)!!
    }

    /**
     * get page contaner in folding element
     */
    fun getBackFoldingPageContainer(): HTMLElement {
        return getPageContainerFromBackFoldingElement(
            foldingSpace!!)!!
    }

    /**
     * get shade container in folding element
     */
    fun getFrontFoldingShadeContainer(): HTMLElement {
        return getShadeContainerFromFrontFoldingElement(
            foldingSpace!!)!!
    }

    /**
     * get shade container in folding element
     */
    fun getBackFoldingShadeContainer(): HTMLElement {
        return getShadeContainerFromBackFoldingElement(
            foldingSpace!!)!!
    }

    
    /**
     * back shade element
     */
    fun getBackShadeElement(container: HTMLElement): HTMLElement? {
        return container.querySelector(".shade-container")?.let {
            it as HTMLElement
        }
    }


    /**
     * set front folding page content
     */
    fun setFrontFoldingPageContent(
        content: HTMLElement?) {
        setFrontFoldingPageContent(foldingSpace!!, content)
    }


    /**
     * set front folding page content
     */
    fun setFrontFoldingPageContent(
        container: HTMLElement,
        content: HTMLElement?) {

        getPageContainerFromFrontFoldingElement(container)?.let {
            val pageContainer = it
            while (pageContainer.childElementCount > 0) {
                pageContainer.lastElementChild?.remove()
            } 
            content?.let {
                pageContainer.append(content)
            }
        }
    }

    /**
     * set back folding page content
     */
    fun setBackFoldingPageContent(
        content: HTMLElement?) {
        setBackFoldingPageContent(foldingSpace!!, content)
    }


    /**
     * set back folding page content
     */
    fun setBackFoldingPageContent(
        container: HTMLElement,
        content: HTMLElement?) {

        getPageContainerFromBackFoldingElement(container)?.let {
            val pageContainer = it
            while (pageContainer.childElementCount > 0) {
                pageContainer.lastElementChild?.remove()
            } 
            content?.let {
                pageContainer.append(it)
            }
        }
    }


    /**
     * set front page content
     */
    fun setFrontPageContent(
        container: HTMLElement,
        index: Int,
        content: HTMLElement?) {
        getFrontPageContainer(container, index)?.let {
            val pageContainer = it
            while (pageContainer.childElementCount > 0) {
                pageContainer.lastElementChild?.remove()
            }
            content?.let {
                pageContainer.append(it)
            }
        }
    }

    /**
     * set back page content
     */
    fun setBackPageContent(
        container: HTMLElement,
        index: Int,
        content: HTMLElement?) {
        getBackPageContainer(container, index)?.let {
            val pageContainer = it
            while (pageContainer.childElementCount > 0) {
                pageContainer.lastElementChild?.remove()
            }
            content?.let {
                pageContainer.append(it)
            }
        }
    }



    /**
     * create content element
     */
    fun createContent(url: URL): HTMLElement {
        val result = document.createElement("div") as HTMLElement
        result.style.backgroundImage = "url(${url})"
        getPageSize()?.let {
            result.style.width = "${it[0]}px"
            result.style.height = "${it[1]}px"
        }
        return result
    }

    /**
     * prepare flipping motion
     */
    fun prepareFlipping(
        motionbaseParam: MotionbaseParam) {
        if (motionbaseParam.startIndex == 0) {
            setBackFoldingPageContent(
                getPageContent(
                    getContentPageIndex(pageIndex)))
            setFrontFoldingPageContent(
                getPageContent(
                    getContentPageIndex(pageIndex - 1))) 

        } else {
            setBackFoldingPageContent(
                getPageContent(
                    getContentPageIndex(pageIndex + 1)))
            setFrontFoldingPageContent(
                getPageContent(
                    getContentPageIndex(pageIndex + 2))) 
        }
        prepareBackShadeForFlipping(motionbaseParam)

    }

    /**
     * prepare back shade for flipping
     */
    fun prepareBackShadeForFlipping(
        motionbaseParam: MotionbaseParam) {
        val pageSize = calcPageSize(foldingSpace!!, direction)
        val backShade = backShadeElement

        val translate = if (direction == Direction.HORIZONTAL) {
            doubleArrayOf(
                pageSize[0] * motionbaseParam.startIndex,
                0.0)
        } else {
            doubleArrayOf(
                0.0,
                pageSize[1] * motionbaseParam.startIndex)
        }
        
        backShade.style.transform = 
            "translate(${translate[0]}px, ${translate[1]}px)"
    }

    /**
     * set flipping status at a point
     */
    fun flippingPage(
        point: DoubleArray, 
        motionbaseParam: MotionbaseParam) {
        val baseLine = motionbaseParam.linesParam.baseLine
        val startPoint = baseLine[motionbaseParam.startIndex]

        val rel = point - startPoint
        val fcr0 = kotlin.math.atan2(rel[1], rel[0])
        val fcr = calcFoldingTopMaskRotation(motionbaseParam, fcr0)

        val fcbLoc = calcFoldingTopMaskBaseLocation(
            motionbaseParam.linesParam.baseLine,
            startPoint + rel.scale(0.5), fcr)


        val maskCenter = getFoldingTopMaskCenter(
            motionbaseParam.linesParam.index) 

        val maskCorner = getFoldingTopMaskCorner(
            motionbaseParam) 

        
        val transform0 = Matrix(tx = maskCenter[0],
            ty = maskCenter[1]) *
        Matrix.rotationMatrix(fcr) *
        Matrix(tx = - maskCenter[0],
            ty = - maskCenter[1])

        val cornerTransed = transform0.apply(
            maskCorner[0], maskCorner[1])

        val transform1Rot = Matrix.rotationMatrix(fcr) 

        val transform1 = 
            Matrix(tx = (fcbLoc[0] - cornerTransed[0]),
                ty = (fcbLoc[1] - cornerTransed[1])) *
            transform1Rot 

        val frontFoldingElement = this.frontFoldingElement
        val backFoldingElement = this.backFoldingElement

        frontFoldingElement.style.transform = transform1.css
        backFoldingElement.style.transform = transform1.css

        val frontPageContainer = getFrontFoldingPageContainer()
        val backPageContainer = getBackFoldingPageContainer()

        val frontShadeContainer = getFrontFoldingShadeContainer()
        val backShadeContainer = getBackFoldingShadeContainer()




        val pageRt = -fcr 
        val frontPageTr = point - fcbLoc


        updatePageContainerCornerLocation(
            frontPageContainer, motionbaseParam)
        updatePageContainerTransformOrigin(
            frontPageContainer, motionbaseParam)

        updatePageContainerCornerLocation(
            backPageContainer, motionbaseParam)
        updatePageContainerTransformOrigin(
            backPageContainer, motionbaseParam)

        updatePageContainerCornerLocation(
            frontShadeContainer, motionbaseParam)
        updatePageContainerTransformOrigin(
            frontShadeContainer, motionbaseParam)

        updatePageContainerCornerLocation(
            backShadeContainer, motionbaseParam)
        updatePageContainerTransformOrigin(
            backShadeContainer, motionbaseParam)
    
     
        val fRotStr0 = "rotate(${pageRt}rad)"
        val fTrsStr  = "translate(${frontPageTr[0]}px, ${frontPageTr[1]}px)"
        val fRotStr1 = "rotate(${2 * fcr0}rad)"
 
        frontPageContainer.style.transform = "$fRotStr0 $fTrsStr $fRotStr1"
        frontShadeContainer.style.transform = "$fRotStr0 $fTrsStr $fRotStr1"
        
        val backPageTr = baseLine.pointAt(0.5) - fcbLoc

        val bRotStr0 = "rotate(${pageRt}rad)"
        val bTrsStr  = "translate(${backPageTr[0]}px, ${backPageTr[1]}px)"
        backPageContainer.style.transform = "$bRotStr0 $bTrsStr"
        backShadeContainer.style.transform = "$bRotStr0 $bTrsStr"

        visibleFoldingElement = true
        
        flippingShade0(point, motionbaseParam, fcr0, fcr, fcbLoc)
        flippingShade1(point, motionbaseParam, fcr0, fcr, fcbLoc)
    }


    /**
     * render front flipping shade
     */
    fun flippingShade0(
        point: DoubleArray,
        motionbaseParam: MotionbaseParam,
        fcr0: Double,
        fcr: Double,
        fcbLoc: DoubleArray) {

        val frontShade = frontFoldingShadeElement
        val pageSize = calcPageSize(foldingSpace!!, direction)
        val d = (pageSize[0].pow(2.0) + pageSize[1].pow(2.0)).pow(0.5)

        val hbcr =  calcHeightBaseCenterRotation(motionbaseParam)
        val coordEnd = kotlin.math.abs(d * kotlin.math.sin(fcr0 + hbcr))

        val baseLine = motionbaseParam.linesParam.baseLine
        val startPoint = baseLine[motionbaseParam.startIndex]
        val rel = point - startPoint
        val shadeEnd = rel.scale(0.5).distance

        val rotOrigin = getRotationOriginCoordinate(motionbaseParam)

        val shadePageCenter = doubleArrayOf(
            pageSize[0] / 2.0, pageSize[1] / 2.0)

        val shadeStart = kotlin.math.max(shadeEnd - 100.0, 0.0) 
        val shadeMiddle = (shadeEnd - shadeStart) * 0.8 + shadeStart

        val opacity = calcGradientOpacity(point, motionbaseParam)
        val opacityMiddle = 0.2 * opacity
        val opacityEnd = 0.2 * opacity
        val colors = arrayOf(
            arrayOf("rgba(0, 0, 0, 0)", "0px"),
            arrayOf("rgba(0, 0, 0, 0)", "${shadeStart}px"),
            arrayOf("rgba(0, 0, 0, ${opacityMiddle})", 
                "${shadeMiddle}px"),
            arrayOf("rgba(0, 0, 0, ${opacityEnd})", 
                "${shadeEnd}px"),
            arrayOf("rgba(0, 0, 0, 0)", "${shadeEnd}px 100%"),
        )
        val colorStr = colors.joinToString(
            transform = {
                it.joinToString(" ")
        })
        val angle = calcFrontShadeRotation(fcr0, motionbaseParam)

        val angleStr = "${angle * 180.0/ kotlin.math.PI}deg"
        val gradient = "linear-gradient(${angleStr}, $colorStr)"
        
        frontShade.style.backgroundImage = gradient
    
        frontShade.style.display = "block"
    }

    /**
     * render front flipping shade
     */
    fun flippingShade1(
        point: DoubleArray,
        motionbaseParam: MotionbaseParam,
        fcr0: Double,
        fcr: Double,
        fcbLoc: DoubleArray) {

        val backShade = backShadeElement
        val pageSize = calcPageSize(foldingSpace!!, direction)
        val d = (pageSize[0].pow(2.0) + pageSize[1].pow(2.0)).pow(0.5)

        val hbcr =  calcHeightBaseCenterRotation(motionbaseParam)
        val coordEnd = kotlin.math.abs(d * kotlin.math.sin(fcr0 + hbcr))

        val baseLine = motionbaseParam.linesParam.baseLine
        val startPoint = baseLine[motionbaseParam.startIndex]
        val rel = point - startPoint
        val shadeEnd = rel.scale(0.5).distance
        val rotOrigin = getRotationOriginCoordinate(motionbaseParam)

        val shadePageCenter = doubleArrayOf(
            pageSize[0] / 2.0, pageSize[1] / 2.0)

        val opacity = calcGradientOpacity(point, motionbaseParam)
        val opacityMiddle = 0.3 * opacity
        val opacityEnd = 0.3 * opacity

        val shadeStart = shadeEnd * 0.8 
        val shadeMiddle = shadeEnd

 
        val colors = arrayOf(
            arrayOf("rgba(0, 0, 0, 0)", "0px"),
            arrayOf("rgba(0, 0, 0, 0)",
                "${shadeStart}px"),
            arrayOf("rgba(0, 0, 0, ${opacityMiddle})", 
                "${shadeMiddle}px"),
            arrayOf("rgba(0, 0, 0, ${opacityEnd})", 
                "${shadeEnd}px"),
            arrayOf("rgba(0, 0, 0, 0)", "${shadeEnd}px 100%"),
        )
        val colorStr = colors.joinToString(
            transform = {
                it.joinToString(" ")
        })
        val angle = calcBackShadeRotation(fcr0, motionbaseParam)

        val gradient = "linear-gradient(${angle}rad, $colorStr)"
        
        backShade.style.backgroundImage = gradient
    
        backShade.style.display = "block"
    }



    /**
     * update page container corner location
     */
    fun updatePageContainerCornerLocation(
        pageContainer: HTMLElement,
        motionbaseParam: MotionbaseParam) {

        pageContainer.style.removeProperty("left")
        pageContainer.style.removeProperty("top")
        pageContainer.style.removeProperty("right")
        pageContainer.style.removeProperty("bottom")
                     

        if (direction == Direction.HORIZONTAL) {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.right = "0px"
                    pageContainer.style.top = "0px"
                    
                } else {
                    pageContainer.style.left = "0px"
                    pageContainer.style.top = "0px"
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.right = "0px"
                    pageContainer.style.bottom = "0px"
                } else {
                    pageContainer.style.left = "0px"
                    pageContainer.style.bottom = "0px"
                }            
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.left = "0px"
                    pageContainer.style.bottom = "0px"
                } else {
                    pageContainer.style.right = "0px"
                    pageContainer.style.bottom = "0px"
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.top = "0px"
                    pageContainer.style.left = "0px"
                } else {
                    pageContainer.style.right = "0px"
                    pageContainer.style.top = "0px"
                }            
            }
        }
    }
    /**
     * update page container transform origin
     */
    fun updatePageContainerTransformOrigin(
        pageContainer: HTMLElement,
        motionbaseParam: MotionbaseParam) {

        if (direction == Direction.HORIZONTAL) {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.transformOrigin = "top right" 
                } else {
                    pageContainer.style.transformOrigin = "top left"
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.transformOrigin = "bottom right"
                } else {
                    pageContainer.style.transformOrigin = "bottom left"
                }            
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.transformOrigin = "bottom left"
                } else {
                    pageContainer.style.transformOrigin = "bottom right"
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    pageContainer.style.transformOrigin = "top left"
                } else {
                    pageContainer.style.transformOrigin = "top right"
                }
            }
        }
    }

    /**
     * rotation origin coordinate
     */
    fun getRotationOriginCoordinate(
        motionbaseParam: MotionbaseParam): DoubleArray {
        val pageSize = calcPageSize(foldingSpace!!, direction)

        return if (direction == Direction.HORIZONTAL) {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    doubleArrayOf(pageSize[0], 0.0)
                } else {
                    doubleArrayOf(0.0, 0.0)
                }
            }  else {
                if (motionbaseParam.startIndex == 0) {
                    pageSize
                } else {
                    doubleArrayOf(0.0, pageSize[1])
                }  
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    doubleArrayOf(0.0, pageSize[1])
                } else {
                    pageSize
                }
            }  else {
                if (motionbaseParam.startIndex == 0) {
                    doubleArrayOf(0.0, 0.0)
                } else {
                    doubleArrayOf(pageSize[0], 0.0)
                }  
            }
        } 
    }

    /**
     * calculate height base center rotation
     */
    fun calcHeightBaseCenterRotation(
        motionbaseParam: MotionbaseParam): Double {
        
        val pageSize = calcPageSize(foldingSpace!!, direction)
        val rot = kotlin.math.atan2(pageSize[0], pageSize[1])

        return if (direction == Direction.HORIZONTAL) {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    rot 
                } else {
                    -rot
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    -rot
                } else {
                    rot
                } 
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    rot 
                } else {
                    -rot
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    -rot 
                } else {
                    rot
                }
            }
        }
    }


    /**
     * calculate shade gradient opacity
     */
    fun calcGradientOpacity(
        point: DoubleArray,
        motionbaseParam: MotionbaseParam): Double {
        val pt = point - 
            motionbaseParam.linesParam.baseLine[motionbaseParam.endIndex]
        
        val pageSize = calcPageSize(foldingSpace!!, direction)
       
        val op0 = if (direction == Direction.HORIZONTAL) { 
            pt.distance / pageSize[0]
        } else {
            pt.distance / pageSize[1]
        }

        return kotlin.math.min(op0, 1.0)
    }

    
    /**
     * calculate front shade rotation
     */

    fun calcFrontShadeRotation(
        foldingCornerRotation: Double,
        motionbaseParam: MotionbaseParam): Double {
        val baseAngle = getLinearGradientRotationBaseFront(motionbaseParam)

        return calcFrontShadeFcr(
            foldingCornerRotation, motionbaseParam) + baseAngle
    }
 
    /**
     * calculate front folding corner rotation 
     */
    fun calcFrontShadeFcr(
        foldingCornerRotation: Double,
        motionbaseParam: MotionbaseParam): Double {
        return if (direction == Direction.HORIZONTAL) {
           if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    - foldingCornerRotation 
                } else {
                    - foldingCornerRotation 
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    - foldingCornerRotation 
                } else {
                    - foldingCornerRotation
                } 
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    foldingCornerRotation
                } else {
                    foldingCornerRotation 
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    foldingCornerRotation 
                } else {
                    foldingCornerRotation 
                } 
            }
        }
    }

    /**
     * calculate back shade rotation
     */

    fun calcBackShadeRotation(
        foldingCornerRotation: Double,
        motionbaseParam: MotionbaseParam): Double {
        val baseAngle = getLinearGradientRotationBaseBack(motionbaseParam)

        return calcBackShadeFcr(
            foldingCornerRotation, motionbaseParam) + baseAngle
    }
 
    /**
     * calculate back shade folding corner rotation
     */
    fun calcBackShadeFcr(
        foldingCornerRotation: Double,
        motionbaseParam: MotionbaseParam): Double {
        return if (direction == Direction.HORIZONTAL) {
           if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    foldingCornerRotation 
                } else {
                    foldingCornerRotation 
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    foldingCornerRotation 
                } else {
                    foldingCornerRotation
                } 
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    foldingCornerRotation
                } else {
                    foldingCornerRotation 
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    foldingCornerRotation 
                } else {
                    foldingCornerRotation 
                } 
            }
        }
    }


    /**
     * get linear gradient rotation base
     */
    fun getLinearGradientRotationBaseFront(
        motionbaseParam: MotionbaseParam): Double {
        
        return if (direction == Direction.HORIZONTAL) {
           if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI * 3.0 / 2.0
                } else {
                    kotlin.math.PI * 3.0 / 2.0  
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI * 3.0 / 2.0
                } else {
                    kotlin.math.PI * 3.0 / 2.0
                } 
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI * 3.0 / 2.0
                } else {
                    kotlin.math.PI / 2.0
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI / 2.0
                } else {
                    kotlin.math.PI * 3.0 / 2.0
                } 
            }
        }
    }

    /**
     * get linear gradient rotation base
     */
    fun getLinearGradientRotationBaseBack(
        motionbaseParam: MotionbaseParam): Double {
        
        return if (direction == Direction.HORIZONTAL) {
           if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI / 2.0
                } else {
                    kotlin.math.PI / 2.0
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI / 2.0
                } else {
                    kotlin.math.PI / 2.0
                } 
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI / 2.0
                } else {
                    kotlin.math.PI / 2.0
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    kotlin.math.PI * 3.0 / 2.0
                } else {
                    kotlin.math.PI * 3.0 / 2.0
                } 
            }
        }
    }



    /**
     * calculate folding container rotation
     */
    fun calcFoldingTopMaskRotation(
        motionbaseParam: MotionbaseParam,
        frcBase: Double): Double {
        val direction = getContainerDirection(foldingSpace!!)
        return if (direction == Direction.HORIZONTAL) {
            if (motionbaseParam.linesParam.index == 0) { 
                if (motionbaseParam.startIndex % 2 == 0) {
                    - kotlin.math.PI / 2.0 + frcBase
                } else {
                    3.0 * kotlin.math.PI / 2.0 + frcBase
                }
            } else {
                if (motionbaseParam.startIndex % 2 == 0) {
                    kotlin.math.PI / 2.0 + frcBase
                } else {
                    - 3.0 * kotlin.math.PI / 2.0 + frcBase                    
                }
            }
        } else {
            frcBase
        }
    }

    /**
     * calculate folding page container rotation
     */
    fun calcFoldingPageContainerRotation(
        motionbaseParam: MotionbaseParam,
        frcBase: Double): Double {
        val direction = getContainerDirection(foldingSpace!!)
        return if (direction == Direction.HORIZONTAL) {
            2 * frcBase  
        } else {
            if (motionbaseParam.linesParam.index == 0) { 
                if (motionbaseParam.startIndex % 2 == 0) {
                    - 2.0 * (kotlin.math.PI / 2.0 - frcBase)
                } else {
                    2.0 * (kotlin.math.PI / 2.0 - frcBase)
                }
            } else {
                if (motionbaseParam.startIndex % 2 == 0) {
                    2.0 * (kotlin.math.PI / 2.0 + frcBase)
                } else {
                    - 2.0 * (kotlin.math.PI / 2.0 - frcBase)
                }
            }
        }
    }
    
    /**
     * calcurate folding container base location
     */
    fun calcFoldingTopMaskBaseLocation(
        baseLine: Line,
        fcCornerPt: DoubleArray, 
        fcr: Double): DoubleArray {

        val center = baseLine.pointAt(0.5)
        
        val lineOnCorner = Line(kotlin.math.tan(fcr), fcCornerPt)
        val lineOnCenter = Line(
            kotlin.math.tan(kotlin.math.PI / 2 + fcr), center)
        
        val matrix = Matrix()
        matrix[0, 0] = lineOnCorner.a
        matrix[0, 1] = lineOnCorner.b
        matrix[0, 2] = lineOnCorner.c

        matrix[1, 0] = lineOnCenter.a
        matrix[1, 1] = lineOnCenter.b
        matrix[1, 2] = lineOnCenter.c
         

        matrix[2, 0] = 0.0
        matrix[2, 1] = 0.0
        matrix[2, 2] = 1.0

        val invMat = matrix.inverse()!!

        return doubleArrayOf(
            invMat[0, 2],
            invMat[1, 2])
        
    }


    /**
     * folding top mask base point
     */
    fun getFoldingTopMaskBasePoint(
        motionbaseParam: MotionbaseParam,
        foldingIndex: Int): DoubleArray {

        return if (foldingIndex % 2 == 0) {
            motionbaseParam.linesParam.baseLine[
                motionbaseParam.startIndex]
        } else {
            motionbaseParam.linesParam.baseLine[
                motionbaseParam.endIndex]
        }
    }
    /**
     * folding top mask center
     */
    fun getFoldingTopMaskCenter(
        index: Int): DoubleArray  {
        val direction = getContainerDirection(foldingSpace!!)
        return getFoldingTopMaskCenter(direction, index)
    } 
 
    /**
     * folding top mask center
     */
    @Suppress("UNUSED_PARAMETER")
    fun getFoldingTopMaskCenter(
        direction: Direction,
        index: Int): DoubleArray  {
        val foldingElementSize = this.foldingElementSize
        
        return doubleArrayOf(
            foldingElementSize / 2.0, foldingElementSize / 2.0)
    }
    /**
     * get folding top mask container
     */
    fun getFoldingTopMaskCorner(
        motionbaseParam: MotionbaseParam): DoubleArray {
        val direction = getContainerDirection(foldingSpace!!)
        return getFoldingTopMaskCorner(direction, motionbaseParam)
    }

    /**
     * get folding top mask container
     */
    fun getFoldingTopMaskCorner(
        direction: Direction,
        motionbaseParam: MotionbaseParam): DoubleArray {
        val foldingElementSize = this.foldingElementSize
        return if (direction == Direction.HORIZONTAL) {
            if (motionbaseParam.linesParam.index == 0) { 
                if (motionbaseParam.startIndex == 0) {
                    doubleArrayOf(foldingElementSize, 0.0)
                } else {
                    doubleArrayOf(0.0, 0.0)
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    doubleArrayOf(foldingElementSize, foldingElementSize)
                } else {
                    doubleArrayOf(0.0, foldingElementSize) 
                }
            }
        } else {
            if (motionbaseParam.linesParam.index == 0) { 
                if (motionbaseParam.startIndex == 0) {
                    doubleArrayOf(0.0, 0.0)
                } else {
                    doubleArrayOf(0.0, foldingElementSize)
                }
            } else {
                if (motionbaseParam.startIndex == 0) {
                    doubleArrayOf(foldingElementSize, 0.0)
                } else {
                    doubleArrayOf(foldingElementSize, foldingElementSize) 
                }
            }
        }
    }
}

// vi: se ts=4 sw=4 et:
