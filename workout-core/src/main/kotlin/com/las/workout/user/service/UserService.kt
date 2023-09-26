package com.las.workout.user.service

import com.las.workout.exception.IllegalArgumentsException
import com.las.workout.exception.UnauthorizedException
import com.las.workout.jwt.service.AuthTokensService
import com.las.workout.user.api.dto.UserAuthenticateRespDto
import com.las.workout.user.api.dto.UserDto
import com.las.workout.user.api.dto.UserRefreshTokensRespDto
import com.las.workout.user.data.entity.UserEntity
import com.las.workout.user.data.repository.UserRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class UserService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var authTokensService: AuthTokensService

    @Transactional
    fun register(username: String, password: String): Mono<UserDto> {
        log.debug("Register new user. Username={}", username)

        if (username.length < 3)
            return Mono.error(IllegalArgumentsException("Username should be at least 3 characters long"))

        if (password.length < 4)
            return Mono.error(IllegalArgumentsException("Password should be at least 4 characters long"))

        return userRepository.save(
            UserEntity(
                id = ObjectId().toString(),
                username = username,
                password = passwordEncoder.encode(password)
            )
        )
            .map { UserDto(it) }
    }

    @Transactional
    fun authenticate(username: String, password: String): Mono<UserAuthenticateRespDto> {
        log.debug("User {} try authenticate", username)

        return userRepository.findByUsername(username)
            .filter { passwordEncoder.matches(password, it.password) }
            .switchIfEmpty(Mono.error(UnauthorizedException("Invalid password or username")))
            .flatMap { authTokensService.createTokens(userId = it.id, createRefreshToken = true) }
            .map {
                UserAuthenticateRespDto(
                    accessToken = it.accessToken,
                    refreshToken = it.refreshToken?.token,
                )
            }
    }

    fun refreshAuthTokens(accessTokenId: String, refreshToken: String): Mono<UserRefreshTokensRespDto> {
        return authTokensService.refreshTokens(accessTokenId = accessTokenId, refreshToken = refreshToken)
            .map {
                UserRefreshTokensRespDto(
                    accessToken = it.accessToken,
                    refreshToken = it.refreshToken!!.token,
                )
            }
    }

    fun findById(id: String): Mono<UserEntity> {
        return userRepository.findById(id)
    }

}