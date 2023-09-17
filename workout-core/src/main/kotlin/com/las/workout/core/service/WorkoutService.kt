package com.las.workout.core.service

import com.las.core.ext.zipWhenToPair
import com.las.workout.core.api.dto.WorkoutRecordExerciseRqDto
import com.las.workout.core.api.dto.WorkoutRecordRqDto
import com.las.workout.core.data.entity.*
import com.las.workout.core.data.repository.ExerciseRepository
import com.las.workout.core.data.repository.WorkoutRepository
import com.las.workout.exception.EntityNotFoundException
import com.las.workout.exception.IllegalArgumentException
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class WorkoutService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired private lateinit var workoutRepository: WorkoutRepository
    @Autowired private lateinit var exerciseRepository: ExerciseRepository

    @Transactional
    fun recordWorkout(userId: String, workout: WorkoutRecordRqDto): Mono<WorkoutEntity> {
        log.debug("User {} record workout {}", userId, workout)

        return workoutRepository.save(
            WorkoutEntity(
                id = ObjectId().toString(),
                date = workout.date ?: Date(),
                userId = userId,
                mesocycle = workout.mesocycle,
            )
        )
    }

    @Transactional
    fun createExercise(userId: String, name: String): Mono<ExerciseEntity> {
        log.debug("User {} create exercise {}", userId, name)

        return exerciseRepository.save(
            ExerciseEntity(
                id = ObjectId().toString(),
                name = name,
                userId = userId,
            )
        )
    }

    @Transactional
    fun recordWorkoutExercise(userId: String, workoutId: String, rq: WorkoutRecordExerciseRqDto): Mono<WorkoutEntity> {
        log.debug("User {} record workout {} exercise {}", userId, workoutId, rq)

        if (rq.weight.bodyWeight != true && rq.weight.kg == null)
            return Mono.error(IllegalArgumentException("One of bodyWeight or kg must be specified"))

        if (rq.weight.bodyWeight == true && rq.weight.kg != null)
            return Mono.error(IllegalArgumentException("Only one of bodyWeight or kg must be specified"))

        return findWorkoutOrThrow(workoutId)
            .zipWhenToPair(findExerciseOrThrow(rq.exerciseId))
            .flatMap { (workout, exercise) ->
                workout.exercises.add(
                    ExerciseRecordEntity(
                        exerciseId = exercise.id,
                        repetitions = rq.repetitions,
                        warmup = rq.warmup,
                        weight = WeightEntity(
                            kg = rq.weight.kg,
                            bodyWeight = rq.weight.bodyWeight
                        ),
                        rir = rq.rir?.let {
                            RepetitionsInReserveEntity(
                                min = it.min,
                                max = it.max,
                            )
                        },
                        mioRepMatch = rq.mioRepMatch,
                    )
                )

                workoutRepository.save(workout)
            }
    }

    private fun findWorkoutOrThrow(workoutId: String) : Mono<WorkoutEntity> {
        return workoutRepository.findById(workoutId)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Workout $workoutId does not exist")))
    }

    fun findExerciseOrThrow(exerciseId: String) : Mono<ExerciseEntity> {
        return exerciseRepository.findById(exerciseId)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Exercise $exerciseId does not exist")))
    }

    fun findExerciseForUser(userId: String, exerciseId: String) : Mono<ExerciseEntity> {
        return exerciseRepository.findByIdAndUserId(exerciseId, userId)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Exercise $exerciseId does not exist")))
    }

    fun getExercises(userId: String): Flux<ExerciseEntity> {
        return exerciseRepository.findAllByUserId(userId)
    }

    fun getWorkouts(userId: String): Flux<WorkoutEntity> {
        return workoutRepository.findAllByUserId(userId)
    }

    fun findAllByExercise(exerciseId: String): Flux<WorkoutEntity> {
        return workoutRepository.findAllByExerciseId(exerciseId)
    }

}