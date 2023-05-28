package com.atech.linksaver.ui.fragment.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.atech.core.data.model.LinkModel
import com.atech.core.data.use_cases.LinkUseCases
import com.atech.core.util.isLink
import com.atech.linksaver.databinding.BottomSheetAddBinding
import com.atech.linksaver.utils.closeKeyboard
import com.atech.linksaver.utils.openKeyboard
import com.atech.urlimageloader.kotlin.UrlImageLoader.Companion.getLinkDetailsUrl
import com.atech.urlimageloader.utils.extractQueryFromUrl
import com.atech.urlimageloader.utils.makeValidUrl
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetAddBinding

    @Inject
    lateinit var testCases: LinkUseCases

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetAddBinding.inflate(layoutInflater)

        binding.apply {
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
                getLinkDetailsUrl(link.extractQueryFromUrl()) { details, error ->
                    if (error != null) {
                        val model = LinkModel(
                            url = link.makeValidUrl(),
                            title = null,
                            description = null,
                            icon = null,
                            thumbnail = null
                        )
                        addLink(model)
                    } else {
                        val model = LinkModel(
                            url = link.makeValidUrl(),
                            title = details?.title,
                            description = details?.description,
                            icon = details?.iconLink,
                            thumbnail = details?.imageLink
                        )
                        addLink(model)
                    }
                }
            }
            closeButton()
        }
        return binding.root
    }

    private fun addLink(model: LinkModel) = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            testCases.insertLink.invoke(model)
            context?.closeKeyboard(binding.textInputLayoutLink.editText!!)
            dismiss()
        }
    }

    private fun BottomSheetAddBinding.closeButton() {
        materialClose.setOnClickListener {
            context?.closeKeyboard(binding.textInputLayoutLink.editText!!)
            dismiss()
        }
    }
}