package com.atech.linksaver.ui.fragment.login.restore

import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atech.backup.backup.model.toBackUpModel
import com.atech.backup.utils.BackupKeys
import com.atech.backup.utils.LogInKeys
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentRestoreBinding
import com.atech.linksaver.ui.fragment.backup.BackupViewModel
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "RestoreFragment"

@AndroidEntryPoint
class RestoreFragment : Fragment(R.layout.fragment_restore) {

    private val binding: FragmentRestoreBinding by viewBinding()

    private val viewModel: BackupViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.logInRepository.getUserDataFromFireStore { getUserModel, exception ->
            if (exception != null) {
                binding.setViews(false)
                Log.d(TAG, "onViewCreated: ${exception.message}")
                return@getUserDataFromFireStore
            }
            if (getUserModel == null) {
                binding.setViews(false)
                return@getUserDataFromFireStore
            }
            if (getUserModel.backUpFolderId == null || getUserModel.backUpFileId == null) {
                binding.setViews(false)
                updateRestoreDone()
                navigateToHome()
                return@getUserDataFromFireStore
            }
            binding.setViews(true)
            viewModel.pref.edit().apply {
                putString(BackupKeys.BACK_UP_FOLDER_ID.name, getUserModel.backUpFolderId)
                putString(BackupKeys.BACK_UP_FILE_ID.name, getUserModel.backUpFileId)
                Log.d(
                    TAG,
                    "onViewCreated: ${getUserModel.backUpFolderId} , ${getUserModel.backUpFileId}"
                )
            }.apply()
        }
        binding.buttonRestore.setOnClickListener {
            handleBackUp()
        }
        binding.buttonSkip.setOnClickListener {
            viewModel.pref.edit().apply {
                putBoolean(LogInKeys.IS_PERMANENT_SKIP.name, true)
            }.apply()
            navigateToHome()
        }
    }

    private fun handleBackUp() = lifecycleScope.launch {
        binding.progressIndicatorRestoreHorizontal.isVisible = true
        try {
            viewModel.restore().apply {
                viewModel.addAllLinks(toBackUpModel().links)
            }
            updateRestoreDone()
            navigateToHome()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "handleBackUp: ${e.message}")
            updateRestoreDone()
            navigateToHome()
        }
    }

    private fun updateRestoreDone() {
        viewModel.pref.edit().apply {
            putBoolean(LogInKeys.IS_RESTORE_DONE.name, true)
        }.apply()
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