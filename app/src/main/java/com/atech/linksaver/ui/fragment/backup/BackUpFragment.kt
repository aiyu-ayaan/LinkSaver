package com.atech.linksaver.ui.fragment.backup

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.atech.backup.backup.LinkSaverDriveManager
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.KEY_BACK_UP_FILE_ID
import com.atech.backup.utils.KEY_BACK_UP_FOLDER_ID
import com.atech.backup.utils.KEY_LAST_BACK_UP_TIME
import com.atech.core.util.convertLongToTime
import com.atech.linksaver.R
import com.atech.linksaver.databinding.BackupDialogBinding
import com.atech.linksaver.databinding.FragmentBackupBinding
import com.atech.linksaver.utils.DialogModel
import com.atech.linksaver.utils.universalDialog
import com.atech.linksaver.work_manager.WorkMangerType
import com.atech.linksaver.work_manager.initWorkManagerOneTime
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "BackUpFragment"

@AndroidEntryPoint
class BackUpFragment : Fragment(R.layout.fragment_backup) {
    private val binding: FragmentBackupBinding by viewBinding()

    @Inject
    lateinit var pref: SharedPreferences

    @Inject
    lateinit var driveManager: LinkSaverDriveManager

    @Inject
    lateinit var logInRepository: LogInRepository

    private lateinit var backupDialog: AlertDialog


    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                resetIDs()
                performCreateBackupWithWorkManger()
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
            setButton()
            tvBackupDate.text = getLastBackup()
        }
    }

    private fun FragmentBackupBinding.setButton() = lifecycleScope.launch {
        btnBackup.isVisible = false
        val p = lifecycleScope.async { driveManager.hasPermission() }
        val pair = p.await()
        btnBackup.isVisible = true
        btnBackup.setOnClickListener {
            Log.d(TAG, "setButton: ${logInRepository.isSignedIn()}")
            if (!logInRepository.isSignedIn()) {
                handleLogIn()
                return@setOnClickListener
            }
            if (!pair.first) {
                activityResult.launch(pair.second)
                return@setOnClickListener
            }
            createBackupDialog()
            performCreateBackupWithWorkManger()
        }
    }

    private fun handleLogIn() {
        DialogModel(title = getString(R.string.log_in),
            message = getString(R.string.log_in_backup_message),
            positiveText = getString(R.string.log_in),
            negativeText = getString(R.string.cancel),
            positiveAction = { dialog ->
                dialog.dismiss()
                navigateToLogin()
            },
            negativeAction = { dialog ->
                dialog.dismiss()
            }).also {
            requireContext().universalDialog(
                it
            )
        }
    }

    private fun navigateToLogin() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        findNavController().navigate(
            BackUpFragmentDirections.actionBackUpFragmentToLogInFragment()
        )
    }


    private fun performCreateBackupWithWorkManger() {
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
                                pref.edit().apply {
                                    putString(
                                        KEY_LAST_BACK_UP_TIME,
                                        System.currentTimeMillis()
                                            .convertLongToTime("dd/MM/yyyy hh:mm:ss a")
                                    )
                                }.apply()
                                backupDialog.dismiss()
                            }

                            WorkInfo.State.FAILED -> {
                                wf.outputData.keyValueMap.forEach {
                                    Log.d(
                                        TAG,
                                        "performCreateBackupWithWorkManger: ${it.key} : ${it.value}"
                                    )
                                }
                                backupDialog.dismiss()
                            }

                            else -> Log.d(TAG, "Worker ${wf.state}")
                        }

                    }
                }
        }
    }


    private fun resetIDs() {
        pref.edit().apply {
            putString(KEY_BACK_UP_FOLDER_ID, null)
            putString(KEY_BACK_UP_FILE_ID, null)
        }.apply()
    }

    private fun getLastBackup() = pref.getString(KEY_LAST_BACK_UP_TIME, null).let {
        if (it != null) getString(R.string.last_backup_s, it)
        else getString(R.string.last_backup_s, getString(R.string.never))
    }


    private fun createBackupDialog() {
        val backupDialogBinding = BackupDialogBinding.inflate(layoutInflater)
        backupDialog =
            MaterialAlertDialogBuilder(requireContext()).setView(backupDialogBinding.root)
                .setCancelable(false).show()
        backupDialog.setOnDismissListener {
            binding.tvBackupDate.text = getLastBackup()
        }
    }

    private fun FragmentBackupBinding.setToolbar() {
        toolbar.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}