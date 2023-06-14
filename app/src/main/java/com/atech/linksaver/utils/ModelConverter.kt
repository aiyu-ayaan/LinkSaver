package com.atech.linksaver.utils

import com.atech.backup.backup.model.LinkBackUpModel
import com.atech.core.data.model.LinkModel
import com.atech.core.util.EntityConverter
import javax.inject.Inject

class ModelConverter @Inject constructor() : EntityConverter<LinkModel, LinkBackUpModel> {
    override fun toDomain(entity: LinkModel): LinkBackUpModel =
        LinkBackUpModel(
            url = entity.url,
            shortDes = entity.shortDes,
            isArchive = entity.isArchive,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt,
            created = entity.created
        )

    override fun toEntity(domainEntity: LinkBackUpModel): LinkModel =
        LinkModel(
            url = domainEntity.url,
            shortDes = domainEntity.shortDes,
            isArchive = domainEntity.isArchive,
            isDeleted = domainEntity.isDeleted,
            deletedAt = domainEntity.deletedAt,
            created = domainEntity.created
        )

    fun toEntityList(domainEntity: List<LinkBackUpModel>): List<LinkModel> =
        domainEntity.map { toEntity(it) }

    fun toDomainList(entity: List<LinkModel>): List<LinkBackUpModel> =
        entity.map { toDomain(it) }

}