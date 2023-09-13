package com.las.workout.user.api.dto

import com.las.workout.user.data.entity.UserEntity

data class UserRegisterDto(
    val username: String,
    val password: String,
)

data class UserDto(
    val id: String,
    val username: String
) {

    constructor(userEntity: UserEntity) : this(
        id = userEntity.id,
        username = userEntity.username,
    )

}

data class AuthenticateRqDto(
    val username: String,
    val password: String,
)

data class UserAuthenticateRespDto(
    val accessToken: String,
    val refreshToken: String?,
)

data class UserRefreshTokensRespDto(
    val accessToken: String,
    val refreshToken: String,
)