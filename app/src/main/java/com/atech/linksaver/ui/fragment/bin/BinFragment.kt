package com.atech.linksaver.ui.fragment.bin

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atech.core.data.model.LinkModel
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentBinBinding
import com.atech.linksaver.ui.fragment.home.adapter.LinkAdapter
import com.atech.linksaver.utils.DELETE_DIALOG
import com.atech.linksaver.utils.addOnContextualMenuListener
import com.atech.linksaver.utils.universalDialog
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BinFragment : Fragment(R.layout.fragment_bin) {
    private val binding: FragmentBinBinding by viewBinding()
    private val viewModel by viewModels<BinViewModel>()

    private val selectedItem: MutableLiveData<HashSet<LinkModel>> = MutableLiveData()

    private lateinit var homeAdapter: LinkAdapter
    private lateinit var deletePermanently: MenuItem

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


    private fun deletePermanentlyAll(menuItem: MenuItem): Boolean {
        requireContext().universalDialog(DELETE_DIALOG.apply {
            positiveText = resources.getString(R.string.delete_permanently)
            positiveAction = {
                viewModel.deletePermanent()
            }
        })
        return true
    }


    private fun FragmentBinBinding.setRecyclerView() {
        recyclerView.apply {
            adapter = LinkAdapter(
                onItemLongClicked = ::onLongClicked, onItemClicked = ::setClickLogic
            ).also { homeAdapter = it }
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

    private fun navigateToDetailFragment(m: Pair<LinkModel, View>) {
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
        val extra = FragmentNavigatorExtras(
            m.second to m.first.url
        )
        findNavController().navigate(
            BinFragmentDirections.actionBinFragmentToDetailFragment(m.first), extra
        )
    }

    private fun onLongClicked() {
        val callback = addOnContextualMenuListener(onCreate = { mode, menu ->
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.contextual_action_bar, menu)
            menu?.findItem(R.id.menu_remove_to_archive)?.isVisible = false
            menu?.findItem(R.id.menu_add_to_archive)?.isVisible = false
            menu?.findItem(R.id.menu_delete)?.isVisible = false
            true
        }, onActionItemClicked = { _, item ->
            when (item?.itemId) {
                R.id.menu_restore -> restoreSelectedItems()
                else -> false
            }
        }, onDestroy = { mode ->
            homeAdapter.setLongClick(false)
            selectedItem.value = hashSetOf()
            mode?.finish()
        })
        val action = requireActivity().startActionMode(callback)
        selectedItem.observe(viewLifecycleOwner) {
            action?.menu?.findItem(R.id.menu_restore)?.isVisible = it.isNotEmpty()
            action?.title = it.size.toString()
        }
    }

    private fun restoreSelectedItems(): Boolean {
        viewModel.restoreDeleted(
            selectedItem.value?.toList() ?: listOf()
        )
        selectedItem.value = hashSetOf()
        return true
    }

    private fun observeViewList() {
        viewModel.link.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
            binding.emptyImage.isVisible = it.isEmpty()
            if (::deletePermanently.isInitialized) deletePermanently.isVisible = it.isNotEmpty()
        }
    }

    private fun FragmentBinBinding.toolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        toolbar.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    val info = menu.add(resources.getString(R.string.info))
                    info.icon = ContextCompat.getDrawable(
                        requireContext(), R.drawable.round_info_24
                    )
                    info.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    info.setOnMenuItemClickListener {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.auto_delete),
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
//                    deleteAll
                    deletePermanently = menu.add(resources.getString(R.string.delete_permanently))
                    deletePermanently.icon = ContextCompat.getDrawable(
                        requireContext(), R.drawable.round_delete_forever_24
                    )
                    deletePermanently.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    deletePermanently.setOnMenuItemClickListener(::deletePermanentlyAll)
                    deletePermanently.isVisible = viewModel.link.value?.isNotEmpty() ?: false
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return false
                }

            }, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }

    private fun changeStatusBarColor() {
        val windows = requireActivity().window
        windows.statusBarColor =
            MaterialColors.getColor(requireView(), android.viewbinding.library.R.attr.colorSurface)
    }
}