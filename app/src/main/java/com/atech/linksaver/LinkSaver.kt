package com.atech.linksaver

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LinkSaver : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}