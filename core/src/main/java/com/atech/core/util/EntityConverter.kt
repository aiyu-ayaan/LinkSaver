package com.atech.core.util

interface EntityConverter<Entity, DomainEntity> {
    fun toDomain(entity: Entity): DomainEntity
    fun toEntity(domainEntity: DomainEntity): Entity
}