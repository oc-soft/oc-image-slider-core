/*
 * Copyright 2022 oc-soft
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.oc_soft

import kotlin.js.Json
import kotlin.js.Promise

import kotlinx.browser.window

import kotlin.collections.MutableList
import kotlin.collections.ArrayList

import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams

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
     * get current query
     */
    fun getQuerysFromUrl(): Array<Pair<String, String>> {
        val queryStr = window.location.search
        val searchParams = URLSearchParams(queryStr)   
        return if (searchParams.has("param-index")) {
            arrayOf(
                Pair("param-index", 
                searchParams.get("param-index") as String))
        } else {
            emptyArray<Pair<String, String>>()
        }
    }

 

    /**
     * start load to setup element background loader
     */
    fun startSyncSetting(url: URL): Promise<Unit> {
        
        val searchParams = url.searchParams
        searchParams.append("action", imageLayoutQuery) 
        getQuerysFromUrl().forEach {
            searchParams.append(it.first, it.second)
        }
        return window.fetch(url).then({
            it.json()
        }).then({
            @Suppress("UNCHECKED_CAST")
            loadSetting(
                it as Json)
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
