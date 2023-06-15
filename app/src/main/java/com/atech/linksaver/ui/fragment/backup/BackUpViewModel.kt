package com.atech.linksaver.ui.fragment.backup

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.backup.model.BackUpModel
import com.atech.backup.backup.model.toJson
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.KEY_BACK_UP_FILE_ID
import com.atech.backup.utils.KEY_BACK_UP_FOLDER_ID
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.linksaver.utils.ModelConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackUpViewModel @Inject constructor(
    private val driveManager: LinkSaverDriveManager,
    private val logInRepository: LogInRepository,
    private val testCases: LinkUseCases,
    private val pref: SharedPreferences,
    private val converter: ModelConverter
) : ViewModel() {

    fun isDriveServiceAvailable() = driveManager.isDriveServiceAvailable()

    fun getAllLinks() = BackUpModel(
        testCases.getAllLinksForOnes.invoke().map {
            converter.toDomain(it)
        }
    ).toJson()

    fun createFolder(
        onFail: (Exception) -> Unit = {},
        action: (Intent) -> Unit = {},
        onComplete: (String) -> Unit = {}
    ) = viewModelScope.launch(Dispatchers.IO) {
        driveManager.createFolder(onFail, action)?.let {
            onComplete(it)
        }
    }

    fun uploadFile(
        jsonData: String,
        folderId: String,
        onFail: (Exception) -> Unit = {},
        onProgress: (Int) -> Unit = {},
        onComplete: (LinkSaverDriveManager.FileData) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        driveManager.uploadFile(jsonData, folderId, onFail, onProgress, onComplete)
    }

    fun updateFile(
        jsonData: String,
        fileId: String,
        onFail: (Exception) -> Unit = {},
        onProgress: (Int) -> Unit = {},
        onComplete: (LinkSaverDriveManager.FileData) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        driveManager.updateBackupFile(jsonData, fileId, onFail, onProgress, onComplete)
    }


    @Throws(Exception::class)
    fun updateBackupFolderIdFirebase(folderId: String) {
        logInRepository.updateFolderId(folderId) {
            if (it != null) throw it

            pref.edit().putString(KEY_BACK_UP_FOLDER_ID, folderId).apply()
        }
    }

    @Throws(Exception::class)
    fun updateBackupFileIdFirebase(fileId: String) {
        logInRepository.updateFileId(fileId) {
            if (it != null) throw it
            pref.edit().putString(KEY_BACK_UP_FILE_ID, fileId).apply()
        }
    }
}