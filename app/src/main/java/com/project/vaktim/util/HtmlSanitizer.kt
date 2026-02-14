package com.project.vaktim.util

object HtmlSanitizer {

    private val htmlTagRegex = Regex("<[^>]*>")

    fun sanitize(input: String): String {
        return input
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(htmlTagRegex, "")
            .trim()
    }
}
