<template>
  <div>
    <v-sheet class="bg-teal pa-6" rounded>
      <v-card class="mx-auto px-6 py-8">
        <div class="d-flex flex-column" style="min-width: 300px">
          <v-form
              v-model="form"
              @submit.prevent="onSubmit"
          >
            <v-text-field
                v-model="username"
                :readonly="loading"
                :rules="usernameRules"
                class="mb-2"
                clearable
                label="Username"
                placeholder="username"
                variant="solo-inverted"
            ></v-text-field>

            <v-text-field
                v-model="password"
                :readonly="loading"
                :rules="passwordRules"
                clearable
                label="Password"
                type="password"
                placeholder="password"
                variant="solo-inverted"
            ></v-text-field>

            <br>

            <v-btn
                :disabled="!form"
                :loading="loading"
                block
                color="teal-darken-1"
                size="large"
                type="submit"
                variant="elevated"
            >
              Sign up
            </v-btn>

            <div class="pt-5 text-red" v-if="errorFromApi">
              {{errorFromApi}}
            </div>
          </v-form>

          <div class="pt-5 d-flex align-self-end align-center">
            Already have an account?
            <v-btn
                class="text-subtitle-1"
                variant="text"
                color="teal-darken-3"
                @click="navigateTo('/login')"
            >
              Sign in
            </v-btn>
          </div>
        </div>
      </v-card>
    </v-sheet>
  </div>
</template>

<script setup lang="ts">
import {useUserApi} from "~/composables/useUserApi";

const route = useRoute()

const username = ref('')
const password = ref('')
const form = ref(false)
const loading = ref(false)
const errorFromApi = ref('')

const usernameRules = [
  (value: any) => {
    if (!value) return 'Username is required'

    if (value.length < 3)
      return 'Username should be at least 3 characters long'

    return true
  }
]

const passwordRules = [
  (value: any) => {
    if (!value) return 'Password is required'

    if (value.length < 4)
      return 'Password should be at least 4 characters long'

    return true
  }
]

const onSubmit = async function () {
  if (!form.value) return

  loading.value = true
  errorFromApi.value = ''
  const {data, error} = await useAuthApi().register(username.value, password.value);

  if (data)
    navigateTo('/login')

  if (error)
    errorFromApi.value = error.body.msg

  loading.value = false
}

const required = function (v: any) {
  return !!v || 'Field is required'
}

const {data, error} = await useAsyncData(() => useUserApi().getMe(false))

if (!error)
  navigateTo(getRedirectUri())
</script>

<style scoped>

</style>