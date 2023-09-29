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
    contraction?: Contraction
    elevation?: Elevation
    myoRepMatch?: boolean
}

export interface Weight {
    kg?: number
    bodyWeight?: boolean
}

export interface RepsInReserve {
    min: number
    max?: number
}

export interface Contraction {
    isometric?: Isometric
    eccentric?: Eccentric
}

export interface Eccentric {
    minSeconds: number
    maxSeconds?: number
}

export interface Isometric {
    minSeconds: number
    maxSeconds?: number
}

export interface Elevation {
    cm: number
}

export interface Exercise {
    id: string
    name: string
}

export interface WorkoutVolume {
    date: Date
    volume: number
}