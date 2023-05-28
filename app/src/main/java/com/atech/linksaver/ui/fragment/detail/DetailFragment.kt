package com.atech.linksaver.ui.fragment.detail

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentLinkDetailsBinding
import com.atech.linksaver.utils.loadIcon
import com.atech.linksaver.utils.loadImage
import com.google.android.material.transition.platform.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_link_details) {
    private val binding: FragmentLinkDetailsBinding by viewBinding()

    private val args: DetailFragmentArgs by navArgs()

    private val model by lazy { args.link }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeStatusBarColor()
        binding.apply {
            setViews()
            closeIcon()
        }
    }

    private fun changeStatusBarColor() {
        val color = binding.root.cardForegroundColor
        val windows = requireActivity().window
        windows.statusBarColor = color.defaultColor
    }

    private fun FragmentLinkDetailsBinding.closeIcon() {
        materialButtonClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun FragmentLinkDetailsBinding.setViews() {
        root.transitionName = model.url
        textInputLayoutLink.apply {
            editText?.setText(model.url)
            editText?.requestFocus()
        }

        if (model.title.isNullOrEmpty()) {
            textInputLayoutTitle.isVisible = false
        } else {
            textInputLayoutTitle.isVisible = true
            textInputLayoutTitle.editText?.setText(model.title)
        }
        if (model.description.isNullOrEmpty() || model.description == model.title) {
            textInputLayoutDescription.isVisible = false
        } else {
            textInputLayoutDescription.isVisible = true
            textInputLayoutDescription.editText?.setText(model.description)
        }
        if (model.thumbnail.isNullOrEmpty()) imageViewThumbnail.isVisible = false
        else {
            imageViewThumbnail.isVisible = true
            imageViewThumbnail.loadImage(model.thumbnail!!)
        }
        if (model.icon.isNullOrEmpty()) {
            imageViewIcon.load(R.drawable.avatar_svgrepo_com)
        } else imageViewIcon.loadIcon(model.icon!!)

    }
}