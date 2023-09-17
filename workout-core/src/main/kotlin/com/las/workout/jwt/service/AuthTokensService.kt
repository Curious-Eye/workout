package com.las.workout.jwt.service

import com.las.core.ext.errorIfEmpty
import com.las.workout.exception.UnauthorizedException
import com.las.workout.jwt.data.entity.AuthTokensEntity
import com.las.workout.jwt.data.entity.RefreshTokenEntity
import com.las.workout.jwt.data.repository.AuthTokensRepository
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

    private val accessTokenValidDurationSeconds = 60L * 60
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
            val accessTokenExpireAt = Date.from(date.toInstant().plusSeconds(accessTokenValidDurationSeconds))
            val refreshTokenExpireAt = Date.from(date.toInstant().plusSeconds(refreshTokenValidDurationSeconds))
            val id = ObjectId().toString()

            val accessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(
                    JwtClaimsSet.builder()
                        .id(id)
                        .subject(userId)
                        .issuer("las.workout")
                        .issuedAt(date.toInstant())
                        .expiresAt(accessTokenExpireAt.toInstant())
                        .build()
                )
            ).tokenValue

            val refreshToken = if (createRefreshToken) refreshTokenGenerator.get() else null

            authTokensRepository.save(
                AuthTokensEntity(
                    id = id,
                    userId = userId,
                    accessToken = accessToken,
                    refreshToken = refreshToken?.let {
                        RefreshTokenEntity(
                            token = refreshToken,
                            expirationDate = refreshTokenExpireAt,
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
    fun refreshTokens(accessTokenId: String, refreshToken: String): Mono<AuthTokensEntity> {
        log.debug("Refresh tokens with accessTokenId={}", accessTokenId)

        return authTokensRepository.findById(accessTokenId)
            .filter {
                it.refreshToken!!.token == refreshToken &&
                !it.refreshToken.used &&
                it.refreshToken.expirationDate.after(Date())
            }
            .errorIfEmpty(UnauthorizedException("Invalid refresh token"))
            .flatMap {
                it.refreshToken!!.used = true
                authTokensRepository.save(it)
            }
            .flatMap {
                createTokens(userId = it.userId, true)
            }
    }

    fun parseAccessToken(accessToken: String): Mono<AccessTokenParsed> {
        return jwtDecoder.decode(accessToken)
            .map {
                AccessTokenParsed(
                    id = it.id,
                    user = UserFromToken(id = it.subject)
                )
            }
            .doOnError {
                log.warn("Error parsing access token: {}", it.message)
            }
    }

    data class AccessTokenParsed(
        val id: String,
        val user: UserFromToken
    )

    data class UserFromToken(
        val id: String,
    )

}
