import {defineStore} from 'pinia'
import {Exercise, ExerciseRecord, Workout} from "~/domain/domain";

export const useMainStore = defineStore('main', {
    state: () => ({
        workouts: ref([] as Workout[]),
        exercises: [] as Exercise[],
        lastExerciseRecordInput: {} as ExerciseRecord,
        authenticated: !!useCookie('accessToken').value
    }),
    getters: {
    },
    actions: {
        setWorkouts(workouts: Workout[]) {
            for (let i = 0; i < workouts.length; ++i) {
                workouts[i].date = new Date(workouts[i].date)
            }
            this.workouts = workouts
        },
        addWorkout(workout: Workout) {
            workout.date = new Date(workout.date)
            this.workouts.splice(0, 0, workout)
        },
        setWorkout(workout: Workout) {
            workout.date = new Date(workout.date)
            const ind = this.workouts.findIndex(value => value.id === workout.id)
            if (ind !== -1) this.workouts.splice(ind, 1, workout)
            else this.workouts.push(workout)
        },
        getExercise(id: string): Exercise {
            return this.exercises.find(value => value.id === id) as Exercise
        },
        setExercises(exercises: Exercise[]) {
            this.exercises = exercises
        },
        addExercise(exercise: Exercise) {
            this.exercises.push(exercise)
        },
        deleteWorkout(workoutId: string) {
            const ind = this.workouts.findIndex(value => value.id === workoutId)
            this.workouts.splice(ind, 1)
        },
    },
    persist: {
        storage: persistedState.localStorage
    },
})
