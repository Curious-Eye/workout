<template>
  <div>
    {{ exerciseRecord }}
  </div>
</template>

<script setup lang="ts">

import {PropType} from "@vue/runtime-core";
import {ExerciseRecord} from "~/domain/domain";

const props = defineProps({
  exerciseRecord: {
    type: Object as PropType<ExerciseRecord>,
    required: true
  }
})

</script>

<style scoped>

</style>