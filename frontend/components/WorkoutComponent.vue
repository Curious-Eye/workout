<template>
  <div>
    <v-card
        color="teal"
        variant="flat"
    >
      <template v-slot:title>
        <div class="d-flex justify-space-between pb-0">
          <div>
            {{ getWorkoutMesocycle() }}
          </div>
          <div>
            <v-btn
                class="mb-5"
                icon="mdi-file-remove-outline"
                variant="text"
                density="compact"
                @click="showDeleteWorkoutDialog = true"
            />
          </div>
        </div>
      </template>

      <template v-slot:subtitle>
        <div class="d-flex align-center">
          <div class="pr-2">
            <v-btn icon="mdi-pound" variant="plain" size="small" density="compact" @click="showAddTagDialog = true"/>
          </div>
          <div class="d-flex">
            <div v-for="(tag, index) in workout.tags">
              <div class="pr-2 italic">
                <v-chip label closable @click:close="removeTagAt(index)">
                  {{tag}}
                </v-chip>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-slot:text>
        <div class="pb-2">
          Exercises:
        </div>
        <v-divider class="pb-2"/>
        <div class="pb-2">
          <draggable
              :list="workout.exercises"
              @update="onRecordDragged"
              dragClass=""
              :item-key="el => el">
            <template #item="{index, element}">
              <div class="bg-teal">
                <div class="pb-2">
                  <ExerciseRecordComponent :exercise-record="element" @deleteRequested="deleteRecordedExercise(index)"/>
                </div>
                <v-divider/>
              </div>
            </template>
          </draggable>
        </div>

        <div class="d-flex justify-space-between">
          <div class="">
            <v-btn density="compact" size="medium" icon="mdi-plus" variant="text"
                   @click="showRecordExerciseDialog = true"/>
          </div>
          <div class="text-teal-lighten-3">
            {{ utilsMain.getWorkoutDisplayDate(workout) }}
          </div>
        </div>
      </template>

      <v-dialog
          v-model="showDeleteWorkoutDialog"
          max-width="250px"
      >
        <v-card>
          <v-card-text>
            Delete this workout?
          </v-card-text>
          <v-card-actions class="d-flex justify-space-between">
            <div class="pl-2 pb-2">
              <v-btn color="teal-darken-1" variant="flat" @click="deleteWorkout">Yes</v-btn>
            </div>
            <div class="pr-2 pb-2">
              <v-btn variant="text" @click="showDeleteWorkoutDialog = false">No</v-btn>
            </div>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <v-dialog
          v-model="showRecordExerciseDialog"
          width="500"
      >
        <v-card>
          <v-card-text>
            <div class="d-flex flex-column">
              <ExerciseSelectMenu v-model="exerciseInput.exerciseId"></ExerciseSelectMenu>
              <div class="d-flex">
                <v-text-field
                    v-model="exerciseInput.repetitions"
                    label="Reps:"
                    type="number"
                />
                <v-text-field
                    class="ml-5"
                    v-model="exerciseInput.weight.kg"
                    label="Weight(kg):"
                    type="number"
                    :disabled="!!exerciseInput.weight.bodyWeight"
                />
                <v-checkbox
                    style="min-width: 20%"
                    v-model="exerciseInput.weight.bodyWeight"
                    label="Bw"
                />
              </div>
              <div class="d-flex align-center">
                Rir:
                <v-text-field
                    class="ml-5"
                    :model-value="exerciseInput.rir?.min"
                    label="min:"
                    type="number"
                    :disabled="!!exerciseInput.warmup"
                    @update:model-value="setExerciseInputRirMin"
                />
                <v-text-field
                    class="ml-5"
                    :model-value="exerciseInput.rir?.max"
                    label="max:"
                    type="number"
                    :disabled="!!exerciseInput.warmup"
                    @update:model-value="setExerciseInputRirMax"
                />
              </div>
              <div class="d-flex align-center">
                <div class="flex-none overflow-hidden">
                  Eccentric (s):
                </div>
                <v-text-field
                    class="ml-5"
                    :model-value="exerciseInput.contraction?.eccentric?.minSeconds"
                    label="min:"
                    type="number"
                    @update:model-value="setExerciseInputEccentricMin"
                />
                <v-text-field
                    class="ml-5"
                    :model-value="exerciseInput.contraction?.eccentric?.maxSeconds"
                    label="max:"
                    type="number"
                    @update:model-value="setExerciseInputEccentricMax"
                />
              </div>
              <div class="d-flex align-center">
                <div class="flex-none overflow-hidden">
                  Isometric (s):
                </div>
                <v-text-field
                    class="ml-5"
                    :model-value="exerciseInput.contraction?.isometric?.minSeconds"
                    label="min:"
                    type="number"
                    @update:model-value="setExerciseInputIsometricMin"
                />
                <v-text-field
                    class="ml-5"
                    :model-value="exerciseInput.contraction?.isometric?.maxSeconds"
                    label="max:"
                    type="number"
                    @update:model-value="setExerciseInputIsometricMax"
                />
              </div>
              <div class="d-flex align-center">
                <div class="flex-none overflow-hidden">
                  Elevation (cm):
                </div>
                <v-text-field
                    class="ml-5"
                    :model-value="exerciseInput.elevation?.cm"
                    label="centimeters:"
                    type="number"
                    @update:model-value="setElevation"
                />
              </div>
              <div class="d-flex align-center mb-5">
                <v-checkbox-btn
                    style="min-width: 20%"
                    v-model="exerciseInput.warmup"
                    label="Warmup"
                />
                <v-checkbox-btn
                    style="min-width: 20%"
                    v-model="exerciseInput.myoRepMatch"
                    label="Myo rep match"
                />
                <v-btn style="max-width: 20%" color="teal-darken-1" @click="recordExercise">Add</v-btn>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-dialog>

      <v-dialog
          v-model="showAddTagDialog"
          max-width="250px"
      >
        <v-card>
          <v-card-text>
            <v-text-field
                v-model="tagInput"
                :rules="[(value => !!value)]"
                class="mb-2"
                clearable
                label="Tag"
                placeholder="Squats"
                variant="solo"
            ></v-text-field>
          </v-card-text>
          <v-card-actions class="d-flex justify-space-between">
            <div class="pl-2 pb-2">
              <v-btn color="teal-darken-1" variant="flat" @click="addTagToWorkout">Add</v-btn>
            </div>
            <div class="pr-2 pb-2">
              <v-btn variant="text" @click="showAddTagDialog = false">Cancel</v-btn>
            </div>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import {useMainStore} from "~/store";
