<template>
  <div>
    <v-container fluid>
      <sparklines
          :value="stats.volume.map(value => value.volume)"
          :smooth="false"
          :padding="0"
          :line-width="2"
          :stroke-linecap="'round'"
          auto-draw
      ></sparklines>

    </v-container>
  </div>
</template>

<script setup lang="ts">
import {WorkoutVolume} from "~/domain/domain";
import {PropType} from "@vue/runtime-core";

const props = defineProps({
  stats: {
    type: Object as PropType<{volume: WorkoutVolume[]}>,
    required: true
  },
  exerciseId: {
    type: String,
    required: true
  }
})
</script>

<style scoped>

</style>