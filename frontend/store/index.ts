import {defineStore} from 'pinia'
import {Workout} from "~/domain/domain";

export const useMainStore = defineStore('main', {
    state: () => ({
        workouts: new Map() as Map<string, Workout>
    }),
    getters: {
    },
    actions: {
        getWorkout(id: string): Workout {
            return this.workouts.get(id) as Workout
        },
        setWorkouts(workouts: Workout[]) {
            this.workouts = new Map<string, Workout>(workouts.map(value => [value.id, value]))
        }
    },
    persist: true,
})

export const useUserStore = defineStore('user', {
    state: () => {
        return {
            user: {
                accessToken: '',
                refreshToken: ''
            }
        }
    },
    getters: {
        /**
         * Returns true if current user is authenticated.
         * No request to the backend is performed.
         * @return {boolean}
         */
        isAuthenticated(): boolean {
            return !!this.user.accessToken
        }
    },
    actions: {
        setUserTokens(accessToken: string, refreshToken: string) {
            this.user.accessToken = accessToken
            this.user.refreshToken = refreshToken
        },
    },
    persist: true
})