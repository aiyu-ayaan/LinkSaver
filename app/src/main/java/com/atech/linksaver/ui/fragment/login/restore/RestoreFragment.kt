package com.atech.linksaver.ui.fragment.login.restore

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.BackupKeys
import com.atech.backup.utils.LogInKeys
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentRestoreBinding
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "RestoreFragment"

@AndroidEntryPoint
class RestoreFragment : Fragment(R.layout.fragment_restore) {

    private val binding: FragmentRestoreBinding by viewBinding()

    @Inject
    lateinit var logInRepository: LogInRepository

    @Inject
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logInRepository.getUserDataFromFireStore { getUserModel, exception ->
            if (exception != null) {
                binding.setViews(false)
                Log.d(TAG, "onViewCreated: ${exception.message}")
                return@getUserDataFromFireStore
            }
            if (getUserModel == null) {
                binding.setViews(false)
                return@getUserDataFromFireStore
            }
            binding.setViews(true)
            pref.edit().apply {
                putString(BackupKeys.BACK_UP_FOLDER_ID.name, getUserModel.backUpFolderId)
                putString(BackupKeys.BACK_UP_FILE_ID.name, getUserModel.backUpFileId)
                Log.d(
                    TAG,
                    "onViewCreated: ${getUserModel.backUpFolderId} , ${getUserModel.backUpFileId}"
                )
            }.apply()
        }
        binding.buttonRestore.setOnClickListener {
            navigateToHome()
        }
        binding.buttonSkip.setOnClickListener {
            pref.edit().apply {
                putBoolean(LogInKeys.IS_PERMANENT_SKIP.name, true)
            }.apply()
            navigateToHome()
        }
    }

    private fun FragmentRestoreBinding.setViews(isLoading: Boolean) {
        relativeLayoutRestore.isVisible = isLoading
        progressIndicatorRestore.isVisible = isLoading.not()
        textViewLoading.isVisible = isLoading.not()
    }

    private fun navigateToHome() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        findNavController().navigate(
            RestoreFragmentDirections.actionRestoreFragmentToHomeFragment()
        )
    }
}