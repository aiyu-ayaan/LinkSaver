package com.atech.linksaver.ui.fragment.home

import android.os.Bundle
import android.view.ActionMode
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.CheckBox
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.atech.core.data.model.FilterModel
import com.atech.core.data.model.LinkModel
import com.atech.core.data.use_cases.DefaultFilter
import com.atech.core.util.openLink
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FilterChipStandaloneBinding
import com.atech.linksaver.databinding.FilterRadioButtonStandaloneBinding
import com.atech.linksaver.databinding.FragmentHomeBinding
import com.atech.linksaver.databinding.LayoutListFiltersBinding
import com.atech.linksaver.ui.fragment.home.HomeViewModel.Companion.DEFAULT_QUERY
import com.atech.linksaver.ui.fragment.home.adapter.LinkAdapter
import com.atech.linksaver.utils.DELETE_DIALOG
import com.atech.linksaver.utils.addOnContextualMenuListener
import com.atech.linksaver.utils.openShare
import com.atech.linksaver.utils.universalDialog
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

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
            setSearchRecyclerView()
            setUpChips()
        }
        observeViewList()
        backButton()
    }


    private fun backButton() {
        viewLifecycleOwnerLiveData.observe(viewLifecycleOwner) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        viewLifecycleOwner.lifecycle.removeObserver(this)
                        return
                    }

                    if (event == Lifecycle.Event.ON_CREATE) {
                        requireActivity().onBackPressedDispatcher.addCallback(this@HomeFragment) {
                            if (binding.searchView.isShowing) {
                                binding.searchView.hide()
                            } else {
                                activity?.finish()
                            }
                        }
                    }
                }
            })
        }
    }


    private fun FragmentHomeBinding.handleBottomAppBar() {
        bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_archive -> navigateToArchive()
                R.id.menu_bin -> navigateToBin()
//                R.id.menu_about -> navigateToAbout()
                else -> false
            }
        }
    }

    private fun navigateToAbout(): Boolean {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        val action = HomeFragmentDirections.actionHomeFragmentToAboutFragment()
        findNavController().navigate(action)
        return true
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
                HomeFragmentDirections.actionGlobalAddBottomSheetFragment("", false)
            )
        }
    }


    private fun FragmentHomeBinding.setRecyclerView() {
        recyclerView.apply {
            adapter = LinkAdapter(
                ::onLongClicked, ::setClickLogic, onEditClick = ::navigateToDetailFragment
            ).also { homeAdapter = it }
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
            disableAllChips(false)
            false
        }, onActionItemClicked = { mode, item ->
            when (item?.itemId) {
                R.id.menu_delete -> handleDelete()
                R.id.menu_add_to_archive -> addToArchive()
                R.id.menu_add_filter -> addToFilter(mode)

                R.id.menu_share -> shareLink().also {
                    mode?.finish()
                }

                else -> false
            }
        }, onDestroy = { mode ->
            binding.fab.show()
            selectedItem.value = hashSetOf()
            binding.bottomAppBar.performShow()
            homeAdapter.setLongClick(false)
            binding.searchBar.isInvisible = false
            disableAllChips(true)
            mode?.finish()
        })
        val action = requireActivity().startActionMode(callback)
        selectedItem.observe(viewLifecycleOwner) {
            action?.menu?.findItem(R.id.menu_delete)?.isVisible = it.isNotEmpty()
            action?.menu?.findItem(R.id.menu_add_to_archive)?.isVisible = it.isNotEmpty()
            action?.menu?.findItem(R.id.menu_add_filter)?.isVisible = it.isNotEmpty()
            action?.menu?.findItem(R.id.menu_share)?.isVisible = it.size == 1
            action?.title = it.size.toString()
        }
    }

    private fun shareLink(): Boolean {
        selectedItem.value?.firstOrNull()?.let {
            requireActivity().openShare(it)
        }
        return true
    }

    private fun disableAllChips(isEnable: Boolean) {
        binding.btnCreateFilter.isEnabled = isEnable
        binding.chipGroupFiler.children.forEach {
            it.isEnabled = isEnable
        }
    }

    private fun addToArchive(): Boolean {
        viewModel.archiveLinks(selectedItem.value?.toList() ?: emptyList())
        selectedItem.value = hashSetOf()
        return true
    }

    private fun addToFilter(mode: ActionMode?): Boolean {
        createFilterListDialog(mode)
        return true
    }

    private fun createFilterListDialog(mode: ActionMode?) {
        var filterName = ""
        val binding = LayoutListFiltersBinding.inflate(layoutInflater)
        binding.addFilters {
            filterName = it
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.filters))
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                if (filterName.isNotEmpty()) {
                    updateList(filterName)
                    mode?.finish()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setView(binding.root).create()
        dialog.show()
    }

    private fun LayoutListFiltersBinding.addFilters(onClick: (String) -> Unit) = this.apply {
        viewModel.filters.value?.map { (it, id) ->
            getFilterRadioButton(it, id, onClick)
        }?.forEach {
            this.root.addView(it.root)
        }
    }

    private fun updateList(it: String) {
        selectedItem.value?.forEach { linkModel ->
            updateFilter(linkModel, it)
        }
        selectedItem.value = hashSetOf()
    }

    private fun getFilterRadioButton(text: String, mId: Int, onClick: (String) -> Unit = {}) =
        FilterRadioButtonStandaloneBinding.inflate(layoutInflater).also {
            it.root.id = mId
            it.root.text = text
            it.root.setOnClickListener { onClick(text) }
        }

    private fun updateFilter(linkModel: LinkModel, filter: String) {
        viewModel.addFilter(linkModel, filter)
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
        m: LinkModel, isLongClick: Boolean, checkBox: CheckBox
    ) {
        if (isLongClick) {
            selectedItem.value = selectedItem.value?.apply {
                if (contains(m)) {
                    checkBox.isChecked = false
                    checkBox.isVisible = false
                    remove(m)
                } else {
                    add(m)
                }
            } ?: hashSetOf(m)
            return
        }
        context?.openLink(m.url)
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
            binding.recyclerView.scrollToPosition(0)
        }
    }


    //    ----------------------------- SearchView------------------------------------------------------
    private fun FragmentHomeBinding.setSearchRecyclerView() {
        setSearchView()
        val searchAdapter = LinkAdapter(
            onItemClicked = ::setClickLogic, isLongClickable = false
        )
        searchBarExt.apply {
            recyclerViewSearch.apply {
                adapter = searchAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
        viewModel.searchLink().observe(viewLifecycleOwner) {
            searchAdapter.submitList(it)
            binding.searchBarExt.emptyImage.isVisible = it.isEmpty()
        }
    }

    private fun FragmentHomeBinding.setSearchView() {
        searchView.editText.doOnTextChanged { text, _, _, _ ->
            if (text?.isEmpty() == true) viewModel.query.value = DEFAULT_QUERY
            else viewModel.query.value = text.toString()
        }
    }

    //    ----------------------------------- Chips ----------------------------------------------------
    private fun FragmentHomeBinding.setUpChips() = this.apply {
        viewModel.filters.observe(viewLifecycleOwner) {
            chipGroupFiler.removeAllViews()
            it.forEach { (name, id) ->
                addChipToChipGrp(
                    createFilterChip(name, id, onLongClick = {
                        runBlocking {
                            viewModel.getFilter(name) { filter ->
                                navigateToAddEditFilter(filter)
                            }
                        }
                    }) { isChecked ->
                        viewModel.filter.value = if (isChecked) name else DefaultFilter.ALL.value
                    }.root
                )
            }
        }
        btnCreateFilter.setOnClickListener {
            navigateToAddEditFilter()
        }
    }

    private fun FragmentHomeBinding.addChipToChipGrp(view: Chip) = this.chipGroupFiler.apply {
        for (i in 0 until this.childCount) {
            val chip = this.getChildAt(i) as Chip
            if (chip.id == view.id) {
                return@apply
            }
        }
        this.addView(view)
    }

    private fun createFilterChip(
        name: String, id: Int, onLongClick: () -> Unit = {}, onClick: (Boolean) -> Unit = {}
    ) = FilterChipStandaloneBinding.inflate(layoutInflater).also { binding ->
        binding.root.id = id
        binding.root.apply {
            text = name
            setOnClickListener {
                onClick(binding.root.isChecked)
            }
            setOnLongClickListener {
                onLongClick()
                true
            }
        }
    }

    private fun navigateToAddEditFilter(filterModel: FilterModel? = null) {
        val action = HomeFragmentDirections.actionHomeFragmentToAddEditFilterDialog(filterModel)
        findNavController().navigate(action)
    }

}