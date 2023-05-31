package com.atech.linksaver.ui.fragment.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.core.util.isLink
import com.atech.linksaver.databinding.BottomSheetAddBinding
import com.atech.linksaver.utils.closeKeyboard
import com.atech.linksaver.utils.launchWhenStarted
import com.atech.linksaver.utils.openKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddBottomSheetFragment : BottomSheetDialogFragment() {

    private val args: AddBottomSheetFragmentArgs by navArgs()

    private lateinit var binding: BottomSheetAddBinding

    @Inject
    lateinit var testCases: LinkUseCases

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetAddBinding.inflate(layoutInflater)
        binding.apply {
            binding.textInputLayoutLink.editText?.setText(args.url)
            context?.openKeyboard(binding.textInputLayoutLink.editText!!)
            binding.materialDone.setOnClickListener {
                val link = binding.textInputLayoutLink.editText?.text.toString()
                if (link.isEmpty()) {
                    binding.textInputLayoutLink.error = "Please enter a link"
                    return@setOnClickListener
                }
                if (!link.isLink()) {
                    binding.textInputLayoutLink.error = "Please enter a valid link"
                    return@setOnClickListener
                }
                addLink(link)
            }
            closeButton()
        }
        return binding.root
    }

    private fun addLink(link: String) = launchWhenStarted {
//        if (args.fromIntent)
//            testCases.insertFromIntent.invoke(
//                link
//            )
//        else
            testCases.insertLink.invoke(
                link
            )

        context?.closeKeyboard(binding.textInputLayoutLink.editText!!)
        dismiss()
    }

    private fun BottomSheetAddBinding.closeButton() {
        materialClose.setOnClickListener {
            context?.closeKeyboard(binding.textInputLayoutLink.editText!!)
            dismiss()
        }
    }
}