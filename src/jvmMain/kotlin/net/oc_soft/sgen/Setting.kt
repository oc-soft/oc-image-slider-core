package net.oc_soft.sgen


import java.io.File
import java.io.IOException

import kotlin.io.readBytes
import kotlin.text.decodeToString
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.HashMap
import kotlin.collections.mapOf
import kotlin.collections.set
import kotlin.collections.get

import com.google.gson.Gson 
/**
 * site generator setting
 */
data class Setting(
    /**
     * output directory
     */
    var outputRoot: String = "buidl/web-site",
    /**
     * resource root directory
     */
    var rootResourceMapping: Array<Map<String, Any>> 
        = arrayOf(
            mapOf(
                "root-dir" to "src/site",
                "master-template" to "master-0.php",
                "contents-dir" to "contents",
                "template-root" to "frame",
                "style-dir" to "style",
                "exclude" to arrayOf( 
                    "^.git$",
                    ".gitignore$", 
                    ".DS_Store$",
                    ".swp$",
                    ".+~$")))) {

    /**
     * class instance
     */
    companion object {

        /**
         * load setting from json
         */
        fun load(path: String = "sgen-setting.json"): Setting {
            val file = File(path)

            val result = Setting()
            try {
                val gson = Gson()
                val prop = gson.fromJson(
                    file.readBytes().decodeToString(), Map::class.java) 

                result.outputRoot =
                    (prop["output-root"] as String?) ?: result.outputRoot

                val rootIterable = prop["root-resource-mapping"] 
                    as Iterable<Map<*, *>>?

                if (rootIterable != null) {
                    val rootList = rootIterable.toList()
                
                    result.rootResourceMapping = Array<Map<String, Any>>(
                        rootList.size) {
                        rootList[it] as Map<String, Any>
                    }
                }
            } catch (e: IOException) {
            }
            return result
        }
    }
}

// vi: se ts=4 sw=4 et:
