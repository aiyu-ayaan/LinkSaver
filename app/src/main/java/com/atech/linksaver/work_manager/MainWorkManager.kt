package com.atech.linksaver.work_manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.KEY_BACK_UP_FILE_ID
import com.atech.backup.utils.KEY_BACK_UP_FOLDER_ID
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.linksaver.R
import com.atech.linksaver.utils.CHANNEL_ID
import com.atech.linksaver.utils.WorkParams
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltWorker
class MainWorkManager @AssistedInject constructor(
    @Assisted private val useCases: LinkUseCases,
    @Assisted private val driveManager: LinkSaverDriveManager,
    @Assisted private val logInRepository: LogInRepository,
    @Assisted private val pref: SharedPreferences,
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(
    context, workerParams
) {
    override suspend fun doWork(): Result {
        return when {
            WorkMangerType.LOAD_IMAGE.name == inputData.getString(WorkMangerType::javaClass.name) -> loadImageForThumbnail(
                useCases
            )

            WorkMangerType.BACKUP.name == inputData.getString(WorkMangerType::javaClass.name) -> createFolder()
            else -> Result.failure()
        }
    }

    private suspend fun createFolder(): Result = suspendCoroutine { scope ->
        driveManager.createFolder(
            onFail = {
                scope.resume(
                    Result.failure(
                        workDataOf(
                            WorkParams.ERROR_MSG to it.message
                        )
                    )
                )
            },
            action = {
                scope.resume(
                    Result.failure(
                        workDataOf(
                            WorkParams.INTENT_ACTION to it
                        )
                    )
                )
            },
        )?.let {
            logInRepository.updateFolderId(it) { exception ->
                if (exception != null) {
                    scope.resume(
                        Result.failure(
                            workDataOf(
                                WorkParams.ERROR_MSG to exception.message
                            )
                        )
                    )
                    return@updateFolderId
                }
                pref.edit().putString(KEY_BACK_UP_FOLDER_ID, it).apply()
                scope.resume(
                    Result.success(
                        workDataOf(
                            WorkParams.FOLDER_ID to it
                        )
                    )
                )
            }
        }
    }

    private suspend fun startForeGroundService() {
        setForeground(
            ForegroundInfo(
                1909, NotificationCompat.Builder(
                    context, CHANNEL_ID
                ).setContentTitle(context.getString(R.string.back_up))
                    .setContentText(context.getString(R.string.back_up_in_progress))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_LOW).build()
            )
        )
    }

    private fun getFolderID(): String? = pref.getString(KEY_BACK_UP_FOLDER_ID, null)

    private fun getFileId(): String? = pref.getString(KEY_BACK_UP_FILE_ID, null)

    companion object {
        const val TAG = "BackupWorkManger"
    }
}