import {ExerciseRecord, Workout} from "~/domain/domain";
import {PropType} from "@vue/runtime-core";
import utilsMain from "~/services/utilsMain";
import draggable from 'vuedraggable'
import {useWorkoutApi} from "~/composables/useWorkoutApi";

const emit = defineEmits<{ (e: 'apiError', value: string): void,  (e: 'loadingStart'): void, (e: 'loadingEnd'): void}>()

const props = defineProps({
  workout: {
    type: Object as PropType<Workout>,
    required: true
  }
})

const exerciseInput = useState('exerciseInput', () => {
  if (!!useMainStore().lastExerciseRecordInput.exerciseId)
    return useMainStore().lastExerciseRecordInput
  else
    return ({
      exerciseId: '',
      repetitions: 0,
      weight: {},
    }) as ExerciseRecord
})
const showRecordExerciseDialog = ref(false)
const showDeleteWorkoutDialog = ref(false)
const showAddTagDialog = ref(false)

const tagInput = ref('')

function getWorkoutMesocycle() {
  return props.workout.mesocycle ? props.workout.mesocycle : ''
}

async function recordExercise() {
  const val = exerciseInput.value
  if (val.exerciseId && val.repetitions > 0 && val.weight) {
    if (val.weight.bodyWeight)
      val.weight.kg = undefined

    if (val.warmup)
      val.rir = undefined

    if (val.rir?.max && val.rir?.max <= 0)
      val.rir.max = undefined

    if (val.rir?.min && val.rir?.min < 0)
      val.rir = undefined

    if (val.contraction?.eccentric && val.contraction.eccentric.minSeconds <= 0)
      val.contraction.eccentric = undefined

    if (val.contraction?.eccentric?.maxSeconds && val.contraction.eccentric.maxSeconds == 0)
      val.contraction.eccentric.maxSeconds = undefined

    if (val.contraction?.isometric && val.contraction.isometric.minSeconds < 0)
      val.contraction.isometric = undefined

    if (val.contraction?.isometric && val.contraction.isometric.minSeconds == 0 && !val.contraction?.isometric.maxSeconds)
      val.contraction.isometric = undefined

    if (val.contraction?.isometric?.maxSeconds && val.contraction.isometric.maxSeconds == 0)
      val.contraction.isometric.maxSeconds = undefined

    if (val.elevation?.cm && val.elevation?.cm <= 0)
      val.elevation = undefined

    const {error} = await useWorkoutApi().recordExercise(props.workout.id, val)

    if (error)
      emit('apiError', error.body.msg)

    showRecordExerciseDialog.value = false
  }
  exerciseInput.value = val
}

