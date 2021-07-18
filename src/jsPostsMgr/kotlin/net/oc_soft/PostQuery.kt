package net.oc_soft
/**
 * post query
 */
data class PostQuery(
	var page: Int = 1,
	var lineCountPerPage: Int = 10,
    var categorySlug: String? = null) 

// vi: se ts=4 sw=4 et:
