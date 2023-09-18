import {useUserStore} from "~/store";
import BaseRequestService from "~/services/baseRequestService";

export const useWorkoutApi = () => {
    const serverHost = useRuntimeConfig().public.serverHost
    const at = useUserStore().user.accessToken
    const rt = useUserStore().user.refreshToken

    return {
        /**
         *
         * @return {Promise<[Workout]>}
         */
        async getWorkouts() {
            console.log('Try getWorkouts')
            const {data, error, newTokens} =
                await BaseRequestService.getAuthed('/api/workouts', serverHost, at, rt)

            if (!!newTokens)
                useUserStore().setUserTokens(newTokens.accessToken, newTokens.refreshToken)

            if (error) {
                console.log('Error from getWorkouts: ')
                console.log(JSON.stringify(error))
                throw new Error(error)
            }

            return data
        },
        /**
         * Record a workout
         * @param mesocycle {string}
         * @param date {Date}
         * @return {Promise<Workout>}
         */
        async recordWorkout(mesocycle, date) {
            console.log('Try recordWorkout. mesocycle=' + mesocycle + ', date=' + date.toString())

            const {data, error, newTokens} =
                await BaseRequestService.postAuthed('/api/workouts', {mesocycle, date}, serverHost, at, rt)

            if (!!newTokens)
                useUserStore().setUserTokens(newTokens.accessToken, newTokens.refreshToken)

            if (error) {
                console.log('Error from recordWorkout: ')
                console.log(JSON.stringify(error))
                throw new Error(error)
            }

            return data
        }
    }
}