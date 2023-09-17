import BaseRequestService from "~/services/baseRequestService";

export default {
    /**
     *
     * @param username {string}
     * @param password {string}
     * @return {Promise<{accessToken: string, refreshToken: string}>}
     */
    async authenticate(username, password) {
        console.log('Try authenticate')
        return await BaseRequestService.postAuthed(
            '/api/auth/actions/authenticate',
            {username, password},
            false
        )
    },
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
