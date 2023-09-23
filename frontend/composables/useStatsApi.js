export const useStatsApi = (app) => {
    if (!app)
        app = useNuxtApp()

    return {
        /**
         * Get stats for an exercise
         * @param exerciseId {string}
         * @return {Promise<{data?: { volume: WorkoutVolume[] }, error?: *}>}
         */
        async getExerciseStats(exerciseId) {
            console.log('Try getExerciseStats')
            const {data, error} = await useApi(app).getAuthed(`/api/stats/exercises/${exerciseId}`)

            if (!!error)
                return {error}

            return {data}
        },
        /**
         * Get stats for an exercise
         * @return {Promise<{data?: { stats: Map<String, { volume: WorkoutVolume[] }> }, error?: *}>}
         * Returns stats, which is a map of exerciseId to Stats
         */
        async getStatsForAllExercises() {
            console.log('Try getExerciseStats')
            const {data, error} = await useApi(app).getAuthed(`/api/stats/exercises`)

            if (!!error)
                return {error}

            data.stats = new Map(Object.entries(data.stats))

            data.stats.forEach((value, key) => {
                value.volume.forEach(v => v.date = new Date(v.date))
            })

            return {data}
        },
    }
}