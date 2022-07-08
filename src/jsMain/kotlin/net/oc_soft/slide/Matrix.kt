package net.oc_soft.slide



/**
 * css matrix 
 */
class Matrix(
    a: Double = 1.0,
    b: Double = 0.0,
    c: Double = 0.0,
    d: Double = 1.0,
    tx: Double = 0.0,
    ty: Double = 0.0) {

    /**
     * class instance
     */
    companion object {

        /**
         * rotation matrix
         */
        fun rotationMatrix(radian: Double):Matrix {
            val cos = kotlin.math.cos(radian)
            val sin = kotlin.math.sin(radian)
            return Matrix(cos, sin, -sin, cos)
        }
    }

    /**
     * matrix components
     */
    val components = doubleArrayOf(
        a, c, tx,
        b, d, ty,
        0.0, 0.0, 1.0)


    /**
     * component 0 0
     */
    var a: Double
        get() = this[0, 0]

        set(value: Double) {
            this[0, 0] = value
        }
    /**
     * component 1 0
     */
    var b: Double
        get() = this[1, 0]
        set(value: Double) {
            this[1, 0] = value
        }

    /**
     * component 0 1
     */
    var c: Double
        get() = this[0, 1]
        set(value: Double) {
            this[0, 1] = value
        }
    /**
     * component 1 1
     */
    var d: Double
        get() = this[1, 1]
        set(value: Double) {
            this[1, 1] = value
        }

    /**
     * component 0 2
     */
    var tx: Double
        get() = this[0, 2]
        set(value: Double) {
            this[0, 2] = value
        }
    /**
     * component 1 2
     */
    var ty: Double
        get() = this[1, 2]
        set(value: Double) {
            this[1, 2] = value
        }

    /**
     * css string representation
     */
    val css: String get() = "matrix($a, $b, $c, $d, $tx, $ty)"
        

    /**
     * calculate determinant
     */
    val determinant: Double 
        get() {
            var result = 0.0
            for (cidx in 0 until 3) {
                result += this[0, cidx] * cofactor(0, cidx)
            }
            return result
        } 
    /**
     * constructor
     */
    constructor(other: Matrix) : 
        this(other.a, other.b, other.c, other.d, other.tx, other.ty)

    /**
     * index access
     */
    operator fun get(row:Int, col:Int): Double {
        return components[row * 3 + col]
    }


    /**
     * index access
     */
    operator fun set(row:Int, col:Int, value: Double) {
        components[row * 3 + col] = value
    }

    /**
     * plus 
     */
    operator fun plus(other: Matrix): Matrix {
        val result = Matrix(this)
        for (rIdx in 0 until 3) {
            for (cIdx in 0 until 3) {
                result[rIdx, cIdx] = this[rIdx, cIdx] + other[rIdx, cIdx]
            }
        }
        return result
    }


    /**
     * multiply
     */
    operator fun times(other: Matrix): Matrix {
        val result = Matrix(this)

        for (rIdx in 0 until 3) {
            for (cIdx in 0 until 3) {
                var compVal = 0.0
                for (cIdx0 in 0 until 3) {
                    compVal += this[rIdx, cIdx0] * other[cIdx0, cIdx]
                }
                result[rIdx, cIdx] = compVal     
            }
        }
        return result
    }

    /**
     * plus 
     */
    operator fun plusAssign(other: Matrix): Unit{
        for (rIdx in 0 until 3) {
            for (cIdx in 0 until 3) {
                this[rIdx, cIdx] += other[rIdx, cIdx]
            }
        }
    }


    /**
     * multiply
     */
    operator fun timesAssign(other: Matrix): Unit {
        val tmp = Matrix(this)

        for (rIdx in 0 until 3) {
            for (cIdx in 0 until 3) {
                var compVal = 0.0
                for (cIdx0 in 0 until 3) {
                    compVal += this[rIdx, cIdx0] * other[cIdx0, cIdx]
                }
                tmp[rIdx, cIdx] = compVal     
            }
        }
        for (rIdx in 0 until 3) {
            for (cIdx in 0 until 3) {
                this[rIdx, cIdx] = tmp[rIdx, cIdx]
            }
        }
    }

    /**
     * calculate cofactor
     */
    fun cofactor(
        i: Int,
        j: Int): Double {
        val mat = DoubleArray(4) { 0.0 }
        for (rIdx in 0 until 3) {
            val row = if (rIdx < i) {
                rIdx
            } else if (rIdx > i) {
                rIdx  - 1 
            } else null
            if (row != null) {
                for (cIdx in 0 until 3) {
                    val col = if (cIdx < j) {
                        cIdx 
                    } else if (cIdx > j) {
                        cIdx - 1
                    } else null
                    if (col != null) {
                        mat[row * 2 + col] = this[rIdx, cIdx]
                    }
                } 
            }
        }
        val sign = if ((i + j) % 2 == 0) { 1 } else { -1 }
        return sign.toDouble() * (mat[2 * 0 + 0] *  mat[2 * 1 + 1] - 
            mat[2 * 0 + 1] * mat[2 * 1 + 0])
    }

    /**
     * calculate inverse matrix
     */
    fun inverse(): Matrix? {
        val determinant = this.determinant
        return if (determinant != 0.0) {

            val mat = Matrix()
            for (rIdx in 0 until 3) {
                for (cIdx in 0 until 3) {
                    mat[cIdx, rIdx] = cofactor(rIdx, cIdx) / determinant
                }
            }
            mat
        } else null
    }

    /**
     * apply vector
     */
    fun apply(
        x: Double,
        y: Double): DoubleArray {
        return apply(doubleArrayOf(x, y, 1.0))
    }
 
    /**
     * apply vector
     */
    fun apply(
        vector: DoubleArray): DoubleArray {
        return DoubleArray(3) {
            this[it, 0] * vector[0] +
                this[it, 1] * vector[1] +
                    this[it, 2] * vector[2]
        }
    }


    /**
     *  convert css string
     */
    fun toCssString(tolerance: Double): String {

        val components = doubleArrayOf(a, b, c, d, tx, ty)

        for (idx in components.indices) {
            var value = components[idx]
            if (kotlin.math.abs(value) <= tolerance) {
                value = 0.0
            }
            components[idx] = value
        } 
        return "matrix(${components.joinToString()})"

    }
}

// vi: se ts=4 sw=4 et:
