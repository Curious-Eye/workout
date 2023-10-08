package com.las.workout.core.api

import com.las.workout.core.api.dto.*
import com.las.workout.core.service.WorkoutService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class WorkoutController {

    @Autowired private lateinit var workoutService: WorkoutService

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/exercises")
    fun createExercise(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @RequestBody rq: ExerciseCreateRqDto,
    ): Mono<ExerciseDto> = workoutService.createExercise(userId = user.username, name = rq.name).map { ExerciseDto(it) }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/exercises")
    fun getExercises(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
    ): Flux<ExerciseDto> = workoutService.getExercises(userId = user.username).map { ExerciseDto(it) }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/workouts")
    fun recordWorkout(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @RequestBody rq: WorkoutRecordRqDto,
    ): Mono<WorkoutDto> = workoutService.recordWorkout(userId = user.username, workout = rq).map { WorkoutDto(it) }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/workouts")
    fun getWorkouts(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
    ): Flux<WorkoutDto> = workoutService.getWorkouts(userId = user.username).map { WorkoutDto(it) }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/workouts/{id}/exercises")
    fun recordWorkoutExercise(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @PathVariable id: String,
        @RequestBody rq: WorkoutRecordExerciseRqDto,
    ): Mono<WorkoutRecordExerciseRespDto> =
        workoutService.recordWorkoutExercise(userId = user.username, workoutId = id, rq = rq).map {
            WorkoutRecordExerciseRespDto(WorkoutDto(it))
        }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/api/workouts/{id}")
    fun deleteWorkout(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @PathVariable id: String
    ): Mono<Void> = workoutService.deleteWorkout(id = id, userId = user.username)

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/api/workouts/{workoutId}/exercises/{exerciseIndex}")
    fun deleteRecordedExercise(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @PathVariable workoutId: String,
        @PathVariable exerciseIndex: Int,
    ): Mono<WorkoutDto> = workoutService.deleteRecordedExercise(
        userId = user.username,
        workoutId = workoutId,
        exerciseIndex = exerciseIndex
    )
        .map { WorkoutDto(it) }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/api/workouts/{workoutId}/actions/move-exercise")
    fun moveRecordedExercise(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @PathVariable workoutId: String,
        @RequestParam(name = "fromIndex") fromIndex: Int,
        @RequestParam(name = "toIndex") toIndex: Int,
    ): Mono<WorkoutDto> = workoutService.moveRecordedExercise(
        userId = user.username,
        workoutId = workoutId,
        fromIndex = fromIndex,
        toIndex = toIndex
    )
        .map { WorkoutDto(it) }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/api/workouts/{workoutId}/tags")
    fun addTagsToWorkout(
        @Parameter(hidden = true) @AuthenticationPrincipal user: User,
        @PathVariable workoutId: String,
        @RequestBody rq: WorkoutAddTagsRqDto
    ): Mono<WorkoutDto> = workoutService.tagWorkout(userId = user.username, workoutId = workoutId, rq.tags)
        .map { WorkoutDto(it) }
}