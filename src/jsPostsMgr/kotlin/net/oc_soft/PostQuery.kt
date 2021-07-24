package net.oc_soft
/**
 * post query
 */
data class PostQuery(
	var page: Int = 1,
	var lineCountPerPage: Int = 10,
    var categorySlug: String? = null,
    var tagNames: Array<String> = Array<String>(0) {""} ) 

// vi: se ts=4 sw=4 et:
