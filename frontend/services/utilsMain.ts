export default {

    getWorkoutDisplayDate(date: Date): string {
        const monthNames = [
            'January', 'February', 'March', 'April',
            'May', 'June', 'July', 'August',
            'September', 'October', 'November', 'December'
        ];

        const year = date.getFullYear()
        const month = monthNames[date.getMonth()]
        const day = date.getDate().toString().padStart(2, '0')

        return `${year} ${month} ${day}`
    }
}