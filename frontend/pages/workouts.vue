<script setup lang="ts">
import {useMainStore} from "~/store";

definePageMeta({
  middleware: 'auth'
})

let workouts = await useWorkoutApi().getWorkouts()
useMainStore().setWorkouts(workouts)
</script>

<template>
  <div>
    <div v-for="workout in workouts">
      <WorkoutComponent :workout-id="workout.id"/>
    </div>
  </div>
</template>

<style scoped>

</style>