import BaseRequestService from "~/services/baseRequestService";

// 10 days
const authCookieMaxAge = 864000

/**
 *
 * @param app {NuxtApp}
 */
export const useApi = (app) => {
    const serverHost = process.server ? useRuntimeConfig().public.serverHost : ''
    const at = useState('accessToken', () => useCookie('accessToken').value)
    const rt = useState('refreshToken', () => useCookie('refreshToken').value)
    if (!!app)
        app = useNuxtApp()

    const setCookies = (tokens) => {
        at.value = tokens.accessToken
        rt.value = tokens.refreshToken
        if (process.server) {
            app.ssrContext.event.node.res.appendHeader(
                'set-cookie',
                `accessToken=${tokens.accessToken}; Max-Age=${authCookieMaxAge}`
            )
            app.ssrContext.event.node.res.appendHeader(
                'set-cookie',
                `refreshToken=${tokens.refreshToken}; Max-Age=${authCookieMaxAge}`
            )
        } else {
            document.cookie = `accessToken=${tokens.accessToken}; Max-Age=${authCookieMaxAge}`
            document.cookie = `refreshToken=${tokens.refreshToken}; Max-Age=${authCookieMaxAge}`
        }
    }

    return {
        /**
         *
         * @param path {string}
         * @param silentReAuthenticate {boolean}
         * @param redirectToLoginOnAuthFail {boolean}
         * @param body {any}
         * @return {Promise<{data: *} | {error: *}>}
         */
        async postAuthed(path, body, silentReAuthenticate = true, redirectToLoginOnAuthFail = true) {
            const {data, error, newTokens} =
                await BaseRequestService.postAuthed(path, body, serverHost, at.value, rt.value, silentReAuthenticate)

            if (!!newTokens)
                setCookies(newTokens)

            if (error) {
                console.log(`Error from useApi::postAuthed(${path},${body}): `)
                console.log(error)

                if (error.status === 401 && redirectToLoginOnAuthFail)
                    await app.runWithContext(() => navigateTo('/login'))

                if (error.status === 502)
                    await app.runWithContext(() => navigateTo('/server-offline'))

                return {error}
            }

            return {data}
        },
        /**
         *
         * @param path {string}
         * @param body {any}
         * @return {Promise<any>}
         */
        async postAnonymous(path, body) {
            const {data, error, newTokens} =
                await BaseRequestService.postAuthed(path, body, serverHost, '', '')

            if (error) {
                console.log(`Error from useApi::postAnonymous(${path},${body}): `)
                console.log(error)

                if (error.status === 502)
                    await app.runWithContext(() => navigateTo('/server-offline'))

                return {error}
            }

            return {data}
        },
        /**
         *
         * @param path {string}
         * @param silentReAuthenticate {boolean}
         * @param redirectToLoginOnAuthFail {boolean}
         * @return {Promise<{data: *} | {error: *}>}
         */
        async getAuthed(path, silentReAuthenticate = true, redirectToLoginOnAuthFail = true) {
            const {data, error, newTokens} =
                await BaseRequestService.getAuthed(path, serverHost, at.value, rt.value, silentReAuthenticate)

            if (!!newTokens)
                setCookies(newTokens)

            if (error) {
                console.log(`Error from useApi::getAuthed(${path}): `)
                console.log(error)

                if (error.status === 401 && redirectToLoginOnAuthFail)
                    await app.runWithContext(() => navigateTo('/login'))

                if (error.status === 502)
                    await app.runWithContext(() => navigateTo('/server-offline'))

                return {error}
            }

            return {data}
        },
        /**
         *
         * @param path {string}
         * @param silentReAuthenticate {boolean}
         * @param redirectToLoginOnAuthFail {boolean}
         * @return {Promise<{data: *} | {error: *}>}
         */
        async deleteAuthed(path, silentReAuthenticate = true, redirectToLoginOnAuthFail = true) {
            const {data, error, newTokens} =
                await BaseRequestService.deleteAuthed(path, serverHost, at.value, rt.value, silentReAuthenticate)

            if (!!newTokens)
                setCookies(newTokens)

            if (error) {
                console.log(`Error from useApi::deleteAuthed(${path}): `)
                console.log(error)

                if (error.status === 401 && redirectToLoginOnAuthFail)
                    await app.runWithContext(() => navigateTo('/login'))

                if (error.status === 502)
                    await app.runWithContext(() => navigateTo('/server-offline'))

                return {error}
            }

            return {data}
        }
    }
}