package com.las.workout.core.api.dto

import com.las.workout.core.data.entity.ExerciseEntity

data class ExerciseCreateRqDto(
    val name: String,
)

data class ExerciseDto(
    val id: String,
    val name: String,
) {
    constructor(exerciseEntity: ExerciseEntity) : this(
        id = exerciseEntity.id,
        name = exerciseEntity.name,
    )
}