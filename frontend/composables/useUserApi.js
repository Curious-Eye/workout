export const useUserApi = () => {
    const app = useNuxtApp()
    return {
        /**
         *
         * @param redirectToLoginOnAuthFail {boolean}
         * @return {Promise<{data?: *, error?: *}>}
         */
        async getMe(redirectToLoginOnAuthFail = true) {
            return await useApi(app).getAuthed('/api/users/me', true, redirectToLoginOnAuthFail)
        }
    }
}