package com.atech.linksaver.ui.fragment.backup

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.atech.backup.utils.KEY_BACK_UP_FILE_ID
import com.atech.backup.utils.KEY_BACK_UP_FOLDER_ID
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentBackupBinding
import com.atech.linksaver.work_manager.WorkMangerType
import com.atech.linksaver.work_manager.initWorkManagerOneTime
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BackUpFragment"

@AndroidEntryPoint
class BackUpFragment : Fragment(R.layout.fragment_backup) {
    private val binding: FragmentBackupBinding by viewBinding()
    private val viewModel: BackUpViewModel by viewModels()

    @Inject
    lateinit var pref: SharedPreferences


    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.createFolder(onFail = {
                    Log.d(TAG, "performBackup: ${it.message}")
                }, action = {
                    Log.d(TAG, "performBackup: $it")
                }) { path ->
                    if (getFolderID() != null) return@createFolder
                    Log.d(TAG, "performBackup: $path")
                    try {
                        viewModel.updateBackupFolderIdFirebase(path)
                    } catch (e: Exception) {
                        Log.e(TAG, "performBackup: ${e.message}")
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setToolbar()
            setSwitch()
            binding.btnBackup.setOnClickListener {}
        }
    }

    private fun performBackup() = lifecycleScope.launch(Dispatchers.IO) {
        val content = viewModel.getAllLinks()
        if (getFileId() == null) createFirstTimeBack(content)
        else updateUpload(content)
    }

    private fun updateUpload(content: String) {
        viewModel.updateFile(content, getFileId()!!, onFail = {
            Log.e(TAG, "performBackup Error : ${it.message}")
        }, onProgress = {
            Log.d(TAG, "performBackup: $it")
        }) {
            Log.d(TAG, "performBackup: Updated $it")
        }
    }

    private fun createFirstTimeBack(content: String) {
        viewModel.uploadFile(content, getFolderID()!!, onFail = {
            Log.e(TAG, "performBackup Error : ${it.message}")
        }, onProgress = {
            Log.d(TAG, "performBackup: $it")
        }) { fileData ->
            Log.d(TAG, "performBackup: $fileData")
            try {
                viewModel.updateBackupFileIdFirebase(fileData.id!!)
            } catch (e: Exception) {
                Log.e(TAG, "performBackup: ${e.message}")
            }
        }
    }

    private fun FragmentBackupBinding.setSwitch() {
        switchBackup.apply {
//            isChecked = getFolderID() != null
            setOnCheckedChangeListener { _, state ->
                performCreateBackupWithWorkManger(state)
            }
        }
    }

    private fun performCreateBackupWithWorkManger(state: Boolean) {
        initWorkManagerOneTime(
            requireContext() to WorkMangerType.BACKUP,
        ) { workManager, oneTimeWorkRequest ->
            workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
                .observe(viewLifecycleOwner) { workInfo ->
                    workInfo?.let { wf ->
                        when (wf.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                wf.outputData.keyValueMap.forEach {
                                    Log.d(
                                        TAG,
                                        "performCreateBackupWithWorkManger: ${it.key} : ${it.value}"
                                    )
                                }
                            }

                            WorkInfo.State.FAILED -> {
                                Log.d(TAG, "Worker FAILED")
//                            if (wf.outputData) TODO: Handle Intent
                            }

                            else -> Log.d(TAG, "Worker ${wf.state}")
                        }

                    }
                }
        }
//        val backUpRequest = OneTimeWorkRequestBuilder<BackupWorkManger>().setConstraints(
//            Constraints.Builder().setRequiresBatteryNotLow(true)
//                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).build()
//        ).setInputData(
//            Data.Builder().putString(BACK_UP_NOW, BACK_UP_NOW).build()
//        ).build()
//        val workManger = WorkManager.getInstance(requireContext())
//        if (!state) {
//            workManger.cancelAllWork()
//            return
//        }
//        workManger.enqueue(backUpRequest)
//
//        workManger.getWorkInfoByIdLiveData(backUpRequest.id)
//            .observe(viewLifecycleOwner) { workInfo ->
//                workInfo?.let { wf ->
//                    when (wf.state) {
//
//                        WorkInfo.State.SUCCEEDED -> {
//                            wf.outputData.keyValueMap.forEach {
//                                Log.d(
//                                    TAG,
//                                    "performCreateBackupWithWorkManger: ${it.key} : ${it.value}"
//                                )
//                            }
//                        }
//
//                        WorkInfo.State.FAILED -> {
//                            Log.d(TAG, "Worker FAILED")
////                            if (wf.outputData) TODO: Handle Intent
//                        }
//
//                        else -> Log.d(TAG, "Worker ${wf.state}")
//                    }
//
//                }
//            }
    }

    private fun getFolderID(): String? = pref.getString(KEY_BACK_UP_FOLDER_ID, null)

    private fun getFileId(): String? = pref.getString(KEY_BACK_UP_FILE_ID, null)

    private fun performCreateBackup() = lifecycleScope.launch(Dispatchers.IO) {
        if (getFolderID() != null) {
            Log.d(TAG, "performBackup: ${getFolderID()}")
            return@launch
        }
        viewModel.createFolder(onFail = {
            Log.d(TAG, "performBackup: ${it.message}")
        }, action = {
            Log.d(TAG, "performBackup: $it")
            activityResult.launch(it)
        }) { path ->
            Log.d(TAG, "performBackup: $path")
            try {
                viewModel.updateBackupFolderIdFirebase(path)
            } catch (e: Exception) {
                Log.e(TAG, "performBackup: ${e.message}")
            }
        }
    }


    private fun FragmentBackupBinding.setToolbar() {
        toolbar.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}