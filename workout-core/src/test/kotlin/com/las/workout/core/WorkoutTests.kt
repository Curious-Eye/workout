package com.las.workout.core

import com.las.workout.BaseTest
import com.las.workout.core.api.dto.*
import com.las.workout.core.data.entity.ExerciseRecordEntity
import com.las.workout.core.data.entity.RepetitionsInReserveEntity
import com.las.workout.core.data.entity.WeightEntity
import com.las.workout.stats.api.dto.StatsGetForExerciseRespDto
import com.las.workout.test.*
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
                    myoRepMatch = null,
                    elevation = ElevationDto(5f),
                    contraction = ContractionDto(
                        isometric = IsometricDto(
                            minSeconds = 1f,
                            maxSeconds = 2f
                        ),
                        eccentric = EccentricDto(
                            minSeconds = 3f,
                            maxSeconds = 4f
                        )
                    )
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
        exerciseDto.myoRepMatch shouldBe null
        exerciseDto.contraction!!.isometric!!.minSeconds shouldBe 1f
        exerciseDto.contraction!!.isometric!!.maxSeconds shouldBe 2f
        exerciseDto.contraction!!.eccentric!!.minSeconds shouldBe 3f
        exerciseDto.contraction!!.eccentric!!.maxSeconds shouldBe 4f

        val workoutEntity = dataHelper.workoutRepository.findById(workoutSetup.id).block()!!
        workoutEntity.exercises.size shouldBe 1
        val exerciseEntity = workoutEntity.exercises[0]
        exerciseEntity.exerciseId shouldBe "e1"
        exerciseEntity.repetitions shouldBe 10
        exerciseEntity.weight.kg shouldBe 50f
        exerciseEntity.weight.bodyWeight shouldBe null
        exerciseEntity.rir!!.min shouldBe 2
        exerciseEntity.rir!!.max shouldBe 3
        exerciseEntity.myoRepMatch shouldBe null
        exerciseEntity.contraction!!.isometric!!.minSeconds shouldBe 1f
        exerciseEntity.contraction!!.isometric!!.maxSeconds shouldBe 2f
        exerciseEntity.contraction!!.eccentric!!.minSeconds shouldBe 3f
        exerciseEntity.contraction!!.eccentric!!.maxSeconds shouldBe 4f
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

        val ex1 = resp.first { it.id == "e1" }
        val ex2 = resp.first { it.id == "e2" }
        val ex3 = resp.first { it.id == "e3" }

        ex1.name shouldBe "Squats"
        ex2.name shouldBe "Bench press"
        ex3.name shouldBe "Overhead press"
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
                        repetitions = 6,
                        weight = WeightEntity(kg = 60f),
                        rir = RepetitionsInReserveEntity(min = 1, max = 2)
                    )
                ),
                date = date1,
                mesocycle = "meso 1"
            ),
            DataHelper.WorkoutSetupRqDto(
                id = "w2",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e2",
                        repetitions = 5,
                        weight = WeightEntity(kg = 52.5f),
                        rir = RepetitionsInReserveEntity(min = 1, max = 2)
                    )
                ),
                date = date2,
                mesocycle = "meso 2"
            )
        )).collectList().block()

        // WHEN
        val res = webTestClient.getAuthed(userSetup.accessToken)
            .uri("/api/workouts")
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(object : ParameterizedTypeReference<List<WorkoutDto>>() {}).returnResult().responseBody!!
        resp.size shouldBe 2
        val w1 = resp.first { it.id == "w1" }

        w1.mesocycle shouldBe "meso 1"
        w1.exercises.size shouldBe 2
        w1.exercises[0].exerciseId shouldBe "e1"
        w1.exercises[0].repetitions shouldBe 5
        w1.exercises[0].weight.kg shouldBe 50
        w1.exercises[0].rir!!.min shouldBe 2
        w1.exercises[0].rir!!.max shouldBe 3
        w1.exercises[1].exerciseId shouldBe "e1"
        w1.exercises[1].repetitions shouldBe 6
        w1.exercises[1].weight.kg shouldBe 60
        w1.exercises[1].rir!!.min shouldBe 1
        w1.exercises[1].rir!!.max shouldBe 2

        val w2 = resp.first { it.id == "w2" }
        w2.mesocycle shouldBe "meso 2"
        w2.exercises.size shouldBe 1
        w2.exercises[0].exerciseId shouldBe "e2"
        w2.exercises[0].repetitions shouldBe 5
        w2.exercises[0].weight.kg shouldBe 52.5
        w2.exercises[0].rir!!.min shouldBe 1
        w2.exercises[0].rir!!.max shouldBe 2
    }

    @Test
    fun `User should be able to delete a workout`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        dataHelper.setupWorkouts(listOf(
            DataHelper.WorkoutSetupRqDto(
                id = "w1",
                userId = "u1"
            ),
        )).collectList().block()

        // WHEN
        val res = webTestClient.deleteAuthed(userSetup.accessToken)
            .uri("/api/workouts/w1")
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful
        dataHelper.workoutRepository.findById("w1").block() shouldBe null
    }

    @Test
    fun `User should be able to delete exercise record`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        dataHelper.setupWorkouts(listOf(
            DataHelper.WorkoutSetupRqDto(
                id = "w1",
                userId = "u1",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 5,
                        weight = WeightEntity(bodyWeight = true)
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e2",
                        repetitions = 6,
                        weight = WeightEntity(kg = 10f)
                    )
                )
            ),
        )).collectList().block()

        // WHEN
        val res = webTestClient.deleteAuthed(userSetup.accessToken)
            .uri("/api/workouts/w1/exercises/0")
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(WorkoutDto::class.java).returnResult().responseBody!!
        resp.exercises.size shouldBe 1
        resp.exercises[0].exerciseId shouldBe "e2"
        resp.exercises[0].repetitions shouldBe 6
        resp.exercises[0].weight.kg shouldBe 10f

        val workoutEntity = dataHelper.workoutRepository.findById("w1").block()!!
        workoutEntity.exercises.size shouldBe 1
        workoutEntity.exercises[0].exerciseId shouldBe "e2"
        workoutEntity.exercises[0].repetitions shouldBe 6
        workoutEntity.exercises[0].weight.kg shouldBe 10f
    }

    @Test
    fun `User should be able to move recorded exercises withing the workout`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        dataHelper.setupWorkouts(listOf(
            DataHelper.WorkoutSetupRqDto(
                id = "w1",
                userId = "u1",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 5,
                        weight = WeightEntity(bodyWeight = true)
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e2",
                        repetitions = 6,
                        weight = WeightEntity(kg = 10f)
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e3",
                        repetitions = 7,
                        weight = WeightEntity(kg = 20f)
                    )
                )
            ),
        )).collectList().block()
        var fromIndex = 0
        var toIndex = 2

        // WHEN
        var resp = webTestClient.putAuthed(userSetup.accessToken)
            .uri {
                it.path("/api/workouts/w1/actions/move-exercise")
                    .queryParam("fromIndex", fromIndex)
                    .queryParam("toIndex", toIndex)
                    .build()
            }
            .exchange()

        // THEN
        resp.expectStatus().is2xxSuccessful

        var respBody = resp.expectBody(WorkoutDto::class.java).returnResult().responseBody!!
        respBody.exercises.size shouldBe 3
        respBody.exercises[0].exerciseId shouldBe "e2"
        respBody.exercises[0].repetitions shouldBe 6
        respBody.exercises[0].weight.kg shouldBe 10f
        respBody.exercises[1].exerciseId shouldBe "e3"
        respBody.exercises[1].repetitions shouldBe 7
        respBody.exercises[1].weight.kg shouldBe 20f
        respBody.exercises[2].exerciseId shouldBe "e1"
        respBody.exercises[2].repetitions shouldBe 5
        respBody.exercises[2].weight.bodyWeight shouldBe true

        // GIVEN
        fromIndex = 2
        toIndex = 0

        // WHEN
        resp = webTestClient.putAuthed(userSetup.accessToken)
            .uri {
                it.path("/api/workouts/w1/actions/move-exercise")
                    .queryParam("fromIndex", fromIndex)
                    .queryParam("toIndex", toIndex)
                    .build()
            }
            .exchange()

        // THEN
        resp.expectStatus().is2xxSuccessful

        respBody = resp.expectBody(WorkoutDto::class.java).returnResult().responseBody!!
        respBody.exercises.size shouldBe 3
        respBody.exercises[0].exerciseId shouldBe "e1"
        respBody.exercises[0].repetitions shouldBe 5
        respBody.exercises[0].weight.bodyWeight shouldBe true
        respBody.exercises[1].exerciseId shouldBe "e2"
        respBody.exercises[1].repetitions shouldBe 6
        respBody.exercises[1].weight.kg shouldBe 10f
        respBody.exercises[2].exerciseId shouldBe "e3"
        respBody.exercises[2].repetitions shouldBe 7
        respBody.exercises[2].weight.kg shouldBe 20f
    }
}