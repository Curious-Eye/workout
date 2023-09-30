package com.las.workout.stats.service

import com.las.core.ext.zipWithToPair
import com.las.workout.core.data.entity.WorkoutEntity
import com.las.workout.core.service.WorkoutService
import com.las.workout.stats.api.dto.ExerciseStatsDto
import com.las.workout.stats.api.dto.StatsGetForExerciseRespDto
import com.las.workout.stats.api.dto.StatsGetForUserRespDto
import com.las.workout.stats.api.dto.WorkoutVolumeDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class StatsService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired private lateinit var workoutService: WorkoutService

    fun getForExercise(userId: String, exerciseId: String): Mono<StatsGetForExerciseRespDto> {
        log.debug("User {} get stats for exercise {}", userId, exerciseId)

        return workoutService.findExerciseForUser(userId = userId, exerciseId = exerciseId)
            .flatMap {
                workoutService.findAllByExercise(exerciseId)
                    .collectList()
            }
            .map { workouts ->
                StatsGetForExerciseRespDto(
                    volume = calcVolumeForExercise(exerciseId, workouts)
                )
            }
    }

    fun getAllForUser(userId: String): Mono<StatsGetForUserRespDto> {
        log.debug("User {} get stats for ALL exercises", userId)

        return workoutService.getExercises(userId)
            .collectList()
            .zipWithToPair(
                workoutService.getWorkouts(userId = userId, order = false)
                    .collectList()
                    .map { it.filter { w -> w.exercises.isNotEmpty() } }
            )
            .map { (exercises, workouts) -> exercises to workouts.sortedBy { it.date } }
            .flatMap { (exercises, workouts) ->
                Mono.fromCallable {
                    StatsGetForUserRespDto(
                        stats = exercises.map { exercise ->
                            val stat = ExerciseStatsDto(calcVolumeForExercise(exercise.id, workouts))
                            Pair(exercise.id, stat)
                        }
                            .associateBy({ it.first }) { it.second }
                    )
                }
            }
    }

    private fun calcVolumeForExercise(exerciseId: String, workouts: List<WorkoutEntity>, includeWarmup: Boolean = true): List<WorkoutVolumeDto> {
        return workouts.filter { it.exercises.any { e -> e.exerciseId == exerciseId } }.map { workout ->
            WorkoutVolumeDto(
                date = workout.date,
                workoutId = workout.id,
                volume = workout.exercises.filter { it.exerciseId == exerciseId && (includeWarmup || it.warmup != true) }
                    .map {
                        if (it.weight.bodyWeight == true) it.repetitions.toFloat()
                        else it.weight.kg!! * it.repetitions
                    }
                    .reduce { acc, vl -> acc + vl }
            )
        }
    }

}