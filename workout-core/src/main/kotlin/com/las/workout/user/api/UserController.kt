package com.las.workout.user.api

import com.las.workout.user.api.dto.*
import com.las.workout.user.service.UserService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

const val AUTH_ACCESS_TOKEN_ID_PARAM_NAME = "accessTokenId"
const val AUTH_REFRESH_TOKEN_PARAM_NAME = "refreshToken"

@RestController
class UserController {

    @Autowired private lateinit var userService: UserService

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/users/me")
    fun getMe(@Parameter(hidden = true) @AuthenticationPrincipal user: User): Mono<UserDto> {
        return userService.findById(user.username)
            .map { UserDto(it) }
    }

    @PostMapping("/api/auth/actions/register")
    fun registerUser(@RequestBody rq: UserRegisterDto): Mono<UserDto> =
        userService.register(username = rq.username, password = rq.password)

    @PostMapping("/api/auth/actions/authenticate")
    fun authenticate(
        @RequestBody rq: UserAuthenticateRqDto,
        @Parameter(hidden = true) exchange: ServerWebExchange
    ): Mono<UserAuthenticateRespDto> {
        return userService.authenticate(username = rq.username, password = rq.password)
    }

    @PostMapping("/api/auth/actions/refresh-tokens")
    fun refreshTokens(
        @RequestBody rq: UserRefreshTokensRqDto,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): Mono<UserRefreshTokensRespDto> {
        return userService.refreshAuthTokens(accessTokenId = rq.accessTokenId, refreshToken = rq.refreshToken)
    }

}