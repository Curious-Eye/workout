import {useMainStore} from "~/store";
import {useCookie} from "#app";

export default defineNuxtRouteMiddleware((to, from) => {
    useMainStore().authenticated = !!useCookie('accessToken').value
    if (!useCookie('accessToken')) {
        return navigateTo('/login?redirectUri=' + encodeURI(to.fullPath))
    }
})