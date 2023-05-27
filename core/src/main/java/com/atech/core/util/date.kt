package com.atech.core.util

import android.annotation.SuppressLint
import android.os.Build
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@SuppressLint("SimpleDateFormat")
fun Long.convertLongToTime(format: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern(format, Locale.getDefault())
            .format(Instant.ofEpochMilli(this))
    } else
        SimpleDateFormat(format).format(Date(this))

}


fun Long.getDate(): String {
    val currentTime = System.currentTimeMillis()
    val difference = currentTime - this
    val seconds = difference / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    return when {
        seconds < 60 -> {
            " Just now"
        }

        minutes < 60 -> {
            " $minutes minutes ago"
        }

        hours < 24 -> {
            " $hours hours ago"
        }

        days < 30 -> {
            " ${this.convertLongToTime("dd MMM")}"
        }

        months < 12 -> {
            " ${this.convertLongToTime("dd MMM")}"
        }

        else -> {
            " ${this.convertLongToTime("dd MMM yyyy")}"
        }

    }
}