package com.atech.linksaver

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import com.atech.linksaver.utils.CHANNEL_ID
import com.atech.linksaver.work_manager.backup.BackupWorkManagerFactory
import com.atech.linksaver.work_manager.MainWorkMangerFactory
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LinkSaver : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: MainWorkMangerFactory

    @Inject
    lateinit var backupWorkManagerFactory: BackupWorkManagerFactory
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(R.string.back_up), NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(
                channel
            )
        }
    }


    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder().setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

}