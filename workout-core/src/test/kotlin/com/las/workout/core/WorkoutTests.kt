package com.las.workout.core

import com.las.workout.BaseTest
import com.las.workout.core.api.dto.*
import com.las.workout.core.data.entity.ExerciseRecordEntity
import com.las.workout.core.data.entity.RepetitionsInReserveEntity
import com.las.workout.core.data.entity.WeightEntity
import com.las.workout.stats.api.dto.StatsGetForExerciseRespDto
import com.las.workout.test.DataHelper
import com.las.workout.test.getAuthed
import com.las.workout.test.postAuthed
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import java.time.Instant
import java.util.*

class WorkoutTests : BaseTest() {

    @Test
    fun `User should be able to record a workout`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        val meso = "meso 1"
        val date = Date()

        // WHEN
        val res = webTestClient.postAuthed(userSetup.accessToken)
            .uri("/api/workouts")
            .bodyValue(WorkoutRecordRqDto(mesocycle = meso, date = date))
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful
        val body = res.expectBody(WorkoutDto::class.java).returnResult().responseBody!!

        body.id shouldNotBe null
        body.date shouldBe date
        body.mesocycle shouldBe "meso 1"
        body.exercises shouldHaveSize 0

        val entity = dataHelper.workoutRepository.findById(body.id).block()!!
        entity.userId shouldBe "u1"
        entity.mesocycle shouldBe "meso 1"
        entity.date shouldBe body.date
    }

    @Test
    fun `User should be able to create exercises`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        val exercise = "Squats"

        // WHEN
        val res = webTestClient.postAuthed(userSetup.accessToken)
            .uri("/api/exercises")
            .bodyValue(ExerciseCreateRqDto(name = exercise))
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(ExerciseDto::class.java).returnResult().responseBody!!
        resp.id shouldNotBe null
        resp.name shouldBe "Squats"

        val entity = dataHelper.exerciseRepository.findById(resp.id).block()!!
        entity.name shouldBe "Squats"
        entity.userId shouldBe "u1"
    }

    @Test
    fun `User should be able to record workout exercises`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        val workoutSetup = dataHelper.setupWorkout(DataHelper.WorkoutSetupRqDto(id = "w1")).block()!!
        val setupExercise = dataHelper.setupExercise(DataHelper.ExerciseSetupRqDto(id = "e1")).block()!!

        // WHEN
        val res = webTestClient.postAuthed(userSetup.accessToken)
            .uri {
                it.path("/api/workouts/{id}/exercises")
                    .build(
                        mapOf(
                            "id" to workoutSetup.id
                        )
                    )
            }
            .bodyValue(
                WorkoutRecordExerciseRqDto(
                    exerciseId = setupExercise.id,
                    repetitions = 10,
                    weight = WeightDto(
                        kg = 50f
                    ),
                    rir = RepetitionsInReserveDto(min = 2, max = 3),
                    warmup = null,
                    mioRepMatch = null,
                )
            )
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(WorkoutRecordExerciseRespDto::class.java).returnResult().responseBody!!
        resp.workout.exercises.size shouldBe 1
        val exerciseDto = resp.workout.exercises[0]
        exerciseDto.exerciseId shouldBe "e1"
        exerciseDto.repetitions shouldBe 10
        exerciseDto.weight.kg shouldBe 50f
        exerciseDto.weight.bodyWeight shouldBe null
        exerciseDto.rir!!.min shouldBe 2
        exerciseDto.rir!!.max shouldBe 3
        exerciseDto.mioRepMatch shouldBe null
    }

    @Test
    fun `User should be able to retrieve all exercises`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        dataHelper.setupExercises(
            listOf(
                DataHelper.ExerciseSetupRqDto(id = "e1", name = "Squats", userId = "u1"),
                DataHelper.ExerciseSetupRqDto(id = "e2", name = "Bench press", userId = "u1"),
                DataHelper.ExerciseSetupRqDto(id = "e3", name = "Overhead press", userId = "u1"),
            )
        ).collectList().block()!!

        // WHEN
        val res = webTestClient.getAuthed(userSetup.accessToken)
            .uri("/api/exercises")
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp =
            res.expectBody(object : ParameterizedTypeReference<List<ExerciseDto>>() {}).returnResult().responseBody!!
        resp.size shouldBe 3
        resp[0].id shouldBe "e1"
        resp[0].name shouldBe "Squats"
        resp[1].id shouldBe "e2"
        resp[1].name shouldBe "Bench press"
        resp[2].id shouldBe "e3"
        resp[2].name shouldBe "Overhead press"
    }

    @Test
    fun `User should be able to retrieve all workouts`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        dataHelper.setupExercises(
            listOf(
                DataHelper.ExerciseSetupRqDto(id = "e1", name = "Squats", userId = "u1"),
                DataHelper.ExerciseSetupRqDto(id = "e2", name = "Bench press", userId = "u1"),
            )
        ).collectList().block()!!
        val date1 = Date()
        val date2 = Date(Instant.now().plusSeconds(60 * 60 * 24).toEpochMilli())
        dataHelper.setupWorkouts(listOf(
            DataHelper.WorkoutSetupRqDto(
                id = "w1",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 5,
                        weight = WeightEntity(kg = 50f),
                        rir = RepetitionsInReserveEntity(min = 2, max = 3)
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 5,
                        weight = WeightEntity(kg = 50f),
                        rir = RepetitionsInReserveEntity(min = 2, max = 3)
                    )
                ),
                date = date1,
                mesocycle = "meso 1"
            ),
            DataHelper.WorkoutSetupRqDto(
                id = "w2",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 6,
                        weight = WeightEntity(kg = 52.5f),
                        rir = RepetitionsInReserveEntity(min = 2, max = 3)
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 5,
                        weight = WeightEntity(kg = 52.5f),
                        rir = RepetitionsInReserveEntity(min = 1, max = 2)
                    )
                ),
                date = date2,
                mesocycle = "meso 1"
            )
        )).collectList().block()

        // WHEN
        val res = webTestClient.getAuthed(userSetup.accessToken)
            .uri {
                it.path("/api/stats/exercises/{id}")
                    .build(mapOf("id" to "e1"))
            }
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(StatsGetForExerciseRespDto::class.java).returnResult().responseBody!!
        resp.volumeProgress.size shouldBe 2
        resp.volumeProgress[0].date shouldBe date1
        resp.volumeProgress[0].volume shouldBe 5f * 50 + 5 * 50
        resp.volumeProgress[1].date shouldBe date2
        resp.volumeProgress[1].volume shouldBe 6f * 52.5 + 5 * 52.5
    }

}