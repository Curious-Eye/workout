package com.las.workout.core.data.repository

import com.las.workout.core.data.entity.WorkoutEntity
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface WorkoutRepository : ReactiveMongoRepository<WorkoutEntity, String> {

    fun findAllByUserId(userId: String): Flux<WorkoutEntity>

    @Query("{ 'exercises.exerciseId' : ?0 }")
    fun findAllByExerciseId(exerciseId: String): Flux<WorkoutEntity>

}
