package net.oc_soft

/**
 * paging direction
 */
enum class PagingDirection(
    /**
     * displacement
     */
    val displacement: Int) {
    /**
     * proceed a page forward
     */
    FORWARD(1),
    /**
     * proceed a page backward
     */
    BACKWARD(-1);

    companion object {
        
        /**
         * int to direction
         */
        fun intToDirection(value: Int): PagingDirection {
            return if (value >= 0) {
                FORWARD
            } else {
                BACKWARD
            }
        }
    }

}


// vi: se ts=4 sw=4 et:
