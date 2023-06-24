package com.atech.backup.backup

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.Keep
import com.atech.backup.backup.MimeType.JSON
import com.atech.backup.utils.BACK_UP_FILE_NAME
import com.atech.backup.utils.BACK_UP_FOLDER_NAME
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LinkSaverDriveManager"

@Keep
private object MimeType {
    const val JSON: String = "application/json"
    const val FOLDER = "application/vnd.google-apps.folder"
}

@Singleton
class LinkSaverDriveManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun provideDriveService(): Drive? =
        createDriveInstance()


    private fun createDriveInstance(): Drive? {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        googleSignInAccount?.let { account ->
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Scopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            return Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(BACK_UP_FOLDER_NAME)
                .build()
        }
        return null
    }

    fun reCreateDrive() {
        createDriveInstance()
    }

    fun isDriveServiceAvailable() = provideDriveService() != null


    suspend fun hasPermission(): Pair<Boolean, Intent?> = withContext(Dispatchers.IO) {
        try {
            provideDriveService()?.files()?.list()?.execute()
            Pair(true, null)
        } catch (e: UserRecoverableAuthIOException) {
            Pair(false, e.intent)
        } catch (e: Exception) {
            Pair(false, null)
        }
    }

    fun createFolder(
        onFail: (Exception) -> Unit = {}, action: (Intent) -> Unit = {}
    ): String? {
        var folder: String? = null
        if (!isDriveServiceAvailable()) {
            Log.d(TAG, "createFolder: drive is not available")
            onFail(Exception("Drive is not available"))
            return null
        }
        try {
            val folderDate = File().setMimeType(MimeType.FOLDER).setName(BACK_UP_FOLDER_NAME)
            val folderId = provideDriveService()?.files()?.create(folderDate)?.execute()?.id
            folder = folderId
        } catch (e: UserRecoverableAuthIOException) {
            val intent = e.intent
            action(intent)
        } catch (e: Exception) {
            onFail(e)
        }
        return folder
    }

    fun uploadFile(
        jsonData: String,
        folderId: String,
        onFail: (Exception) -> Unit = {},
        onProgress: (Int) -> Unit = {},
        onSuccess: (FileData) -> Unit = {}
    ) {
        provideDriveService()?.let {
            try {
                val fileMetadata =
                    File().setParents(listOf(folderId)).setMimeType(JSON).setName(BACK_UP_FILE_NAME)

                val byteArrayContent = ByteArrayContent.fromString(JSON, jsonData)
                val uploader = it.files().create(fileMetadata, byteArrayContent).apply {
                    mediaHttpUploader.isDirectUploadEnabled = false
                    mediaHttpUploader.setProgressListener { uploader ->
                        onProgress((uploader.progress * 100).toInt())
                    }
                }
                val file = uploader.execute()
                onSuccess(
                    FileData(
                        id = file.id,
                        name = file.name,
                        webContentLink = file.webContentLink,
                        webViewLink = file.webViewLink
                    )
                )
            } catch (e: Exception) {
                onFail(e)
            }
        } ?: onFail(Exception("Drive is not available"))
    }

    fun updateBackupFile(
        jsonData: String,
        fileId: String,
        onFail: (Exception) -> Unit = {},
        onProgress: (Int) -> Unit = {},
        onSuccess: (FileData) -> Unit = {}
    ) {
        provideDriveService()?.let {
            try {
                val fileMetadata = File().setMimeType(JSON).setName(BACK_UP_FILE_NAME)

                val byteArrayContent = ByteArrayContent.fromString(JSON, jsonData)
                val uploader = it.files().update(fileId, fileMetadata, byteArrayContent).apply {
                    mediaHttpUploader.isDirectUploadEnabled = false
                    mediaHttpUploader.setProgressListener { uploader ->
                        onProgress((uploader.progress * 100).toInt())
                    }
                }
                val file = uploader.execute()
                onSuccess(
                    FileData(
                        id = file.id,
                        name = file.name,
                        webContentLink = file.webContentLink,
                        webViewLink = file.webViewLink
                    )
                )
            } catch (e: Exception) {
                onFail(e)
            }
        } ?: onFail(Exception("Drive is not available"))
    }

    fun restoreBackupFile(
        fileId: String,
        onFail: (Exception) -> Unit = {},
        onSuccess: (String) -> Unit = {}
    ) {
        provideDriveService()?.let {
            try {
                val file = it.files().get(fileId).execute()
                val fileContent = it.files().get(fileId).executeMediaAsInputStream()
                val fileData = fileContent.bufferedReader().use { it.readText() }
                onSuccess(fileData)
            } catch (e: Exception) {
                onFail(e)
            }
        } ?: onFail(Exception("Drive is not available"))
    }


    @Keep
    data class FileData(
        val id: String?, val name: String?, val webContentLink: String?, val webViewLink: String?
    )
}