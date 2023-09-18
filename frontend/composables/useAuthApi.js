import {useUserStore} from "~/store";
import BaseRequestService from "~/services/baseRequestService";

export const useAuthApi = () => {
    const serverHost = useRuntimeConfig().public.serverHost
    const at = useUserStore().user.accessToken
    const rt = useUserStore().user.refreshToken

    return {
        /**
         *
         * @param username {string}
         * @param password {string}
         * @return {Promise<{accessToken: string, refreshToken: string}>}
         */
        async authenticate(username, password) {
            console.log('Try authenticate')
            const {data, error} =
                await BaseRequestService.postAuthed(
                    '/api/auth/actions/authenticate',
                    {username, password},
                    serverHost, '', '', false
                )

            if (error) {
                console.log('Error from authenticate: ')
                console.log(JSON.stringify(error))
                throw new Error(error)
            }

            useUserStore().setUserTokens(data.accessToken, data.refreshToken)

            return data
        }
    }
}