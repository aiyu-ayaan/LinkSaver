package com.atech.linksaver.ui.fragment.add_edit_filter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atech.core.data.model.FilterModel
import com.atech.core.data.use_cases.FilterUseCases
import com.atech.core.data.use_cases.LinkUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditFilterViewModel @Inject constructor(
    state: SavedStateHandle,
    private val case: FilterUseCases,
    private val linkUseCases: LinkUseCases
) : ViewModel() {
    val filterModel = state.get<FilterModel?>("model")

    fun addFilter(filter: String) = viewModelScope.launch {
        case.insertFilter(FilterModel(filter))
    }

    fun updateFilter(filter: String) = viewModelScope.launch {
        filterModel?.let {
            case.insertFilter(it.copy(filter = filter))
            linkUseCases.updateFilter.invoke(it.filter, filter)
        }
    }

    fun deleteFilter(filter: FilterModel) = viewModelScope.launch {
        linkUseCases.removeFilter.invoke(filter.filter)
        case.deleteFilter(filter)
    }
}