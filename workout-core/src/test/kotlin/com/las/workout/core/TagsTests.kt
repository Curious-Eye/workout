package com.las.workout.core

import com.las.workout.BaseTest
import com.las.workout.tags.service.TagsService
import com.las.workout.test.getAuthed
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference

class TagsTests : BaseTest() {

    @Autowired private lateinit var tagsService: TagsService

    @Test
    fun `User should be able to get their tags`() {
        // GIVEN
        val userSetup = dataHelper.setupUser(id = "u1").block()!!
        tagsService.addTags("u1", listOf("Squats", "Biceps curls")).block()!!

        // WHEN
        val resp = webTestClient.getAuthed(userSetup)
            .uri("/api/tags")
            .exchange()

        // THEN
        resp.expectStatus().is2xxSuccessful

        val respBody =
            resp.expectBody(object : ParameterizedTypeReference<List<String>>() {}).returnResult().responseBody!!
        respBody.size shouldBe 2
        respBody shouldContainAll listOf("Squats", "Biceps curls")
    }

}