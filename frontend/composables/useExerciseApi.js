import {useMainStore} from "~/store";
import {useNuxtApp} from "#app";

export const useExerciseApi = (app) => {
    if (!app)
        app = useNuxtApp()
    return {
        /**
         * Get all exercises
         * @param redirectToLoginOnAuthFail {boolean}
         * @return {Promise<{data?: [Exercise], error?: *}>}
         */
        async getExercises(redirectToLoginOnAuthFail = true) {
            console.log('Try getExercises')
            const {data, error} = await useApi(app).getAuthed('/api/exercises', redirectToLoginOnAuthFail)

            if (!!error) {
                console.log('Error from getExercises: ')
                console.log(error)
                return {error}
            }

            if (process.client) useMainStore().setExercises(data)
            else app.runWithContext(() => useMainStore().setExercises(data))

            return {data}
        },
        /**
         * Create exercise
         * @param exercise {{name:string}}
         * @return {Promise<{data?: [Exercise], error?: *}>}
         */
        async createExercise(exercise) {
            console.log('Try getExercises')
            const {data, error} = await useApi(app).postAuthed('/api/exercises', exercise)

            if (!!error) {
                console.log('Error from getExercises: ')
                console.log(error)
                return {error}
            }

            useMainStore().addExercise(data)

            return {data}
        }
    }
}