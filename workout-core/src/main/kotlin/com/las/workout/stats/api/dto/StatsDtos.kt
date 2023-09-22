package com.las.workout.stats.api.dto

import java.util.Date

data class StatsGetForUserRespDto(
    val stats: Map<String, ExerciseStatsDto>
)

data class StatsGetForExerciseRespDto(
    val volume: List<WorkoutVolumeDto>,
)

data class ExerciseStatsDto(
    val volume: List<WorkoutVolumeDto>
)

data class WorkoutVolumeDto(
    val date: Date,
    val workoutId: String,
    val volume: Float,
)