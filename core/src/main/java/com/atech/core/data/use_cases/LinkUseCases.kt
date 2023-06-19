package com.atech.core.data.use_cases

import android.util.Log
import com.atech.core.data.database.LinkDao
import com.atech.core.data.model.LinkModel
import javax.inject.Inject


enum class LinkType {
    ALL, ARCHIVE, DELETED
}


data class LinkUseCases @Inject constructor(
    val getAllLinks: GetAllLinks,
    val insertLink: InsertLink,
    val insertLinks: InsertLinks,
    val updateLink: UpdateLink,
    val updateArchive: UpdateArchive,
    val updateIsDeleted: UpdateIsDeleted,
    val deletePermanent: DeletePermanent,
    val deletePermanentAll: DeletePermanentAll,
    val deleteAllLinks: DeleteAllLinks,
    val autoDeleteIn30Days: AutoDeleteIn30Days,
    val searchLink: SearchLink,
    val getAllLinksNotLoaded: GetAllLinksNotLoaded,
    val updateIsThumbnailLoaded: UpdateIsThumbnailLoaded,
    val getAllLinksForOnes: GetAllLinksForOnes
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
        link: String, shortDes: String
    ) {
        dao.insertLink(
            LinkModel(
                url = link,
                shortDes = shortDes,
            )
        )
    }
}

class InsertLinks @Inject constructor(
    private val dao: LinkDao
) {
    suspend operator fun invoke(
        list: List<LinkModel>
    ) {
        dao.insertLink(
            list
        )
    }
}


class UpdateLink @Inject constructor(
    private val dao: LinkDao
) {
    /**
     * @param date Pair<url,shortDes>
     */
    suspend operator fun invoke(
        date: Pair<String, String>, old: LinkModel
    ) {
        if (date.second != old.shortDes && date.first == old.url) {
            dao.updateLink(old.copy(shortDes = date.second))
            return
        }

        if (date.first == old.url) return
        dao.deleteLink(old)

        dao.insertLink(
            old.copy(
                url = date.first,
                shortDes = date.second,
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

@Suppress("INTEGER_OVERFLOW")
class AutoDeleteIn30Days @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke() {
        doa.getAllDeletedLinksOnes().filter { link ->
            val diff = System.currentTimeMillis() - link.deletedAt!!
            val days = diff / (24 * 60 * 60 * 1000)
            days >= 30
        }.forEach { link ->
            doa.deleteLink(link)
        }
    }
}

class DeletePermanentAll @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke() {
        doa.deleteAllLinksPermanent()
    }
}

class SearchLink @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke(query: String) = doa.getSearchResult(query)

}


class GetAllLinksNotLoaded @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke() = doa.getAllLinksNotLoaded()

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

class GetAllLinksForOnes @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke() = doa.getAllLinksOnes()
}