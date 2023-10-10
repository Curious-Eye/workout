import {Workout} from "~/domain/domain";

export default {

    getWorkoutDisplayDate(workout: Workout): string {
        return this.getWorkoutDisplayDateFromDate(new Date(workout.date))
    },
    getWorkoutDisplayDateFromDate(date: Date): string {
        const monthNames = [
            'January', 'February', 'March', 'April',
            'May', 'June', 'July', 'August',
            'September', 'October', 'November', 'December'
        ];

        const year = date.getFullYear()
        const month = monthNames[date.getMonth()]
        const day = date.getDate().toString().padStart(2, '0')

        return `${year} ${month} ${day}`
    },
    capitalizeFirstCharacter(str: string): string {
        return str.charAt(0).toUpperCase() + str.slice(1);
    }
}