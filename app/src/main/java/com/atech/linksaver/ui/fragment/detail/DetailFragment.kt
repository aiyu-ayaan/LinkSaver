package com.atech.linksaver.ui.fragment.detail

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.core.util.isLink
import com.atech.core.util.openLink
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentLinkDetailsBinding
import com.atech.linksaver.utils.DELETE_DIALOG
import com.atech.linksaver.utils.launchWhenStarted
import com.atech.linksaver.utils.loadIcon
import com.atech.linksaver.utils.loadImage
import com.atech.linksaver.utils.universalDialog
import com.google.android.material.transition.platform.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_link_details) {
    private val binding: FragmentLinkDetailsBinding by viewBinding()

    private val args: DetailFragmentArgs by navArgs()

    private val model by lazy { args.link }

    @Inject
    lateinit var usedCases: LinkUseCases
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
            openLinkClick()
            saveLinkClick()
            deleteLinkClick()
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

    private fun FragmentLinkDetailsBinding.openLinkClick() {
        materialButtonOpen.setOnClickListener {
            context?.openLink(model.url)
        }
    }

    private fun FragmentLinkDetailsBinding.saveLinkClick() {
        materialButtonSave.setOnClickListener {
            val link = binding.textInputLayoutLink.editText?.text.toString()
            if (link.isEmpty()) {
                binding.textInputLayoutLink.error = "Please enter a link"
                return@setOnClickListener
            }
            if (!link.isLink()) {
                binding.textInputLayoutLink.error = "Please enter a valid link"
                return@setOnClickListener
            }
            updateNote(link)
        }
    }

    private fun FragmentLinkDetailsBinding.deleteLinkClick() {
        materialButtonDelete.setOnClickListener {
            requireContext().universalDialog(DELETE_DIALOG.also {
                it.positiveAction = { dialog ->
                    deleteNote()
                    dialog.dismiss()
                }
            })
        }
    }


    private fun updateNote(link: String) = launchWhenStarted {
        usedCases.updateLink(link, model)
        findNavController().navigateUp()
    }

    private fun deleteNote() = launchWhenStarted {
        usedCases.updateIsDeleted(model)
        findNavController().navigateUp()
    }


    private fun FragmentLinkDetailsBinding.setViews() {
        root.transitionName = model.url
        materialButtonOpen.transitionName = model.title
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