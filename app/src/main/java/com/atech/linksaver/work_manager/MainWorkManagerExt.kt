package com.atech.linksaver.work_manager

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.core.util.loadImageCallback
import com.atech.linksaver.ui.main_activity.MainActivity
import kotlinx.coroutines.runBlocking


enum class WorkMangerType {
    LOAD_IMAGE,
    BACKUP,
}


inline fun initWorkManagerOneTime(
    builder: Pair<Context, WorkMangerType>,
    crossinline apply: OneTimeWorkRequest.Builder.() -> Unit = {},
    crossinline manager: (WorkManager, OneTimeWorkRequest) -> Unit = { _, _ -> }
) {
    val workRequest = OneTimeWorkRequestBuilder<MainWorkManager>()
        .setConstraints(
            Constraints.Builder().setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).build()
        ).setInputData(
            Data.Builder().putString(WorkMangerType::javaClass.name, builder.second.name).build()
        ).apply(apply)
        .build()

    val workManger = WorkManager.getInstance(builder.first)
    workManger.enqueue(workRequest)
    manager(workManger, workRequest)
}

suspend fun loadImageForThumbnail(useCases: LinkUseCases): ListenableWorker.Result {
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
        return ListenableWorker.Result.success()
    } catch (e: Exception) {
        Log.e(MainActivity.TAG, "doWork error : ${e.message}")
        return ListenableWorker.Result.failure()
    }
}