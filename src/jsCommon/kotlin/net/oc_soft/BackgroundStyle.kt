package net.oc_soft

import kotlin.js.Json
import kotlin.js.Promise

import kotlinx.browser.window

import kotlin.collections.MutableList
import kotlin.collections.ArrayList

import org.w3c.dom.HTMLElement

/**
 * manage background style 
 */
class BackgroundStyle(
    /**
     * action query to get image layout 
     */
     val imageLayoutQuery: String) {


    
    /**
     * class instance
     */
    companion object {

    }
    /**
     * image template
     */
    var backgroundImageTemplate: String = "url(\${imgUrl0})"


    /**
     * background position
     */
    var backgroundPosition: String = "center"

    /**
     * background repeat
     */
    var backgroundRepeat: String = "no-repeat"

    /**
     * background size
     */
    var backgroundSize: String = "cover"



    /**
     * update background template
     */
    fun updateBackgroundImageTemplate(setting: Json) {
        val templateSetting = setting["background-template"]
        if (templateSetting is String) {
            backgroundImageTemplate = templateSetting
        }
    } 
    
    /**
     * update background image position
     */
    fun updateBackgroundPosition(setting: Json) {
        val positionSetting = setting["background-position"]
        if (positionSetting is String) {
            backgroundPosition = positionSetting 
        }
    } 

    /**
     * update background image repeat 
     */
    fun updateBackgroundRepeat(setting: Json) {
        val repeatSetting = setting["background-repeat"]
        if (repeatSetting is String) {
            backgroundRepeat = repeatSetting 
        }
    } 
    /**
     * update background size
     */
    fun updateBackgroundSize(setting: Json) {
        val sizeSetting = setting["background-size"]
        if (sizeSetting is String) {
            backgroundSize = sizeSetting 
        }
    } 




    /**
     * start load to setup element background loader
     */
    fun startSyncSetting(): Promise<Unit> {
        val url = Site.requestUrl
        val searchParams = url.searchParams
        searchParams.append("action", imageLayoutQuery) 
        return window.fetch(url).then({
            it.json()
        }).then({
            loadSetting(it as Json)
        })
    }

    /**
     * create background image style
     */
    fun createBackgroundImageStyle(
        imageUrls: Array<String>,
        colors: Array<DoubleArray> = emptyArray<DoubleArray>()): String {

        var style = backgroundImageTemplate


        colors.forEachIndexed {
            idx, colorArray ->
            val colorPattern = "\\\$\\{color${idx}\\}"
             
            style = style.replace(
                Regex(colorPattern), colorArray.toHexString())
        }
        
        imageUrls.forEachIndexed {
            idx, url ->
            val urlPattern = "\\\$\\{url${idx}\\}"
            style = style.replace(Regex(urlPattern), url)
        }
        val result = style
        return result 
    }
 
    /**
     * load setting from json object
     */
    fun loadSetting(setting: Json) {
        updateBackgroundImageTemplate(setting)
        updateBackgroundPosition(setting)
        updateBackgroundRepeat(setting)
        updateBackgroundSize(setting)
    }
}

// vi: se ts=4 sw=4 et:
