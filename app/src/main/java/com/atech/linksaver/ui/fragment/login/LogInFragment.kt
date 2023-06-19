package com.atech.linksaver.ui.fragment.login

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.atech.backup.login.LogInRepository
import com.atech.backup.utils.LogInKeys
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentLoginBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "LogInFragment"

@AndroidEntryPoint
class LogInFragment : Fragment(R.layout.fragment_login) {
    private val binding: FragmentLoginBinding by viewBinding()

    @Inject
    lateinit var logInRepository: LogInRepository

    @Inject
    lateinit var pref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "${it.resultCode}")
            if (it.resultCode == Activity.RESULT_OK) {
                val task = logInRepository.getSignInAccount(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account.idToken == null) {
                        Log.d(TAG, "firebaseAuthWithGoogle: idToken is null")
                        return@registerForActivityResult
                    }
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }

    private fun firebaseAuthWithGoogle(token: String) {
        val credential = logInRepository.getCredentials(token)
        logInRepository.signInWithCredential(credential) { (isNewUser, exception) ->
            if (exception != null) {
                Toast.makeText(requireContext(), "${exception.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "firebaseAuthWithGoogle: ${exception.message}")
                return@signInWithCredential
            }
            if (isNewUser) {
                pref.edit()
                    .apply {
                        putBoolean(LogInKeys.IS_RESTORE_DONE.name, true)
                    }.apply()
                navigateToHome()
                return@signInWithCredential
            }
            navigateToRestore()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeStatusBarColor()
        val isRestoreDone = pref.getBoolean(LogInKeys.IS_RESTORE_DONE.name, false)
        val isPermanentlySkip = pref.getBoolean(LogInKeys.IS_PERMANENT_SKIP.name, false)
        if (logInRepository.isSignedIn() && !isRestoreDone && !isPermanentlySkip) {
            navigateToRestore()
            return // to avoid the code below
        }
        if (logInRepository.isSignedIn()) {
            navigateToHome()
        }
        binding.apply {
            signInButton.setOnClickListener {
                performLogin()
            }
            buttonSkip.setOnClickListener {
                navigateToHome()
            }
        }
    }

    private fun performLogin() {
        val signInIntent = logInRepository.googleSignInClient.signInIntent
        activityResult.launch(signInIntent)
    }

    private fun navigateToHome() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        findNavController().navigate(
            LogInFragmentDirections.actionLogInFragmentToHomeFragment()
        )
    }

    private fun navigateToRestore() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        findNavController().navigate(
            LogInFragmentDirections.actionLogInFragmentToRestoreFragment()
        )
    }

    private fun changeStatusBarColor() {
        val windows = requireActivity().window
        windows.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.icon_back)
    }
}