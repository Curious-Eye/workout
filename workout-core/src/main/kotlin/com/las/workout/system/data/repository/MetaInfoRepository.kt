package com.las.workout.system.data.repository

import com.las.workout.system.data.entity.MetaInfoEntity
import com.las.workout.system.data.entity.MetaObjectType
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface MetaInfoRepository : ReactiveMongoRepository<MetaInfoEntity, String> {
    fun findById(id: MetaObjectType): Mono<MetaInfoEntity>
}