package com.atech.linksaver.work_manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.backup.model.BackUpModel
import com.atech.backup.backup.model.toJson
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.BackupKeys
import com.atech.backup.utils.DriveScope
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.core.util.loadImageCallback
import com.atech.linksaver.ui.main_activity.MainActivity
import com.atech.linksaver.utils.ModelConverter
import com.atech.linksaver.utils.WorkParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "MainWorkManagerExt"
@Keep
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

//-------------------------- Load Image --------------------------//
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

//-------------------------- Backup --------------------------//
class BackupHelper constructor(
    private val useCases: LinkUseCases,
    private val driveManager: LinkSaverDriveManager,
    private val logInRepository: LogInRepository,
    private val pref: SharedPreferences,
    private val converter: ModelConverter,
    @DriveScope
    private val coScope: CoroutineScope
) {
    /**
     * Init backup process
     * @return [ListenableWorker.Result] with [WorkParams.FOLDER_ID] if folder created successfully
     */
    suspend fun createFolderForBackup() =
        if (getFolderID() == null) createFolder() // first time
        else ListenableWorker.Result.success(
            workDataOf(
                WorkParams.FOLDER_ID to getFolderID()
            )
        )

    /**
     * Create the folder in google drive and save the folder id in shared preferences
     */
    private suspend fun createFolder(): ListenableWorker.Result = suspendCoroutine { scope ->
        driveManager.createFolder(
            onFail = {
                scope.resume(
                    ListenableWorker.Result.failure(
                        workDataOf(
                            WorkParams.ERROR_MSG to it.message
                        )
                    )
                )
            },
            action = {
                scope.resume(
                    ListenableWorker.Result.failure(
                        workDataOf(
                            WorkParams.ERROR_MSG to "Permission denied"
                        )
                    )
                )
            },
        )?.let {
            logInRepository.updateFolderId(it) { exception ->
                if (exception != null) {
                    scope.resume(
                        ListenableWorker.Result.failure(
                            workDataOf(
                                WorkParams.ERROR_MSG to exception.message
                            )
                        )
                    )
                    return@updateFolderId
                }
                pref.edit().putString(BackupKeys.BACK_UP_FOLDER_ID.name, it).apply()
                scope.resume(
                    ListenableWorker.Result.success(
                        workDataOf(
                            WorkParams.FOLDER_ID to it
                        )
                    )
                )
            }
        }
    }

    suspend fun createFileForBackup() =
        if (getFileId() == null) createFile() // first time
        else updateFile() // update file


    /**
     * Create and push the file to google drive and save the file id in shared preferences
     */
    private suspend fun createFile(): ListenableWorker.Result = withContext(Dispatchers.IO) {
        suspendCoroutine { scope ->
            val def = coScope.async { getDataFromDatabaseAndConvertToJSON() }
            driveManager.uploadFile(
                runBlocking { def.await() }, getFolderID()?: "",
                onFail = {
                    scope.resume(
                        ListenableWorker.Result.failure(
                            workDataOf(
                                WorkParams.ERROR_MSG to it.message
                            )
                        )
                    )
                },
            ) { file ->
                logInRepository.updateFileId(file.id!!) {
                    if (it != null) {
                        scope.resume(
                            ListenableWorker.Result.failure(
                                workDataOf(
                                    WorkParams.ERROR_MSG to it.message
                                )
                            )
                        )
                        return@updateFileId
                    }
                    pref.edit().putString(BackupKeys.BACK_UP_FILE_ID.name, file.id!!).apply()
                    scope.resume(
                        ListenableWorker.Result.success(
                            workDataOf(
                                WorkParams.FILE_ID to "Create!! -> File ID ${file.id} with folder id ${getFolderID()}"
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun updateFile(): ListenableWorker.Result = suspendCoroutine { scope ->
        val def = coScope.async { getDataFromDatabaseAndConvertToJSON() }
        driveManager.updateBackupFile(
            runBlocking { def.await() },
            getFileId()!!,
            onFail = {
                scope.resume(
                    ListenableWorker.Result.failure(
                        workDataOf(
                            WorkParams.ERROR_MSG to it.message
                        )
                    )
                )
            }
        ) {
            scope.resume(
                ListenableWorker.Result.success(
                    workDataOf(
                        WorkParams.FILE_ID to "Updated!! -> File ID ${getFileId()} with folder id ${getFolderID()}"
                    )
                )
            )
        }
    }

    private suspend fun getDataFromDatabaseAndConvertToJSON() = withContext(Dispatchers.IO) {
        BackUpModel(
            useCases.getAllLinksForOnes.invoke().map {
                converter.toDomain(it)
            }
        ).toJson()
    }


    //-------------------------- Helper --------------------------//
    private fun getFolderID(): String? = pref.getString(BackupKeys.BACK_UP_FOLDER_ID.name, null)

    private fun getFileId(): String? = pref.getString(BackupKeys.BACK_UP_FILE_ID.name, null)

}