function setExerciseInputRirMin(min: number) {
  if (!exerciseInput.value.rir)
    exerciseInput.value.rir = {min}
  else
    exerciseInput.value.rir.min = min
}

function setExerciseInputRirMax(max: number) {
  if (!exerciseInput.value.rir)
    exerciseInput.value.rir = {min: 0, max}
  else
    exerciseInput.value.rir.max = max

  if (!exerciseInput.value.rir.min && exerciseInput.value.rir.min !== 0)
    exerciseInput.value.rir.min = 0
}

function setExerciseInputEccentricMin(minSeconds: number) {
  if (!exerciseInput.value.contraction)
    exerciseInput.value.contraction = {eccentric: {minSeconds}}
  else {
    if (exerciseInput.value.contraction.eccentric)
      exerciseInput.value.contraction.eccentric.minSeconds = minSeconds
    else
      exerciseInput.value.contraction.eccentric = {minSeconds}
  }
}

function setExerciseInputEccentricMax(maxSeconds: number) {
  if (!exerciseInput.value.contraction)
    exerciseInput.value.contraction = {eccentric: {minSeconds: 0, maxSeconds}}
  else {
    if (exerciseInput.value.contraction.eccentric)
      exerciseInput.value.contraction.eccentric.maxSeconds = maxSeconds
    else
      exerciseInput.value.contraction.eccentric = {minSeconds: 0, maxSeconds}
  }

  if (exerciseInput.value.contraction.eccentric && !exerciseInput.value.contraction.eccentric.minSeconds)
    exerciseInput.value.contraction.eccentric.minSeconds = 0
}

function setExerciseInputIsometricMin(minSeconds: number) {
  if (!exerciseInput.value.contraction)
    exerciseInput.value.contraction = {isometric: {minSeconds}}
  else {
    if (exerciseInput.value.contraction.isometric)
      exerciseInput.value.contraction.isometric.minSeconds = minSeconds
    else
      exerciseInput.value.contraction.isometric = {minSeconds}
  }
}

function setExerciseInputIsometricMax(maxSeconds: number) {
  if (!exerciseInput.value.contraction)
    exerciseInput.value.contraction = {isometric: {minSeconds: 0, maxSeconds}}
  else {
    if (exerciseInput.value.contraction.isometric)
      exerciseInput.value.contraction.isometric.maxSeconds = maxSeconds
    else
      exerciseInput.value.contraction.isometric = {minSeconds: 0, maxSeconds}
  }

  if (exerciseInput.value.contraction.isometric && !exerciseInput.value.contraction.isometric.minSeconds)
    exerciseInput.value.contraction.isometric.minSeconds = 0
}

function setElevation(elevation: number) {
  if (!elevation)
    exerciseInput.value.elevation = undefined
  else {
    if (!exerciseInput.value.elevation) exerciseInput.value.elevation = {cm: elevation}
    else exerciseInput.value.elevation.cm = elevation
  }
}

async function deleteWorkout() {
  showDeleteWorkoutDialog.value = false
  const {error} = await useWorkoutApi().deleteWorkout(props.workout.id)

  if (error)
    emit('apiError', error.body.msg)
}

async function deleteRecordedExercise(recordInd: number) {
  const {error} = await useWorkoutApi().deleteRecordedExercise(props.workout?.id, recordInd)

  if (error)
    emit('apiError', error.body.msg)
}

function onRecordDragged(evt: any) {
  emit('loadingStart')
  useWorkoutApi()
      .moveRecordedExercise(props.workout?.id, evt.oldIndex, evt.newIndex)
      .then(value => {
        emit('loadingEnd')
        if (value.error)
          emit('apiError', value.error.body.msg)
      })
}

async function addTagToWorkout() {
  if (tagInput.value) {
    showAddTagDialog.value = false
    tagInput.value = utilsMain.capitalizeFirstCharacter(tagInput.value)

    emit('loadingStart')
    const {error} = await useWorkoutApi().addTag(tagInput.value, props.workout.id)
    emit('loadingEnd')

    if (error)
      emit('apiError', error.body.msg)
    else
      tagInput.value = ''
  }
}


async function removeTagAt(index: number) {
  emit('loadingStart')
  const {error} = await useWorkoutApi().removeTag(props.workout.id, index)
  emit('loadingEnd')
  if (error)
    emit('apiError', error.body.msg)
}
</script>

<style scoped>
</style>