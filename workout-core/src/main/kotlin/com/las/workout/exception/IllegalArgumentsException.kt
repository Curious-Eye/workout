package com.las.workout.exception

import org.springframework.http.HttpStatus

class IllegalArgumentException(msg: String) : BaseHttpException(
    msg = msg,
    type = "illegal_argument_exception",
    status = HttpStatus.BAD_REQUEST
)