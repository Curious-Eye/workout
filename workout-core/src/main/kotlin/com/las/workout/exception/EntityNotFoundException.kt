package com.las.workout.exception

import org.springframework.http.HttpStatus

class EntityNotFoundException(msg: String) : BaseHttpException(
    msg = msg,
    type = "not_found_exception",
    status = HttpStatus.NOT_FOUND
)