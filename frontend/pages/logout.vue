<script setup lang="ts">
import {useMainStore} from "~/store";

function eraseCookie(name: string) {
  document.cookie = name+'=; Max-Age=-1';
}

if (process.client) {
  eraseCookie('accessToken')
  eraseCookie('refreshToken')
  const at = useState('accessToken', () => useCookie('accessToken').value)
  const rt = useState('refreshToken', () => useCookie('refreshToken').value)
  at.value = ''
  rt.value = ''
  useMainStore().authenticated = false
  navigateTo('/login')
}
</script>

<template>

</template>

<style scoped>

</style>