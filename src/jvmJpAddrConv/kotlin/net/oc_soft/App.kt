package net.oc_soft

import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.MutableList
import kotlin.collections.ArrayList
import kotlin.collections.joinToString

import java.io.IOException
import java.nio.file.Files
import java.nio.file.FileSystems

import com.google.gson.Gson

import gnu.getopt.Getopt

import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVFormat

/**
 * application
 */
class App(
    /**
     * the option to control application behevior
     */
    val option: Option) {

    /**
     * class instance
     */
    companion object {

        /**
         * parse command line
         */
        fun parse(gopt: Getopt): Option {

            var help = false
            var sourceFile: String? = null 
            var conversion = Conversion.ZIP  
            while (true) {
                when (gopt.getopt()) {
                    'f'.toInt() -> sourceFile = gopt.getOptarg()
                    'c'.toInt() -> {
                        try {
                            conversion = 
                                Conversion.valueOf(gopt.getOptarg())
                        } catch (ex: IllegalArgumentException) {
                        }
                    }
                    'h'.toInt() -> help = true
                    else -> break
                }
            }
            return Option(sourceFile, conversion, help)
        }
        /**
         * entry point
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val g = Getopt("jp-addr-conv", args, "hf:c:")
            val app = App(parse(g))
            app.run()
        }
    }

    /**
     * the way to convert origina address to json
     */
    enum class Conversion {
        ZIP,
        PREF_CITY
    }

    /**
     * application option
     */
    data class Option(
        /**
         * data source file for japanese address.
         */
        val addressSource: String?, 
        /**
         * the way to convert source address to optimized json
         */
        val conversion: Conversion,
        /**
         * you get help message if this value is true.
         */
        val help: Boolean)

    /**
     * source address contents
     */ 

    val addressSourceContents: String?
        get() {
           var result: String? = null 
           result = option.addressSource?.let {
                var contents: String? = null
                try {
                    val addrPath = FileSystems.getDefault().getPath(it)
                    val lines = Files.readAllLines(addrPath) 
                    contents = lines.joinToString("\n")

                } catch (ex: Exception) {
                }
                contents
            }
            return result 
        }

    /**
     * parser for source address
     */
    val sourceAddressParser: CSVParser?
        get() {
            return addressSourceContents?.let {
                CSVParser.parse(it, CSVFormat.DEFAULT.withHeader())
            }
        }

    /**
     * key index from csv address to json
     */
    val keyIndex: Int
        get() {
            return 0
        }

    /**
     * convert csv source to json
     */
    fun convert() {
        sourceAddressParser?.let {
            val convertedObj: MutableMap<String, Any> = LinkedHashMap()
            it.forEach {
                if (it.size() > 16) { 
                    val items = arrayOf(
                        it[4], it[7], it[9], it[11], it[14], it[15])
                    appendItems(convertedObj, items) 
                }
            } 
            val gson = Gson() 
            println(gson.toJson(convertedObj))
        }?: println("no-contents")
    }

    /**
     * append items 
     */
    fun appendItems(
        keyAndValue: MutableMap<String, Any>,
        items: Array<String>) {
         
        when (option.conversion) {
            Conversion.ZIP -> {
                keyAndValue[items[0]] = arrayOf(
                    items[1], items[2], items[3])
            }
            else -> {
                var cityMap: MutableMap<String, Any>? = null
                cityMap = keyAndValue[items[1]] as MutableMap<String, Any>?
                if (cityMap == null) {
                    cityMap = LinkedHashMap<String, Any>()
                    keyAndValue[items[1]] = cityMap
                }
                var streetsArea: MutableMap<String, Any>? = null
                streetsArea = cityMap!![items[2]] as MutableMap<String, Any>?
                if (streetsArea == null) {
                    streetsArea = LinkedHashMap<String, Any>()
                    cityMap[items[2]] = streetsArea
                } 
                var zipCodes: MutableList<String>? = null
                zipCodes = streetsArea!![items[3]] as MutableList<String>?
                if (zipCodes == null) {
                    zipCodes = ArrayList<String>()
                    streetsArea!![items[3]] = zipCodes
                }
                zipCodes!!.add(items[0])
            }
        }
    }


    /**
     * run the application
     */
    fun run() {
        if (!option.help) {
            convert()
        } else {
            println("jpAddrConv -f [file path]")
        }
    }
}


// vi: se ts=4 sw=4 et:
