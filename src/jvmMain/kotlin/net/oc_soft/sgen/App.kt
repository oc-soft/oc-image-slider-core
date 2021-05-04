package net.oc_soft.sgen

import java.nio.file.Path
import java.io.File


import kotlin.collections.Set
import kotlin.collections.HashSet
import kotlin.collections.Iterable

import kotlin.text.endsWith
import kotlin.text.replace
import kotlin.text.Regex
import kotlin.text.MatchResult
import kotlin.io.readText
import kotlin.io.writeText

import gnu.getopt.Getopt
import com.google.common.io.Files

/**
 * application to generate site resource
 */
class App(val option: Option,
    val setting: Setting) {

    companion object {

        fun parse(opt: Getopt): Option {

            var help = false
            while (true) {
                when (opt.getopt()) {
                    'h'.toInt() -> help = true
                    else -> break
                }

            }
            return Option(help)
        }


        /**
         * entry point
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val g = Getopt("site-generator", args, "h")
            val app = App(parse(g), Setting.load())
            app.run()
        }
    }


    data class Option(val help: Boolean) 



       

    /**
     * run generator
     */
    fun run() {
        iterate()
    }
    /**
     * master template contents
     */
    fun getMasterTemplate(
        rootResourceMap: Map<String, Any>): Pair<String, String> {
        val masterTempFile = 
            File(
                File(rootResourceMap["root-dir"] as String, 
                    rootResourceMap["template-root"] as String),
                    rootResourceMap["master-template"] as String)

        
        return Pair(masterTempFile.readText(),
            Files.getFileExtension(
                rootResourceMap["master-template"] as String))

    }
    /**
     * exclude pattern
     */
    fun getExcludePattern(
        rootResourceMap: Map<String, Any>): Array<Regex> {
        
        val strIter = rootResourceMap["exclude"] as Iterable<*>
        
        val strList = strIter.toList()
        return Array<Regex>(strList.size) {
            Regex(strList[it] as String) 
        }
    }



    /**
     * you get true if file name is reserved name
     */
    fun isReservedDirectory(
        rootResourceMapping: Map<String, Any>,
        file: File): Boolean {
        return when (file.name) {
            rootResourceMapping["template-root"],
            rootResourceMapping["contents-dir"],
            rootResourceMapping["style-dir"]-> true
            else -> false
        }
    }

    /**
     * you get true if the file is matched exclude pattern
     */
    fun isExclude(
        rootResourceMapping: Map<String, Any>,
        file: File): Boolean {
        return getExcludePattern(rootResourceMapping).find {
            it.containsMatchIn(file.name)
        } != null
    }



    /**
     * iterate source directory
     */
    fun iterate() {
        setting.rootResourceMapping.forEach {
            
            val resRoot = File(it["root-dir"] as String)

            val rootResMapping = it
            resRoot.listFiles().forEach {
                if (!isReservedDirectory(rootResMapping, it)) { 
                    iterate(rootResMapping, resRoot, it)
                }
            }
        }
    }

    /**
     * iterate aDirectory
     */
    fun iterate(rootResourceMapping: Map<String, Any>,
        resourceRoot: File,
        aFile: File) {
        
        if (!isExclude(rootResourceMapping, aFile)) {
            if (aFile.isDirectory) {
                if (aFile.name.endsWith(".html")) {
                    generateContent(rootResourceMapping, resourceRoot, aFile)
                } else {
                    aFile.listFiles().forEach {
                        iterate(rootResourceMapping, resourceRoot, it)
                    }
                }
            } else {
                copyFile(resourceRoot, aFile)
            }
        }
    }


    /**
     * copy file
     */
    fun copyFile(
        resourceRoot: File,
        aFile: File) {
        val relpath = resourceRoot.toPath().relativize(
            aFile.parentFile.toPath())
        var destDir = File(setting.outputRoot)

        destDir = destDir.toPath().resolve(relpath).toFile() 
        destDir.mkdirs()
        try {
            Files.copy(aFile, File(destDir, aFile.name))
        } catch (e: Exception) {
        }
 
    }


    /**
     * generate page content
     */
    fun generateContent(
        rootResourceMapping: Map<String, Any>,
        resourceRoot: File,
        aFile: File) {
        val contentAndExtension = createPage(rootResourceMapping, aFile)

        if (contentAndExtension != null) {

            val relpath = resourceRoot.toPath().relativize(
                aFile.parentFile.toPath())
            var destDir = File(setting.outputRoot)

            destDir = destDir.toPath().resolve(relpath).toFile() 
            destDir.mkdirs()

            var dstName = Files.getNameWithoutExtension(aFile.name)
            val destFile = File(destDir,
                "${dstName}.${contentAndExtension.second}")
             
            try {
                destFile.writeText(contentAndExtension.first)
            } catch (e: Exception) {
            }
            
        }
     }

   

    /**
     * create page contents
     */
    fun createPage(
        rootResourceMapping: Map<String, Any>,
        resDir: File): Pair<String, String>? {
        
        val bodyFile = File(resDir, "body.txt")
        val headFile = File(resDir, "head.txt")

        var result: Pair<String, String>? = null

        
        try {
            val bodyTxt = bodyFile.readText()

            var headTxt: String? = null
            try {
                headTxt = headFile.readText()
            } catch (e : Exception) {
            }
            val templateAndExtension = readTemplate(
                rootResourceMapping, resDir)
            result = Pair(createContent(
                rootResourceMapping,
                templateAndExtension.first, headTxt, bodyTxt),
                templateAndExtension.second)
        } catch (e: Exception) {
        }

        return result
    }


    /**
     * create page content
     */
    fun createContent(
        rootResourceMapping: Map<String, Any>,
        template: String,
        headText: String?,
        bodyText: String): String {

        var content = template
        if (headText != null) {
            val headPattern = Regex("<!--\\s*head\\s+contents\\s*-->")
            var match = headPattern.find(content)
            if (match != null) {
                val headContent = createContent(
                    rootResourceMapping, headText, HashSet<String>())
                while (match != null) {
                    content = content.replaceRange(match.range, headContent)  
                    match = headPattern.find(content)
                }
            }
        }
        val bodyPattern = Regex("<!--\\s*body\\s+contents\\s*-->")
        var match = bodyPattern.find(content)
        if (match != null) {
            val bodyContent = createContent(
                rootResourceMapping, bodyText, HashSet<String>())
            while (match != null) {
                content = content.replaceRange(match.range, bodyContent)
                match = bodyPattern.find(content) 
            }
        }
        val result = content 
        return result
    }

    /**
     * create content
     */
    fun createContent(
        rootResourceMapping: Map<String, Any>,
        template: String,
        processingContentKeys: Set<String>): String {
        val regEx = Regex("<!--\\s*(\\S+)\\s*-->")

        var procTemp = template

        val procKeys = HashSet(processingContentKeys)
        

        var match: MatchResult? = regEx.find(procTemp)
    
        while (match != null) {
            val contentKey = match.groups[1]!!.value
            if (contentKey !in procKeys) {
                procKeys.add(contentKey)
                val content = resolve(
                    rootResourceMapping, contentKey, procKeys)
                if (content != null) {
                    procTemp = procTemp.replaceRange(match.range, content)  
                    match = regEx.find(procTemp)
                } else {
                    match = match.next()
                }
                procKeys.remove(contentKey) 
            } else {
                procTemp = procTemp.replaceRange(match.range, "") 
                match = regEx.find(procTemp) 
            }
        }
        val result = procTemp
        return result
    }

    /**
     * resolve content
     */
    fun resolve(
        rootResourceMapping: Map<String, Any>,
        contentKey: String,
        processingContentKeys: Set<String>) : String? {
        var result: String? = null
        val contentFile = File(
            File(rootResourceMapping["root-dir"] as String,
                rootResourceMapping["contents-dir"] as String), contentKey)
        try {
            result = createContent(
                rootResourceMapping,
                contentFile.readText().trim(), 
                processingContentKeys)
        } catch (e : Exception) {
        } 
        return result
    }

    /**
     * read template frame
     */
    fun readTemplate(
        rootResourceMapping: Map<String, Any>,
        resDir: File): Pair<String, String>  {
        var result = getMasterTemplate(rootResourceMapping)
        
        try {
            val templateTxtFile = File(resDir, "template.txt")
            val templateName = templateTxtFile.readText().trim()
            val templateFile = File(
                File(rootResourceMapping["root-dir"] as String,
                    rootResourceMapping["template-root"] as String),
                    templateName) 
            result = Pair(templateFile.readText().trim(),
                Files.getFileExtension(templateName))
        } catch (e: Exception) {
        } 
        return result 
    } 

}

// vi: se ts=4 sw=4 et:
