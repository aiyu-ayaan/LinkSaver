package com.atech.core.data.use_cases

import android.util.Log
import com.atech.core.data.database.LinkDao
import com.atech.core.data.model.LinkModel
import javax.inject.Inject


enum class LinkType {
    ALL,
    ARCHIVE,
    DELETED
}


data class LinkUseCases @Inject constructor(
    val getAllLinks: GetAllLinks,
    val insertLink: InsertLink,
    val updateLink: UpdateLink,
    val updateArchive: UpdateArchive,
    val updateIsDeleted: UpdateIsDeleted,
    val deletePermanent: DeletePermanent,
    val deleteAllLinks: DeleteAllLinks,
    val autoDeleteIn30Days: AutoDeleteIn30Days,
    val searchLink: SearchLink,
    val getAllLinksNotLoaded: GetAllLinksNotLoaded,
    val updateIsThumbnailLoaded: UpdateIsThumbnailLoaded
)


class GetAllLinks @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke(type: LinkType = LinkType.ALL) = when (type) {
        LinkType.ALL -> doa.getAllLinks()
        LinkType.ARCHIVE -> doa.getAllArchivedLinks()
        LinkType.DELETED -> doa.getAllDeletedLinks()
    }
}


class InsertLink @Inject constructor(
    private val dao: LinkDao
) {
    suspend operator fun invoke(
        link: String,
    ) {
        dao.insertLink(
            LinkModel(
                url = link,
            )
        )
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
        dao.deleteLink(old)
        dao.insertLink(
            old.copy(
                url = link,
                created = old.created,
                isThumbnailLoaded = false
            )
        )
    }
}

class UpdateArchive @Inject constructor(
    private val dao: LinkDao
) {
    suspend operator fun invoke(
        linkModel: LinkModel
    ) {
        dao.updateLink(linkModel.copy(isArchive = !linkModel.isArchive))
    }
}

class UpdateIsDeleted @Inject constructor(
    private val dao: LinkDao
) {
    suspend operator fun invoke(
        linkModel: LinkModel
    ) {
        dao.updateLink(
            linkModel.copy(
                isDeleted = !linkModel.isDeleted,
                isArchive = false,
                deletedAt = if (linkModel.isDeleted) null else System.currentTimeMillis()
            )
        )
    }
}

class DeletePermanent @Inject constructor(
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

class AutoDeleteIn30Days @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke() {
        doa.autoDeleteIn30Days()
    }
}

class SearchLink @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke(query: String) =
        doa.getSearchResult(query)

}


class GetAllLinksNotLoaded @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke() =
        doa.getAllLinksNotLoaded()

}

class UpdateIsThumbnailLoaded @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke(
        linkModel: LinkModel
    ) {
        doa.updateLink(linkModel.copy(isThumbnailLoaded = true)).let {
            Log.d("AAA", "UpdateIsThumbnailLoaded: $it")
        }
    }
}