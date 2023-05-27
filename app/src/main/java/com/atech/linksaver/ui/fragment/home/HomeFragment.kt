package com.atech.linksaver.ui.fragment.home

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.atech.linksaver.R
import com.atech.linksaver.databinding.FragmentHomeBinding
import com.atech.linksaver.ui.fragment.home.adapter.LinkAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var homeAdapter: LinkAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setRecyclerView()
        }
        observeViewList()
    }


    private fun FragmentHomeBinding.setRecyclerView() {
        recyclerView.apply {
            adapter = LinkAdapter().also { homeAdapter = it }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeViewList() {
        viewModel.link.observe(viewLifecycleOwner) {
            homeAdapter.submitList(it)
        }
    }
}