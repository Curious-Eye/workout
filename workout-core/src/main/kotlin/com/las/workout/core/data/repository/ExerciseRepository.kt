package com.las.workout.core.data.repository

import com.las.workout.core.data.entity.ExerciseEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ExerciseRepository : ReactiveMongoRepository<ExerciseEntity, String> {

    fun findAllByUserId(userId: String): Flux<ExerciseEntity>

    fun findByIdAndUserId(id: String, userId: String): Mono<ExerciseEntity>

}