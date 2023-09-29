<template>
  <div class="d-flex flex-column w-100" style="height: 100vh">
    <div class="pb-5">
      <v-toolbar color="teal-darken-2" dark fixed app>
        <v-toolbar-title>LAS Workouts</v-toolbar-title>
        <v-btn icon @click="navigateTo('/workouts')" v-if="useMainStore().authenticated">
          <v-icon>mdi-calendar</v-icon>
        </v-btn>
        <v-btn icon @click="navigateTo('/stats')" v-if="useMainStore().authenticated">
          <v-icon>mdi-chart-line</v-icon>
        </v-btn>
        <v-btn icon @click="navigateTo('/logout')" v-if="useMainStore().authenticated">
          <v-icon>mdi-export</v-icon>
        </v-btn>
      </v-toolbar>
    </div>
    <div class="d-flex flex-grow-1 align-center justify-center">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">

import {useMainStore} from "~/store";
</script>
