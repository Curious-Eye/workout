<template>
  <div>
    <v-card
        :subtitle="utilsMain.getWorkoutDisplayDate(workout.date)"
        color="teal"
        variant="flat"
    >
      <template v-slot:title>
        <div class="d-flex justify-space-between">
          <div>
            {{ getWorkoutMesocycle() }}
          </div>
          <div>
            <v-btn
                class="mb-5"
                icon="mdi-file-remove-outline"
                variant="text"
                density="compact"
                @click="showDeleteWorkoutDialog = true"
            />
          </div>
        </div>

        <v-dialog
            v-model="showDeleteWorkoutDialog"
            max-width="250px"
        >
          <v-card>
            <v-card-text>
              Delete this workout?
            </v-card-text>
            <v-card-actions class="d-flex justify-space-between">
              <div class="pl-2 pb-2">
                <v-btn color="teal-darken-1" variant="flat" @click="deleteWorkout">Yes</v-btn>
              </div>
              <div class="pr-2 pb-2">
                <v-btn variant="text" @click="showDeleteWorkoutDialog = false">No</v-btn>
              </div>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </template>

      <template v-slot:text>
        <div class="pb-2">
          Exercises:
        </div>
        <v-divider class="pb-2"/>
        <div class="pb-2" v-for="(item, index) in workout.exercises" :key="index">
          <div class="pb-2">
            <ExerciseRecordComponent :exercise-record="item" @deleteRequested="deleteRecordedExercise(index)"/>
          </div>
          <v-divider/>
        </div>

        <div class="pl-3">
          <v-btn density="compact" size="medium" icon="mdi-plus" variant="text"
                 @click="showRecordExerciseDialog = true"/>
        </div>

        <v-dialog
            v-model="showRecordExerciseDialog"
            width="500"
        >
          <v-card>
            <v-card-text>
              <div class="d-flex flex-column">
                <ExerciseSelectMenu v-model="exerciseInput.exerciseId"></ExerciseSelectMenu>
                <div class="d-flex">
                  <v-text-field
                      v-model="exerciseInput.repetitions"
                      label="Reps:"
                      type="number"
                  />
                  <v-text-field
                      class="ml-5"
                      v-model="exerciseInput.weight.kg"
                      label="Weight(kg):"
                      type="number"
                      :disabled="!!exerciseInput.weight.bodyWeight"
                  />
                  <v-checkbox
                      style="min-width: 20%"
                      v-model="exerciseInput.weight.bodyWeight"
                      label="Bw"
                  />
                </div>
                <div class="d-flex align-center">
                  Rir:
                  <v-text-field
                      class="ml-5"
                      :model-value="exerciseInput.rir?.min"
                      label="min:"
                      type="number"
                      :disabled="!!exerciseInput.warmup"
                      @update:model-value="value => setExerciseInputRir({min: value} as RepsInReserve)"
                  />
                  <v-text-field
                      class="ml-5"
                      :model-value="exerciseInput.rir?.max"
                      label="max:"
                      type="number"
                      :disabled="!!exerciseInput.warmup"
                      @update:model-value="value => setExerciseInputRir({max: value} as RepsInReserve)"
                  />
                </div>
                <div class="d-flex align-center">
                  <div class="flex-none overflow-hidden">
                    Eccentric (s):
                  </div>
                  <v-text-field
                      class="ml-5"
                      :model-value="exerciseInput.contraction?.eccentric?.minSeconds"
                      label="min:"
                      type="number"
                      @update:model-value="value => setExerciseInputEccentric({minSeconds: value} as Eccentric)"
                  />
                  <v-text-field
                      class="ml-5"
                      :model-value="exerciseInput.contraction?.eccentric?.maxSeconds"
                      label="max:"
                      type="number"
                      @update:model-value="value => setExerciseInputEccentric({maxSeconds: value} as Eccentric)"
                  />
                </div>
                <div class="d-flex align-center">
                  <div class="flex-none overflow-hidden">
                    Isometric (s):
                  </div>
                  <v-text-field
                      class="ml-5"
                      :model-value="exerciseInput.contraction?.isometric?.minSeconds"
                      label="min:"
                      type="number"
                      @update:model-value="value => setExerciseInputIsometric({minSeconds: value} as Isometric)"
                  />
                  <v-text-field
                      class="ml-5"
                      :model-value="exerciseInput.contraction?.isometric?.maxSeconds"
                      label="max:"
                      type="number"
                      @update:model-value="value => setExerciseInputIsometric({maxSeconds: value} as Isometric)"
                  />
                </div>
                <div class="d-flex align-center">
                  <div class="flex-none overflow-hidden">
                    Elevation (cm):
                  </div>
                  <v-text-field
                      class="ml-5"
                      :model-value="exerciseInput.elevation?.cm"
                      label="centimeters:"
                      type="number"
                      @update:model-value="setElevation"
                  />
                </div>
                <div class="d-flex align-center mb-5">
                  <v-checkbox-btn
                      style="min-width: 20%"
                      v-model="exerciseInput.warmup"
                      label="Warmup"
                  />
                  <v-checkbox-btn
                      style="min-width: 20%"
                      v-model="exerciseInput.myoRepMatch"
                      label="Myo rep match"
                  />
                  <v-btn style="max-width: 20%" color="teal-darken-1" @click="recordExercise">Add</v-btn>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-dialog>
      </template>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";
