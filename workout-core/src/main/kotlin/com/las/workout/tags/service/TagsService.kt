package com.las.workout.tags.service

import com.las.workout.tags.data.entity.TagsEntity
import com.las.workout.tags.data.repository.TagsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class TagsService {

    @Autowired private lateinit var tagsRepository: TagsRepository

    @Transactional
    fun addTags(userId: String, tags: List<String>): Mono<TagsEntity> {
        return tagsRepository.findById(userId)
            .flatMap {
                tags.forEach { tag ->
                    if (!it.tags.contains(tag))
                        it.tags.add(tag)
                }

                tagsRepository.save(it)
            }
            .switchIfEmpty(
                tagsRepository.save(
                    TagsEntity(
                        id = userId,
                        userId = userId,
                        tags = tags.toMutableList()
                    )
                )
            )
    }

    fun getTags(userId: String): Mono<TagsEntity> =
        tagsRepository.findById(userId)
            .defaultIfEmpty(
                TagsEntity(
                    id = userId,
                    userId = userId,
                    tags = mutableListOf()
                )
            )

}