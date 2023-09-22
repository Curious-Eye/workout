package com.las.workout.stats.api

import com.las.workout.stats.api.dto.StatsGetForExerciseRespDto
import com.las.workout.stats.api.dto.StatsGetForUserRespDto
import com.las.workout.stats.service.StatsService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class StatsController {

    @Autowired private lateinit var statsService: StatsService

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/stats/exercises/{id}")
    fun getStatsForExercise(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @PathVariable id: String,
    ): Mono<StatsGetForExerciseRespDto> = statsService.getForExercise(userId = user.username, exerciseId = id)

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/stats/exercises")
    fun getStatsForAllUserExercises(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
    ): Mono<StatsGetForUserRespDto> = statsService.getAllForUser(userId = user.username)

}