package net.oc_soft.sgen


import java.io.File
import java.io.IOException

import kotlin.io.readBytes
import kotlin.text.decodeToString
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.ArrayList

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
    var resourceRoot: String = "src/site",
    /**
     * contents directory
     */
    var contentsDir: String = "contents",
    /**
     * template root directory
     */
    var templateRoot: String = "frame",
    /**
     * master template files
     */
    var masterTemplate: String = "master-0.php") {

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
                result.resourceRoot = 
                    (prop["resource-root"] as String?) ?: result.resourceRoot
                result.contentsDir = 
                    (prop["contents-dir"] as String?) ?: result.contentsDir
                result.templateRoot =
                    (prop["template-root"] as String?) ?: result.templateRoot

                result.masterTemplate = 
                    (prop["master-template"] as String?) ?:     
                        result.masterTemplate
            } catch (e: IOException) {
            }
            return result
        }
    }
}

// vi: se ts=4 sw=4 et:
