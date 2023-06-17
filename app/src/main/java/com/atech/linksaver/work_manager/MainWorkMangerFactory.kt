package com.atech.linksaver.work_manager

import android.content.Context
import android.content.SharedPreferences
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.DriveScope
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.linksaver.utils.ModelConverter
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class MainWorkMangerFactory @Inject constructor(
    private val useCases: LinkUseCases,
    private val driveManager: LinkSaverDriveManager,
    private val logInRepository: LogInRepository,
    private val pref: SharedPreferences,
    private val converter: ModelConverter,
    @DriveScope private val scope: CoroutineScope
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = MainWorkManager(
        useCases,
        driveManager,
        logInRepository,
        pref,
        converter,
        scope,
        appContext,
        workerParameters
    )
}