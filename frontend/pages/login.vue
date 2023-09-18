<script setup lang="ts">
import {useUserStore} from "~/store";

const route = useRoute()

const username = useState('username', () => '')
const password = useState('password', () => '')
const form = useState('form', () => false)
const loading = useState('loading', () => false)

const getRedirectUri = function () {
  let redirect = '/'
  if (!!route.query.redirectUri) {
    if (route.query.redirectUri instanceof String)
      redirect = route.query.redirectUri as string
    else {
      const t = route.query.redirectUri[0]
      if (t != null) redirect = t
    }
  }
  return decodeURI(redirect)
}

const onSubmit = async function () {
  if (!form.value) return

  loading.value = true
  await useAuthApi().authenticate(username.value, password.value);
  navigateTo(getRedirectUri())
  loading.value = false
}

const required = function (v: any) {
  return !!v || 'Field is required'
}

if (useUserStore().isAuthenticated)
  navigateTo(getRedirectUri())
</script>

<template>
  <div>
    <v-sheet class="bg-deep-purple pa-12 w-50" rounded>
      <v-card class="mx-auto px-6 py-8">
        <v-form
            v-model="form"
            @submit.prevent="onSubmit"
        >
          <v-text-field
              v-model="username"
              :readonly="loading"
              :rules="[required]"
              class="mb-2"
              clearable
              label="Username"
              placeholder="username"
              variant="solo-inverted"
          ></v-text-field>

          <v-text-field
              v-model="password"
              :readonly="loading"
              :rules="[required]"
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
              color="success"
              size="large"
              type="submit"
              variant="elevated"
          >
            Sign In
          </v-btn>
        </v-form>
      </v-card>
    </v-sheet>
  </div>
</template>

<style scoped>

</style>