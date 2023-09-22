<template>
  <div>
    <div class="d-flex flex-column" style="min-width: 30%">
<!--      <div class="pb-5">-->
<!--        <v-btn @click="reloadWorkouts">Refresh</v-btn>-->
<!--      </div>-->
      <div class="pb-5">
        <v-btn @click="showRecordWorkoutDialog = true" variant="outlined">Record</v-btn>
      </div>
      <div class="d-flex" style="height: 600px">
        <v-virtual-scroll :items="useMainStore().workouts">
          <template v-slot:default="{ item }">
            <WorkoutComponent class="mb-5" :workout="item"/>
          </template>
        </v-virtual-scroll>
      </div>
    </div>

    <v-dialog
        v-model="showRecordWorkoutDialog"
        min-width="300px"
        max-width="390px"
    >
      <v-card >
        <div >
          <v-card-text>
            <div class="d-flex flex-column">
              <v-text-field
                  v-model="workoutInput.meso"
                  label="Meso:"
              />
              <div class="pb-5 text-h6">
                Select date:
              </div>
              <v-date-picker width="200" v-model="workoutInput.date"  hide-actions title="">
                <template v-slot:header="{header}">
                  <div class="ml-6 mt-5 text-h6">
                    {{header}}
                  </div>
                </template>
              </v-date-picker>
            </div>
          </v-card-text>
        </div>
        <v-card-actions class="d-flex justify-space-between mb-2">
          <v-btn color="green" @click="recordWorkout">Record</v-btn>
          <v-btn variant="tonal" @click="showRecordWorkoutDialog = false">Cancel</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";
import {useAsyncData} from "#app";
import { VDatePicker } from 'vuetify/labs/VDatePicker'

definePageMeta({
  middleware: 'auth'
})

const {data: workouts} = await useAsyncData('workouts', (ctx) => useWorkoutApi(ctx).getWorkouts())
const {data: exercises} = await useAsyncData('exercises', (ctx) => useExerciseApi(ctx).getExercises())
if (workouts.value?.data)
  useMainStore().setWorkouts(workouts.value?.data)

if (exercises.value?.data)
  useMainStore().setExercises(exercises.value?.data)

async function reloadWorkouts() {
  await useWorkoutApi(null).getWorkouts()
}

const showRecordWorkoutDialog = ref(false)
const workoutInput = ref({
  meso: '',
  date: new Date()
})

async function recordWorkout() {
  if (workoutInput.value.meso) {
    await useWorkoutApi().recordWorkout(workoutInput.value.meso, workoutInput.value.date)
    showRecordWorkoutDialog.value = false
  }
}
</script>

<style scoped>

</style>