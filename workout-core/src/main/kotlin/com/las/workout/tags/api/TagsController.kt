package com.las.workout.tags.api

import com.las.workout.tags.service.TagsService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class TagsController {

    @Autowired private lateinit var tagsService: TagsService

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/tags")
    fun getTags(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User
    ): Mono<List<String>> = tagsService.getTags(user.username).map { it.tags }

}