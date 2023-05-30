package com.atech.core.data.use_cases

import com.atech.core.data.database.LinkDao
import com.atech.core.data.model.LinkModel
import com.atech.core.util.loadImageCallback
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
    private val dao: LinkDao
) {
    suspend operator fun invoke(
        link: String,

        ) {
        val linkModel = loadImageCallback(link)
        dao.insertLink(linkModel)
    }
}


class UpdateLink @Inject constructor(
    private val dao: LinkDao
) {
    suspend operator fun invoke(
        link: String,
        old: LinkModel
    ) {
        if (link == old.url)
            return
        val linkModel = loadImageCallback(link)
        dao.deleteLink(old)
        dao.insertLink(linkModel.copy(created = old.created))
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