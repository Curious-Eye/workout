<template>
  <div>
    <div class="pb-5">
      {{ useMainStore().getExercise(exerciseId).name }}
    </div>
    <div>
      <ClientOnly>
        <apexchart
            height="400"
            width="100%"
            type="bar"
            :options="options"
            :series="series"
        ></apexchart>
      </ClientOnly>
    </div>
  </div>
</template>

<script setup lang="ts">
import {WorkoutVolume} from "~/domain/domain";
import {PropType} from "@vue/runtime-core";
import {useMainStore} from "~/store";
import utilsMain from "~/services/utilsMain";

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

await useExerciseApi().getExercises()
const name = useMainStore().getExercise(props.exerciseId).name
const series = [{
  name,
  data: props.stats?.volume.map(v => v.volume)
}]
const options = {
  chart: {
    id: `volume-chart-${props.exerciseId}`
  },
  xaxis: {
    title: {
      text: 'Date',
      style: {
        fontSize: '14px'
      }
    },
    categories: props.stats?.volume.map(v => utilsMain.getWorkoutDisplayDate(v.date))
  },
  yaxis: {
    title: {
      text: 'Volume',
      style: {
        fontSize: '14px'
      }
    },
  }
}
</script>

<style scoped>

</style>