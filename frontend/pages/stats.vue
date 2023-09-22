<template>
  <div>
    <div v-for="exerciseId in stats.keys()" :key="exerciseId">
      <StatsExerciseGraph :exercise-id="exerciseId" :stats="stats.get(exerciseId)">

      </StatsExerciseGraph>
    </div>
  </div>
</template>

<script setup lang="ts">
import {WorkoutVolume} from "~/domain/domain";

definePageMeta({
  middleware: 'auth'
})

const {data} = await useAsyncData('stats_all', () => useStatsApi().getStatsForAllExercises())

const stats = data.value?.data ? data.value?.data.stats : new Map<string, {volume: WorkoutVolume[]}>()
</script>

<style scoped>

</style>