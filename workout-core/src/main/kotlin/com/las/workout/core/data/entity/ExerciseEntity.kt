package com.las.workout.core.data.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "exercises")
@TypeAlias("ExerciseEntity")
class ExerciseEntity(
    @Id
    val id: String,
    val name: String,
    val userId: String,
) {

    @CreatedDate
    var createdDate: Date? = null

    @LastModifiedDate
    var lastModifiedDate: Date? = null

    @Version
    var version: Long = 0L

}