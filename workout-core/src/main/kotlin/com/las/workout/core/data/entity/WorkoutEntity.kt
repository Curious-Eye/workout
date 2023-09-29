package com.las.workout.core.data.entity

import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "workouts")
@TypeAlias("WorkoutEntity")
class WorkoutEntity(
    @Id
    val id: String,
    var date: Date,
    val userId: String,
    val exercises: MutableList<ExerciseRecordEntity> = mutableListOf(),
    var mesocycle: String? = null,
) {

    @CreatedDate
    var createdDate: Date? = null

    @LastModifiedDate
    var lastModifiedDate: Date? = null

    @Version
    var version: Long = 0L

}

data class ExerciseRecordEntity(
    val exerciseId: String,
    var repetitions: Int,
    var weight: WeightEntity,
    var warmup: Boolean? = null,
    var rir: RepetitionsInReserveEntity? = null,
    var elevation: ElevationEntity? = null,
    var contraction: ContractionEntity? = null,
    var myoRepMatch: Boolean? = null,
)

data class WeightEntity(
    var kg: Float? = null,
    var bodyWeight: Boolean? = null
)

data class RepetitionsInReserveEntity(
    var min: Int,
    var max: Int? = null,
)

data class ElevationEntity(
    var cm: Float
)

data class ContractionEntity(
    var isometric: IsometricEntity? = null,
    var eccentric: EccentricEntity? = null,
)

data class IsometricEntity(
    var minSeconds: Float,
    var maxSeconds: Float? = null
)

data class EccentricEntity(
    var minSeconds: Float,
    var maxSeconds: Float? = null
)