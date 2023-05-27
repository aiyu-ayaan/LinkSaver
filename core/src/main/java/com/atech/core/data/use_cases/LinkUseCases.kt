package com.atech.core.data.use_cases

import com.atech.core.data.database.LinkDao
import com.atech.core.data.model.LinkModel
import javax.inject.Inject


data class LinkUseCases @Inject constructor(
    val getAllLinks: GetAllLinks,
    val insertLink: InsertLink,
    val updateLink: UpdateLink,
    val deleteLink: DeleteLink,
    val deleteAllLinks: DeleteAllLinks
)


class GetAllLinks @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke() = doa.getAllLinks()
}


class InsertLink @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke(
        linkModel: LinkModel
    ) {
        doa.insertLink(linkModel)
    }
}


class UpdateLink @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke(
        linkModel: LinkModel
    ) {
        doa.updateLink(linkModel)
    }
}

class DeleteLink @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke(
        linkModel: LinkModel
    ) {
        doa.deleteLink(linkModel)
    }
}

class DeleteAllLinks @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke() {
        doa.deleteAllLinks()
    }
}