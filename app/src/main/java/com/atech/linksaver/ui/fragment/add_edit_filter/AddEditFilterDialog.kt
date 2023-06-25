package com.atech.linksaver.ui.fragment.add_edit_filter

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.atech.linksaver.R
import com.atech.linksaver.databinding.AddEditFilterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditFilterDialog : DialogFragment() {
    private lateinit var binding: AddEditFilterBinding
    private val viewModel: AddEditFilterViewModel by viewModels()


    private val model
        get() = viewModel.filterModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AddEditFilterBinding.inflate(layoutInflater)
        binding.apply {
            textInputLayoutFilter.editText?.setText(model?.filter)
            textInputLayoutFilter.requestFocus()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                if (model == null) getString(R.string.add_filter)
                else getString(R.string.edit_filter)
            )
            .setView(binding.root)
            .setPositiveButton(
                if (model == null) getString(R.string.add)
                else getString(R.string.update)
            ) { _, _ ->
                setPositiveButtonLogic()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
            }.also { dialog ->
                model?.let {
                    dialog.setNeutralButton(getString(R.string.delete)) { _, _ ->
                        viewModel.deleteFilter(model!!)
                    }
                }
            }.create()
    }

    private fun validateInput(): Boolean {
        binding.textInputLayoutFilter.editText?.text?.let {
            if (it.isEmpty()) {
                binding.textInputLayoutFilter.error = getString(R.string.cant_be_empty)
                return false
            }
        }
        return true
    }

    private fun setPositiveButtonLogic() {
        if (!validateInput()) return
        val filter = binding.textInputLayoutFilter.editText?.text.toString()
        if (model == null) {
            insertFilter(filter)
            return
        }
        updateFilter(filter)
    }

    private fun insertFilter(filter: String) =
        viewModel.addFilter(filter)

    private fun updateFilter(filter: String) = viewModel.updateFilter(filter)
}