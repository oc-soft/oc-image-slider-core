package net.oc_soft

import kotlin.js.Promise
import kotlin.js.Json

import kotlinx.browser.window

import kotlin.text.toDoubleOrNull

import kotlin.collections.ArrayList
import kotlin.collections.MutableList

import org.w3c.dom.Image

/**
 * manage header image resource 
 */
class HeaderImages(
    /**
     * url request query
     */
    val headerImagesQuery: String,
    /**
     * url request query for header image addition
     */
    val additionsForImageQuery: String) {


    /**
     * class instance
     */
    companion object {

        /**
         * load colors
         */
        fun loadColors(entry: dynamic, 
            defaultColor: DoubleArray): Array<DoubleArray> {

            val colors = ArrayList<DoubleArray>()
            if (entry is Array<*>) {
                val entries: Array<*> = entry
                entries.forEach {
                   colors.add(loadColor(it, defaultColor))
                }
            }
            return colors.toTypedArray()
        }

        /**
         * load color
         */
        fun loadColor(entry: dynamic, defaultColor: DoubleArray): DoubleArray {
            return if (entry is Array<*>) {
                DoubleArray(defaultColor.size) {
                    val colorValue = defaultColor[it] 
                    if (it < entry.size) {
                        val entryValue = entry[it]
                        when (entryValue) {
                            is String -> entryValue.toDoubleOrNull()?: 
                                colorValue
                            is Number-> entryValue.toDouble()
                            else -> colorValue
                        }
                    } else {
                        colorValue
                    }
                } 
            } else {
                defaultColor
            }
        }
    }

    /**
     * image data entry
     */
    data class ImageEntry(
        /**
         * image url
         */
        val url: String)

    /**
     * addtion for image
     */
    data class AdditionForImageEntry(
        /**
         * color on image
         */
        val colors: Array<DoubleArray>)

    /**
     * image entry
     */
    val images: MutableList<ImageEntry> = ArrayList<ImageEntry>()

    /**
     * addtion setting
     */
    val additionsForImage: MutableList<AdditionForImageEntry> = 
        ArrayList<AdditionForImageEntry>()


    /**
     * images size
     */
    val imagesSize: Int get() = images.size

    val colorsSize: Int get() = additionsForImage.size


    /**
     * get image urls
     */
    fun getImageUrls(indices: IntRange): Array<String> {
        return images.slice(indices).map({ it.url }).toTypedArray()
    }

    /**
     * get colors
     */
    fun getColors(indices: IntRange): Array<Array<DoubleArray>> {
        return additionsForImage.slice(
            indices).map({ it.colors }).toTypedArray()
    }

    /**
     * synchroinze setting with site
     */
    fun startSyncSetting(): Promise<Unit> {

        val urls = arrayOf(Site.requestUrl, Site.requestUrl)
        urls[0].searchParams.append("action", headerImagesQuery)
        urls[1].searchParams.append("action", additionsForImageQuery)

        val promises = Array<Promise<Any>>(urls.size) {
            window.fetch(urls[it]).then({
                it.json()
            })
        }

        return Promise<Unit>({
            resolve, _ -> 
            Promise.all(promises).then({
                updateSetting(it[0], it[1]).then({
                    resolve(it)
                })
            })
        })
    } 

    /**
     * get image source url
     */
    fun getImageSourceUrl(
        imageSources: Array<Json>,
        index: Int): String? {
        return imageSources[index]["url"]?.let {
             it as String
        }
    }


    /**
     * update setting
     */
    fun updateSetting(
        imageSetting: Any?, 
        additionSetting: Any?):Promise<Unit> {
       
        updateAdditionSetting(additionSetting)  
        return updateImages(imageSetting)
    }


    /**
     * update images
     */
    fun updateImages(
        imageSetting: Any?): Promise<Unit> {
        images.clear()
        val promises =  if (imageSetting is Array<*>) {
            Array<Promise<Pair<Image?, String?>>>(imageSetting.size) {

                val entry: dynamic = imageSetting[it]

                val promiseDefault = Promise<Pair<Image?, String?>>({ 
                    resolve, _ -> 
                    resolve(Pair<Image?, String?>(null, null))
                })
                var promise: Promise<Pair<Image?, String?>>? = null
                if (entry != null) {
                    val url = entry.url
                    if (url is String) {
                        loadImage(url) 
                    } else {
                        promiseDefault
                    }
                } else {
                    promiseDefault 
                }
            }
        } else {
            emptyArray<Promise<Pair<Image?, String?>>>()
        }
        return Promise.all(promises).then({
            it.forEach {
                it.second?.let {
                    images.add(ImageEntry(it))
                }
            }      
        })
    }


    /**
     * update addition setting
     */
    fun updateAdditionSetting(
        additionsSetting: Any?) {
        additionsForImage.clear()
        if (additionsSetting is Array<*>) {
            additionsSetting.forEach {
                val defaultColor = DoubleArray(4) { 0.0 }
                val colors = if (it != null) {
                    val entry: dynamic = it
                    loadColors(entry.colors, defaultColor)
                } else {
                    arrayOf(defaultColor)
                }
                additionsForImage.add(
                    AdditionForImageEntry(colors))
            }  
        }
    }

    /**
     * load image
     */
    fun loadImage(
        url: String): Promise<Pair<Image?, String?>> {
        return Promise<Pair<Image?, String?>>() {
            resolve, _ ->
            val img = Image()
            img.src = url 
            img.onload = { 
                resolve(Pair<Image?, String?>(img, url))
            }
            img.onerror = { 
                msg, src, lineno, col, err ->
                resolve(Pair<Image?, String?>(null, null))
            } 
        }
    }
    

}

// vi: se ts=4 sw=4 et:
