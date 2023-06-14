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
import com.atech.backup.utils.KEY_BACK_UP_FOLDER_ID
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentBackupBinding
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
                    viewModel.updateBackupFolderPath(path)
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
            switchBackup.apply {
                isChecked = getFolderID() != null
                setOnCheckedChangeListener { _, state ->
                    if (state) performBackup()
                }
            }
        }
    }

    private fun getFolderID(): String? =
        pref.getString(KEY_BACK_UP_FOLDER_ID, null)


    private fun performBackup() = lifecycleScope.launch(Dispatchers.IO) {
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
            viewModel.updateBackupFolderPath(path)
        }
    }


    private fun FragmentBackupBinding.setToolbar() {
        toolbar.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}