<template>
  <Workouts></Workouts>
</template>
<script setup lang="ts">
import Workouts from "~/pages/workouts.vue";

definePageMeta({
  middleware: 'auth'
})
</script>