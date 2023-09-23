<template>
  <div>
    <v-card
        :subtitle="utilsMain.getWorkoutDisplayDate(workout.date)"
        variant="tonal"
    >
      <template v-slot:title>
        <div class="d-flex justify-space-between">
          <div>
            {{getWorkoutMesocycle()}}
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
            width="210px"
        >
          <v-card >
            <v-card-text>
              Delete this workout?
            </v-card-text>
            <v-card-actions class="d-flex justify-space-between">
                <v-btn color="red" @click="deleteWorkout">Yes</v-btn>
                <v-btn variant="tonal" @click="showDeleteWorkoutDialog = false">No</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </template>

      <template v-slot:text>
        <div class="mb-2">
          Exercises:
        </div>
        <div v-for="(item, index) in workout.exercises" :key="index">
          <ExerciseRecordComponent :exercise-record="item"/>
        </div>

        <div class="mt-2">
          <v-btn density="compact" size="medium" icon="mdi-plus" variant="text" @click="showRecordExerciseDialog = true"/>
        </div>

        <v-dialog
            v-model="showRecordExerciseDialog"
            width="500"
        >
          <v-card >
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
                  <v-btn style="max-width: 20%" color="primary" @click="recordExercise">Add</v-btn>
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
import {ExerciseRecord, RepsInReserve, Workout} from "~/domain/domain";
import {PropType} from "@vue/runtime-core";
import utilsMain from "~/services/utilsMain";

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
    return ({ exerciseId: '',  repetitions: 0, weight: {} }) as ExerciseRecord
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

    await useWorkoutApi().recordExercise(props.workout.id, val)
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

async function deleteWorkout() {
  await useWorkoutApi().deleteWorkout(props.workout.id)
  showDeleteWorkoutDialog.value = false
}
</script>

<style scoped>

</style>