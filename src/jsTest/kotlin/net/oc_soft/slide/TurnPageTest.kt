package net.oc_soft.slide

import kotlin.test.Test

/**
 * turn page slide test
 */
class TurnPageTest {

    @Test
    fun lineTest1() {
        val line = TurnPage.Line(
            doubleArrayOf(0.0, 1.0), 
            doubleArrayOf(2.0, 0.0))

        kotlin.test.expect(true, "a must be -1 but ${line.a}")  {
            0.001 > kotlin.math.abs(line.a - (-1.0))
        }

        kotlin.test.expect(true, "b must be 2 but ${line.b}")  {
            0.001 > kotlin.math.abs(line.b - (-2.0))
        }
        kotlin.test.expect(true, "c must be -2 but ${line.c}")  {
            0.001 > kotlin.math.abs(line.c - (2.0))
        }
    }
    
        
}

// vi: se ts=4 sw=4 et:
