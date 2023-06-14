package com.atech.linksaver.ui.fragment.backup

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.KEY_BACK_UP_FOLDER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackUpViewModel @Inject constructor(
    private val driveManager: LinkSaverDriveManager,
    private val logInRepository: LogInRepository,
    private val pref: SharedPreferences
) : ViewModel() {

    fun isDriveServiceAvailable() = driveManager.isDriveServiceAvailable()

    fun createFolder(
        onFail: (Exception) -> Unit = {},
        action: (Intent) -> Unit = {},
        onComplete: (String) -> Unit = {}
    ) = viewModelScope.launch(Dispatchers.IO) {
        driveManager.createFolder(onFail, action)?.let {
            onComplete(it)
        }
    }


    fun updateBackupFolderPath(folderId: String) {
        logInRepository.updateFolderPath(folderId) {
            pref.edit().putString(KEY_BACK_UP_FOLDER_ID, folderId).apply()
        }
    }
}