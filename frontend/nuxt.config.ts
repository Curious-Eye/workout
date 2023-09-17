// https://nuxt.com/docs/api/configuration/nuxt-config
// How to use Vuetify https://codybontecou.com/how-to-use-vuetify-with-nuxt-3.html
export default defineNuxtConfig({
  css: [
    'vuetify/lib/styles/main.sass',
    '@mdi/font/css/materialdesignicons.min.css'
  ],
  build: {
    transpile: ['vuetify'],
  },
  devtools: { enabled: true },
  modules: [
    '@pinia/nuxt',
    '@pinia-plugin-persistedstate/nuxt'
  ],
  runtimeConfig: {
    // The private keys which are only available within server-side
    // apiSecret: '',
    // Keys within public, will be also exposed to the client-side
    public: {
      serverHost: ''
    }
  }
})
