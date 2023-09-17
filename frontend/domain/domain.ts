export interface Workout {
    id: string
    date: Date
    exercises: Array<ExerciseRecord>
    mesocycle?: string
}

export interface ExerciseRecord {
    exerciseId: string
    repetitions: number
    weight: Weight
    warmup?: boolean
    rir?: RepsInReserve
    mioRepMatch?: boolean
}

export interface Weight {
    kg?: number
    bodyWeight?: boolean
}

export interface RepsInReserve {
    min: number
    max?: number
}