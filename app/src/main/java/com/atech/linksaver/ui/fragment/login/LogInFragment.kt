package com.atech.linksaver.ui.fragment.login

import android.app.Activity
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
        logInRepository.signInWithCredential(credential) {
            if (it != null) {
                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "firebaseAuthWithGoogle: ${it.message}")
                return@signInWithCredential
            }
            navigateToHome()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeStatusBarColor()
        if(logInRepository.isSignedIn()) {
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

    private fun changeStatusBarColor() {
        val windows = requireActivity().window
        windows.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.icon_back)
    }
}