package com.las.workout.exception

import org.springframework.http.HttpStatus

abstract class BaseHttpException(
    msg: String,
    val type: String,
    val status: HttpStatus,
    var payload: BaseExceptionPayload? = null
) : RuntimeException(msg) {

    fun data(): DataExceptionDto {
        return DataExceptionDto(
            type = type,
            msg = message,
            payload = payload
        )
    }

}

open class BaseExceptionPayload

data class DataExceptionDto(
    val type: String,
    val msg: String?,
    val payload: BaseExceptionPayload?,
)
