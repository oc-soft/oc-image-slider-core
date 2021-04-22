package net.oc_soft.sgen

import java.nio.file.Files
import java.nio.file.Path

import gnu.getopt.Getopt

/**
 * application to generate site resource
 */
class App(val option: Option) {

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
            val app = App(parse(g))

            app.run()
        }
    }


    data class Option(val help: Boolean) 
        

    fun run() {
        if (option.help) {
            println("Got help")
        } else {
            println("Hello world")
        }
    }


    
}

// vi: se ts=4 sw=4 et:
