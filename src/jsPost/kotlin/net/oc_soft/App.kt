package net.oc_soft

import kotlinx.browser.window

class App {


	/**
	 * attach application into html elements
 	 */
	fun bind() {
	}

	/**
	 * detach appcliation from html elements
	 */
	fun unbind() {
	}

	/**
	 * run application
	 */
	fun run() {

		window.addEventListener("load",
			{ bind() },
			object {
				@JsName("once")
				val once = true
			})
		window.addEventListener("unlod",
			{ unbind() },
			object {
				@JsName("once")
				val once = true
			})

	}
}


// vi: se ts=4 sw=4 et:
