package com.atech.linksaver.ui.fragment.backup

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.backup.model.LinkBackUpModel
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.BackupKeys
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.linksaver.utils.ModelConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class BackupViewModel @Inject constructor(
    val logInRepository: LogInRepository,
    val pref: SharedPreferences,
    private val driveManager: LinkSaverDriveManager,
    private val converter: ModelConverter,
    private val useCases: LinkUseCases
) : ViewModel() {

    suspend fun restore() = withContext(Dispatchers.IO) {
        suspendCoroutine { scope ->
            driveManager.restoreBackupFile(
                pref.getString(BackupKeys.BACK_UP_FILE_ID.name, null)!!,
                onFail = {
                    scope.resumeWith(Result.failure(it))
                }
            ) {
                scope.resumeWith(Result.success(it))
            }
        }
    }


    fun addAllLinks(list: List<LinkBackUpModel>) = viewModelScope.launch {
        useCases.insertLinks(converter.toEntityList(list))
    }

}