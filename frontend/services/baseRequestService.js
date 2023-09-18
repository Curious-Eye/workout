export default {
    /**
     *
     * @param path {string}
     * @param baseUri {string}
     * @param accessToken {string}
     * @param refreshToken {string}
     * @param silentReAuthenticate {boolean}
     * @return {Promise<{data: *}|{data: *, newTokens: { refreshToken: string, accessToken: string }}
     * |{error:any}|{error: any, newTokens: { refreshToken: string, accessToken: string }}>}
     */
    async getAuthed(
        path,
        baseUri,
        accessToken,
        refreshToken,
        silentReAuthenticate = true
    ) {
        return await makeRequest(
            path,
            null,
            baseUri,
            accessToken,
            refreshToken,
            silentReAuthenticate
        )
    },
    /**
     *
     * @param path {string}
     * @param body any
     * @param baseUri {string}
     * @param accessToken {string}
     * @param refreshToken {string}
     * @param silentReAuthenticate {boolean}
     * @return {Promise<{data: *}|{data: *, newTokens: { refreshToken: string, accessToken: string }}
     * |{error:any}|{error: any, newTokens: { refreshToken: string, accessToken: string }}>}
     */
    async postAuthed(
        path,
        body,
        baseUri,
        accessToken,
        refreshToken,
        silentReAuthenticate = true
    ) {
        return await makeRequest(
            path,
            body,
            baseUri,
            accessToken,
            refreshToken,
            silentReAuthenticate
        )
    }
}

/**
 *
 * @param path {string}
 * @param baseUri {string}
 * @param body any
 * @param accessToken {string}
 * @param refreshToken {string}
 * @param silentReAuthenticate {boolean}
 * @return {Promise<{data: *}|{data: *, newTokens: { refreshToken: string, accessToken: string }}
 * |{error:any}|{error: any, newTokens: { refreshToken: string, accessToken: string }}>}
 */
async function makeRequest(
    path,
    body,
    baseUri,
    accessToken,
    refreshToken,
    silentReAuthenticate = true
) {
    let headers = {Authorization: 'Bearer ' + accessToken}

    if (!!!accessToken)
        headers = {}

    const opts = {headers}

    if (!!body) {
        opts.body = body
        opts.method = 'post'
    }

    const {data, error} = await fetchWrapped(baseUri + path, opts)

    if (!!error) {
        if (error.response.status === 401 && silentReAuthenticate && !!refreshToken) {
            const {newAccessToken, newRefreshToken, error2} = await refreshTokens(baseUri, accessToken, refreshToken)

            if (!!error2)
                return {error: error2}

            headers.Authorization = 'Bearer ' + newAccessToken
            opts.headers = headers

            const {data: data2, error: error3} = await fetchWrapped(baseUri + path, opts)

            if (!!error3)
                return {
                    error: error3,
                    newTokens: {
                        accessToken: newAccessToken,
                        refreshToken: newRefreshToken
                    }
                }

            return {
                data: data2,
                newTokens: {
                    accessToken: newAccessToken,
                    refreshToken: newRefreshToken
                }
            }
        } else {
            return {error}
        }
    }

    return {data}
}

/**
 *
 * @param baseUri {string}
 * @param accessToken {string}
 * @param refreshToken {string}
 * @return {Promise<{accessToken: string, refreshToken: string}|{error: *}>}
 */
async function refreshTokens(baseUri, accessToken, refreshToken) {
    console.log('Try refresh tokens')
    const accessTokenId = getAccessTokenId(accessToken)

    const opts = {
        headers: {Authorization: 'Bearer ' + accessToken},
        body: {refreshToken, accessTokenId},
        method: 'post'
    }

    const {data, error} = await fetchWrapped(baseUri + '/api/auth/actions/refresh-tokens', opts)

    if (error.response.status !== 200)
        return {error}

    return {
        accessToken: data.accessToken,
        refreshToken: data.refreshToken
    }
}

/**
 *
 * @param path {string}
 * @param opts {any}
 * @return {Promise<{data: *}|{error: *}>}
 */
async function fetchWrapped(path, opts) {
    return await $fetch(path, opts)
        .then(data => ({data}))
        .catch(error => ({error}))
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