package com.atech.core.util


fun String.isLink(): Boolean {
    val pattern =
        "^https?://(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?:/[a-zA-Z0-9-]*)*(?:\\?[a-zA-Z0-9-_=]*)?(?:#[a-zA-Z0-9-_]*)?$"
    return this.matches(pattern.toRegex())
}

fun String.replaceHttps(): String {
    return this.replace("http://", "")
        .replace("https://", "")
}