import {Eccentric, ExerciseRecord, Isometric, RepsInReserve, Workout} from "~/domain/domain";
import {PropType} from "@vue/runtime-core";
import utilsMain from "~/services/utilsMain";

const emit = defineEmits<{(e: 'apiError', value: string): void}>()

const props = defineProps({
  workout: {
    type: Object as PropType<Workout>,
    required: true
  }
})

let exerciseInput = useState('exerciseInput', () => {
  if (!!useMainStore().lastExerciseRecordInput.exerciseId)
    return useMainStore().lastExerciseRecordInput
  else
    return ({
      exerciseId: '',
      repetitions: 0,
      weight: {},
    }) as ExerciseRecord
})
let showRecordExerciseDialog = ref(false)
let showDeleteWorkoutDialog = ref(false)

function getWorkoutMesocycle() {
  return props.workout.mesocycle ? props.workout.mesocycle : ''
}

async function recordExercise() {
  const val = exerciseInput.value
  if (val.exerciseId && val.repetitions && val.weight) {
    if (val.weight.bodyWeight)
      val.weight.kg = undefined

    if (val.warmup)
      val.rir = undefined

    if (val.rir?.max && val.rir?.max <= 0)
      val.rir.max = undefined

    if (val.rir?.min && val.rir?.min <= 0)
      val.rir = undefined

    if (val.contraction?.eccentric && val.contraction.eccentric.minSeconds <= 0)
      val.contraction.eccentric = undefined

    if (val.contraction?.eccentric?.maxSeconds && val.contraction.eccentric.maxSeconds == 0)
      val.contraction.eccentric.maxSeconds = undefined

    if (val.contraction?.isometric && val.contraction.isometric.minSeconds < 0)
      val.contraction.isometric = undefined

    if (val.contraction?.isometric && val.contraction.isometric.minSeconds == 0 && !val.contraction?.isometric.maxSeconds)
      val.contraction.isometric = undefined

    if (val.contraction?.isometric?.maxSeconds && val.contraction.isometric.maxSeconds == 0)
      val.contraction.isometric.maxSeconds = undefined

    if (val.elevation?.cm && val.elevation?.cm <= 0)
      val.elevation = undefined

    const {error} = await useWorkoutApi().recordExercise(props.workout.id, val)

    if (error)
      emit('apiError', error.body.msg)

    showRecordExerciseDialog.value = false
  }
  exerciseInput.value = val
}

function setExerciseInputRir(rir: RepsInReserve) {
  if (!exerciseInput.value.rir)
    exerciseInput.value.rir = rir
  else {
    if (rir.min)
      exerciseInput.value.rir.min = rir.min
    else
      exerciseInput.value.rir.max = rir.max
  }
}

function setExerciseInputEccentric(eccentric: Eccentric) {
  if (!exerciseInput.value.contraction)
    exerciseInput.value.contraction = {}

  if (!exerciseInput.value.contraction.eccentric)
    exerciseInput.value.contraction.eccentric = eccentric
  else {
    if (eccentric.minSeconds)
      exerciseInput.value.contraction.eccentric.minSeconds = eccentric.minSeconds
    else
      exerciseInput.value.contraction.eccentric.maxSeconds = eccentric.maxSeconds
  }
}


function setExerciseInputIsometric(isometric: Isometric) {
  if (!exerciseInput.value.contraction)
    exerciseInput.value.contraction = {}

  if (!exerciseInput.value.contraction.isometric)
    exerciseInput.value.contraction.isometric = isometric
  else {
    if (isometric.minSeconds)
      exerciseInput.value.contraction.isometric.minSeconds = isometric.minSeconds
    else
      exerciseInput.value.contraction.isometric.maxSeconds = isometric.maxSeconds
  }
}

function setElevation(elevation: number) {
  if (!elevation)
    exerciseInput.value.elevation = undefined
  else {
    if (!exerciseInput.value.elevation) exerciseInput.value.elevation = {cm: elevation}
    else exerciseInput.value.elevation.cm = elevation
  }
}

async function deleteWorkout() {
  const {error} = await useWorkoutApi().deleteWorkout(props.workout.id)

  if (error)
    emit('apiError', error.body.msg)

  showDeleteWorkoutDialog.value = false
}

async function deleteRecordedExercise(recordInd: number) {
  const {error} = await useWorkoutApi().deleteRecordedExercise(props.workout?.id, recordInd)

  if (error)
    emit('apiError', error.body.msg)
}
</script>

<style scoped>

</style>