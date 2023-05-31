package com.atech.linksaver.ui.fragment.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.atech.core.data.model.LinkModel
import com.atech.core.data.use_cases.LinkUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cases: LinkUseCases
) : ViewModel() {

    val link = cases.getAllLinks.invoke()
    val query = MutableLiveData(DEFAULT_QUERY)


    fun deleteLinks(links: List<LinkModel>) = viewModelScope.launch {
        links.forEach {
            cases.updateIsDeleted.invoke(it)
        }
    }

    fun archiveLinks(links: List<LinkModel>) = viewModelScope.launch {
        links.forEach {
            cases.updateArchive.invoke(it)
        }
    }

    fun searchLink() = query.switchMap {
        cases.searchLink.invoke(it)
    }

    fun autoDeleteIn30Days() = viewModelScope.launch {
        cases.autoDeleteIn30Days()
    }

    companion object {
        const val DEFAULT_QUERY = "no_query"
    }
}