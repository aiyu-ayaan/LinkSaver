package com.atech.linksaver.work_manager.load_image

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.atech.core.data.use_cases.LinkUseCases
import javax.inject.Inject

class LoadWorkMangerFactory  @Inject constructor(
    private val useCases: LinkUseCases
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = LoadImageManager(
        useCases,
        appContext,
        workerParameters
    )
}