package com.atech.core.util

import android.app.Activity
import android.content.Context


fun String.isLink(): Boolean {
    val pattern =
        "^https?://(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?:/[a-zA-Z0-9-]*)*(?:\\?[a-zA-Z0-9-_=]*)?(?:#[a-zA-Z0-9-_]*)?$"
    return this.matches(pattern.toRegex())
}

fun String.replaceHttps(): String {
    return this.replace("http://", "")
        .replace("https://", "")
}


fun Context.openLink(link : String){
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
    intent.data = android.net.Uri.parse(link)
    startActivity(intent)
}