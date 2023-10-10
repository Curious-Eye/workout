import {useMainStore} from "~/store";
import {useApi} from "~/composables/useApi";

/**
 *
 * @param app {NuxtApp}
 */
export const useWorkoutApi = (app = undefined) => {
    return {
        /**
         * Get all workouts
         * @param tags {string[]}
         * @param redirectToLoginOnAuthFail {boolean}
         * @return {Promise<{data?: [Workout], error?: *}>}
         */
        async getWorkouts(tags = [], redirectToLoginOnAuthFail = true) {
            console.log(`Try getWorkouts with tags ${tags}`)
            let uri = '/api/workouts'

            if (tags.length > 0) {
                uri += '?'
                tags.forEach(tag => uri += `tags=${tag}&`)
                uri = uri.slice(0, -1);
            }

            const {data, error} = await useApi(app).getAuthed(uri, redirectToLoginOnAuthFail)

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
            const {data, error} = await useApi(app).postAuthed(`/api/workouts/${workoutId}/exercises`, exercise)

            if (!!error)
                return {error}

            useMainStore().lastExerciseRecordInput = exercise

            useMainStore().setWorkout(data.workout)

            return {data: data.workout}
        },
        /**
         * Delete a workout
         * @param workoutId {string}
         * @return {Promise<{error?: *}>}
         */
        async deleteWorkout(workoutId) {
            console.log(`Try deleteWorkout. workoutId=${workoutId}`)
            const {error} = await useApi(app).deleteAuthed(`/api/workouts/${workoutId}`)

            if (!!error)
                return {error}

            useMainStore().deleteWorkout(workoutId)

            return Promise.resolve()
        },
        /**
         * Delete a workout
         * @param workoutId {string}
         * @param exerciseIndex {number}
         * @return {Promise<{data?: Workout, error?: *}>}
         */
        async deleteRecordedExercise(workoutId, exerciseIndex) {
            console.log(`Try deleteRecordedExercise. workoutId=${workoutId}, exerciseIndex=${exerciseIndex}`)
            const {
                data,
                error
            } = await useApi(app).deleteAuthed(`/api/workouts/${workoutId}/exercises/${exerciseIndex}`)

            if (!!error)
                return {error}

            useMainStore().setWorkout(data)

            return {data}
        },
        /**
         * Move recorded exercise withing a workout
         * @param workoutId {string}
         * @param fromIndex {number}
         * @param toIndex {number}
         * @return {Promise<{data?: Workout, error?: *}>}
         */
        async moveRecordedExercise(workoutId, fromIndex, toIndex) {
            console.log(`Try moveRecordedExercise. workoutId=${workoutId}, fromIndex=${fromIndex}, toIndex=${toIndex}`)
            const {data, error} = await useApi(app).putAuthed(
                `/api/workouts/${workoutId}/actions/move-exercise?fromIndex=${fromIndex}&toIndex=${toIndex}`,
                undefined
            )

            if (!!error)
                return {error}

            useMainStore().setWorkout(data)

            return {data}
        },
        /**
         * Add a tag to a workout
         * @param tag {string}
         * @param workoutId {string}
         * @return {Promise<{data?: Workout, error?: *}>}
         */
        async addTag(tag, workoutId) {
            console.log(`Try add tag "${tag}" to workout ${workoutId}`)
            const {data, error} = await useApi(app).postAuthed(
                `/api/workouts/${workoutId}/tags`,
                {tags: [tag]}
            )

            if (!!error)
                return {error}

            useMainStore().setWorkout(data)
            useMainStore().addTag(tag)

            return {data}
        },
        /**
         * Remove a tag from a workout
         * @param workoutId {string}
         * @param tagInd {number}
         * @return {Promise<{data?: Workout, error?: *}>}
         */
        async removeTag(workoutId, tagInd) {
            console.log(`Try remove tag at position ${tagInd} from workout ${workoutId}`)
            const {data, error} = await useApi(app).deleteAuthed(`/api/workouts/${workoutId}/tags/${tagInd}`)

            if (!!error)
                return {error}

            useMainStore().setWorkout(data)

            return {data}
        },
    }
}