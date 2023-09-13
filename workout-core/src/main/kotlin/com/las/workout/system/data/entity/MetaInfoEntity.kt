package com.las.workout.system.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "meta_info")
@TypeAlias("MetaInfoEntity")
data class MetaInfoEntity(
    @Id val id: MetaObjectType,
    val data: Map<String, Any?>
) {

    @Version
    var version: Long = 0

}

enum class MetaObjectType {
    JWKSET
}