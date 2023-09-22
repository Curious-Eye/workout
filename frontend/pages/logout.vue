<script setup lang="ts">
function eraseCookie(name: string) {
  document.cookie = name+'=; Max-Age=-99999999;';
}

if (process.client) {
  eraseCookie('accessToken')
  eraseCookie('refreshToken')
  navigateTo('/login')
}
</script>

<template>

</template>

<style scoped>

</style>