package com.atech.linksaver.ui.main_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.linksaver.NavHostDirections
import com.atech.linksaver.R
import com.atech.linksaver.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewBinding()

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment)
            .navController
    }

    @Inject
    lateinit var cases: LinkUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
        }
        checkForIntent()
    }

    private fun checkForIntent() {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            Log.d(TAG, "checkForIntent: $sharedText")
            sharedText?.let {
                val action = NavHostDirections.actionGlobalAddBottomSheetFragment(it, true)
                navController.navigate(action)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        checkForIntent()
    }


    companion object {
        const val TAG = "AAA"
    }
}