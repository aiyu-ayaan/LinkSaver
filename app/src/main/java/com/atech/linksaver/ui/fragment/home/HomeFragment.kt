package com.atech.linksaver.ui.fragment.home

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.CheckBox
import androidx.core.view.doOnPreDraw
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atech.core.data.model.LinkModel
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentHomeBinding
import com.atech.linksaver.ui.fragment.home.adapter.LinkAdapter
import com.atech.linksaver.utils.DELETE_DIALOG
import com.atech.linksaver.utils.addOnContextualMenuListener
import com.atech.linksaver.utils.universalDialog
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val viewModel: HomeViewModel by viewModels()
    private val selectedItem: MutableLiveData<HashSet<LinkModel>> = MutableLiveData()

    private lateinit var homeAdapter: LinkAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        changeStatusBarColor()
        viewModel.autoDeleteIn30Days()
        binding.apply {
            setRecyclerView()
            setFab()
            handleBottomAppBar()
        }
        observeViewList()
    }

    private fun FragmentHomeBinding.handleBottomAppBar() {
        bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_archive -> navigateToArchive()
                R.id.menu_bin -> navigateToBin()
                else -> false
            }
        }
    }

    private fun navigateToBin(): Boolean {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        val action = HomeFragmentDirections.actionHomeFragmentToBinFragment()
        findNavController().navigate(action)
        return true
    }

    private fun navigateToArchive(): Boolean {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        val action = HomeFragmentDirections.actionHomeFragmentToArchiveFragment()
        findNavController().navigate(action)
        return true
    }

    private fun changeStatusBarColor() {
        val windows = requireActivity().window
        windows.statusBarColor =
            MaterialColors.getColor(requireView(), android.viewbinding.library.R.attr.colorSurface)
    }

    private fun FragmentHomeBinding.setFab() {
        fab.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAddBottomSheetFragment()
            )
        }
    }


    private fun FragmentHomeBinding.setRecyclerView() {
        recyclerView.apply {
            adapter = LinkAdapter(::onLongClicked, ::setClickLogic).also { homeAdapter = it }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun onLongClicked() {
        val callback = addOnContextualMenuListener(onCreate = { mode, menu ->
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.contextual_action_bar, menu)
            menu?.findItem(R.id.menu_remove_to_archive)?.isVisible = false
            menu?.findItem(R.id.menu_restore)?.isVisible = false
            true
        }, onPrepare = { _, _ ->
            binding.searchBar.isInvisible = true
            binding.fab.hide()
            binding.bottomAppBar.performHide()
            false
        }, onActionItemClicked = { _, item ->
            when (item?.itemId) {
                R.id.menu_delete -> handleDelete()
                R.id.menu_add_to_archive -> addToArchive()
                else -> false
            }
        }, onDestroy = { mode ->
            binding.fab.show()
            selectedItem.value = hashSetOf()
            binding.bottomAppBar.performShow()
            homeAdapter.setLongClick(false)
            binding.searchBar.isInvisible = false
            mode?.finish()
        })
        val action = requireActivity().startActionMode(callback)
        selectedItem.observe(viewLifecycleOwner) {
            action?.menu?.findItem(R.id.menu_delete)?.isVisible = it.isNotEmpty()
            action?.menu?.findItem(R.id.menu_add_to_archive)?.isVisible = it.isNotEmpty()
            action?.title = it.size.toString()
        }
    }

    private fun addToArchive(): Boolean {
        viewModel.archiveLinks(selectedItem.value?.toList() ?: emptyList())
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

    private fun navigateToDetailFragment(m: Pair<LinkModel, View>) {
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
        val extra = FragmentNavigatorExtras(
            m.second to m.first.url
        )
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToDetailFragment(m.first), extra
        )
    }

    private fun observeViewList() {
        viewModel.link.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
            binding.emptyImage.isVisible = it.isEmpty()
        }
    }
}