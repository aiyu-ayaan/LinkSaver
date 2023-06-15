package com.atech.linksaver

import android.app.Application
import androidx.work.Configuration
import com.atech.linksaver.work_manager.load_image.LoadWorkMangerFactory
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LinkSaver : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: LoadWorkMangerFactory
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

}