package net.oc_soft.wp_fetch

import kotlin.collections.ArrayList

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpHeaders
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonArray
import com.google.gson.JsonObject


class App {


    companion object {
        /**
         * entry point
         */
        @JvmStatic
        fun main(args: Array<String>) {
            (App()).run()
        }

    }

    var url: String = "http://junseikai.org"

    /**
     * read number of pages
     */
    fun readNumberOfPages() : Int {
        val client = HttpClient.newHttpClient()
        var reqBuilder = HttpRequest.newBuilder()
        reqBuilder.uri(URI.create("http://junseikai.org/wp-json/wp/v2/posts"))
        val req = reqBuilder.build()
         
        val res: HttpResponse<String>  = client.send(req,
            HttpResponse.BodyHandlers.ofString())

        val headers = res.headers()
        val totalPages = headers.firstValueAsLong("X-WP-TotalPages")

        val result = totalPages.orElse(0L).toInt()
        return result
    }

    /**
     * read all pages
     */
    fun readAllPages() {
        val client = HttpClient.newHttpClient()

        for (i in 1 .. readNumberOfPages()) {
            var reqBuilder = HttpRequest.newBuilder()
            val url = URI.create(
                "http://junseikai.org/wp-json/wp/v2/posts?page=${i}")
            reqBuilder.uri(url)
            val req = reqBuilder.build()
             
            val res:HttpResponse<String> = 
                client.send(req, HttpResponse.BodyHandlers.ofString())
            savePost(res.body()) 
        }
    }
    
    /**
     * save post
     */
    fun savePost(body: String) {
        val gson = Gson()

        val contents = gson.fromJson(body, JsonArray::class.java)
        
        
        contents.forEach {
            if (it.isJsonObject()) {
                val obj0 = it.getAsJsonObject()
                
                obj0["content"]?.let {
                    if (it.isJsonObject()) {
                        val obj1 = it.getAsJsonObject()
                        obj1["rendered"]?.let {
                            println(it)
                        }
                    }
               }

                obj0.keySet().forEach {
                    if (it != "content") {
                        println(it)
                        println(obj0[it])
                    }
                }
                obj0["status"]?.let {
                    println("status: ${it}")
                }
            } else {
                println(it::class)
            }
        } 

    } 

    /**
     * run application
     */
    fun run() {
        readAllPages()
    }


}
// vi: se ts=4 sw=4 et:
