package com.atech.linksaver.work_manager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.core.util.loadImageCallback
import com.atech.linksaver.ui.main_activity.MainActivity.Companion.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking

@HiltWorker
class LoadImageManager @AssistedInject constructor(
    @Assisted private val useCases: LinkUseCases,
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(
    context,
    workerParams
) {
    override suspend fun doWork(): Result {
        try {
            useCases.getAllLinksNotLoaded.invoke().collect {
                if (it.isNotEmpty()) {
                    it.forEach { link ->
                        val linkModel = runBlocking { loadImageCallback(link.url) }
                        link.copy(
                            title = linkModel.title,
                            description = linkModel.description,
                            icon = linkModel.icon,
                            thumbnail = linkModel.thumbnail,
                        ).let { link ->
                            runBlocking { useCases.updateIsThumbnailLoaded.invoke(link) }
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.d(TAG, "doWork error : ${e.message}")
            return Result.failure()
        }

    }
}