package com.las.workout.jwt.service

import com.las.core.ext.errorIfEmpty
import com.las.workout.jwt.data.entity.AuthTokensEntity
import com.las.workout.jwt.data.entity.RefreshTokenEntity
import com.las.workout.jwt.data.repository.AuthTokensRepository
import com.las.workout.jwt.exception.JwtException
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Supplier

@Service
class AuthTokensService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired private lateinit var jwtEncoder: JwtEncoder
    @Autowired private lateinit var jwtDecoder: ReactiveJwtDecoder
    @Autowired private lateinit var authTokensRepository: AuthTokensRepository

    private val refreshTokenValidDurationSeconds = 60L * 60 * 24 * 7

    val refreshTokenGenerator =
        Supplier {
            Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96)
                .generateKey()
        }

    @Transactional
    fun createTokens(userId: String, createRefreshToken: Boolean = false): Mono<AuthTokensEntity> {
        log.debug("Create tokens for user {}. createRefreshToken={}", userId, createRefreshToken)

        return Mono.defer {
            val date = Date()
            val expiresAt = Date.from(date.toInstant().plusSeconds(refreshTokenValidDurationSeconds))
            val id = ObjectId().toString()

            val accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(
                    JwtClaimsSet.builder()
                        .id(id)
                        .subject(userId)
                        .issuer("las.workout")
                        .issuedAt(date.toInstant())
                        .expiresAt(expiresAt.toInstant())
                        .build()
                )
            ).tokenValue

            val refreshToken = if (createRefreshToken) refreshTokenGenerator.get() else null

            authTokensRepository.save(
                AuthTokensEntity(
                    id = id,
                    userId = userId,
                    accessToken = accessToken,
                    refreshToken = if (refreshToken == null) null
                    else {
                        RefreshTokenEntity(
                            token = refreshToken,
                            expirationDate = expiresAt,
                            issuedAt = date
                        )
                    },
                )
            )
        }
            .doOnNext {
                log.debug(
                    "Created new auth tokens for user={}",
                    userId,
                )
            }
    }

    @Transactional
    fun refreshTokens(refreshToken: String): Mono<AuthTokensEntity> {
        log.debug("Refresh tokens with refreshToken={}", refreshToken)

        return authTokensRepository.findByRefreshTokenValueAndUsed(refreshToken, false)
            .errorIfEmpty(JwtException("Invalid refresh token"))
            .flatMap {
                it.refreshToken!!.used = true
                authTokensRepository.save(it)
            }
            .flatMap {
                createTokens(userId = it.userId, true)
            }
    }

    fun parseAccessToken(accessToken: String): Mono<UserFromToken> {
        return jwtDecoder.decode(accessToken)
            .map { UserFromToken(id = it.subject) }
    }

    data class UserFromToken(
        val id: String,
    )

}
