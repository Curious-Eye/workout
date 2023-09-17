package com.las.workout.user

import com.las.workout.BaseTest
import com.las.workout.jwt.service.AuthTokensService
import com.las.workout.test.getAuthed
import com.las.workout.user.api.AUTH_ACCESS_TOKEN_ID_PARAM_NAME
import com.las.workout.user.api.AUTH_REFRESH_TOKEN_PARAM_NAME
import com.las.workout.user.api.dto.UserAuthenticateRqDto
import com.las.workout.user.api.dto.UserAuthenticateRespDto
import com.las.workout.user.api.dto.UserDto
import com.las.workout.user.api.dto.UserRegisterDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveMinLength
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder

class UserTests : BaseTest() {

    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var authTokensService: AuthTokensService

    @Test
    fun `API - User should be able to register`() {
        // GIVEN
        val username = "User 1"
        val password = "pass"

        // WHEN
        val res = webTestClient.post()
            .uri("/api/auth/actions/register")
            .bodyValue(UserRegisterDto(username = username, password = password))
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful
        val body = res.expectBody(UserDto::class.java).returnResult()

        body.responseBody!!.id shouldNotBe null
        body.responseBody!!.username shouldBe "User 1"

        val userEntity = dataHelper.userRepository.findById(body.responseBody!!.id).block()!!
        userEntity.username shouldBe "User 1"
        userEntity.password shouldNotBe "pass"
        passwordEncoder.matches("pass", userEntity.password) shouldBe true
    }

    @Test
    fun `API - User should be able to authenticate`() {
        // GIVEN
        val username = "User 1"
        val password = "pass"

        val user = dataHelper.setupUser(username = username, password = password).block()!!.user

        // WHEN
        val res = webTestClient.post()
            .uri("/api/auth/actions/authenticate")
            .bodyValue(UserAuthenticateRqDto(username = username, password = password))
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(UserAuthenticateRespDto::class.java).returnResult().responseBody!!
        resp.accessToken shouldHaveMinLength 5
        resp.refreshToken shouldHaveMinLength 5

        // WHEN
        val res2 = webTestClient.get()
            .uri("/api/users/me")
            .header("Authorization", "Bearer ${resp.accessToken}")
            .exchange()

        // THEN
        res2.expectStatus().is2xxSuccessful

        res2.expectBody(UserDto::class.java).consumeWith {
            it.responseBody!!.id shouldBe user.id
            it.responseBody!!.username shouldBe "User 1"
        }
    }

    @Test
    fun `API - User should be able to refresh auth tokens`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        val accessToken = userSetup.accessToken
        val refreshToken = userSetup.refreshToken!!
        val accessTokenId = authTokensService.parseAccessToken(accessToken).block()!!.id

        // WHEN
        val res = webTestClient.post()
            .uri {
                it.path("/api/auth/actions/refresh-tokens")
                    .queryParam(AUTH_ACCESS_TOKEN_ID_PARAM_NAME, accessTokenId)
                    .queryParam(AUTH_REFRESH_TOKEN_PARAM_NAME, refreshToken)
                    .build()
            }
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        val resp = res.expectBody(UserAuthenticateRespDto::class.java).returnResult().responseBody!!
        resp.accessToken shouldHaveMinLength 5
        resp.refreshToken shouldHaveMinLength 5

        resp.accessToken shouldHaveMinLength 5
        resp.accessToken shouldNotBe accessToken

        resp.refreshToken shouldHaveMinLength 5
        resp.refreshToken shouldNotBe refreshToken

        // WHEN
        val res2 = webTestClient.getAuthed(resp.accessToken)
            .uri("/api/users/me")
            .exchange()

        // THEN
        res2.expectStatus().is2xxSuccessful

        res2.expectBody(UserDto::class.java).consumeWith {
            it.responseBody!!.id shouldBe "u1"
        }
    }

}