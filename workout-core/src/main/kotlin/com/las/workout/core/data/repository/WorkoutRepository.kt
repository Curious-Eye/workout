package com.las.workout.core.data.repository

import com.las.workout.core.data.entity.WORKOUT_DATE_FIELD_NAME
import com.las.workout.core.data.entity.WORKOUT_TAGS_FIELD_NAME
import com.las.workout.core.data.entity.WORKOUT_USER_ID_FIELD_NAME
import com.las.workout.core.data.entity.WorkoutEntity
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WorkoutRepository : WorkoutRepositoryCustom, ReactiveMongoRepository<WorkoutEntity, String> {

    fun findAllByUserId(userId: String): Flux<WorkoutEntity>

    @org.springframework.data.mongodb.repository.Query("{ 'exercises.exerciseId' : ?0 }")
    fun findAllByExerciseId(exerciseId: String): Flux<WorkoutEntity>

    fun findByIdAndUserId(id: String, userId: String): Mono<WorkoutEntity>

}

interface WorkoutRepositoryCustom {

    fun findWorkoutsForUser(userId: String, order: Boolean, tags: List<String>): Flux<WorkoutEntity>

}

@Component
class WorkoutRepositoryCustomImpl(val mongo: ReactiveMongoTemplate) : WorkoutRepositoryCustom {

    override fun findWorkoutsForUser(userId: String, order: Boolean, tags: List<String>): Flux<WorkoutEntity> {
        val query =
            Query.query(
                where(WORKOUT_USER_ID_FIELD_NAME).`is`(userId)
                    .let {
                        if (tags.isNotEmpty()) it.and(WORKOUT_TAGS_FIELD_NAME).all(tags)
                        else it
                    }
            )
                .let {
                    if (order) it.with(Sort.by(Sort.Direction.DESC, WORKOUT_DATE_FIELD_NAME))
                    else it
                }

        return mongo.find(query, WorkoutEntity::class.java)
    }

}