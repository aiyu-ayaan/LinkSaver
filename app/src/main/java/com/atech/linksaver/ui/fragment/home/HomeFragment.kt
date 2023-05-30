package com.atech.linksaver.ui.fragment.home

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
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
import com.google.android.material.color.MaterialColors
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
        binding.apply {
            setRecyclerView()
            setFab()
        }
        observeViewList()
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
        val callback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                val inflater = mode?.menuInflater
                inflater?.inflate(R.menu.contextual_action_bar, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                binding.searchBar.isInvisible = true
                binding.fab.hide()
                binding.bottomAppBar.performHide()
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.menu_delete -> handleDelete()
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                binding.fab.show()
                selectedItem.value = hashSetOf()
                binding.bottomAppBar.performShow()
                homeAdapter.setLongClick(false)
                binding.searchBar.isInvisible = false
                mode?.finish()
            }

        }
        val action = requireActivity().startActionMode(callback)
        selectedItem.observe(viewLifecycleOwner) {
            action?.title = it.size.toString()
        }
    }

    private fun handleDelete(): Boolean {
        viewModel.deleteLinks(selectedItem.value?.toList() ?: emptyList())
        selectedItem.value = hashSetOf()
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
        }
    }
}