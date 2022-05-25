package net.oc_soft.slide

import kotlin.js.JSON

import kotlin.math.pow
import kotlin.collections.MutableList
import kotlin.collections.ArrayList

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.HTMLElement

import org.w3c.dom.url.URL

import org.w3c.dom.get
import org.w3c.dom.set



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
        fun calcPointAtT(
            curve: Bezier,
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
            
            return DoubleArray(curve.p0.size) {
                var cmp = a * curve.p0[it]
                cmp += b * curve.p1[it]
                cmp += c * curve.p2[it] 
                cmp += d * curve.p3[it] 
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
     * spread pages 
     */
    var pagesView: HTMLElement? = null


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
     * folding element size
     */
    val foldingElementSize: Double
        get() {
            return getFoldingElementSize(foldingSpace!!,
                getContainerDirection(foldingSpace!!)) 
        }


    /**
     * folding space element
     */
    val foldingSpaceSize: DoubleArray
        get() {
            val bounds = foldingSpace!!.getBoundingClientRect()
            return doubleArrayOf(bounds.width, bounds.height)
        }

    
    /**
     * page bounds
     */
    val pageBounds: Array<DoubleArray>
        get() {
            val spaceSize = foldingSpaceSize
            return arrayOf(
                doubleArrayOf(0.0, 0.0),
                doubleArrayOf(spaceSize[0], 0.0),
                doubleArrayOf(spaceSize[0], spaceSize[1]),
                doubleArrayOf(0.0, spaceSize[1]))
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
     * get half page size
     */
    fun getHalfPageSize(direction: Direction): DoubleArray {
        
        val result = foldingSpaceSize 

        result[direction.crossDirection.coordinateSelector] /= 2.0
        return result
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

        val pages = createSpreadPageElements(container, direction)
        pages.forEach {
            container.append(it)
        }
        val foldingElements = createFoldingElements(container, direction)
        foldingElements.forEach {
            container.append(it)
        }
        container.dataset["direction"] = direction.toString()
        container.style.overflowX = "hidden"
        container.style.overflowY = "hidden"
        foldingSpace = container
    }

    /**
     * create pages
     */
    fun createSpreadPageElements( 
        container: HTMLElement,
        direction :Direction): Array<HTMLElement> {

        val pageSize = calcPageSize(container, direction)

        val pageOffset = if (direction == Direction.HORIZONTAL) {
            arrayOf(
                intArrayOf(0, pageSize[0]),
                intArrayOf(0, 0))
        } else {
            arrayOf(
                intArrayOf(0, 0),
                intArrayOf(0, pageSize[1]))
        }
         
        return Array<HTMLElement>(pageOffset.size) {
            val elem = document.createElement("div") as HTMLElement
            setupFoldingElementStyle(elem)
            elem.style.width = "${pageSize[0]}px"
            elem.style.height = "${pageSize[1]}px"
            elem.style.transform = 
                "translate(${pageOffset[0][it]}px, ${pageOffset[1][it]}px)"
            elem
        }
    }


    /**
     * create folding elements
     */
    fun createFoldingElements(
        container: HTMLElement,
        direction :Direction): Array<HTMLElement> {


        val pageSize = calcPageSize(container, direction)

        val maskSize = getFoldingElementSize(
            container, direction)


        val pageOffset =  calcInitialTopMaskPositions(
            container, direction, maskSize) 

        return Array<HTMLElement>(pageOffset.size) {
            val elem = document.createElement("div") as HTMLElement
            setupFoldingElementStyle(elem)

            val pageContainer = document.createElement("div") as HTMLElement

            setupFoldingElementStyle(pageContainer)

            pageContainer.style.width = "${pageSize[0]}px"
            pageContainer.style.height = "${pageSize[1]}px"

            elem.append(pageContainer)
            // shadow gradient container
            elem.style.width = "${maskSize}px"
            elem.style.height = "${maskSize}px"
            elem.style.transform =
                "translate(${pageOffset[0][it]}px, ${pageOffset[1][it]}px)" 
            elem
        }
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
    fun getPageSize(): IntArray? {
        return foldingSpace?.let {
            calcPageSize(it, getContainerDirection(it))
        } 
    }


    /**
     * calculate page size
     */
    fun calcPageSize(
        container: HTMLElement,
        direction: Direction): IntArray {
        val bounds = container.getBoundingClientRect()
        
        return if (direction == Direction.HORIZONTAL) { 
            intArrayOf(
                kotlin.math.round(bounds.width / 2.0).toInt(),
                kotlin.math.round(bounds.height).toInt()) 
        } else {
            intArrayOf(
                kotlin.math.round(bounds.width).toInt(),
                kotlin.math.round(bounds.height / 2.0).toInt())
        }
    }

    /**
     * synchronize view with current page index
     */
    fun syncViewWithPageIndex() {
        if (this.pageIndex >= 0 && this.pageIndex < pageCount - 1) {
            foldingSpace?.let {
                if (pageIndex > 0) {
                    setFoldingPageContent(
                        it, 0, contents[pageIndex - 1])
                }
                setPageContent(it,
                    0, contents[pageIndex]) 
                setPageContent(it,
                    1, contents[pageIndex + 1]) 
                if (pageIndex + 2 <  pageCount) {
                    setFoldingPageContent(
                        it, 1, contents[pageIndex + 2])
                }
            }
        }
    }


    /**
     * get page container
     */
    fun getPageContainer(
        container: HTMLElement,
        index: Int): HTMLElement? {

        return if (container.childElementCount > 2) {
            if (0 <= index && index < 2) {
                container.children[index] as HTMLElement
            } else null
        } else null
    }

    /**
     * get top mask element
     */
    fun getFoldingElement(
        index: Int): HTMLElement {
        return getFoldingElement(foldingSpace!!, index)!!
    }

    /**
     * folding page container
     */
    fun getFoldingElement(
        container: HTMLElement,
        index: Int): HTMLElement? {
        
        return if (container.childElementCount > 3) {
            if (0 <= index  && index < 2) {
                container.children[2 + index] as HTMLElement
            } else null
        } else null
    }


    

    /**
     * folding page container
     */
    fun getPageContainerFromFoldingElement(
        container: HTMLElement,
        index: Int): HTMLElement? {

        return getFoldingElement(container, index)?.let {
            if (it.childElementCount > 0) {
                it.children[0] as HTMLElement
            } else null
        }
    }

    /**
     * get page contaner in folding element
     */
    fun getFoldingPageContainer(
        index: Int): HTMLElement {
        return getPageContainerFromFoldingElement(
            foldingSpace!!, index)!!
    }

    /**
     * set folding page content
     */
    fun setFoldingPageContent(
        container: HTMLElement,
        index: Int,
        content: HTMLElement) {

        getPageContainerFromFoldingElement(container, index)?.let {
            while (it.childElementCount > 0) {
                it.lastElementChild?.remove()
            } 
            it.append(content)
        }
    }

    /**
     * set page content
     */
    fun setPageContent(
        container: HTMLElement,
        index: Int,
        content: HTMLElement) {
        getPageContainer(container, index)?.let {
            while (it.childElementCount > 0) {
                it.lastElementChild?.remove()
            }
            it.append(content)
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
        val foldingElement = getFoldingElement(
            motionbaseParam.startIndex)

        val pageContainer = getFoldingPageContainer(
            motionbaseParam.startIndex)

        foldingElement.style.transform = transform1.css

        val pageRt = -fcr 
        val pageTr = point - fcbLoc

        updatePageContainerCornerLocation(pageContainer, motionbaseParam)
        updatePageContainerTransformOrigin(pageContainer, motionbaseParam)

        val rotStr0 = "rotate(${pageRt}rad)"
        val trsStr  = "translate(${pageTr[0]}px, ${pageTr[1]}px)"
        val rotStr1 = "rotate(${2 * fcr0}rad)"
        pageContainer.style.transform = "$rotStr0 $trsStr $rotStr1"
     }

    /**
     * update page container corner location
     */
    fun updatePageContainerCornerLocation(
        pageContainer: HTMLElement,
        motionbaseParam: MotionbaseParam) {

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
