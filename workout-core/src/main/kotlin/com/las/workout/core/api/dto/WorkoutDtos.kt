package com.las.workout.core.api.dto

import com.las.workout.core.data.entity.ContractionEntity
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
    var elevation: ElevationDto?,
    var contraction: ContractionDto?,
    val warmup: Boolean?,
    val myoRepMatch: Boolean?,
) {

    constructor(exerciseRecord: ExerciseRecordEntity) : this(
        exerciseId = exerciseRecord.exerciseId,
        repetitions = exerciseRecord.repetitions,
        weight = WeightDto(exerciseRecord.weight),
        rir = exerciseRecord.rir?.let { RepetitionsInReserveDto(min = it.min, max = it.max) },
        elevation = exerciseRecord.elevation?.let { ElevationDto(it.cm) },
        contraction = exerciseRecord.contraction?.let { ContractionDto(it) },
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
    val elevation: ElevationDto?,
    val contraction: ContractionDto?,
    val warmup: Boolean?,
    val myoRepMatch: Boolean?,
)

data class WorkoutRecordExerciseRespDto(
    val workout: WorkoutDto
)

data class ElevationDto(
    val cm: Float
)

data class ContractionDto(
    val isometric: IsometricDto?,
    val eccentric: EccentricDto?,
) {

    constructor(contractionEntity: ContractionEntity) : this(
        isometric = contractionEntity.isometric?.let {
            IsometricDto(
                minSeconds = it.minSeconds,
                maxSeconds = it.maxSeconds
            )
        },
        eccentric = contractionEntity.eccentric?.let {
            EccentricDto(
                minSeconds = it.minSeconds,
                maxSeconds = it.maxSeconds
            )
        },
    )

}

data class IsometricDto(
    val minSeconds: Float,
    val maxSeconds: Float?
)

data class EccentricDto(
    val minSeconds: Float,
    val maxSeconds: Float?
)