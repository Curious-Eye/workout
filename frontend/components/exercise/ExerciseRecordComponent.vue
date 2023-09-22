<template>
  <div class="d-flex">
    <div class="mr-5">
      {{ exercise.name }}
    </div>
    <div class="mr-5 d-flex">
      <div v-if="exerciseRecord.weight.bodyWeight">
        bw
      </div>
      <div v-else>
        {{exerciseRecord.weight.kg}}
      </div>
      x
      <div>
        {{exerciseRecord.repetitions}}
      </div>
    </div>
    <div v-if="exerciseRecord.warmup">
      w-up
    </div>
    <div v-if="exerciseRecord.rir" class="d-flex">
      rir: {{exerciseRecord.rir.min}}&nbsp;
      <div v-if="exerciseRecord.rir.max">
        - {{exerciseRecord.rir.max}}
      </div>
    </div>
    <div v-if="exerciseRecord.myoRepMatch">
      myo-match
    </div>
  </div>
</template>

<script setup lang="ts">
import {PropType} from "@vue/runtime-core";
import {ExerciseRecord} from "~/domain/domain";
import {useMainStore} from "~/store";

const props = defineProps({
  exerciseRecord: {
    type: Object as PropType<ExerciseRecord>,
    required: true
  }
})

const exercise = useMainStore().getExercise(props.exerciseRecord.exerciseId)

</script>

<style scoped>

</style>