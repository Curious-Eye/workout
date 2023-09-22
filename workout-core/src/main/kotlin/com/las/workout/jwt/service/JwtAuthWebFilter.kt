package com.las.workout.jwt.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class JwtAuthWebFilter(
    private val authTokensService: AuthTokensService
) : WebFilter {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        /**
         * Allowing for mobile to pass accessToken via headers
         */
        val bearerTokenHeader =
            exchange.request.headers["Authorization"]?.firstOrNull { it.startsWith("Bearer") }

        var accessToken = bearerTokenHeader?.substringAfter("Bearer ")

        if (accessToken.isNullOrEmpty())
            accessToken = exchange.request.cookies["accessToken"]?.firstOrNull()?.value

        if (accessToken == null)
            return chain.filter(exchange)

        return authTokensService.parseAccessToken(accessToken)
            .onErrorResume {
                chain.filter(exchange).then(Mono.empty())
            }
            .flatMap {
                val userId = it.user.id

                val user = User(
                    userId,
                    accessToken,
                    listOf(SimpleGrantedAuthority("USER"))
                )

                chain.filter(exchange)
                    .contextWrite(
                        ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(
                                SecurityContextImpl().apply {
                                    authentication = PreAuthenticatedAuthenticationToken(user, accessToken)
                                }
                            )
                        )
                    )
            }
    }

}