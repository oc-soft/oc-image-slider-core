package net.oc_soft.slide

import kotlin.test.Test

/**
 * matrix test
 */
class MatrixTest {



    @Test
    fun test1() {

        val matrix = Matrix()
        matrix[0, 0] = 1.0
        matrix[0, 1] = 2.0
        matrix[0, 2] = -1.0
        
        matrix[1, 0] = 2.0
        matrix[1, 1] = 1.0
        matrix[1, 2] = 2.0

        matrix[2, 0] = -1.0
        matrix[2, 1] = 2.0
        matrix[2, 2] = 1.0

        kotlin.test.expect(true,
            "unexpected determinant") { 0.0001 > (-16.0 - matrix.determinant) }

        val inverseMat = matrix.inverse()

        kotlin.test.expect(true,
            "You must have an inverse matrix.") { inverseMat != null }

        inverseMat?.let {
            val expectMat = Matrix()    
            expectMat[0, 0] = 3.0 / 16.0
            expectMat[0, 1] = 1.0 / 4.0
            expectMat[0, 2] = -5.0 / 16.0

            expectMat[1, 0] = 1.0 / 4.0
            expectMat[1, 1] = 0.0
            expectMat[1, 2] = 1.0 / 4.0

            expectMat[2, 0] = -5.0 / 16.0
            expectMat[2, 1] = 1.0 / 4.0
            expectMat[2, 2] = 3.0 / 16.0 

            for (rIdx in 0 until 3) {
                for (cIdx in 0 until 3) {
                    kotlin.test.expect<Boolean>(true,
                        "Unexpected value in inverse matrix") {
                         0.0001 > kotlin.math.abs(expectMat[rIdx, cIdx]
                            - inverseMat[rIdx, cIdx])
                    }
                }
            }
        }
    }


    @Test
    fun test2() {

        val mat = Matrix()

        mat[0, 0] = -3.0
        mat[0, 1] = 1.0
        mat[0, 2] = -1.0

        mat[1, 0] = 1.0
        mat[1, 1] = 2.0
        mat[1, 2] = -6.0

        mat[2, 0] = 0.0
        mat[2, 1] = 0.0
        mat[2, 2] = 1.0
        
        
        val invMat = mat.inverse()!!
        val res = doubleArrayOf(
            invMat[0, 0] * 0.0 + invMat[0, 1] * 0.0 + invMat[0, 2] * 1.0,
            invMat[1, 0] * 0.0 + invMat[1, 1] * 0.0 + invMat[1, 2] * 1.0,
            invMat[2, 0] * 0.0 + invMat[2, 1] * 0.0 + invMat[2, 2] * 1.0)

        kotlin.test.expect(
            true,
            "unexpected result") {
            0.0001 > kotlin.math.abs(4.0 / 7.0 - res[0])
        }

        kotlin.test.expect(
            true,
            "unexpected result") {
            0.0001 > kotlin.math.abs(19.0 / 7.0 - res[1])
        }

        kotlin.test.expect(
            true,
            "unexpected result") {
            0.0001 > kotlin.math.abs(1.0 - res[2])
        }
    }


    @Test
    fun test3() {
        val matA = Matrix()
        matA[0, 0] = 10.0
        matA[0, 1] = 20.0
        matA[0, 2] = 10.0

        matA[1, 0] = 4.0
        matA[1, 1] = 5.0
        matA[1, 2] = 6.0

        matA[2, 0] = 2.0
        matA[2, 1] = 3.0
        matA[2, 2] = 5.0  


        val matB = Matrix()
        matB[0, 0] = 3.0
        matB[0, 1] = 2.0
        matB[0, 2] = 4.0


        matB[1, 0] = 3.0
        matB[1, 1] = 3.0
        matB[1, 2] = 9.0

        matB[2, 0] = 4.0
        matB[2, 1] = 4.0
        matB[2, 2] = 2.0

        
        val expect = Matrix()

        expect[0, 0] = 130.0
        expect[0, 1] = 120.0
        expect[0, 2] = 240.0

        expect[1, 0] = 51.0
        expect[1, 1] = 47.0
        expect[1, 2] = 73.0
        
        expect[2, 0] = 35.0
        expect[2, 1] = 33.0
        expect[2, 2] = 45.0
 
        val res = matA * matB

        for (rIdx in 0 until 3) {
            for (cIdx in 0 until 3) {
                kotlin.test.expect(true,
                    "unexpected value") {
                    0.0001 > kotlin.math.abs(
                        res[rIdx, cIdx] - expect[rIdx, cIdx])
                }
            }
        }
    }
}


// vi: se ts=4 sw=4 et:
