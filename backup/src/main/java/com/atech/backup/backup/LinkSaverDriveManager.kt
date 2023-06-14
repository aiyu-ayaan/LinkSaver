package com.atech.backup.backup

import android.content.Intent
import android.util.Log
import com.atech.backup.backup.MimeType.JSON
import com.atech.backup.utils.BACK_UP_FILE_NAME
import com.atech.backup.utils.BACK_UP_FOLDER_NAME
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LinkSaverDriveManager"

private object MimeType {
    const val JSON: String = "application/json"
    const val FOLDER = "application/vnd.google-apps.folder"
}

@Singleton
class LinkSaverDriveManager @Inject constructor(
    private val drive: Drive?
) {

    fun isDriveServiceAvailable() = drive != null


    fun createFolder(
        onFail: (Exception) -> Unit = {},
        action: (Intent) -> Unit = {}
    ): String? {
        var folder: String? = null
        if (!isDriveServiceAvailable()) {
            Log.d(TAG, "createFolder: drive is not available")
            onFail(Exception("Drive is not available"))
            return null
        }
        try {
            val folderDate = File()
                .setMimeType(MimeType.FOLDER)
                .setName(BACK_UP_FOLDER_NAME)
            val folderId = drive?.files()?.create(folderDate)?.execute()?.id
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
        onSuccess: (FileData) -> Unit = {}
    ) {
        drive!!.let {
            try {
                val fileMetadata = File()
                    .setParents(listOf(folderId))
                    .setMimeType(JSON)
                    .setName(BACK_UP_FILE_NAME)

                val byteArrayContent = ByteArrayContent.fromString(JSON, jsonData)
                it.files().create(fileMetadata, byteArrayContent).execute().let { file ->
                    Log.d(
                        TAG,
                        "uploadFile: File is uploaded successfully ${file.id} , ${file.name}"
                    )
                    onSuccess(
                        FileData(
                            id = file.id,
                            name = file.name,
                            webContentLink = file.webContentLink,
                            webViewLink = file.webViewLink
                        )
                    )
                }
            } catch (e: Exception) {
                onFail(e)
            }
        }
    }

    data class FileData(
        val id: String?,
        val name: String?,
        val webContentLink: String?,
        val webViewLink: String?
    )
}