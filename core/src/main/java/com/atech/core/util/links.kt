package com.atech.core.util

import android.content.Context


fun String.isLink(): Boolean {
    val regex = "(?i)((http|https)://)?(www\\.)?[a-zA-Z0-9@:%._\\+~#?&//=]" +
            "{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)"
    return this.matches(regex.toRegex())
}

fun String.replaceHttps(): String {
    return this.replace("http://", "")
        .replace("https://", "")
}


fun Context.openLink(link: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
    intent.data = android.net.Uri.parse(link)
    startActivity(intent)
}