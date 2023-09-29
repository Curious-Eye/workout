<template>
  <div class="d-flex">
    <div class="mr-5">
      {{ exercise.name }}
    </div>
    <div class="d-flex flex-wrap">
      <div class="mr-5 d-flex" v-if="exerciseRecord.elevation?.cm">
        <v-chip>
          <v-icon icon="mdi-chevron-up"/>
          {{ exerciseRecord.elevation.cm }}
        </v-chip>
      </div>
      <div class="mr-5 d-flex">
        <v-chip>
          <div v-if="exerciseRecord.weight.bodyWeight">
            <v-icon icon="mdi-human-handsdown"/>
          </div>
          <div v-else>
            {{ exerciseRecord.weight.kg }}
          </div>
          x
          <div>
            {{ exerciseRecord.repetitions }}
          </div>
        </v-chip>
      </div>
      <div v-if="exerciseRecord.warmup">
        <v-chip>
          w-up
        </v-chip>
      </div>
      <div class="d-flex pr-5" v-if="exerciseRecord.rir">
        <v-chip>
          <v-icon icon="mdi-plus"/>
          {{ exerciseRecord.rir.min }}&nbsp;
          <div v-if="exerciseRecord.rir.max">
            - {{ exerciseRecord.rir.max }}
          </div>
        </v-chip>
      </div>
      <div class="d-flex pr-5"
           v-if="exerciseRecord.contraction?.eccentric?.minSeconds && exerciseRecord.contraction.eccentric.minSeconds > 0">
        <v-chip>
          <v-icon icon="mdi-waves-arrow-right" style="transform: rotate(90deg);"/>&nbsp;
          {{ exerciseRecord.contraction.eccentric.minSeconds }}&nbsp;
          <div v-if="exerciseRecord.contraction.eccentric.maxSeconds">
            - {{ exerciseRecord.contraction.eccentric.maxSeconds }}
          </div>
        </v-chip>
      </div>
      <div class="d-flex"
           v-if="exerciseRecord.contraction?.isometric && exerciseRecord.contraction.isometric.minSeconds >= 0">
        <v-chip>
          <v-icon icon="mdi-motion-pause"/>&nbsp;
          {{ exerciseRecord.contraction.isometric.minSeconds }}&nbsp;
          <div v-if="exerciseRecord.contraction.isometric.maxSeconds">
            - {{ exerciseRecord.contraction.isometric.maxSeconds }}
          </div>
        </v-chip>
      </div>
      <div v-if="exerciseRecord.myoRepMatch">
        <v-chip>
          myo-match
        </v-chip>
      </div>
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