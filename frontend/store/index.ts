import {defineStore} from 'pinia'
import {Exercise, ExerciseRecord, Workout} from "~/domain/domain";

export const useMainStore = defineStore('main', {
    state: () => ({
        workouts: ref([] as Workout[]),
        exercises: [] as Exercise[],
        lastExerciseRecordInput: {} as ExerciseRecord,
        authenticated: !!useCookie('accessToken').value,
        tags: ref([] as string[])
    }),
    getters: {
    },
    actions: {
        setWorkouts(workouts: Workout[]) {
            this.workouts = workouts
        },
        addWorkout(workout: Workout) {
            this.workouts.splice(0, 0, workout)
        },
        setWorkout(workout: Workout) {
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
        setTags(tags: string[]) {
            this.tags = tags
        },
        addTag(tag: string) {
            if (this.tags.findIndex(value => value === tag) == -1)
                this.tags.splice(this.tags.length - 1, 0, tag)
        }
    },
    persist: {
        storage: persistedState.localStorage
    },
})
