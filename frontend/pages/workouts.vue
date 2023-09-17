<script setup lang="ts">
import WorkoutService from "~/services/workoutService";
import {useMainStore} from "~/store";

definePageMeta({
  middleware: 'auth'
})

let workouts = await WorkoutService.getWorkouts()
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