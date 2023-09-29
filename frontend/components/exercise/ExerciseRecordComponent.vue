<template>
  <div class="d-flex align-center">
    <div class="d-flex mr-5 text-no-wrap align-center justify-center">
      <div class="pr-2">
        <v-menu transition="slide-y-transition" location="bottom">
          <template v-slot:activator="{ props }">
            <v-btn variant="text" icon="mdi-menu" v-bind="props"/>
          </template>

          <div class="mt-2">
            <v-btn color="teal" icon="mdi-delete" @click="showDeleteExerciseRecordDialog = true"/>
          </div>
        </v-menu>
      </div>
      {{ exercise.name }}
    </div>
    <div class="d-flex flex-wrap align-center">
      <div class="d-flex recordField" v-if="exerciseRecord.elevation?.cm">
        <v-chip>
          <v-icon icon="mdi-chevron-up"/>
          {{ exerciseRecord.elevation.cm }}
        </v-chip>
      </div>
      <div class="d-flex recordField">
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
      <div class="recordField" v-if="exerciseRecord.warmup">
        <v-chip>
          w-up
        </v-chip>
      </div>
      <div class="d-flex recordField" v-if="exerciseRecord.rir">
        <v-chip>
          <v-icon icon="mdi-plus"/>
          {{ exerciseRecord.rir.min }}&nbsp;
          <div v-if="exerciseRecord.rir.max">
            - {{ exerciseRecord.rir.max }}
          </div>
        </v-chip>
      </div>
      <div class="d-flex recordField"
           v-if="exerciseRecord.contraction?.eccentric?.minSeconds && exerciseRecord.contraction.eccentric.minSeconds > 0">
        <v-chip>
          <v-icon icon="mdi-waves-arrow-right" style="transform: rotate(90deg);"/>&nbsp;
          {{ exerciseRecord.contraction.eccentric.minSeconds }}&nbsp;
          <div v-if="exerciseRecord.contraction.eccentric.maxSeconds">
            - {{ exerciseRecord.contraction.eccentric.maxSeconds }}
          </div>
        </v-chip>
      </div>
      <div class="d-flex recordField"
           v-if="exerciseRecord.contraction?.isometric && exerciseRecord.contraction.isometric.minSeconds >= 0">
        <v-chip>
          <v-icon icon="mdi-motion-pause"/>&nbsp;
          {{ exerciseRecord.contraction.isometric.minSeconds }}&nbsp;
          <div v-if="exerciseRecord.contraction.isometric.maxSeconds">
            - {{ exerciseRecord.contraction.isometric.maxSeconds }}
          </div>
        </v-chip>
      </div>
      <div class="recordField" v-if="exerciseRecord.myoRepMatch">
        <v-chip>
          myo-match
        </v-chip>
      </div>
    </div>

    <v-dialog
        v-model="showDeleteExerciseRecordDialog"
        max-width="250px"
    >
      <div class="">
        <v-card>
          <v-card-text>
            Delete this record?
          </v-card-text>
          <v-card-actions class="d-flex justify-space-between">
            <div class="pl-2 pb-2">
              <v-btn color="teal-darken-1" variant="flat" @click="emitDeleteExerciseRecordRequested">Yes</v-btn>
            </div>
            <div class="pr-2 pb-2">
              <v-btn variant="text" @click="showDeleteExerciseRecordDialog = false">No</v-btn>
            </div>
          </v-card-actions>
        </v-card>
      </div>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import {PropType} from "@vue/runtime-core";
import {ExerciseRecord} from "~/domain/domain";
import {useMainStore} from "~/store";

const emit = defineEmits<{(e: 'deleteRequested'): void}>()

const props = defineProps({
  exerciseRecord: {
    type: Object as PropType<ExerciseRecord>,
    required: true
  }
})

const exercise = useMainStore().getExercise(props.exerciseRecord.exerciseId)
const showDeleteExerciseRecordDialog = ref(false)

function emitDeleteExerciseRecordRequested() {
  emit('deleteRequested')
  showDeleteExerciseRecordDialog.value = false
}
</script>

<style scoped>
.recordField {
  margin: 0.25rem;
}
</style>