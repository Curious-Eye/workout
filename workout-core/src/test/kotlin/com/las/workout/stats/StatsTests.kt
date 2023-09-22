package com.las.workout.stats

import com.las.workout.BaseTest
import com.las.workout.core.data.entity.ExerciseRecordEntity
import com.las.workout.core.data.entity.RepetitionsInReserveEntity
import com.las.workout.core.data.entity.WeightEntity
import com.las.workout.stats.api.dto.StatsGetForExerciseRespDto
import com.las.workout.stats.api.dto.StatsGetForUserRespDto
import com.las.workout.test.DataHelper
import com.las.workout.test.getAuthed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class StatsTests : BaseTest() {

    @Test
    fun `User should be able to get stats for an exercise`() {
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
        resp.volume.size shouldBe 2
        val v1 = resp.volume.first { it.date == date1 }
        v1.date shouldBe date1
        v1.volume shouldBe 5f * 50 + 5 * 50
        val v2 = resp.volume.first { it.date == date2 }
        v2.date shouldBe date2
        v2.volume shouldBe 6f * 52.5 + 5 * 52.5
    }

    @Test
    fun `User should be able to get stats for all exercises`() {
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
                        exerciseId = "e2",
                        repetitions = 6,
                        weight = WeightEntity(kg = 52.5f),
                        rir = RepetitionsInReserveEntity(min = 2, max = 3)
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e2",
                        repetitions = 5,
                        weight = WeightEntity(kg = 52.5f),
                        rir = RepetitionsInReserveEntity(min = 1, max = 2)
                    )
                ),
                date = date2,
                mesocycle = "meso 1"
            ),
            DataHelper.WorkoutSetupRqDto(
                id = "w3",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e2",
                        repetitions = 6,
                        weight = WeightEntity(kg = 60f),
                        rir = RepetitionsInReserveEntity(min = 2, max = 3)
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e2",
                        repetitions = 6,
                        weight = WeightEntity(kg = 60f),
                        rir = RepetitionsInReserveEntity(min = 1, max = 2)
                    )
                ),
                date = date2,
                mesocycle = "meso 1"
            )
        )).collectList().block()

        // WHEN
        val res = webTestClient.getAuthed(userSetup.accessToken)
            .uri("/api/stats/exercises")
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(StatsGetForUserRespDto::class.java).returnResult().responseBody!!
        resp.stats.size shouldBe 2
        val v1 = resp.stats["e1"]!!.volume
        v1.size shouldBe 1
        v1[0].date shouldBe date1
        v1[0].volume shouldBe 5f * 50 + 5 * 50
        val v2 = resp.stats["e2"]!!.volume
        v2.size shouldBe 2
        v2[0].date shouldBe date2
        v2[0].volume shouldBe 6f * 52.5 + 5 * 52.5
        v2[1].date shouldBe date2
        v2[1].volume shouldBe 6f * 60 + 6 * 60
    }

}