import axios from "axios";
import {useUserStore} from "~/store";

export default {
    /**
     *
     * @param path {string}
     * @param silentReAuthenticate {boolean}
     * @return {Promise<any>}
     */
    async getAuthed(
        path,
        silentReAuthenticate = true
    ) {
        const res = await this.getAuthedSimple(
            path,
            useRuntimeConfig().public.serverHost,
            useUserStore().user.accessToken,
            useUserStore().user.refreshToken,
            silentReAuthenticate
        )

        if (!!res.newTokens)
            useUserStore().setUserTokens(res.newTokens.accessToken, res.newTokens.refreshToken)

        return res.data
    },
    /**
     *
     * @param path {string}
     * @param body {any}
     * @param silentReAuthenticate {boolean}
     * @return {Promise<any>}
     */
    async postAuthed(
        path,
        body,
        silentReAuthenticate = true
    ) {
        const res = await this.postAuthedSimple(
            path,
            body,
            useRuntimeConfig().public.serverHost,
            useUserStore().user.accessToken,
            useUserStore().user.refreshToken,
            silentReAuthenticate
        )

        if (!!res.newTokens)
            useUserStore().setUserTokens(res.newTokens.accessToken, res.newTokens.refreshToken)

        return res.data
    },
    /**
     *
     * @param path {string}
     * @param body {any}
     * @param baseUri {string}
     * @param accessToken {string}
     * @param refreshToken {string}
     * @param silentReAuthenticate {boolean}
     * @return {Promise<{data: any, newTokens?: { refreshToken: string, accessToken: string }}>}
     */
    async postAuthedSimple(
        path,
        body,
        baseUri,
        accessToken,
        refreshToken,
        silentReAuthenticate = true
    ) {
        let headers = {
            Authorization: 'Bearer ' + accessToken
        }
        if (!!!accessToken)
            headers = {}

        const res = await axios.post(path, body, {
            headers,
            baseURL: baseUri
        })


        if (res.status !== 200) {
            console.log('Err on postAuthed:')
            console.log(res)
            if (res.status === 401 && silentReAuthenticate && !!refreshToken) {
                const {newAccessToken, newRefreshToken} = await refreshTokens(accessToken, refreshToken)

                const res2 = await axios.post(path, body, {
                    headers: {
                        Authorization: 'Bearer ' + newAccessToken
                    },
                    baseURL: baseUri
                })

                if (res2.status !== 200)
                    throw new Error('Unauthorized')

                return {
                    data: res2.data,
                    newTokens: {
                        accessToken: newAccessToken,
                        refreshToken: newRefreshToken
                    }
                }
            } else {
                throw new Error(res.statusText)
            }
        }

        return {
            data: res.data
        }
    },
    /**
     *
     * @param path {string}
     * @param baseUri {string}
     * @param accessToken {string}
     * @param refreshToken {string}
     * @param silentReAuthenticate {boolean}
     * @return {Promise<{data: any, newTokens?: { refreshToken: string, accessToken: string }}>}
     */
    async getAuthedSimple(
        path,
        baseUri,
        accessToken,
        refreshToken,
        silentReAuthenticate = true
    ) {
        let headers = {
            Authorization: 'Bearer ' + accessToken
        }
        if (!!!accessToken)
            headers = {}

        const res = await axios.get(path, {
            headers,
            baseURL: baseUri
        })

        if (res.status !== 200) {
            console.log('Err on postAuthed:')
            console.log(res)
            if (res.status === 401 && silentReAuthenticate && !!refreshToken) {
                const {newAccessToken, newRefreshToken} = await refreshTokens(accessToken, refreshToken)

                const res2 = await axios.get(path, {
                    headers: {
                        Authorization: 'Bearer ' + newAccessToken
                    },
                    baseURL: baseUri
                })

                if (res2.status !== 200)
                    throw new Error('Unauthorized')

                return {
                    data: res2.data,
                    newTokens: {
                        accessToken: newAccessToken,
                        refreshToken: newRefreshToken
                    }
                }
            } else {
                throw new Error(res.statusText)
            }
        }

        return {
            data: res.data
        }
    }
}

/**
 *
 * @param accessToken {string}
 * @param refreshToken {string}
 * @return {Promise<{refreshToken: string, accessToken: string}>}
 */
async function refreshTokens(accessToken, refreshToken) {
    const refreshTokens = await axios.post('/api/auth/actions/refresh-tokens', {
        refreshToken,
        accessTokenId: getAccessTokenId(accessToken)
    })

    if (refreshTokens.status !== 200)
        throw new Error('Unauthorized')

    return {
        accessToken: refreshTokens.data.accessToken,
        refreshToken: refreshTokens.data.refreshToken
    }
}

/**
 * @param accessToken {string}
 * @return string
 */
function getAccessTokenId(accessToken) {
    const tokens = accessToken.split('.')
    const claims = JSON.parse(atob(tokens[1]))
    return claims['jti']
}