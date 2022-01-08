package net.oc_soft

import kotlin.js.Json
import kotlin.collections.MutableList
import kotlin.collections.ArrayList

import org.w3c.dom.HTMLElement

/**
 * manage element back-ground
 */
class ElementBackground {


    /**
     * background image generator
     */
    val elementGenerator: MutableList<MutableList<(Array<String>)->String>> =
        ArrayList<MutableList<(Array<String>)->String>>()
    
    /**
     * class instance
     */
    companion object {

        
        /**
         * create image style generator
         */
        fun createDirectValueGenerator(
            value: String): (Array<String>)->String {
            val result: (Array<String>)->String = {
                value
            }
            return result 
        }

        /**
         * create url image generator
         */
        fun createUrlGenerator(index: Int): (Array<String>)->String {
            val result: (Array<String>)->String = {
                val index0 = (index % it.size + it.size) % it.size
                "url(\"${it[index0]}\")"
            }
            return result
        }
    }

    /**
     * apply back ground
     */
    fun createBackground(index: Int, url: Array<String>): String? {
        val result = if (elementGenerator.size > 0) {  
            var index0 = index % elementGenerator.size
            elementGenerator[index0].joinToString(
                separator = " ",
                transform = { it(url) })
        } else {
            null
        }
        return result
    }

    /**
     * load setting from json object
     */
    fun loadSetting(param: Json) {
        val params = if (param is Array<*>) {
            param as Array<Json>
        } else {
            arrayOf(param)
        }
        elementGenerator.forEach {
            it.clear()
        }
        params.forEachIndexed {
            idx, param0 ->
            val param1 = if (param0 is Array<*>) {
                param0 as Array<Json>
            } else {
                arrayOf(param0)
            }
            if (idx >= elementGenerator.size) {
                elementGenerator.add(
                    ArrayList<(Array<String>)->String>())
            }
            param1.forEach {
                val generator = when (it["kind"]) {
                    "direct-value" -> {
                        val value = it["value"]
                        if (value is String) {
                            createDirectValueGenerator(value)
                        } else {
                            null
                        }
                    }
                    "url" -> {
                        val idxValue = it["index"]
                        val idx0 = when(idxValue) {
                            is String -> idxValue.toIntOrNull()
                            is Number -> idxValue.toInt()
                            else -> null
                        }
                        idx0?.let { 
                            createUrlGenerator(it)
                        } 
                    }
                    else -> null
                } 
                generator?.let {

                    elementGenerator[idx].add(it)
                }
            }
        }
    }
}

// vi: se ts=4 sw=4 et:
