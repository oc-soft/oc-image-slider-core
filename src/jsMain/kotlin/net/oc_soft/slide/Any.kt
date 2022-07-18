package net.oc_soft.slide

/**
 * convert any to integer
 */
fun Any.toInt(): Int {
    return when (this) {
        is String -> toInt()
        is Number -> toInt()
        else -> throw IllegalStateException()
    }
}
/**
 * convert any to boolean
 */
fun Any.toBoolean(): Boolean {
    return when (this) {
        is Number -> toInt() != 0
        is Boolean -> this
        is String -> toBoolean()
        else -> throw IllegalStateException()
    }
}
/**
 * convert to int array
 */
fun Any.toIntArray(): IntArray {
    return when (this) {
        is String -> {
            intArrayOf(this.toInt())
        }
        is Array<*> -> {
            IntArray(this.size) {
                val item = this[it]
                when (item) {
                    is Number -> item.toInt()
                    is String -> item.toInt()
                    else -> throw IllegalArgumentException()
                }
            }
        }
        is Number -> {
            intArrayOf(this.toInt())
        }
        else -> throw IllegalArgumentException()
    }
}


// vi: se ts=4 sw=4 et:
