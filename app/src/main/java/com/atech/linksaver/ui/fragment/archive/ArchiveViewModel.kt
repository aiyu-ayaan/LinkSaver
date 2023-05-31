package com.atech.linksaver.ui.fragment.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atech.core.data.model.LinkModel
import com.atech.core.data.use_cases.LinkType
import com.atech.core.data.use_cases.LinkUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val cases: LinkUseCases
) : ViewModel() {

    val link = cases.getAllLinks.invoke(LinkType.ARCHIVE)

    fun deleteLinks(links: List<LinkModel>) = viewModelScope.launch {
        links.forEach {
            cases.updateIsDeleted.invoke(it)
        }
    }

    fun unArchiveLinks(links: List<LinkModel>) = viewModelScope.launch {
        links.forEach {
            cases.updateArchive.invoke(it)
        }
    }
}