package com.las.workout.stats.service

import com.las.workout.core.service.WorkoutService
import com.las.workout.stats.api.dto.StatsGetForExerciseRespDto
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
                    volumeProgress = workouts.map { workout ->
                        WorkoutVolumeDto(
                            date = workout.date,
                            volume = workout.exercises.filter { it.exerciseId == exerciseId && it.warmup != true }
                                .map { it.weight.kg!! * it.repetitions }
                                .reduce { acc, vl -> acc + vl }
                        )
                    }
                )
            }
    }

}