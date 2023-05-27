package com.atech.linksaver.ui.main_activity

import android.os.Bundle
import android.viewbinding.library.activity.viewBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.atech.linksaver.R
import com.atech.linksaver.databinding.ActivityMainBinding
import com.atech.urlimageloader.utils.makeValidUrl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewBinding()

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment)
            .navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
        }
    }
}