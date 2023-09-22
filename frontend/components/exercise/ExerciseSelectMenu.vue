<template>
  <div>
    <v-select
        v-model="exercise"
        :items="useMainStore().exercises"
        label="Exercise"
        @update:model-value="emitSelectedExercise"
    >
      <template v-slot:selection="{ item }">
        <div v-if="item.raw.name">
          {{ item.raw.name }}
        </div>
      </template>
      <template v-slot:item="{ props, item }">
        <v-list-item v-bind="props" :title="item.raw.name"/>
      </template>
      <template v-slot:prepend-item>
        <v-list-item>
          <div class="d-flex align-center">
            <div class="pr-5">
              New:
            </div>
            <v-text-field
                class="pr-2"
                v-model="input.name"
                :readonly="loading"
                clearable
                placeholder="name"
                hide-details="auto"
            />
            <v-btn @click="createExercise" variant="text" icon="mdi-plus" />
          </div>
        </v-list-item>

        <v-divider class="mt-2"></v-divider>
      </template>
    </v-select>
  </div>
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";
import {Exercise} from "~/domain/domain";

const emit = defineEmits<{(e: 'update:modelValue', value: string): void}>()

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  }
})

const loading = useState('loading', () => false)
const input = {
  name: ''
}

let exercise = useState('selectedExercise', () => {
  if (!!useMainStore().lastExerciseRecordInput.exerciseId) {
    return useMainStore().getExercise(useMainStore().lastExerciseRecordInput.exerciseId)
  }

  return undefined as unknown as Exercise
})

async function createExercise() {
  if (input.name) {
    await useExerciseApi().createExercise({name: input.name})
    input.name = ''
  }
}

function emitSelectedExercise(e: any) {
  exercise.value = e
  emit('update:modelValue', exercise.value.id)
}

if (exercise.value)
  emitSelectedExercise(exercise.value)
</script>

<style scoped>

</style>