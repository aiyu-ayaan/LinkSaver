package com.atech.core.data.use_cases

import android.util.Log
import com.atech.core.data.database.LinkDao
import com.atech.core.data.model.LinkModel
import javax.inject.Inject


enum class LinkType {
    ALL, ARCHIVE, DELETED
}

enum class DefaultFilter(val value: String) {
    ALL(""),
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
    val updateIsThumbnailLoaded: UpdateIsThumbnailLoaded,
    val addFilter: AddFilter,
    val removeFilter: RemoveFilter,
    val updateFilter: UpdateFilter
)


class GetAllLinks @Inject constructor(
    private val doa: LinkDao
) {
    operator fun invoke(type: LinkType = LinkType.ALL, filter: String = DefaultFilter.ALL.value) =
        when (type) {
            LinkType.ALL -> doa.getAllLinks(filter)
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


class UpdateLink @Inject constructor(
    private val dao: LinkDao
) {
    /**
     * @param date Triple<url,shortDes,Filter>
     */
    suspend operator fun invoke(
        date: Triple<String, String, String>, old: LinkModel
    ) {
        if ((date.second != old.shortDes || date.third != old.filter) && date.first == old.url) {
            dao.updateLink(old.copy(
                shortDes = date.second,
                filter = date.third
            ))
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

class AddFilter @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke(linkModel: LinkModel, newFilter: String) {
        doa.updateLink(linkModel.copy(filter = DefaultFilter.ALL.value))
        doa.updateLink(linkModel.copy(filter = newFilter))
    }
}

class RemoveFilter @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke(oldFilter: String) {
        doa.removeFilter(oldFilter)
    }
}

class UpdateFilter @Inject constructor(
    private val doa: LinkDao
) {
    suspend operator fun invoke(oldFilter: String, newFilter: String) {
        doa.updateFilter(oldFilter, newFilter)
    }
}