<template>
  <div class="d-flex flex-column flex-grow-1 ">
    <div v-for="exerciseId in stats.keys()" :key="exerciseId">
      <div v-if="stats.get(exerciseId).volume.length > 0">
        <StatsExerciseGraph :exercise-id="exerciseId" :stats="stats.get(exerciseId)"/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {WorkoutVolume} from "~/domain/domain";

definePageMeta({
  middleware: 'auth'
})

const {data} = await useAsyncData('stats_all', (ctx) => useStatsApi(ctx).getStatsForAllExercises())

const stats = data.value?.data ? data.value?.data.stats : new Map<string, {volume: WorkoutVolume[]}>()
</script>

<style scoped>

</style>