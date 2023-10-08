package com.las.workout.test

import com.las.workout.core.data.entity.ExerciseEntity
import com.las.workout.core.data.entity.ExerciseRecordEntity
import com.las.workout.core.data.entity.WorkoutEntity
import com.las.workout.core.data.repository.ExerciseRepository
import com.las.workout.core.data.repository.WorkoutRepository
import com.las.workout.jwt.service.AuthTokensService
import com.las.workout.tags.data.repository.TagsRepository
import com.las.workout.user.data.entity.UserEntity
import com.las.workout.user.data.repository.UserRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

class DataHelper(
    private val repos: List<ReactiveMongoRepository<*, *>>,
    val workoutRepository: WorkoutRepository,
    val userRepository: UserRepository,
    val exerciseRepository: ExerciseRepository,
    val tagsRepository: TagsRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokensService: AuthTokensService,
) {

    fun clearData() {
        Mono.`when`(repos.map { it.deleteAll() })
            .block()
    }

    fun setupUser(id: String = "u1",username: String = "User 1", password: String = "pass"): Mono<UserSetupResDto> {
        return userRepository.save(
            UserEntity(
                id = id,
                username = username,
                password = passwordEncoder.encode(password),
            )
        )
            .flatMap { user ->
                authTokensService.createTokens(userId = id, createRefreshToken = true)
                    .map {
                        UserSetupResDto(user, accessToken = it.accessToken, refreshToken = it.refreshToken?.token)
                    }
            }
    }

    fun setupWorkout(workout: WorkoutSetupRqDto): Mono<WorkoutEntity> {
        return workoutRepository.save(
            WorkoutEntity(
                id = workout.id,
                date = workout.date,
                userId = workout.userId,
                exercises = workout.exercises,
                mesocycle = workout.mesocycle,
                tags = workout.tags
            )
        )
    }

    fun setupWorkouts(workouts: List<WorkoutSetupRqDto>): Flux<WorkoutEntity> {
        return Flux.fromIterable(workouts).flatMap { setupWorkout(it) }
    }

    fun setupExercise(exercise: ExerciseSetupRqDto): Mono<ExerciseEntity> {
        return exerciseRepository.save(
            ExerciseEntity(
                id = exercise.id,
                name = exercise.name,
                userId = exercise.userId
            )
        )
    }

    fun setupExercises(exercises: List<ExerciseSetupRqDto>): Flux<ExerciseEntity> {
        return Flux.fromIterable(exercises)
            .flatMap { setupExercise(it) }
    }

    data class UserSetupResDto(
        val user: UserEntity,
        val accessToken: String,
        val refreshToken: String? = null
    )

    data class WorkoutSetupRqDto(
        val id: String = "w1",
        val date: Date = Date(),
        val mesocycle: String = "meso 1",
        val userId: String = "u1",
        val exercises: MutableList<ExerciseRecordEntity> = mutableListOf(),
        val tags: MutableList<String>? = null,
    )

    data class ExerciseSetupRqDto(
        val id: String = "e1",
        val name: String = "Squats",
        val userId: String = "u1",
    )

}