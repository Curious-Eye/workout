package com.las.workout.test

import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.postAuthed(accessToken: String): WebTestClient.RequestBodyUriSpec =
    this.post()
        .apply {
            this.header("Authorization", "Bearer $accessToken")
        }

fun WebTestClient.getAuthed(accessToken: String): WebTestClient.RequestHeadersUriSpec<*> =
    this.get()
        .apply {
            this.header("Authorization", "Bearer $accessToken")
        }

fun WebTestClient.getAuthed(userSetup: DataHelper.UserSetupResDto): WebTestClient.RequestHeadersUriSpec<*> =
    this.getAuthed(userSetup.accessToken)

fun WebTestClient.deleteAuthed(accessToken: String): WebTestClient.RequestHeadersUriSpec<*> =
    this.delete()
        .apply {
            this.header("Authorization", "Bearer $accessToken")
        }

fun WebTestClient.putAuthed(accessToken: String): WebTestClient.RequestBodyUriSpec =
    this.put()
        .apply {
            this.header("Authorization", "Bearer $accessToken")
        }