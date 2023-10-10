<template>
  <div class="d-flex flex-column align-center justify-center">
    <v-snackbar
        v-model="showErrorSnackbar"
        location="top"
        multi-line
        :timeout="6000"
    >
      {{ errorMsg }}

      <template v-slot:actions>
        <v-btn
            color="teal-darken-1"
            variant="flat"
            @click="showErrorSnackbar = false"
        >
          Close
        </v-btn>
      </template>
    </v-snackbar>
    <v-select
        class="w-50 align-self-end mr-4 mb-3"
        style="min-width: 150px"
        v-model="selectedTags"
        :items="useMainStore().tags"
        color="teal-darken-1"
        base-color="teal-darken-4"
        variant="outlined"
        label="Tags"
        hide-details
        chips
        multiple
        @update:model-value="reloadWorkouts"
    >
      <template v-slot:selection="{ item, index }">
        <v-chip v-if="index < 2">
          <span>{{ item.title }}</span>
        </v-chip>
        <span
            v-if="index === 2"
            class="text-grey text-caption align-self-center"
        >
<!--          (+{{ selectedTags.length - 2 }} others)-->
          ...
        </span>
      </template>
    </v-select>
    <v-virtual-scroll
        :items="useMainStore().workouts"
        height="100%"
    >
      <template v-slot:default="{ item }">
        <v-list-item>
          <WorkoutComponent class="mb-5" :workout="item" @apiError="displayErrorMsg"/>
        </v-list-item>
      </template>
    </v-virtual-scroll>
    <v-fab-transition>
      <v-btn
          class="add-workout-btn"
          color="teal-darken-1"
          icon="mdi-plus"
          size="x-large"
          @click="showRecordWorkoutDialog = true"
          dark
          bottom
          right
          fab
      >
        <v-icon>mdi-plus</v-icon>
      </v-btn>
    </v-fab-transition>

    <v-dialog v-model="showRecordWorkoutDialog" width="auto">
      <v-card max-width="400">
        <div>
          <v-card-text>
            <div class="d-flex flex-column">
              <v-text-field
                  v-model="workoutInput.meso"
                  label="Meso:"
              />
              <div class="pb-5 text-h6">
                Select date:
              </div>

              <v-virtual-scroll :items="[1]">
                <template v-slot:default="{ item }">
                  <v-date-picker v-model="workoutInput.date" color="teal-darken-1" hide-actions show-adjacent-months
                                 title="">
                    <template v-slot:header="{header}">
                      <div class="ml-6 text-h6">
                        {{ header }}
                      </div>
                    </template>
                  </v-date-picker>
                </template>
              </v-virtual-scroll>
            </div>
          </v-card-text>
        </div>
        <v-card-actions class="d-flex justify-space-between mb-2">
          <div class="pl-2">
            <v-btn color="teal-darken-1" variant="flat" @click="recordWorkout">Record</v-btn>
          </div>
          <div class="pr-2">
            <v-btn variant="text" @click="showRecordWorkoutDialog = false">Cancel</v-btn>
          </div>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";
import {useAsyncData} from "#app";
import {VDatePicker} from 'vuetify/labs/VDatePicker'

definePageMeta({
  middleware: 'auth'
})

await useAsyncData('workouts', (ctx) => useWorkoutApi(ctx).getWorkouts())
await useAsyncData('exercises', (ctx) => useExerciseApi(ctx).getExercises())
await useAsyncData('tags', (ctx) => useTagsApi(ctx).getTags())
const selectedTags = ref([] as string[])

async function reloadWorkouts() {
  await useWorkoutApi(null).getWorkouts(selectedTags.value)
}

const showRecordWorkoutDialog = ref(false)
const workoutInput = ref({
  meso: '',
  date: new Date()
})

const showErrorSnackbar = ref(false)
const errorMsg = ref('')

async function recordWorkout() {
  if (workoutInput.value.date) {
    const {error} = await useWorkoutApi().recordWorkout(workoutInput.value.meso, workoutInput.value.date)

    if (error)
      displayErrorMsg(error.body.msg)

    showRecordWorkoutDialog.value = false
  }
}

function displayErrorMsg(msg: string) {
  showErrorSnackbar.value = true
  errorMsg.value = msg
}
</script>

<style scoped>
.add-workout-btn {
  bottom: 0;
  right: 0;
  position: fixed;
  margin: 0 16px 16px 0;
}
</style>