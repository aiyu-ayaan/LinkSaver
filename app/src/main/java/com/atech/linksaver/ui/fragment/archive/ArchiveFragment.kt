package com.atech.linksaver.ui.fragment.archive

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.CheckBox
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atech.core.data.model.LinkModel
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentArchiveBinding
import com.atech.linksaver.ui.fragment.home.adapter.LinkAdapter
import com.atech.linksaver.utils.DELETE_DIALOG
import com.atech.linksaver.utils.addOnContextualMenuListener
import com.atech.linksaver.utils.universalDialog
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchiveFragment : Fragment(R.layout.fragment_archive) {
    private val binding: FragmentArchiveBinding by viewBinding()
    private val viewModel by viewModels<ArchiveViewModel>()
    private lateinit var archiveAdapter: LinkAdapter

    private val selectedItem: MutableLiveData<HashSet<LinkModel>> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        changeStatusBarColor()
        binding.apply {
            toolbar()
            setRecyclerView()
        }
    }

    private fun setRecyclerView() {
        binding.recyclerView.apply {
            adapter = LinkAdapter(
                onItemLongClicked = ::onLongClicked,
                onItemClicked = ::setClickLogic
            ).also {
                archiveAdapter = it
            }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        observeViewList()
    }

    private fun setClickLogic(
        m: Pair<LinkModel, View>, isLongClick: Boolean, checkBox: CheckBox
    ) {
        if (isLongClick) {
            selectedItem.value = selectedItem.value?.apply {
                if (contains(m.first)) {
                    checkBox.isChecked = false
                    checkBox.isVisible = false
                    remove(m.first)
                } else {
                    add(m.first)
                }
            } ?: hashSetOf(m.first)
            return
        }
        navigateToDetailFragment(m)
    }

    private fun onLongClicked() {
        val callback = addOnContextualMenuListener(onCreate = { mode, menu ->
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.contextual_action_bar, menu)
            menu?.findItem(R.id.menu_add_to_archive)?.isVisible = false
            menu?.findItem(R.id.menu_restore)?.isVisible = true
            true
        }, onActionItemClicked = { _, item ->
            when (item?.itemId) {
                R.id.menu_delete -> handleDelete()
                R.id.menu_remove_to_archive -> handleUnArchive()
                else -> false
            }
        }, onDestroy = { mode ->
            archiveAdapter.setLongClick(false)
            mode?.finish()
        })
        val action = requireActivity().startActionMode(callback)
        selectedItem.observe(viewLifecycleOwner) {
            action?.menu?.findItem(R.id.menu_delete)?.isVisible = it.isNotEmpty()
            action?.menu?.findItem(R.id.menu_remove_to_archive)?.isVisible = it.isNotEmpty()
            action?.title = it.size.toString()
        }
    }

    private fun handleUnArchive(): Boolean {
        viewModel.unArchiveLinks(selectedItem.value?.toList() ?: emptyList())
        selectedItem.value = hashSetOf()
        return true
    }

    private fun handleDelete(): Boolean {
        requireContext().universalDialog(DELETE_DIALOG.apply {
            positiveText = getString(R.string.deleteSelected)
            positiveAction = {
                viewModel.deleteLinks(selectedItem.value?.toList() ?: emptyList())
                selectedItem.value = hashSetOf()
            }
        })
        return true
    }


    private fun navigateToDetailFragment(m: Pair<LinkModel, View>) {
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
        val extra = FragmentNavigatorExtras(
            m.second to m.first.url
        )
        findNavController().navigate(
            ArchiveFragmentDirections.actionArchiveFragmentToDetailFragment(m.first), extra
        )
    }

    private fun observeViewList() {
        viewModel.link.observe(viewLifecycleOwner) {
            binding.emptyImage.isVisible = it.isEmpty()
            archiveAdapter.submitList(it)
        }
    }

    private fun FragmentArchiveBinding.toolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun changeStatusBarColor() {
        val windows = requireActivity().window
        windows.statusBarColor =
            MaterialColors.getColor(requireView(), android.viewbinding.library.R.attr.colorSurface)
    }
}