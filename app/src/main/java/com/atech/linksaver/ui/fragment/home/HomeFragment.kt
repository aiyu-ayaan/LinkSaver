package com.atech.linksaver.ui.fragment.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
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
import coil.load
import coil.transform.CircleCropTransformation
import com.atech.core.data.model.LinkModel
import com.atech.core.util.openLink
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentHomeBinding
import com.atech.linksaver.ui.fragment.home.HomeViewModel.Companion.DEFAULT_QUERY
import com.atech.linksaver.ui.fragment.home.adapter.LinkAdapter
import com.atech.linksaver.utils.DELETE_DIALOG
import com.atech.linksaver.utils.DialogModel
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
        viewModel.autoDeleteIn30Days()
        binding.apply {
            setRecyclerView()
            setFab()
            handleBottomAppBar()
            setSearchRecyclerView()
            setToolbar()
            handleDrawerClicks()
        }
        observeViewList()
        backButton()
    }

    private fun FragmentHomeBinding.setToolbar() = this.apply {
        searchBar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_toolbar, menu)
                val searchItem = menu.findItem(R.id.menu_profile)
                val view = searchItem.actionView as FrameLayout
                val imageView = view.findViewById<ImageView>(R.id.toolbar_profile_image)
                if (viewModel.isSignedIn()) {
                    imageView.load(viewModel.getCurrentUser()?.profileImage) {
                        crossfade(true)
                        placeholder(R.drawable.ic_account)
                        error(R.drawable.ic_account)
                        transformations(CircleCropTransformation())
                    }
                }
                imageView.setOnClickListener {
                    if (!viewModel.isSignedIn())
                        navigateToLogIn().let {
                            return@setOnClickListener
                        }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    else -> false
                }
            }
        }, viewLifecycleOwner)
        searchBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun navigateToLogIn() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToLogInFragment()
        )
    }

    private fun FragmentHomeBinding.handleDrawerClicks() = this.navigationView.apply {
        setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (it.itemId) {
                R.id.menu_github -> {
                    context?.openLink(resources.getString(R.string.github_link))
                    true
                }

                R.id.menu_issue -> {
                    context?.openLink(resources.getString(R.string.issue_link))
                    true
                }

                R.id.menu_whats_new -> {
                    context?.openLink(resources.getString(R.string.release_link))
                    true
                }

                R.id.menu_backup -> navigateToBackUp()

                R.id.menu_logout -> perFormSignOut()

                else -> false
            }
        }
    }

    private fun navigateToBackUp(): Boolean {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToBackUpFragment()
        )
        return true
    }

    private fun perFormSignOut(): Boolean {
        DialogModel(title = getString(R.string.log_out),
            message = getString(R.string.log_out_message),
            positiveText = getString(R.string.yes),
            negativeText = getString(R.string.no),
            positiveAction = { dialog ->
                viewModel.logOut()
                dialog.dismiss()
            },
            negativeAction = { dialog ->
                dialog.dismiss()
            }).let {
            requireContext().universalDialog(it)
        }
        return true
    }


    private fun backButton() {
        viewLifecycleOwnerLiveData.observe(viewLifecycleOwner) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event
                ) {
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
            MaterialColors.getColor(
                requireView(),
                android.viewbinding.library.R.attr.colorSurface
            )
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

}