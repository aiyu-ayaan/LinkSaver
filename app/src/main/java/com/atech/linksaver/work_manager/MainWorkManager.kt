package com.atech.linksaver.work_manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.login.LogInRepository
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.linksaver.R
import com.atech.linksaver.utils.CHANNEL_ID
import com.atech.linksaver.utils.ModelConverter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 *
 * This class is used to perform background task
 * Responsible for loading image for thumbnail and backup
 * @link [WorkMangerType] WorkMangerType is used to identify the type of work
 * @link [loadImageForThumbnail] loadImageForThumbnail is used to load image for thumbnail
 * @link [BackupHelper] BackupHelper is used to perform backup
 * @see MainWorkMangerFactory MainWorkMangerFactory is used to create instance of this class
 */
@HiltWorker
class MainWorkManager @AssistedInject constructor(
    @Assisted private val useCases: LinkUseCases,
    @Assisted private val driveManager: LinkSaverDriveManager,
    @Assisted private val logInRepository: LogInRepository,
    @Assisted private val pref: SharedPreferences,
    @Assisted private val converter: ModelConverter,
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

            WorkMangerType.BACKUP.name == inputData.getString(WorkMangerType::javaClass.name) -> {
                val helper = BackupHelper(
                    useCases,
                    driveManager,
                    logInRepository,
                    pref,
                    converter
                )
                helper.createFolderForBackup().let {
                    helper.createFileForBackup()
                }

            }

            else -> Result.failure()
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

    companion object {
        const val TAG = "BackupWorkManger"
    }
}