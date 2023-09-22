import {useMainStore} from "~/store";

/**
 *
 * @param app {NuxtApp}
 */
export const useWorkoutApi = (app = undefined) => {
    return {
        /**
         * Get all workouts
         * @param redirectToLoginOnAuthFail {boolean}
         * @return {Promise<{data?: [Workout], error?: *}>}
         */
        async getWorkouts(redirectToLoginOnAuthFail = true) {
            console.log('Try getWorkouts')
            const {data, error} = await useApi(app).getAuthed('/api/workouts', redirectToLoginOnAuthFail)

            if (!!error)
                return {error}

            if (process.client)
                useMainStore().setWorkouts(data)
            else
                app.runWithContext(() => useMainStore().setWorkouts(data))

            return {data}
        },
        /**
         * Record a workout
         * @param mesocycle {string}
         * @param date {Date}
         * @return {Promise<{data?: Workout, error?: *}>}
         */
        async recordWorkout(mesocycle, date) {
            console.log('Try recordWorkout. mesocycle=' + mesocycle + ', date=' + date.toString())
            const {data, error} = await useApi(app).postAuthed('/api/workouts', {mesocycle, date})

            if (!!error)
                return {error}

            useMainStore().addWorkout(data)

            return {data}
        },
        /**
         * Record a workout
         * @param workoutId {string}
         * @param exercise {ExerciseRecord}
         * @return {Promise<{data?: Workout, error?: *}>}
         */
        async recordExercise(workoutId, exercise) {
            console.log(`Try recordExercise. exercise=${JSON.stringify(exercise)}`)
            const {data: {workout}, error} = await useApi(app).postAuthed(`/api/workouts/${workoutId}/exercises`, exercise)

            if (!!error)
                return {error}

            useMainStore().lastExerciseRecordInput = exercise

            useMainStore().setWorkout(workout)

            return {data: workout}
        },
        /**
         * Delete a workout
         * @param workoutId {string}
         * @return {Promise<{data?: Workout, error?: *}>}
         */
        async deleteWorkout(workoutId) {
            console.log(`Try deleteWorkout. workoutId=${workoutId}`)
            const {error} = await useApi(app).deleteAuthed(`/api/workouts/${workoutId}`)

            if (!!error)
                return {error}

            useMainStore().deleteWorkout(workoutId)

            return Promise.resolve()
        },
    }
}