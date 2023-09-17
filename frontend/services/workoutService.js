import BaseRequestService from "~/services/baseRequestService";

export default {
    /**
     * Record a workout
     * @param mesocycle {string}
     * @param date {Date}
     * @return {Promise<Workout>}
     */
    async recordWorkout(mesocycle, date) {
        console.log('Try recordWorkout. mesocycle=' + mesocycle + ', date=' + date.toString())
        return await BaseRequestService.postAuthed(
            '/api/workouts',
            {mesocycle, date}
        )
    },
    /**
     *
     * @return {Promise<[Workout]>}
     */
    async getWorkouts() {
        console.log('Try getWorkouts')
        return await BaseRequestService.getAuthed('/api/workouts')
    }
}
