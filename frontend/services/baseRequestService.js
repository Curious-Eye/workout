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
            'get',
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
            'post',
            path,
            body,
            baseUri,
            accessToken,
            refreshToken,
            silentReAuthenticate
        )
    },
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
    async deleteAuthed(
        path,
        baseUri,
        accessToken,
        refreshToken,
        silentReAuthenticate = true
    ) {
        return await makeRequest(
            'delete',
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
    async putAuthed(
        path,
        body,
        baseUri,
        accessToken,
        refreshToken,
        silentReAuthenticate = true
    ) {
        return await makeRequest(
            'put',
            path,
            body,
            baseUri,
            accessToken,
            refreshToken,
            silentReAuthenticate
        )
    },
}

/**
 *
 * @param method {string}
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
    method,
    path,
    body,
    baseUri,
    accessToken,
    refreshToken,
    silentReAuthenticate = true
) {
    let headers = {Cookie: 'accessToken=' + accessToken}

    if (!!!accessToken || process.client)
        headers = {}

    const opts = {headers: headers}

    if (!!body) {
        opts.body = body
    }
    opts.method = method

    const {data, error} = await fetchWrapped(baseUri + path, opts)

    if (!!error) {
        if (error.status === 401 && silentReAuthenticate && !!refreshToken) {
            const {accessToken: newAccessToken, refreshToken: newRefreshToken, error: error2} =
                await refreshTokens(baseUri, accessToken, refreshToken)

            if (!!error2)
                return {error: error2}

            console.log('Tokens refreshed. Try repeat request...')
            if (process.client) {
                document.cookie = `accessToken=${newAccessToken}`
                document.cookie = `refreshToken=${newRefreshToken}`
            }
            headers.Cookie = 'accessToken=' + newAccessToken
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
    console.log('Try refreshTokens')
    const accessTokenId = getAccessTokenId(accessToken)

    const opts = {
        body: {refreshToken, accessTokenId},
        method: 'post'
    }

    const {data, error} = await fetchWrapped(baseUri + '/api/auth/actions/refresh-tokens', opts)

    if (!!error)
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
 * @return {Promise<{data: *}|{error: { status: number }}>}
 */
async function fetchWrapped(path, opts) {
    return await $fetch(path, opts)
        .then(data => ({data}))
        .catch(error => {
            return {error: {status: error.response.status, body: error.data}}
        })
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