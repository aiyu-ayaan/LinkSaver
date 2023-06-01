package com.atech.linksaver.ui.main_activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.linksaver.NavHostDirections
import com.atech.linksaver.R
import com.atech.linksaver.databinding.ActivityMainBinding
import com.atech.linksaver.work_manager.LoadImageManager
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewBinding()
    private var fromIntent: Boolean = false

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
        workManager()
    }

    private fun checkForIntent() {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            fromIntent = true
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            Log.d(TAG, "checkForIntent: $sharedText")
            sharedText?.let {
                val action = NavHostDirections.actionGlobalAddBottomSheetFragment(it, true)
                navController.navigate(action)
            }
        }
    }

    private fun workManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        val workManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            OneTimeWorkRequestBuilder<LoadImageManager>()
                /*.setInitialDelay(Duration.ofSeconds(5))*/
                .setConstraints(
                    constraints
                )
                .setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.LINEAR,
                    duration = Duration.ofSeconds(5)
                )
                .build()
        } else {
            OneTimeWorkRequestBuilder<LoadImageManager>()
                /*   .setInitialDelay(5, TimeUnit.SECONDS)*/
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    5,
                    TimeUnit.SECONDS
                )
                .build()
        }
        WorkManager.getInstance(applicationContext).enqueue(workManager)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        checkForIntent()
    }


    override fun onPause() {
        super.onPause()
        if (fromIntent) {
            finish()
        }
    }


    companion object {
        const val TAG = "AAA"
    }
}