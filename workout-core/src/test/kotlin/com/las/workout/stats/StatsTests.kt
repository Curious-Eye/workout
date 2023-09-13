package com.las.workout.stats

import com.las.workout.BaseTest
import com.las.workout.core.data.entity.ExerciseRecordEntity
import com.las.workout.core.data.entity.RepetitionsInReserveEntity
import com.las.workout.core.data.entity.WeightEntity
import com.las.workout.test.DataHelper
import org.junit.jupiter.api.Test

class StatsTests : BaseTest() {

    @Test
    fun `User should be able to get stats for exercise`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        dataHelper.setupExercises(
            listOf(
                DataHelper.ExerciseSetupRqDto(id = "e1", name = "Squats", userId = "u1"),
                DataHelper.ExerciseSetupRqDto(id = "e2", name = "Bench press", userId = "u1"),
            )
        ).collectList().block()!!
        dataHelper.setupWorkouts(listOf(
            DataHelper.WorkoutSetupRqDto(
                id = "w1",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 5,
                        weight = WeightEntity(kg = 50f),
                        mioRepMatch = true,
                    ),
                    ExerciseRecordEntity(
                        exerciseId = "e1",
                        repetitions = 5,
                        weight = WeightEntity(kg = 50f),
                        mioRepMatch = true,
                    )
                ),
                mesocycle = "meso 1"
            ),
            DataHelper.WorkoutSetupRqDto(
                id = "w2",
                exercises = mutableListOf(
                    ExerciseRecordEntity(
                        exerciseId = "e2",
                        repetitions = 6,
                        weight = WeightEntity(kg = 40f),
                        rir = RepetitionsInReserveEntity(min = 2, max = 3)
                    )
                ),
                mesocycle = "meso 2"
            )
        )).collectList().block()
    }

}