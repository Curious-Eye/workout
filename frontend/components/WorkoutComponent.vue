<template>
  <div>
    <v-card
        :title="getWorkoutMesocycle()"
        :subtitle="workout.date.toString()"
        variant="tonal"
    >
      <template v-slot:text>
        Exercises:
        <div v-for="exercise in workout.exercises">
          <ExerciseRecordComponent :exercise-record="exercise"/>
        </div>
        <v-btn density="compact" icon="mdi-plus" @click="dialog = true">
        </v-btn>
        <v-dialog
            v-model="dialog"
            width="auto"
        >
          <v-card >
            <v-card-text>
              Input
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" block @click="dialog = false">Close</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </template>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";

const props = defineProps({
  workoutId: {
    type: String,
    required: true
  }
})

const workout = useMainStore().getWorkout(props.workoutId)

let exerciseInput = {

}
let dialog = useState('dialog', () => false)

function getWorkoutMesocycle() {
  return workout.mesocycle ? workout.mesocycle : ''
}

function addExercise() {
  dialog.value = true
}
</script>

<style scoped>

</style>