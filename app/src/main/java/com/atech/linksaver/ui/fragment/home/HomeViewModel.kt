package com.atech.linksaver.ui.fragment.home

import androidx.lifecycle.ViewModel
import com.atech.core.data.use_cases.LinkUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cases: LinkUseCases
) : ViewModel() {

    val link = cases.getAllLinks.invoke()

}