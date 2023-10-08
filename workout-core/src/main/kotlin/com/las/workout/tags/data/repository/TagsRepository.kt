package com.las.workout.tags.data.repository

import com.las.workout.tags.data.entity.TagsEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface TagsRepository : ReactiveMongoRepository<TagsEntity, String> {
}