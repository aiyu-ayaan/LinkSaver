package com.atech.core.data.use_cases

import com.atech.core.data.database.FilterDao
import com.atech.core.data.model.FilterModel
import javax.inject.Inject


class FilterUseCases @Inject constructor(
    val insertFilter: InsertFilter,
    val getAllFilters: GetAllFilters,
    val deleteFilter: DeleteFilter,
    val deleteAllFilters: DeleteAllFilters,
    val getFilter: GetFilter
)

class InsertFilter @Inject constructor(
    private val dao: FilterDao
) {
    suspend operator fun invoke(filter: FilterModel) {
        dao.insertFilter(filter)
    }
}

class GetAllFilters @Inject constructor(
    private val dao: FilterDao
) {
    operator fun invoke() = dao.getFilters()
}

class DeleteFilter @Inject constructor(
    private val dao: FilterDao
) {
    suspend operator fun invoke(filter: FilterModel) {
        dao.deleteFilter(filter)
    }
}

class DeleteAllFilters @Inject constructor(
    private val dao: FilterDao
) {
    suspend operator fun invoke() {
        dao.deleteAllFilters()
    }
}

class GetFilter @Inject constructor(
    private val dao: FilterDao
) {
    suspend operator fun invoke(filter: String) = dao.getFilter(filter)
}