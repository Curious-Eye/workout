package com.las.workout.stats.api.dto

import java.util.Date

data class StatsGetForExerciseRespDto(
    val volumeProgress: List<WorkoutVolumeDto>,
)

data class WorkoutVolumeDto(
    val date: Date,
    val volume: Float,
)