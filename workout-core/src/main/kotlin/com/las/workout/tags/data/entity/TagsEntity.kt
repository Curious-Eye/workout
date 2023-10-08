package com.las.workout.tags.data.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "user_tags")
class TagsEntity(
    @Id
    val id: String,
    val userId: String,
    val tags: MutableList<String>,
) {

    @CreatedDate
    var createdDate: Date? = null

    @LastModifiedDate
    var lastModifiedDate: Date? = null

    @Version
    var version: Long = 0L

}