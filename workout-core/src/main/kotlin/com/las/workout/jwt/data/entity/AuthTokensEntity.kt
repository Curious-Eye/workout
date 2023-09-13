package com.las.workout.jwt.data.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "auth_tokens")
@TypeAlias("AuthTokensEntity")
class AuthTokensEntity(
    @Id
    val id: String,
    val userId: String,
    val accessToken: String,
    val refreshToken: RefreshTokenEntity?,
) {

    @Version
    var version: Long = 0

    @CreatedDate
    var createdDate: Date? = null

    @LastModifiedDate
    var lastModifiedDate: Date? = null

}

data class RefreshTokenEntity(
    val token: String,
    val expirationDate: Date,
    val issuedAt: Date,
    var used: Boolean = false,
)