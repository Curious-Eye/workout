package com.las.workout.core.api.dto

import com.las.workout.core.data.entity.ExerciseRecordEntity
import com.las.workout.core.data.entity.WeightEntity
import com.las.workout.core.data.entity.WorkoutEntity
import java.util.*

data class WorkoutDto(
    val id: String,
    val date: Date,
    val exercises: List<WorkoutRecordedExerciseDto>,
    var mesocycle: String? = null,
) {

    constructor(entity: WorkoutEntity) : this(
        id = entity.id,
        date = entity.date,
        exercises = entity.exercises.map { WorkoutRecordedExerciseDto(it) },
        mesocycle = entity.mesocycle
    )

}

data class WorkoutRecordedExerciseDto(
    val exerciseId: String,
    val repetitions: Int,
    val weight: WeightDto,
    val rir: RepetitionsInReserveDto?,
    val warmup: Boolean?,
    val myoRepMatch: Boolean?,
) {

    constructor(exerciseRecord: ExerciseRecordEntity) : this(
        exerciseId = exerciseRecord.exerciseId,
        repetitions = exerciseRecord.repetitions,
        weight = WeightDto(exerciseRecord.weight),
        rir = exerciseRecord.rir?.let { RepetitionsInReserveDto(min = it.min, max = it.max) },
        warmup = exerciseRecord.warmup,
        myoRepMatch = exerciseRecord.myoRepMatch,
    )

}

data class WeightDto(
    val kg: Float?,
    val bodyWeight: Boolean? = null,
) {
    constructor(weight: WeightEntity) : this(
        kg = weight.kg,
        bodyWeight = weight.bodyWeight,
    )
}

data class RepetitionsInReserveDto(
    val min: Int,
    val max: Int?,
)

data class WorkoutRecordRqDto(
    val mesocycle: String? = null,
    val date: Date? = null,
)

data class WorkoutRecordExerciseRqDto(
    val exerciseId: String,
    val repetitions: Int,
    val weight: WeightDto,
    val rir: RepetitionsInReserveDto?,
    val warmup: Boolean?,
    val myoRepMatch: Boolean?,
)

data class WorkoutRecordExerciseRespDto(
    val workout: WorkoutDto
)