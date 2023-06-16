package com.atech.linksaver.ui.fragment.add

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.work.BackoffPolicy
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.core.util.isLink
import com.atech.linksaver.databinding.BottomSheetAddBinding
import com.atech.linksaver.utils.closeKeyboard
import com.atech.linksaver.utils.launchWhenStarted
import com.atech.linksaver.utils.openKeyboard
import com.atech.linksaver.work_manager.WorkMangerType
import com.atech.linksaver.work_manager.initWorkManagerOneTime
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.util.concurrent.TimeUnit
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
            context?.openKeyboard(
                if (args.fromIntent) {
                    binding.textInputLayoutShortDes.requestFocus()
                    binding.textInputLayoutShortDes.editText!!
                } else
                    binding.textInputLayoutLink.editText!!
            )
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

    private fun workManager() {
        initWorkManagerOneTime(requireContext() to WorkMangerType.LOAD_IMAGE, apply = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.LINEAR,
                    duration = Duration.ofSeconds(5)
                )
            else
                setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    5,
                    TimeUnit.SECONDS
                )
        })
    }

    private fun addLink(link: String) = launchWhenStarted {
        testCases.insertLink.invoke(
            link,
            if (binding.textInputLayoutShortDes.editText?.text?.isBlank() == true) " "
            else binding.textInputLayoutShortDes.editText?.text.toString()
        )
        context?.closeKeyboard(binding.textInputLayoutLink.editText!!)
//        workManager()
        dismiss()
    }

    private fun BottomSheetAddBinding.closeButton() {
        materialClose.setOnClickListener {
            context?.closeKeyboard(
                if (args.fromIntent)
                    binding.textInputLayoutShortDes.editText!!
                else
                    binding.textInputLayoutLink.editText!!
            )
            dismiss()
        }
    }
}