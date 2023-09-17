package com.las.workout.exception

import org.springframework.http.HttpStatus

class UnauthorizedException(msg: String) : BaseHttpException(
    msg = msg,
    type = "unauthorized_exception",
    status = HttpStatus.UNAUTHORIZED
)