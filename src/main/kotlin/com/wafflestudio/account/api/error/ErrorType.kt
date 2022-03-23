package com.wafflestudio.account.api.error

import org.springframework.http.HttpStatus

enum class ErrorType(
    val code: Int,
    val httpStatus: HttpStatus
) {

    // 401
    WRONG_PASSWORD(1401001, HttpStatus.UNAUTHORIZED),

    // 403
    USER_INACTIVE(1403001, HttpStatus.FORBIDDEN),

    // 404
    USER_NOT_FOUND(1404001, HttpStatus.NOT_FOUND),

    // 409
    EMAIL_ALREADY_EXISTS(1409001, HttpStatus.CONFLICT),
}
