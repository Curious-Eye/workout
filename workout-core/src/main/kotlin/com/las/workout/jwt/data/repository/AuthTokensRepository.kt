package com.las.workout.jwt.data.repository

import com.las.workout.jwt.data.entity.AuthTokensEntity
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface AuthTokensRepository : ReactiveMongoRepository<AuthTokensEntity, String> {

    @Query("{ 'refreshToken.token' : ?0, 'refreshToken.used' : ?1 }")
    fun findByRefreshTokenValueAndUsed(refreshToken: String, used: Boolean): Mono<AuthTokensEntity>

}