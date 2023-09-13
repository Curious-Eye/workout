package com.las.workout.config

import com.las.workout.jwt.service.AuthTokensService
import com.las.workout.jwt.service.JwtAuthWebFilter
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import reactor.core.publisher.Mono

@Configuration
//@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig {

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        authTokensService: AuthTokensService,
    ): SecurityWebFilterChain? {
        return http
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .exceptionHandling {
                it
                    .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                    .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
            }
            .authorizeExchange { it.anyExchange().permitAll() }
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .addFilterAt(JwtAuthWebFilter(authTokensService), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>): JwtEncoder {
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
        return NimbusJwtDecoder(DefaultJWTProcessor<SecurityContext?>().apply {
            this.jwsKeySelector = JWSVerificationKeySelector(JWSAlgorithm.Family.RSA, jwkSource)
        })
    }

    /**
     * Creating ReactiveJwtDecoder so that ReactiveUserDetailsServiceAutoConfiguration is not loaded
     */
    @Bean
    fun reactiveJwtDecoder(jwtDecoder: JwtDecoder): ReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder {
            Mono.fromCallable {
                JWTClaimsSet.Builder()
                    .apply {
                        jwtDecoder.decode(it.parsedString).claims
                            .forEach { (name, claim) ->
                                this.claim(name, claim)
                            }
                    }
                    .build()
            }
        }
    }

}