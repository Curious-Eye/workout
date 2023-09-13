package com.las.workout.user.data.repository

import com.las.workout.user.data.entity.UserEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveMongoRepository<UserEntity, String> {

    fun findByUsername(username: String): Mono<UserEntity>

}