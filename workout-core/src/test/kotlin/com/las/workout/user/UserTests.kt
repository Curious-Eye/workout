package com.las.workout.user

import com.las.workout.BaseTest
import com.las.workout.test.getAuthed
import com.las.workout.user.api.AUTH_ACCESS_TOKEN_HEADER_NAME
import com.las.workout.user.api.AUTH_REFRESH_TOKEN_HEADER_NAME
import com.las.workout.user.api.AUTH_REFRESH_TOKEN_PARAM_NAME
import com.las.workout.user.api.dto.AuthenticateRqDto
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

    @Test
    fun `API - User should be able to register`() {
        // GIVEN
        val username = "User 1"
        val password = "pass"

        // WHEN
        val res = webTestClient.post()
            .uri("/api/auth/actions/registration")
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
            .bodyValue(AuthenticateRqDto(username = username, password = password))
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        var accessToken = ""
        res.expectHeader().value(AUTH_ACCESS_TOKEN_HEADER_NAME) { accessToken = it }
        accessToken shouldHaveMinLength 5

        res.expectHeader().value(AUTH_REFRESH_TOKEN_HEADER_NAME) {
            it shouldHaveMinLength 5
        }

        // WHEN
        val res2 = webTestClient.get()
            .uri("/api/users/me")
            .header("Authorization", "Bearer $accessToken")
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

        // WHEN
        val res = webTestClient.post()
            .uri {
                it.path("/api/auth/actions/refresh-tokens")
                    .queryParam(AUTH_REFRESH_TOKEN_PARAM_NAME, refreshToken)
                    .build()
            }
            .exchange()

        // THEN
        res.expectStatus().is2xxSuccessful

        var newAccessToken = ""
        res.expectHeader().value(AUTH_ACCESS_TOKEN_HEADER_NAME) { newAccessToken = it }
        newAccessToken shouldHaveMinLength 5
        newAccessToken shouldNotBe accessToken

        var newRefreshToken = ""
        res.expectHeader().value(AUTH_REFRESH_TOKEN_HEADER_NAME) { newRefreshToken = it }
        newRefreshToken shouldHaveMinLength 5
        newRefreshToken shouldNotBe refreshToken

        // WHEN
        val res2 = webTestClient.getAuthed(newAccessToken)
            .uri("/api/users/me")
            .exchange()

        // THEN
        res2.expectStatus().is2xxSuccessful

        res2.expectBody(UserDto::class.java).consumeWith {
            it.responseBody!!.id shouldBe "u1"
        }
    }

}