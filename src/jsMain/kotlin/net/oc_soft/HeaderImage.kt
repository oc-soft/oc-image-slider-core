package net.oc_soft

import kotlin.js.Promise
import kotlin.js.Json

import kotlin.collections.ArrayList

import kotlin.text.toDoubleOrNull
import kotlin.text.Regex

import kotlinx.browser.window
import kotlinx.browser.document

import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import org.w3c.dom.events.EventListener

import org.w3c.dom.HTMLElement
import org.w3c.dom.Element

import net.oc_soft.animation.PointsAnimation
import net.oc_soft.animation.QubicBezier
import kotlin.math.roundToInt

/**
 * animate backgournd image
 */
class HeaderImage(
    /**
     * color layer element query
     */
    val colorLayerElementQuery: String,
    /**
     * blur color layer element query
     */
    val blurColorLayerElementQuery: String,
    /**
     * header image elment query
     */
    val imageLayerElementQuery: String,

    /**
     * header image parameter query
     */
    val headerImageParamQuery: String,
    /**
     * background style
     */
    val backgroundStyle: BackgroundStyle) {

    /**
     * class instance
     */
    companion object {

    
        /**
         * general array to double array
         */
        fun arrayToDoubleArray(
            arrayObj: Array<*>,
            expectedSize: Int,
            fillValue: Double): DoubleArray {
            return DoubleArray(expectedSize) {
                if (it < arrayObj.size) {
                    val elem = arrayObj[it]
                    when (elem) {
                        is Number -> elem.toDouble()
                        is String -> elem.toDoubleOrNull()?: fillValue
                        else -> fillValue
                    }
                } else { fillValue }
            }
        }

        /**
         * json to setting animation params
         */
        fun jsonToSettingAnimationParams(
            jsonObj: Json): Array<SettingAnimationParam> {

            val animationParams = ArrayList<SettingAnimationParam>()

            val paramSetting = jsonObj["animation"]
            if (paramSetting is Array<*>) {
                for (idx in paramSetting.indices) {
                    val aSetting = paramSetting[idx]
                    if (jsTypeOf(aSetting) == "object") {

                        val param = jsonToSettingAnaimationParam(
                            aSetting.unsafeCast<Json>())
                        if (param != null) {
                            animationParams.add(param)
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }
            return animationParams.toTypedArray()
        }


        /**
         * convert json to setting animation param
         */
        fun jsonToSettingAnaimationParam(
            param: Json): SettingAnimationParam? {

            var aValue: dynamic = param["color"]
            val color = when(aValue) {
                is Array<*> -> arrayToDoubleArray(aValue, 4, 1.0)
                is String -> Color.hexStringToDoubleArray(aValue)
                else -> null 
            }

            aValue = param["blur-color"]
            val blurColor = when(aValue) {
                is Array<*> -> arrayToDoubleArray(aValue, 4, 1.0)
                is String -> Color.hexStringToDoubleArray(aValue)
                else -> null
            }

            aValue = param["blur"]
            val blur = when(aValue) {
                is Number -> aValue.toDouble()
                else -> null
            }

            aValue = param["background-colors"]
            val bgColors = when(aValue) {
                is Array<*> -> {
                    Array<DoubleArray>(aValue.size) {
                        val anElem = aValue[it]
                        when (anElem) {
                            is Array<*> -> arrayToDoubleArray(anElem, 4, 1.0)
                            is Number -> DoubleArray(4) { anElem.toDouble() }
                            is String -> {
                                Color.hexStringToDoubleArray(anElem)?.let {
                                    it
                                }?: DoubleArray(4) { 1.0 }
                            }
                            else -> DoubleArray(4) { 1.0 }
                        }
                    } 
                }
                else -> null
            }
            return if (color != null
                && blurColor != null
                && blur != null
                && bgColors != null) {
                SettingAnimationParam(color, blurColor, blur, bgColors)
            } else {
                null
            }
        }


        /**
         * json to setting duration param array
         */
        fun jsonToSettingDurationParams(
            jsonObj: Json): Array<SettingDurationParam> {
            val durationParams = ArrayList<SettingDurationParam>()

            val setting = jsonObj["animation-duration"]
            if (setting is Array<*>) {
                for (idx in setting.indices) {
                    val aSetting = setting[idx]
                    if (jsTypeOf(aSetting) == "object") {
                        val param = jsonToSettingDurationParam(
                            aSetting.unsafeCast<Json>())
                        if (param != null) {
                            durationParams.add(param)
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }
            return durationParams.toTypedArray()
        }

        /**
         * json to setting duration param
         */
        fun jsonToSettingDurationParam(
            jsObj: Json): SettingDurationParam?  {

            var aValue = jsObj["duration"]
            val duration = when(aValue) {
                is Number -> aValue.toDouble()
                is String -> aValue.toDoubleOrNull()
                else -> null
            }

            val easingFunction = jsObj["easing-fuction"]?.let {
                when (it) {
                    is String-> QubicBezier.stringToConverter(it)
                    else -> null
                }
            }?: QubicBezier.linearFunction 
             

            aValue = jsObj["delay"]
            val delay = when(aValue) {
                is Number -> aValue.toDouble()
                is String -> aValue.toDoubleOrNull()
                null ->  0.0
                else -> null 
            }
            aValue = jsObj["end-delay"]
            val endDelay = when(aValue) {
                is Number -> aValue.toDouble()
                is String -> aValue.toDoubleOrNull()
                null ->  0.0
                else -> null 
            } 
            return if (duration != null
                && easingFunction != null
                && delay != null
                && endDelay != null) {
                SettingDurationParam(duration, delay, endDelay, easingFunction)
            } else {
                null
            }
        }
    }



    /**
     * animation parameter
     */
    data class AnimationParam(
        /**
         * base color
         */
        val color: Array<DoubleArray>,
        /**
         * blur color
         */
        val blurColor: Array<DoubleArray>,
        /**
         * blur radius
         */
        val blur: Array<Double>,
        /**
         * image layer background color
         */
        val backgroundColors: Array<Array<DoubleArray>>,
        /**
         * procedure to time to frame index
         */
        val easingFunction: (Double)->Double,
        /**
         * animation duration 
         */
        val duration: Double,
        /**
         * delay time to start animation
         */
        val delay: Double = 0.0,
        /**
         * end time to finish animation
         */
        val endDelay: Double = 0.0) {
        

        /**
         * count of animation keys
         */
        val countOfAnimationKeys: Int get() = color.size

        /**
         * get setting animation param
         */
        fun getSettingAnimationParam(idx: Int): SettingAnimationParam {
            return SettingAnimationParam(
                color[idx],
                blurColor[idx],
                blur[idx],
                backgroundColors[idx])
        }

        /**
         * get setting animations
         */
        fun getSettingAnimationParams() : Array<SettingAnimationParam> {
            return Array<SettingAnimationParam>(countOfAnimationKeys) {
                getSettingAnimationParam(it)
            }
        }

        /**
         * get animation points
         */
        fun getAnimationPoints(): Array<DoubleArray> {
            val settingAnimationParams = getSettingAnimationParams()
            return Array<DoubleArray>(settingAnimationParams.size) {
                settingAnimationParams[it].toDoubleArray()
            }
        }

    }
    
    /**
     * animation param in setting
     */
    data class SettingAnimationParam(
        /**
         * base color
         */
        val color: DoubleArray,
        /**
         * blur color
         */
        val blurColor: DoubleArray,
        /**
         * blur radius
         */
        val blur: Double,
        /**
         * image layer background color
         */
        val backgroundColors: Array<DoubleArray>) {

        /**
         * secondary contstrucor
         */
        constructor(points: DoubleArray): this(
            points.sliceArray(0 until 4),
            points.sliceArray(4 until 8),
            points[8],
            Array<DoubleArray>((points.size - 9) / 4) {
                val idx = it
                DoubleArray(4) {
                    points[4 * idx + 9 + it]
                }
            })

        /**
         * convert to flat double array
         */
        fun toDoubleArray(): DoubleArray {
            return DoubleArray(4 + 4 + 1 + backgroundColors.size * 4) {
                if (it < 4) {
                    color[it]
                } else if (it < 8) {
                    blurColor[it - 4]
                } else if (it < 9) {
                    blur
                } else {
                    val idx = (it - 9) / 4
                    backgroundColors[idx][(it - 9) % 4]
                }

            } 
        }
    }


    /**
     * duration parameter in setting
     */
    data class SettingDurationParam(
        /**
         * duration of animation
         */
        val duration: Double,
        /**
         * delay time to start animation
         */
        val delay: Double = 0.0,
        /**
         * delay time to fire finished animation event
         */
        val endDelay: Double = 0.0,
        /**
         * procedure to convert time to frame index
         */
        val easingFunction: (Double) -> Double)



   
    /**
     * background position
     */
    val backgroundPosition: String get() = backgroundStyle.backgroundPosition

    /**
     * background repeat
     */
    val backgroundRepeat: String get() = backgroundStyle.backgroundRepeat

    /**
     * background size
     */
    val backgroundSize: String get() = backgroundStyle.backgroundSize


    /**
     * blur filter unit
     */
    var blurUnit: String = "px"


    /**
     * image url
     */
    var imageUrls = emptyArray<Array<String>>()


    /**
     * animation parameter
     */
    var animationParams = emptyArray<SettingAnimationParam>()


    /**
     * animation duration parameter
     */
    var durationParams = emptyArray<SettingDurationParam>()
    /**
     * color layer element query
     */
    val colorLayerElement: HTMLElement?
        get() {
            return document.querySelector(
                colorLayerElementQuery) as HTMLElement?
        }
    /**
     * blur color layer element query
     */
    val blurColorLayerElement: HTMLElement?
        get() {
            return document.querySelector(
                blurColorLayerElementQuery) as HTMLElement?
        }
    /**
     * header image elment query
     */
    val imageLayerElement: HTMLElement?
        get() {
            return document.querySelector(
                imageLayerElementQuery) as HTMLElement?
        }
    /**
     * start synchronize setting
     */
    fun startSyncSetting(): Promise<Unit> {

        val url = Site.requestUrl
        val searchParams = url.searchParams
        searchParams.append("action", headerImageParamQuery) 
        return window.fetch(url).then({
            it.json()
        }).then({
            updateSetting(it as Json)
        })
    }

    /**
     * start animation
     */
    fun createAnimation(
        eventTarget: EventTarget): ()->Unit {
        val pointsAnimation = createPointsAnimation(eventTarget) 
        return { pointsAnimation.start() } 
    }
     

    /**
     * update setting
     */
    fun updateSetting(setting: Json) {

        updateBlurUnit(setting)
        updateImageUrls(setting)
        animationParams = jsonToSettingAnimationParams(setting)
        durationParams = jsonToSettingDurationParams(setting)
    }




    /**
     * update blur unit
     */
    fun updateBlurUnit(setting: Json) {
        val unitSetting = setting["blur-unit"]
        if (unitSetting is String) {
            this.blurUnit = unitSetting
        }
    }

    /**
     * update image urls
     */
    fun updateImageUrls(setting: Json) {

        val urlSetting = setting["image-url"]
        when (urlSetting) {
            is String -> imageUrls = arrayOf(arrayOf(urlSetting))
            is Array<*> -> {
                val urls = ArrayList<Array<String>>()

                imageUrls = if (urlSetting.size > 0) {
            
                    var lastUrlArray: Array<String> = loadStringArray(
                        urlSetting[0] as Any)
                    val urls = ArrayList<Array<String>>()
                    urls.add(lastUrlArray) 
                    for (idx in 1 until urlSetting.size) {
                        val elem = loadStringArray(urlSetting[idx] as Any)
                        if (elem.size < lastUrlArray.size) {
                            urls.add(Array<String>(lastUrlArray.size) {
                                if (it < elem.size) {
                                    elem[it]
                                } else {
                                    lastUrlArray[it]
                                }
                            }) 
                        } else if (elem.size > lastUrlArray.size) {
                            urls.add(elem.sliceArray(0 until lastUrlArray.size))
                        } else {
                            urls.add(elem)
                        }
                        lastUrlArray = urls.last() 
                    }
                    urls.toTypedArray()
                } else {
                    emptyArray<Array<String>>()
                }
                
            }
        }
    }


    /**
     * load string array
     */
    fun loadStringArray(anObj: Any) : Array<String> {

        return if (anObj is String) {
            arrayOf(anObj)
        } else if (anObj is Array<*>) {
            val strArray = ArrayList<String>()
            for (idx in anObj.indices) {
                val anElem = anObj[idx]
                if (anElem is String) {
                    strArray.add(anElem)
                } else {
                    break
                }
            }
            strArray.toTypedArray()
        } else {
            emptyArray<String>()
        }
    }

    /**
     * create animation parameters
     */
    fun createAnimationParams(): Array<AnimationParam> {

        val settingAnimationParams = this.animationParams
        val settingDurationParams = this.durationParams
        return if (settingAnimationParams.size > 0
           && settingDurationParams.size > 0) {
            val paramSize = kotlin.math.min(
                settingAnimationParams.size - 1, 
                settingDurationParams.size) 

            var lastBackColors = settingAnimationParams[0].backgroundColors
            
            Array<AnimationParam>(paramSize) {
                val backColors1 = settingAnimationParams[
                    it + 1].backgroundColors
                val backColors = arrayOf(
                    lastBackColors,
                    Array<DoubleArray>(lastBackColors.size) {
                        if (it < backColors1.size) {
                            backColors1[it]
                        } else {
                            lastBackColors[it]
                        }
                    }) 
                
                
                lastBackColors = backColors[1]

                AnimationParam(
                    arrayOf(settingAnimationParams[it].color,
                        settingAnimationParams[it + 1].color),
                    arrayOf(settingAnimationParams[it].blurColor,
                        settingAnimationParams[it + 1].blurColor),
                    arrayOf(settingAnimationParams[it].blur,
                        settingAnimationParams[it + 1].blur),
                    backColors,
                    settingDurationParams[it].easingFunction,
                    settingDurationParams[it].duration,
                    settingDurationParams[it].delay,
                    settingDurationParams[it].endDelay)     
            }
            
        } else {
            emptyArray<AnimationParam>()
        }
    }


    /**
     * create points animation
     */
    fun createPointsAnimation(
        eventTarget: EventTarget): PointsAnimation {

        val eventDelegate = document.createElement("div")

        val startHdlr: (Event)->Unit = { notifyStart(eventTarget) }
        val finishedHdlr: (Event)->Unit = { notifyFinish(eventTarget) }

        eventDelegate.addEventListener(
            "start", startHdlr, object {
                @JsName("once")
                val once = true
            })
        eventDelegate.addEventListener(
            "finish", finishedHdlr, object {
                @JsName("once")
                val once = true
            })


        val result = PointsAnimation(eventDelegate)

        val animationParams = createAnimationParams()
        animationParams.forEach {
            result.add(
                it.getAnimationPoints(),
                { points, idx -> handleAnimate(points, idx) },
                { handleBegin() },
                { handleFinish() },
                it.duration,
                it.easingFunction,
                it.delay,
                it.endDelay) 
        }

        return result
    }



    /**
     * handle animation begin event
     */
    fun handleBegin() {
    }


    /**
     * handle animation finished event
     */
    fun handleFinish() {
    }


    /**
     * notify start
     */
    fun notifyStart(eventTarget: EventTarget) {
        eventTarget.dispatchEvent(Event("start"))
    }


    /**
     * notifiy finish
     */
    fun notifyFinish(eventTarget: EventTarget) {
        eventTarget.dispatchEvent(Event("finish"))
    }
    
    /**
     * it is the time to change html style
     */
    fun handleAnimate(points: DoubleArray, durationIndex: Double) {

        val settingAnimationParam = SettingAnimationParam(points)
        updateColorLayerStyle(settingAnimationParam)

        updateBlurColorLayerStyle(settingAnimationParam)
        updateImageLayerStyle(settingAnimationParam,
            durationIndex.roundToInt())
        
    }

    /**
     * update color layer style
     */
    fun updateColorLayerStyle(
       settingAnimationParam: SettingAnimationParam) {

        colorLayerElement?.let {
            it.style.color = settingAnimationParam.color.toHexString()
        }
    } 

    /**
     * copy direct style on color layer from source to destination.
     */
    fun copyColorLayerStyle(
        sourceElement: HTMLElement,
        destElement: HTMLElement) {
        destElement.style.color = sourceElement.style.color
    }

    /**
     * update blur layer style
     */
    fun updateBlurColorLayerStyle(
        settingAnimationParam: SettingAnimationParam) {
        blurColorLayerElement?.let {
            var blurRadius = settingAnimationParam.blur 
            val filter = if (blurUnit == "px") {
                "blur(${blurRadius.roundToInt()}${blurUnit})"
            } else {
                "blur(${blurRadius}${blurUnit})"
            }
            it.style.filter = filter
            it.style.color = settingAnimationParam.blurColor.toHexString()
        }
    }

    /**
     * copy direct style on blur color layer from source to destination.
     */
    fun copyBlurColorLayerStyle(
        sourceElement: HTMLElement,
        destElement: HTMLElement) {
        destElement.style.color = sourceElement.style.color
        destElement.style.filter = sourceElement.style.filter
    }

 

    /**
     * update image layer style
     */
    fun updateImageLayerStyle(
        settingAnimationParam: SettingAnimationParam,
        imageUrlIndex: Int) {
        imageLayerElement?.let {
            it.style.backgroundImage = createBackgroundImageStyle(
                settingAnimationParam.backgroundColors,
                imageUrlIndex)
            it.style.backgroundRepeat = backgroundRepeat
            it.style.backgroundPosition = backgroundPosition
            it.style.backgroundSize = backgroundSize
        } 

    }


    /**
     * create background image style
     */
    fun createBackgroundImageStyle(
        backgroundColors: Array<DoubleArray>,
        imageUrlIndex: Int): String {

        val urls = imageUrls[imageUrlIndex]?: emptyArray<String>()

        val style = backgroundStyle.createBackgroundImageStyle(
            urls, backgroundColors)

        val result = style
        return result 
    }
   
  
    fun add(
        points: Array<DoubleArray>,
        addional: String) {
    }
}

// vi: se ts=4 sw=4 et:
