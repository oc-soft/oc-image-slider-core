package net.oc_soft.animation

import kotlin.math.pow

/**
 * qubic bezier
 */
class QubicBezier(
    /**
     * first control point x 
     */
    val x1: Double,
    /**
     * first control point y
     */
    val y1: Double,
    /**
     * second control point x
     */
    val x2: Double,
    /**
     * second control point y
     */
    val y2: Double,
    /**
     * poly line count
     */
    val polyLineCount: Int = 100) {


    /**
     * poly-line
     */
    val polyLine: Array<DoubleArray> by lazy {
        createPolyLine() 
    }

    /**
     * class instance
     */
    companion object {

        /**
         * linear
         */
        val linear = QubicBezier(0.0, 0.0, 1.0, 1.0)

        /**
         * ease
         */
        val ease = QubicBezier(0.25, 0.1, 0.25, 0.1) 

        /**
         * ease in 
         */
        val easeIn = QubicBezier(0.42, 0.0, 1.0, 1.0) 

        /**
         * ease in out
         */
        val easeInOut = QubicBezier(0.42, 0.0, 0.58, 1.0) 

        /**
         * ease out
         */
        val easeOut = QubicBezier(0.0, 0.0, 0.58, 1.0) 

    } 

    /**
     * create polyline
     */
    fun createPolyLine(): Array<DoubleArray> {
        return Array<DoubleArray>(polyLineCount + 1) {
            calcPoint(it.toDouble() / polyLineCount.toDouble())
        }

    }

    /**
     * filter out t 
     */
    operator fun invoke(t: Double): Double {
        return if (0 <= t && t < 1.0) {
            var polyLine: Array<DoubleArray>? = null
            for (idx in 0 until this.polyLine.size - 1) {
                if (this.polyLine[idx][0] <= t 
                    && t < this.polyLine[idx + 1][0]) {
                    polyLine = arrayOf(
                        this.polyLine[idx],
                        this.polyLine[idx + 1])
                    break
                }
            }
            val polyLine0 = polyLine!!
            var t0 = t - polyLine0[0][0]
            t0 /= polyLine0[1][0] - polyLine0[0][0]
            polyLine0[0][1] * (1.0 - t0) + polyLine0[1][1] * t0
        } else {
            if (t < 0) {
                0.0
            } else {
                1.0
            }
        }
    }

    /**
     * calcurate bezier point
     */
    fun calcPoint(t: Double) : DoubleArray {
        return doubleArrayOf(
            calculate(t, 0.0, x1, x2, 1.0),
            calculate(t, 0.0, y1, y2, 1.0))
    }

    /**
     * calcurate single bezier point
     */
    fun calculate(
        t: Double,
        v0: Double, v1: Double, v2: Double, v3: Double): Double {
        var result = t.pow(3.0) * v0

        result += 3 * (1.0 - t).pow(2.0) * t * v1

        result += 3 * (1.0 - t) * t.pow(2.0) * v2

        result += t.pow(3.0) * v3
        return result
 
    }
}


// vi: se ts=4 sw=4 et:
