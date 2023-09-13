package com.las.workout.user.api

import com.las.workout.user.api.dto.AuthenticateRqDto
import com.las.workout.user.api.dto.UserDto
import com.las.workout.user.api.dto.UserRegisterDto
import com.las.workout.user.service.UserService
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

const val AUTH_ACCESS_TOKEN_HEADER_NAME = "accessToken"
const val AUTH_REFRESH_TOKEN_HEADER_NAME = "refreshToken"
const val AUTH_REFRESH_TOKEN_PARAM_NAME = "refresh_token"

@RestController
class UserController {

    @Autowired private lateinit var userService: UserService

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/users/me")
    fun getMe(@Parameter(hidden = true) @AuthenticationPrincipal user: User): Mono<UserDto> {
        return userService.findById(user.username)
            .map { UserDto(it) }
    }

    @PostMapping("/api/auth/actions/registration")
    fun registerUser(@RequestBody rq: UserRegisterDto): Mono<UserDto> =
        userService.register(username = rq.username, password = rq.password)

    @ApiResponse(
        headers = [
            Header(
                name = AUTH_ACCESS_TOKEN_HEADER_NAME,
                description = "If auth is success, then this will have access token"
            ),
            Header(
                name = AUTH_REFRESH_TOKEN_HEADER_NAME,
                description = "If auth is success, then this will have refresh token"
            ),
        ]
    )
    @PostMapping("/api/auth/actions/authenticate")
    fun authenticate(
        @RequestBody rq: AuthenticateRqDto,
        @Parameter(hidden = true) exchange: ServerWebExchange
    ): Mono<Void> {
        return userService.authenticate(username = rq.username, password = rq.password)
            .doOnNext {
                exchange.response.headers.set(AUTH_ACCESS_TOKEN_HEADER_NAME, it.accessToken)
                it.refreshToken?.also { refreshToken ->
                    exchange.response.headers.set(AUTH_REFRESH_TOKEN_HEADER_NAME, refreshToken)
                }
            }
            .then()
    }

    @ApiResponse(
        headers = [
            Header(
                name = AUTH_ACCESS_TOKEN_HEADER_NAME,
                description = "If refresh is success, then this will have new access token"
            ),
            Header(
                name = AUTH_REFRESH_TOKEN_HEADER_NAME,
                description = "If refresh is success, then this will have new refresh token"
            ),
        ]
    )
    @PostMapping("/api/auth/actions/refresh-tokens")
    fun refreshTokens(
        @RequestParam(AUTH_REFRESH_TOKEN_PARAM_NAME) refreshToken: String,
        @Parameter(hidden = true) exchange: ServerWebExchange,
    ): Mono<Void> {
        return userService.refreshAuthTokens(refreshToken)
            .doOnNext {
                exchange.response.headers.set(AUTH_ACCESS_TOKEN_HEADER_NAME, it.accessToken)
                exchange.response.headers.set(AUTH_REFRESH_TOKEN_HEADER_NAME, it.refreshToken)
            }
            .then()
    }

}