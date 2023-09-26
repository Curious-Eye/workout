import {useApi} from "~/composables/useApi";
import {useCookie} from "#app";

export const useAuthApi = () => {
    const app = useNuxtApp()
    const at = useState('accessToken', () => useCookie('accessToken').value)
    const rt = useState('refreshToken', () => useCookie('refreshToken').value)

    return {
        /**
         *
         * @param username {string}
         * @param password {string}
         * @return {Promise<{data?: {accessToken: string, refreshToken: string}, error?: *}>}
         */
        async authenticate(username, password) {
            console.log('Try authenticate')
            const {data, error} =
                await useApi(app).postAnonymous(
                    '/api/auth/actions/authenticate',
                    {username, password}
                )

            if (!!error) {
                console.log('Error from authenticate: ')
                console.log(error)
                return {error}
            }

            document.cookie = `accessToken=${data.accessToken}`
            document.cookie = `refreshToken=${data.refreshToken}`
            at.value = data.accessToken
            rt.value = data.refreshToken

            return {data}
        },
        /**
         *
         * @param username {string}
         * @param password {string}
         * @return {Promise<{data?: {id: string, username: string}, error?: *}>}
         */
        async register(username, password) {
            console.log('Try register')
            const {data, error} =
                await useApi(app).postAnonymous(
                    '/api/auth/actions/register',
                    {username, password}
                )

            if (!!error) {
                console.log('Error from register: ')
                console.log(error)
                return {error}
            }

            return {data}
        }
    }
}