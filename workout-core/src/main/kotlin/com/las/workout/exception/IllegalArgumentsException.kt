package com.las.workout.exception

import org.springframework.http.HttpStatus

class IllegalArgumentsException(msg: String) : BaseHttpException(
    msg = msg,
    type = "illegal_arguments_exception",
    status = HttpStatus.BAD_REQUEST
)