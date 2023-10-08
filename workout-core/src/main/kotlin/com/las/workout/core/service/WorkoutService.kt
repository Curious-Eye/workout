package com.las.workout.core.service

import com.las.core.ext.errorIf
import com.las.core.ext.flatNext
import com.las.core.ext.thisOrEmpty
import com.las.core.ext.zipWhenToPair
import com.las.workout.core.api.dto.WorkoutRecordExerciseRqDto
import com.las.workout.core.api.dto.WorkoutRecordRqDto
import com.las.workout.core.data.entity.*
import com.las.workout.core.data.repository.ExerciseRepository
import com.las.workout.core.data.repository.WorkoutRepository
import com.las.workout.exception.EntityNotFoundException
import com.las.workout.exception.IllegalArgumentsException
import com.las.workout.tags.service.TagsService
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
    @Autowired private lateinit var tagsService: TagsService

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
    fun deleteWorkout(id: String, userId: String): Mono<Void> {
        return findWorkoutForUser(userId = userId, workoutId = id)
            .flatMap { workoutRepository.deleteById(it.id) }
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
            return Mono.error(IllegalArgumentsException("One of bodyWeight or kg must be specified"))

        if (rq.weight.bodyWeight == true && rq.weight.kg != null)
            return Mono.error(IllegalArgumentsException("Only one of bodyWeight or kg must be specified"))

        if (rq.rir?.min?.let { it < 0 } == true)
            return Mono.error(IllegalArgumentsException("Min value for repetitions in reserve must not be negative"))

        if (rq.rir?.max?.let { it < 0 } == true)
            return Mono.error(IllegalArgumentsException("Max value for repetitions in reserve must not be negative"))

        if (rq.rir?.min != null && rq.rir.max != null && rq.rir.min > rq.rir.max)
            return Mono.error(IllegalArgumentsException("Max value for repetitions in reserve must not be smaller than min value"))

        if (rq.elevation?.cm?.let { it <= 0 } == true)
            return Mono.error(IllegalArgumentsException("Elevation must be positive number"))

        if (rq.contraction?.eccentric != null && rq.contraction.eccentric.minSeconds < 0)
            return Mono.error(IllegalArgumentsException("Eccentric contraction min seconds must not be negative"))

        if (rq.contraction?.eccentric != null && rq.contraction.eccentric.maxSeconds?.let { it < 0 } == true)
            return Mono.error(IllegalArgumentsException("Eccentric contraction max seconds must not be negative"))

        if (rq.contraction?.eccentric?.minSeconds != null &&
            rq.contraction.eccentric.maxSeconds != null &&
            rq.contraction.eccentric.minSeconds > rq.contraction.eccentric.maxSeconds
        )
            return Mono.error(IllegalArgumentsException("Eccentric contraction max seconds must not be smaller than min seconds"))

        if (rq.contraction?.isometric != null && rq.contraction.isometric.minSeconds < 0)
            return Mono.error(IllegalArgumentsException("Isometric contraction min seconds must not be negative"))

        if (rq.contraction?.isometric != null && rq.contraction.isometric.maxSeconds?.let { it < 0 } == true)
            return Mono.error(IllegalArgumentsException("Isometric contraction max seconds must not be negative"))

        if (rq.contraction?.isometric?.minSeconds != null &&
            rq.contraction.isometric.maxSeconds != null &&
            rq.contraction.isometric.minSeconds > rq.contraction.isometric.maxSeconds
        )
            return Mono.error(IllegalArgumentsException("Isometric contraction max seconds must not be smaller than min seconds"))

        return findWorkoutForUser(userId = userId, workoutId = workoutId)
            .zipWhenToPair { findExerciseForUser(userId = userId, exerciseId = rq.exerciseId) }
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
                                max = it.max?.let { max -> if (max > it.min) max else null },
                            )
                        },
                        elevation = rq.elevation?.let { ElevationEntity(it.cm) },
                        contraction = rq.contraction?.let {
                            ContractionEntity(
                                isometric = it.isometric?.let { iso ->
                                    IsometricEntity(
                                        minSeconds = iso.minSeconds,
                                        maxSeconds = iso.maxSeconds?.let { max -> if (max > iso.minSeconds) max else null }
                                    )
                                },
                                eccentric = it.eccentric?.let { ecc ->
                                    EccentricEntity(
                                        minSeconds = ecc.minSeconds,
                                        maxSeconds = ecc.maxSeconds?.let { max -> if (max > ecc.minSeconds) max else null }
                                    )
                                }
                            )
                        },
                        myoRepMatch = rq.myoRepMatch,
                    )
                )

                workoutRepository.save(workout)
            }
    }

    private fun findWorkoutForUser(userId: String, workoutId: String): Mono<WorkoutEntity> {
        return workoutRepository.findByIdAndUserId(workoutId, userId)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Workout $workoutId does not exist")))
    }

    fun findExerciseForUser(userId: String, exerciseId: String): Mono<ExerciseEntity> {
        return exerciseRepository.findByIdAndUserId(exerciseId, userId)
            .switchIfEmpty(Mono.error(EntityNotFoundException("Exercise $exerciseId does not exist")))
    }

    fun getExercises(userId: String): Flux<ExerciseEntity> {
        return exerciseRepository.findAllByUserId(userId)
    }

    fun getWorkouts(userId: String, order: Boolean = true, tags: List<String>? = null): Flux<WorkoutEntity> {
        return workoutRepository.findWorkoutsForUser(userId = userId, order = order, tags = tags.thisOrEmpty())
    }

    fun findAllByExercise(exerciseId: String): Flux<WorkoutEntity> {
        return workoutRepository.findAllByExerciseId(exerciseId)
    }

    @Transactional
    fun deleteRecordedExercise(userId: String, workoutId: String, exerciseIndex: Int): Mono<WorkoutEntity> {
        log.debug("User {} delete recorded exercise with index {} for workout {}", userId, exerciseIndex, workoutId)

        if (exerciseIndex < 0)
            return Mono.error(EntityNotFoundException("Exercise record with index $exerciseIndex does not exist"))

        return findWorkoutForUser(userId = userId, workoutId = workoutId)
            .errorIf(EntityNotFoundException("Exercise record with index $exerciseIndex does not exist")) {
                it.exercises.size <= exerciseIndex
            }
            .flatMap {
                it.exercises.removeAt(exerciseIndex)
                workoutRepository.save(it)
            }
    }

    @Transactional
    fun moveRecordedExercise(userId: String, workoutId: String, fromIndex: Int, toIndex: Int): Mono<WorkoutEntity> {
        log.debug("User {} move recorded exercise for workout {} from index {} to {}", userId, workoutId, fromIndex, toIndex)

        if (fromIndex < 0)
            return Mono.error(IllegalArgumentsException("fromIndex must not be negative"))

        if (toIndex < 0)
            return Mono.error(IllegalArgumentsException("toIndex must not be negative"))

        if (fromIndex == toIndex)
            return Mono.error(IllegalArgumentsException("toIndex must not be equal to fromIndex"))

        return findWorkoutForUser(userId = userId, workoutId = workoutId)
            .errorIf(IllegalArgumentsException("fromIndex is out of bounds")) { fromIndex >= it.exercises.size }
            .errorIf(IllegalArgumentsException("toIndex is out of bounds")) { toIndex >= it.exercises.size }
            .flatMap {
                val e = it.exercises.removeAt(fromIndex)
                it.exercises.add(if (fromIndex > toIndex) toIndex else toIndex, e)
                workoutRepository.save(it)
            }
    }

    @Transactional
    fun tagWorkout(userId: String, workoutId: String, tags: List<String>): Mono<WorkoutEntity> {
        log.debug("User {} add tags to workout {}. Tags: {}", userId, workoutId, tags)

        if (tags.isEmpty())
            return Mono.error(IllegalArgumentsException("tags should not be empty"))

        return findWorkoutForUser(userId = userId, workoutId = workoutId)
            .flatMap {
                if (it.tags == null)
                    it.tags = mutableListOf()

                tags.forEach { tag ->
                    if (!it.tags!!.contains(tag))
                        it.tags!!.add(tag)
                }

                workoutRepository.save(it)
                    .flatNext { tagsService.addTags(userId, tags) }
            }
    }

}