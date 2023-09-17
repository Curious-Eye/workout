import {useUserStore} from "~/store";

export default defineNuxtRouteMiddleware((to, from) => {
    if (!useUserStore().isAuthenticated) {
        return navigateTo('/login?redirectUri=' + encodeURI(to.fullPath))
    }
})